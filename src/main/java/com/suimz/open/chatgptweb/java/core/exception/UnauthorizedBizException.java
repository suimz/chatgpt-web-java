package com.suimz.open.chatgptweb.java.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Unauthorized Exception
 *
 * @author https://github.com/suimz
 */
public class UnauthorizedBizException extends BizException {
    public UnauthorizedBizException() {
        super(HttpStatus.UNAUTHORIZED, "Error: 无访问权限 | No access rights");
    }
}
