package com.suimz.open.chatgptweb.java.service.store;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.suimz.open.chatgptweb.java.bean.po.ChatMsgStoreItem;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Store chat messages in memory, which will be cleared when the program stops running
 *
 * @author https://github.com/suimz
 */
@Slf4j
public class MemMsgStoreService implements IMsgStoreService {
    private Map<String, ChatMsgStoreItem> MSG_MAP = new ConcurrentHashMap<>();

    @Override
    public void save(ChatMsgStoreItem... messages) {
        List<ChatMsgStoreItem> messageList = Arrays.stream(messages).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        if (ObjectUtil.isEmpty(messageList)) return;

        for (ChatMsgStoreItem chatMsgItem : messageList) {
            MSG_MAP.put(chatMsgItem.getMessageId(), chatMsgItem);
        }
    }

    @Override
    public List<ChatMsgStoreItem> getParentMessages(String parentMessageId) {
        List<ChatMsgStoreItem> list = new ArrayList<>();
        if (StrUtil.isNotBlank(parentMessageId)) {
            String nextParentMessageId = parentMessageId;
            do {
                ChatMsgStoreItem msg = MSG_MAP.get(nextParentMessageId);
                if (msg == null) break;
                list.add(msg);
                nextParentMessageId = msg.getParentMessageId();
                if (StrUtil.isBlank(nextParentMessageId)) break;
            } while (true);
        }
        return list.stream()
                // Sort by created
                .sorted(Comparator.comparing(ChatMsgStoreItem::getCreated))
                .collect(Collectors.toList());
    }
}
