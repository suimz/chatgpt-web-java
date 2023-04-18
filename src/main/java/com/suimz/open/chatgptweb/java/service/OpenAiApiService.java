package com.suimz.open.chatgptweb.java.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.suimz.open.chatgptweb.java.bean.po.ChatMsgStoreItem;
import com.suimz.open.chatgptweb.java.bean.req.ChatProcessReq;
import com.suimz.open.chatgptweb.java.bean.resp.ChatProcessResp;
import com.suimz.open.chatgptweb.java.core.exception.ApiServiceNotInitializedBizException;
import com.suimz.open.chatgptweb.java.util.ObjUtil;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import io.reactivex.Single;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * OpenAI API Service
 *
 * @author https://github.com/suimz
 */
@Slf4j
@Service
public class OpenAiApiService extends AbstractChatService implements CommandLineRunner {
    private OpenAiService openAiService;
    private OpenAiExtApi openAiExtApi;

    @Override
    public void run(String... args) {
        if (StrUtil.isBlank(appProperties.getOpenaiApiKey())) {
            log.warn("The [ app.openai-api-key ] configuration option was not found, init of openAi api service has been skipped");
            return;
        }

        String baseUrl = ObjUtil.getNotBlankValSequential("https://api.openai.com", appProperties.getOpenaiApiBaseUrl());
        Retrofit retrofit = OpenAiService.defaultRetrofit(okHttpClient, okHttpObjectMapper).newBuilder().baseUrl(baseUrl).build();
        openAiService = new OpenAiService(retrofit.create(OpenAiApi.class), okHttpClient.dispatcher().executorService());
        openAiExtApi = retrofit.create(OpenAiExtApi.class);

        log.info("Successfully created the openAi api service instance");
    }

    public void checkService() {
        if (StrUtil.isBlank(appProperties.getOpenaiApiKey())) {
            throw new ApiServiceNotInitializedBizException();
        }
    }

    @Override
    public void streamChat(SseEmitter sseEmitter, ChatProcessReq req) {
        this.checkService();
        LocalDateTime startTime = LocalDateTime.now();
        StringBuilder receiveMsgBuilder = new StringBuilder("");

        Double temperature = 0.8;
        Integer maxTokens = 4096;
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(ObjUtil.getNotBlankValSequential("gpt-3.5-turbo", appProperties.getOpenaiApiMode()))
                .messages(buildSendMsgBody(req))
                .temperature(temperature)
                // .maxTokens(maxTokens)
                .build();
        openAiService.streamChatCompletion(request)
                .blockingForEach(chunk -> {
                    String backMsg = null;
                    boolean stop = false;

                    if (ObjectUtil.isNotEmpty(chunk.getChoices())) {
                        ChatCompletionChoice choice = chunk.getChoices().get(0);
                        String finishReason = choice.getFinishReason();
                        if(finishReason == null && choice.getMessage().getContent() != null) {
                            backMsg = choice.getMessage().getContent();
                        } else if (finishReason != null){
                            backMsg = choice.getMessage().getContent();
                            stop = true;
                        }
                        if (backMsg != null) {
                            receiveMsgBuilder.append(backMsg);

                            ChatProcessResp.Choice choiceResp = ChatProcessResp.Choice.builder()
                                    .index(choice.getIndex())
                                    .finish_reason(choice.getFinishReason())
                                    .delta(ChatProcessResp.Delta.builder().content(backMsg).build())
                                    .build();
                            ChatProcessResp.Detail detailResp = ChatProcessResp.Detail.builder()
                                    .id(chunk.getId())
                                    .created(chunk.getCreated())
                                    .object(chunk.getObject())
                                    .model(chunk.getModel())
                                    .choices(Arrays.asList(choiceResp))
                                    .build();
                            ChatProcessResp resp = ChatProcessResp.builder()
                                    .id(chunk.getId())
                                    .role(choice.getMessage().getRole())
                                    .text(receiveMsgBuilder.toString())
                                    .delta(backMsg)
                                    .detail(detailResp)
                                    .build();
                            super.pushClient(sseEmitter, resp);
                            log.debug("push message to client：{}", resp);
                        }
                        if (stop) {
                            // save message to store
                            String sendMessageId = IdUtil.simpleUUID();
                            super.saveMessages(
                                    // send message
                                    ChatMsgStoreItem.builder()
                                            .messageId(sendMessageId)
                                            .text(req.getPrompt())
                                            .parentMessageId(req.getOptions() != null ? req.getOptions().getParentMessageId() : null)
                                            .role(ChatMessageRole.USER.value())
                                            .created(startTime)
                                            .build(),
                                    // receive message
                                    ChatMsgStoreItem.builder()
                                            .messageId(chunk.getId())
                                            .text(receiveMsgBuilder.toString())
                                            .parentMessageId(sendMessageId)
                                            .role(ChatMessageRole.ASSISTANT.value())
                                            .created(LocalDateTime.now())
                                            .build()
                            );

                            sseEmitter.complete();
                        }
                    }

                });
    }

    private List<ChatMessage> buildSendMsgBody(ChatProcessReq req) {
        List<ChatMessage> messages = new ArrayList<>();
        // system message
        if (StrUtil.isNotBlank(req.getSystemMessage())) {
            ChatMessage msg = new ChatMessage();
            msg.setRole(ChatMessageRole.SYSTEM.value());
            msg.setContent(req.getSystemMessage());
            messages.add(msg);
        }

        // history message
        if (req.getOptions() != null && StrUtil.isNotBlank(req.getOptions().getParentMessageId())) {
            List<ChatMsgStoreItem> historyMessages = super.getParentMessages(req.getOptions().getParentMessageId());
            historyMessages.forEach(row -> {
                ChatMessage msg = new ChatMessage();
                msg.setRole(row.getRole());
                msg.setContent(row.getText());
                messages.add(msg);
            });
            if (ObjectUtil.isEmpty(historyMessages)) {
                // not found parent messages
                req.getOptions().setParentMessageId(null);
            }
        }

        // user send message
        ChatMessage latestMsg = new ChatMessage();
        latestMsg.setRole(ChatMessageRole.USER.value());
        latestMsg.setContent(req.getPrompt());
        messages.add(latestMsg);

        return messages;
    }

    /**
     * Query account balance
     *
     * @return
     */
    public Double queryBalance() {
        this.checkService();
        Double balance = null;
        try {
            if (StrUtil.isNotBlank(appProperties.getOpenaiSensitiveId())) {
                String authHeader = "Bearer " + appProperties.getOpenaiSensitiveId();
                balance = openAiExtApi.billing(authHeader).blockingGet().getTotal_available();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return balance;
    }

    interface OpenAiExtApi {
        @GET("/dashboard/billing/credit_grants")
        Single<BillingResult> billing(@Header("Authorization") String authHeader);
    }

    @Data
    static class BillingResult {
        Object error;
        @JsonProperty("total_available")
        Double total_available;
    }

}
