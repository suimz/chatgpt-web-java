package com.suimz.open.chatgptweb.java.bean.constant;

import cn.hutool.core.util.StrUtil;
import com.suimz.open.chatgptweb.java.core.properties.AppProperties;
import lombok.Getter;

/**
 * Server chat API running mode
 *
 * @author https://github.com/suimz
 */
@Getter
public enum ApiRunMode {
    API("ChatGPTAPI"),
    REVERSE("ChatGPTUnofficialProxyAPI"),
    ;
    ApiRunMode(String name) {
        this.name = name;
    }

    private String name;

    public static ApiRunMode get(AppProperties properties) {
        return StrUtil.isNotBlank(properties.getOpenaiApiKey()) ? ApiRunMode.API : ApiRunMode.REVERSE;
    }

}
