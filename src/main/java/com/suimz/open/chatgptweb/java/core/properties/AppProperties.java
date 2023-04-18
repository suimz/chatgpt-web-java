package com.suimz.open.chatgptweb.java.core.properties;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Server Configuration Properties
 *
 * @author https://github.com/suimz
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * Front-End Authorization Key
     */
    private String authSecretKey;

    /**
     * OpenAI API KEY
     */
    private String openaiApiKey;

    /**
     * OpenAI API Model - https://platform.openai.com/docs/models
     */
    private String openaiApiMode="gpt-3.5-turbo";

    /**
     * OpenAI API Base URL - https://api.openai.com
     */
    private String openaiApiBaseUrl="https://api.openai.com";

    /**
     * Change this to an `accessToken` extracted from the ChatGPT site's `https://chat.openai.com/api/auth/session` response
     */
    private String openaiAccessToken;

    /**
     * Used to query balance, change this to an `sensitiveId` extracted from the ChatGPT site's `https://platform.openai.com/account/usage`
     */
    private String openaiSensitiveId;

    /**
     * Uses an unofficial proxy server to access ChatGPT's backend API
     */
    private String openaiReverseApiProxyUrl="https://bypass.churchless.tech/api/conversation";

    /**
     * Print API request log
     */
    private Boolean apiDisableLog=false;

    /**
     * API request timeout, ms
     */
    private Long apiTimeoutMs = 2 * 60 * 1000L;

    /**
     * Chat API maximum number of requests per hour, 0 - unlimited
     */
    private Integer maxRequestPerHour=0;

    /**
     * Socks Proxy
     */
    private SocksProxy socksProxy;

    /**
     * HTTP Proxy
     */
    private HttpProxy httpProxy;

    /**
     * (optional) Cross domain lists.
     * Examples:
     *  https://*.domain1.com -- domains ending with domain1.com;
     *  https://*.domain1.com:[8080,8081] -- domains ending with domain1.com on port 8080 or port 8081;
     *  https://*.domain1.com:[*] -- domains ending with domain1.com on any port, including the default port;
     *  comma-delimited list of patters, e.g. "https://*.a1.com,https://*.a2.com";
     *  "*"  - will be fully open;
     */
    private String corsAllowedOrigin;

    @Data
    public static class SocksProxy {
        private String host;
        private Integer port;
        private String username;
        private String password;

        public boolean isAvailable() {
            return ObjectUtil.isAllNotEmpty(host, port);
        }
    }

    @Data
    public static class HttpProxy {
        private String host;
        private Integer port;

        public boolean isAvailable() {
            return ObjectUtil.isAllNotEmpty(host, port);
        }
    }


}
