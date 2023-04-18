package com.suimz.open.chatgptweb.java.core.interceptor;

import cn.hutool.core.util.StrUtil;
import com.suimz.open.chatgptweb.java.core.exception.UnauthorizedBizException;
import com.suimz.open.chatgptweb.java.core.properties.AppProperties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.suimz.open.chatgptweb.java.util.SpringUtil;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor - Client permission valid
 *
 * @author https://github.com/suimz
 */
public class AuthInterceptor implements HandlerInterceptor {

    private final AppProperties appProperties;

    public AuthInterceptor(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (StrUtil.isNotBlank(appProperties.getAuthSecretKey())) {
            String token = getToken(request);
            if (!StrUtil.equals(appProperties.getAuthSecretKey(), token)) {
                throw new UnauthorizedBizException();
            }
        }
        return true;
    }

    public String getToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        return StrUtil.startWith(bearer, "Bearer ") ? bearer.split(" ")[1] : null;
    }

}
