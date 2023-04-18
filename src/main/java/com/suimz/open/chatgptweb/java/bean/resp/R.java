package com.suimz.open.chatgptweb.java.bean.resp;

import lombok.Builder;
import lombok.Data;

/**
 * API response payload structure
 *
 * @author https://github.com/suimz
 */
@Builder
@Data
public class R<T> {

    private String status;

    private String message;

    private T data;

    public static R ok() {
        return R.ok(null);
    }

    public static R ok(String message) {
        return R.ok(null, null);
    }

    public static <T> R<T> ok(T data) {
        return R.ok(null, data);
    }

    public static <T> R<T> ok(String message, T data) {
        return (R<T>) R.builder().status("Success").message(message).data(data).build();
    }

    public static R error() {
        return R.error("服务异常 | server exception");
    }

    public static R error(String error) {
        return R.builder()
                .status("Fail")
                .message(error)
                .build();
    }

}