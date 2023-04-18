package com.suimz.open.chatgptweb.java.core.exception;

public class ChatApiRequestTooManyBizException extends BizException {
    public ChatApiRequestTooManyBizException() {
        super("Too many request from this IP in 1 hour");
    }
}
