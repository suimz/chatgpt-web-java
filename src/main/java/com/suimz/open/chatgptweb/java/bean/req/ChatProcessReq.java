package com.suimz.open.chatgptweb.java.bean.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Request Params - chat-process
 *
 * @author https://github.com/suimz
 */
@Data
public class ChatProcessReq {

    @NotBlank(message = "prompt is empty")
    private String prompt;

    private String systemMessage;

    private Options options;

    @Data
    public static class Options {
        private String parentMessageId;
        private String conversationId;
    }

}
