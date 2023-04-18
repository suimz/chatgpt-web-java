package com.suimz.open.chatgptweb.java.core.conf;

import cn.hutool.core.util.StrUtil;
import com.suimz.open.chatgptweb.java.core.properties.AppProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp Client initialize
 *
 * @author https://github.com/suimz
 */
@Slf4j
@Configuration
public class OkHttpConfig {

    @Bean
    public OkHttpClient okHttpClient(AppProperties appProperties) {
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .readTimeout(appProperties.getApiTimeoutMs(), TimeUnit.MILLISECONDS);

        String openaiApiKey = appProperties.getOpenaiApiKey();
        if (StrUtil.isNotBlank(openaiApiKey)) {
            clientBuilder.addInterceptor(new AuthenticationInterceptor(openaiApiKey));
        }

        // Print api request log
        if (appProperties.getApiDisableLog()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(logging).build();
        }

        // proxy
        this.setupProxy(clientBuilder, appProperties);

        return clientBuilder.build();
    }

    /**
     * Set okHttp Client Proxy
     */
    private void setupProxy(OkHttpClient.Builder clientBuilder, AppProperties appProperties) {
        AppProperties.SocksProxy socksProxyConfig = appProperties.getSocksProxy();
        AppProperties.HttpProxy httpProxyConfig = appProperties.getHttpProxy();

        Proxy proxy = null;
        if (socksProxyConfig != null && socksProxyConfig.isAvailable()) {
            // Socks Proxy
            proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksProxyConfig.getHost(), socksProxyConfig.getPort()));
            // socks authentication
            if (StrUtil.isAllNotBlank(socksProxyConfig.getUsername(), socksProxyConfig.getPassword())) {
                java.net.Authenticator.setDefault(new ProxyAuthenticator(socksProxyConfig.getUsername(), socksProxyConfig.getPassword()));
            }
        } else if (httpProxyConfig != null && httpProxyConfig.isAvailable()) {
            // HTTP proxy
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyConfig.getHost(), httpProxyConfig.getPort()));
            clientBuilder.proxy(proxy);
        }

        if (proxy != null) {
            clientBuilder.proxy(proxy);
            log.info("OkHttp Proxy configured: {}", proxy);
        }
    }

    public static class AuthenticationInterceptor implements Interceptor {
        private final String token;

        public AuthenticationInterceptor(String token) {
            Objects.requireNonNull(token, "OpenAI token required");
            this.token = token;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (chain.request().header("Authorization") == null) {
                request = chain.request()
                        .newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
            }
            return chain.proceed(request);
        }
    }

    public class ProxyAuthenticator extends java.net.Authenticator {
        private PasswordAuthentication auth;
        public ProxyAuthenticator(String strUserName, String strPasswd) {
            auth = new PasswordAuthentication(strUserName, strPasswd.toCharArray());
        }
        protected PasswordAuthentication getPasswordAuthentication() {
            return auth;
        }
    }

}
