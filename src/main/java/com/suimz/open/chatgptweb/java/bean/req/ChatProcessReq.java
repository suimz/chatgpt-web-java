package com.suimz.open.chatgptweb.java.bean.req;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    private Double temperature;

    @JsonProperty(value = "top_p")
    private Double topP;

    @Data
    public static class Options {
        private String parentMessageId;
        private String conversationId;
    }

}
