package com.suimz.open.chatgptweb.java.bean.resp;

import lombok.Builder;
import lombok.Getter;

/**
 * Response Params - config
 *
 * @author https://github.com/suimz
 */
@Builder
@Getter
public class ConfigResp {

    private String apiModel;

    private String reverseProxy;

    private Long timeoutMs;

    private String socksProxy;

    private String httpsProxy;

    private String balance;
}
