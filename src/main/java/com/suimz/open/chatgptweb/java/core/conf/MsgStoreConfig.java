package com.suimz.open.chatgptweb.java.core.conf;

import com.suimz.open.chatgptweb.java.service.store.IMsgStoreService;
import com.suimz.open.chatgptweb.java.service.store.MemMsgStoreService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chat Message Store config
 *
 * @author https://github.com/suimz
 */
@Configuration
public class MsgStoreConfig {

    @Bean
    @ConditionalOnProperty(name = "app.chat-message-store", havingValue = "memory", matchIfMissing = true)
    public IMsgStoreService memoryMsgStoreService() {
        // memory-store
        return new MemMsgStoreService();
    }

    @Bean
    @ConditionalOnProperty(name = "app.chat-message-store", havingValue = "mysql")
    public IMsgStoreService mysqlMsgStoreService() {
        // TODO mysql-store
        return null;
    }

}
