package com.suimz.open.chatgptweb.java.bean.resp;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Response Params - chat-process
 *
 * @author https://github.com/suimz
 */
@Builder
@Getter
@ToString
public class ChatProcessResp {

    private String id;

    private String text;

    private String role;

    private Detail detail;

    private String delta;

    private String parentMessageId;

    private String conversationId;

    @Builder
    @Getter
    public static class Detail {
        private String id;
        private String object;
        private Long created;
        private String model;
        private List<Choice> choices;
    }

    @Builder
    @Getter
    public static class Choice {
        private Integer index;
        private String finish_reason;
        private Delta delta;
    }

    @Builder
    @Getter
    public static class Delta {
        private String content;
    }

}
