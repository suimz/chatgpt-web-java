package com.suimz.open.chatgptweb.java.core.component.ratelimiter;

import java.lang.annotation.*;

/**
 * API request rate restriction - Annotation
 *
 * @author https://github.com/suimz
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiRateLimiter {

}

