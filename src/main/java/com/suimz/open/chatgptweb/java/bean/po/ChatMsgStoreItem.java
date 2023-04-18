package com.suimz.open.chatgptweb.java.bean.po;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Save chat history
 *
 * @author https://github.com/suimz
 */
@Builder
@Getter
public class ChatMsgStoreItem {

    /**
     * required
     */
    private String messageId;

    private String text;

    /**
     * required
     */
    private String role;

    private String parentMessageId;

    /**
     * required
     */
    private LocalDateTime created;
}
