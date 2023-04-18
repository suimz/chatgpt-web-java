package com.suimz.open.chatgptweb.java.core.component.ratelimiter;

import com.suimz.open.chatgptweb.java.core.exception.ChatApiRequestTooManyBizException;
import com.suimz.open.chatgptweb.java.core.properties.AppProperties;
import com.suimz.open.chatgptweb.java.util.SpringUtil;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * API request rate restriction - Handle
 *
 * @author https://github.com/suimz
 */
@Aspect
@Component
public class ApiRateLimiterAspect {
    private RateLimiterRegistry rateLimiterRegistry = null;

    public ApiRateLimiterAspect(AppProperties appProperties) {
        Integer period = appProperties.getMaxRequestPerHour();
        if (period != null && period > 0) {
            this.rateLimiterRegistry = RateLimiterRegistry.of(
                    RateLimiterConfig.custom()
                            .limitForPeriod(period) // Maximum number of requests
                            .limitRefreshPeriod(Duration.ofHours(1)) // 1 hour
                            .timeoutDuration(Duration.ofMillis(1))
                            .build()
            );
        }
    }

    @Before("@annotation(apiRateLimiter)")
    public void doBefore(JoinPoint point, ApiRateLimiter apiRateLimiter) {
        if (this.rateLimiterRegistry == null) return;
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(getCombineKey(point));
        if (!rateLimiter.acquirePermission()) throw new ChatApiRequestTooManyBizException();
    }

    public String getCombineKey(JoinPoint point) {
        StringBuilder sb = new StringBuilder(SpringUtil.getClientIp()).append("-");
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        sb.append(targetClass.getName()).append("-").append(method.getName());
        return sb.toString();
    }

}
