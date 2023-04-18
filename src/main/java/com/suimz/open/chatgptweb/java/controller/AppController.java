package com.suimz.open.chatgptweb.java.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.suimz.open.chatgptweb.java.bean.constant.ApiRunMode;
import com.suimz.open.chatgptweb.java.bean.req.AuthVerifyReq;
import com.suimz.open.chatgptweb.java.bean.req.ChatProcessReq;
import com.suimz.open.chatgptweb.java.bean.resp.ConfigResp;
import com.suimz.open.chatgptweb.java.bean.resp.R;
import com.suimz.open.chatgptweb.java.bean.resp.SessionResp;
import com.suimz.open.chatgptweb.java.core.component.ratelimiter.ApiRateLimiter;
import com.suimz.open.chatgptweb.java.core.exception.ApiRequestErrorBizException;
import com.suimz.open.chatgptweb.java.core.exception.BizException;
import com.suimz.open.chatgptweb.java.core.properties.AppProperties;
import com.suimz.open.chatgptweb.java.service.OpenAiApiService;
import com.suimz.open.chatgptweb.java.service.OpenAiReverseService;
import com.suimz.open.chatgptweb.java.util.ObjUtil;
import com.theokanning.openai.OpenAiHttpException;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Server API Controller
 *
 * @author https://github.com/suimz
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class AppController {
    @Resource
    private AppProperties appProperties;
    @Resource
    private ThreadPoolTaskExecutor asyncTaskExecutor;
    @Resource
    private OpenAiApiService openAiApiService;
    @Resource
    private OpenAiReverseService openAiReverseService;

    @PostMapping("/session")
    public R<SessionResp> session() {
        return R.ok(
                SessionResp.builder()
                        .auth(StrUtil.isNotBlank(appProperties.getAuthSecretKey()))
                        .model(ApiRunMode.get(appProperties).getName())
                        .build()
        );
    }

    @PostMapping("/config")
    public R<ConfigResp> config() {
        String socksProxy;
        if (appProperties.getSocksProxy() != null && ObjectUtil.isAllNotEmpty(appProperties.getSocksProxy().getHost(), appProperties.getSocksProxy().getPort())) {
            socksProxy = StrUtil.format("{}:{}", appProperties.getSocksProxy().getHost(), appProperties.getSocksProxy().getPort());
        } else {
            socksProxy = "-";
        }

        String httpProxy;
        if (appProperties.getHttpProxy() != null && ObjectUtil.isAllNotEmpty(appProperties.getHttpProxy().getHost(), appProperties.getHttpProxy().getPort())) {
            httpProxy = StrUtil.format("{}:{}", appProperties.getHttpProxy().getHost(), appProperties.getHttpProxy().getPort());
        } else {
            httpProxy = "-";
        }

        Double balance = null;
        ApiRunMode apiRunMode = ApiRunMode.get(appProperties);
        if (apiRunMode == ApiRunMode.API) {
            balance = openAiApiService.queryBalance();
        }

        return R.ok(
                ConfigResp.builder()
                        .apiModel(apiRunMode.getName())
                        .timeoutMs(appProperties.getApiTimeoutMs())
                        .httpsProxy(httpProxy)
                        .socksProxy(socksProxy)
                        .reverseProxy(ObjUtil.getNotBlankValSequential("-", appProperties.getOpenaiReverseApiProxyUrl()))
                        .balance(ObjUtil.getNotNullValSequential("-", balance))
                        .build()
        );
    }

    @PostMapping("/verify")
    public R<SessionResp> authVerify(@RequestBody @Validated AuthVerifyReq req) {
        if (!StrUtil.equals(appProperties.getAuthSecretKey(), req.getToken())) {
            throw new BizException("Secret key is invalid");
        }
        return R.ok("Verify successfully");
    }

    @ApiRateLimiter
    @PostMapping("/chat-process")
    public SseEmitter chatProcess(@RequestBody @Validated ChatProcessReq req) {
        SseEmitter sseEmitter = new SseEmitter(appProperties.getApiTimeoutMs());
        asyncTaskExecutor.execute(() -> {
            try {
                switch (ApiRunMode.get(appProperties)) {
                    case API:
                        openAiApiService.streamChat(sseEmitter, req);
                        break;
                    case REVERSE:
                        openAiReverseService.streamChat(sseEmitter, req);
                        break;
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                BizException thrEx;
                if (e instanceof BizException) {
                    thrEx = (BizException) e;
                } else if (e instanceof OpenAiHttpException) {
                    OpenAiHttpException exception = (OpenAiHttpException) e;
                    thrEx = new ApiRequestErrorBizException(exception.statusCode, exception.getMessage());
                } else if (e.getCause() instanceof SocketTimeoutException) {
                    thrEx = new ApiRequestErrorBizException(0);
                } else if (e.getCause() instanceof SocketException || e.getCause() instanceof IOException) {
                    thrEx = new ApiRequestErrorBizException(-1);
                } else {
                    thrEx = new ApiRequestErrorBizException();
                }
                sseEmitter.completeWithError(thrEx);
            }
        });
        return sseEmitter;
    }


}
