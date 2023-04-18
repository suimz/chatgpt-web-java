package com.suimz.open.chatgptweb.java.bean.po;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReverseChatChunk {

    private String error;

    @JsonProperty("conversation_id")
    private String conversationId;

    private Message message;

    @Data
    public static class Message {
        private String id;
        private Author author;
        @JsonProperty("create_time")
        private Double createTime;
        @JsonProperty("update_time")
        private Double updateTime;
        private Content content;
        @JsonProperty("end_turn")
        private Boolean endTurn;
        private Integer weight;
        private Metadata metadata;
        private String recipient;
    }

    @Data
    public static class Content {
        @JsonProperty("content_type")
        private String contentType;
        private List<String> parts;
    }

    @Data
    public static class Metadata {
        @JsonProperty("message_type")
        private String messageType;
        @JsonProperty("modelSlug")
        private String model_slug;
        @JsonProperty("finish_details")
        private FinishDetails finishDetails;
    }

    @Data
    public static class FinishDetails {
        private String type;
        private String stop;
    }

    @Data
    public static class Author {
        private String role;
        private String name;
    }

}
