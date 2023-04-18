package com.suimz.open.chatgptweb.java.service;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.suimz.open.chatgptweb.java.bean.po.ReverseChatChunk;
import com.suimz.open.chatgptweb.java.bean.po.ReverseResponseBodyCallback;
import com.suimz.open.chatgptweb.java.bean.po.ReverseSSE;
import com.suimz.open.chatgptweb.java.bean.req.ChatProcessReq;
import com.suimz.open.chatgptweb.java.bean.resp.ChatProcessResp;
import com.suimz.open.chatgptweb.java.core.exception.BizException;
import com.suimz.open.chatgptweb.java.core.exception.ApiRequestErrorBizException;
import com.suimz.open.chatgptweb.java.core.exception.ReverseServiceNotInitializedBizException;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.*;

/**
 * Reverse ChatGPT website backend API service
 *
 * @author https://github.com/suimz
 */
@Slf4j
@Service
public class OpenAiReverseService extends AbstractChatService implements CommandLineRunner {
    private ReverseApi reverseApi;

    @Override
    public void run(String... args) {
        if (StrUtil.isBlank(appProperties.getOpenaiAccessToken())) {
            log.warn("The [ app.openai-access-token ] configuration option was not found, init of openAi reverse service has been skipped");
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost/") // placeholder
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create(OpenAiService.defaultObjectMapper()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        reverseApi = retrofit.create(ReverseApi.class);

        log.info("Successfully created the openAi reverse service instance");
    }

    public void checkService() {
        if (StrUtil.isBlank(appProperties.getOpenaiAccessToken()) || StrUtil.isBlank(appProperties.getOpenaiReverseApiProxyUrl())) {
            throw new ReverseServiceNotInitializedBizException();
        }
    }

    @Override
    public void streamChat(SseEmitter sseEmitter, ChatProcessReq req) {
        this.checkService();
        String authHeader = "Bearer " + appProperties.getOpenaiAccessToken();
        ObjectNode body = buildSendMsgBody(req);
        String sendMsgId = body.findValues("messages").get(0).findValue("id").asText();

        Flowable.<ReverseSSE>create(emitter -> reverseApi.conversation(appProperties.getOpenaiReverseApiProxyUrl(), body, authHeader).enqueue(new ReverseResponseBodyCallback(emitter, false)), BackpressureStrategy.BUFFER)
                .map(sse -> okHttpObjectMapper.readValue(sse.getData(), ReverseChatChunk.class))
                .blockingForEach(chunk -> {
                    try {
                        if (StrUtil.isNotBlank(chunk.getError())) {
                            log.debug(chunk.getError());
                            sseEmitter.completeWithError(new BizException(chunk.getError()));
                        }

                        if (!ChatMessageRole.ASSISTANT.value().equalsIgnoreCase(chunk.getMessage().getAuthor().getRole())) return;

                        boolean stop = BooleanUtil.isTrue(chunk.getMessage().getEndTurn());

                        if (!stop) {
                            ChatProcessResp resp = ChatProcessResp.builder()
                                    .id(chunk.getMessage().getId())
                                    .role(chunk.getMessage().getAuthor().getRole())
                                    .text(chunk.getMessage().getContent().getParts().get(0))
                                    .parentMessageId(sendMsgId)
                                    .conversationId(chunk.getConversationId())
                                    .build();
                            super.pushClient(sseEmitter, resp);
                            log.debug("push message to client：{}", resp);
                        } else {
                            sseEmitter.complete();
                        }
                    } catch (Exception e) {
                        sseEmitter.completeWithError(e);
                        throw e;
                    }
                });
    }

    private ObjectNode buildSendMsgBody(ChatProcessReq req) {
        String sendMsg = req.getPrompt();
        String msgId = IdUtil.randomUUID();
        String parentMessageId = IdUtil.randomUUID();
        String conversationId = null;

        ChatProcessReq.Options options = req.getOptions();
        if (options != null) {
            if (StrUtil.isNotBlank(options.getParentMessageId())) {
                parentMessageId = options.getParentMessageId();
            }
            if (StrUtil.isNotBlank(options.getConversationId())) {
                conversationId = options.getConversationId();
            }
        }

        ObjectNode message = okHttpObjectMapper.createObjectNode();
        message.put("id", msgId);
        message.put("author", okHttpObjectMapper.createObjectNode().put("role", ChatMessageRole.USER.value()));
        ObjectNode content = okHttpObjectMapper.createObjectNode().put("content_type", "text");
        content.putArray("parts").add(sendMsg);
        message.put("content", content);

        ObjectNode root = okHttpObjectMapper.createObjectNode();
        root.put("action", "next");
        root.put("model", "text-davinci-002-render-sha");
        root.putArray("messages").add(message);
        root.put("conversation_id", conversationId);
        root.put("parent_message_id", parentMessageId);

        return root;
    }

    interface ReverseApi {
        @Streaming
        @Headers({"Cache-Control: no-cache", "X-Accel-Buffering: no"})
        @POST
        Call<ResponseBody> conversation(@Url String url, @Body ObjectNode body, @Header("Authorization") String authHeader);
    }

}
