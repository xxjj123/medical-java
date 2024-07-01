package com.yinhai.mids.common.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhuhs
 * @date 2024/6/27 14:53
 */
@Configuration
public class OpenApiConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(
                new Info().title("医疗影像诊断系统").description("医疗影像诊断系统文档").version("1.0.0")
        );
    }

    @Bean
    public GroupedOpenApi frameworkOpenApi() {
        return GroupedOpenApi.builder()
                .group("framework")
                .displayName("框架文档")
                .packagesToScan("com.yinhai")
                .packagesToExclude("com.yinhai.mids")
                .build();
    }

    @Bean
    public GroupedOpenApi businessOpenApi() {
        return GroupedOpenApi.builder()
                .group("business")
                .displayName("业务文档")
                .packagesToScan("com.yinhai.mids")
                .build();
    }
}
