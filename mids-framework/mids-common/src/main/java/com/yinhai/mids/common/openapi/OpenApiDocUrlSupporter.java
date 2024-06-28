package com.yinhai.mids.common.openapi;

import com.yinhai.ta404.core.restservice.interceptor.advice.Ta4AdviceSupporter;
import com.yinhai.ta404.core.security.matcher.UrlPathMatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 阻止TA404框架包装OpenAPI的接口返回结果
 *
 * @author zhuhs
 * @date 2024/6/27 16:26
 */
@Component
public class OpenApiDocUrlSupporter implements Ta4AdviceSupporter {

    @Value("${springdoc.api-docs.path:#{T(org.springdoc.core.Constants).DEFAULT_API_DOCS_URL}}/swagger-config")
    private String configUrl;
    @Value("${springdoc.api-docs.path:#{T(org.springdoc.core.Constants).DEFAULT_API_DOCS_URL}}")
    private String apiDocJsonUrl;
    @Value("${springdoc.api-docs.path:#{T(org.springdoc.core.Constants).DEFAULT_API_DOCS_URL}}.yaml")
    private String apiDocYmlUrl;

    @Override
    public boolean supports(MethodParameter methodParameter, @NonNull Class<? extends HttpMessageConverter<?>> aClass) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String uri = request.getRequestURI();
        String context = request.getServletContext().getContextPath();
        String url = uri.substring(context.length());
        if (UrlPathMatcher.pathMatchesUrl(this.configUrl, url)) {
            return false;
        } else if (UrlPathMatcher.pathMatchesUrl(this.apiDocJsonUrl, url)) {
            return false;
        } else {
            return !UrlPathMatcher.pathMatchesUrl(this.apiDocYmlUrl, url);
        }
    }
}
