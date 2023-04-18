package com.suimz.open.chatgptweb.java.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suimz.open.chatgptweb.java.bean.po.SsePushEventBuilder;
import com.suimz.open.chatgptweb.java.bean.req.ChatProcessReq;
import com.suimz.open.chatgptweb.java.bean.resp.ChatProcessResp;
import com.suimz.open.chatgptweb.java.core.properties.AppProperties;
import com.suimz.open.chatgptweb.java.bean.po.ChatMsgStoreItem;
import com.suimz.open.chatgptweb.java.service.store.IMsgStoreService;
import com.theokanning.openai.service.OpenAiService;
import javax.annotation.Resource;
import okhttp3.OkHttpClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

/**
 * @author https://github.com/suimz
 */
public abstract class AbstractChatService {
    protected ObjectMapper okHttpObjectMapper = OpenAiService.defaultObjectMapper();

    @Resource
    protected AppProperties appProperties;
    @Resource
    protected OkHttpClient okHttpClient;
    @Resource
    protected IMsgStoreService msgStoreService;

    /**
     * stream chat(SSE)
     *
     * @param sseEmitter
     * @param req
     */
    public abstract void streamChat(SseEmitter sseEmitter, ChatProcessReq req);

    /**
     * Push the chat result to the client
     *
     * @param sseEmitter
     * @param resp
     */
    protected void pushClient(SseEmitter sseEmitter, ChatProcessResp resp) {
        try {
            sseEmitter.send(new SsePushEventBuilder().data(resp));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save conversation message
     *
     * @param messages
     */
    protected void saveMessages(ChatMsgStoreItem...messages) {
        msgStoreService.save(messages);
    }

    /**
     * Save conversation message
     *
     * @param parentMessageId
     * @return
     */
    protected List<ChatMsgStoreItem> getParentMessages(String parentMessageId) {
        return msgStoreService.getParentMessages(parentMessageId);
    }
}
