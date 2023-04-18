package com.suimz.open.chatgptweb.java.bean.resp;

import com.suimz.open.chatgptweb.java.bean.constant.ApiRunMode;
import lombok.Builder;
import lombok.Getter;

/**
 * Response Params - verify
 *
 * @author https://github.com/suimz
 */
@Builder
@Getter
public class SessionResp {

    private boolean auth;

    /**
     * @see ApiRunMode
     */
    private String model;
}
