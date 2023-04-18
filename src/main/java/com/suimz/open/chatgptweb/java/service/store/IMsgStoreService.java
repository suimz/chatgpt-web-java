package com.suimz.open.chatgptweb.java.service.store;

import com.suimz.open.chatgptweb.java.bean.po.ChatMsgStoreItem;

import java.util.List;

/**
 * Chat Message Store interface
 *
 * @author https://github.com/suimz
 */
public interface IMsgStoreService {

    /**
     * Save conversation message
     *
     * @param messages
     */
    void save(ChatMsgStoreItem...messages);

    /**
     * Get all parent messages
     *
     * @param parentMessageId
     * @return
     */
    List<ChatMsgStoreItem> getParentMessages(String parentMessageId);

}
