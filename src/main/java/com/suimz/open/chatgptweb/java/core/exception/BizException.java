package com.suimz.open.chatgptweb.java.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Business Exception Class
 *
 * @author https://github.com/suimz
 */
@Getter
public class BizException extends RuntimeException {

    private HttpStatus httpStatus = HttpStatus.OK;

    public BizException(String msg) {
        super(msg);
    }

    public BizException(HttpStatus status) {
        this(status, status.getReasonPhrase());
    }

    public BizException(HttpStatus httpStatus, String msg) {
        super(msg);
        this.httpStatus = httpStatus;
    }

}
