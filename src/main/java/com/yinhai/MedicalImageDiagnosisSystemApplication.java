package com.yinhai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableWebSecurity(debug = true)
@ServletComponentScan
@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class MedicalImageDiagnosisSystemApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(MedicalImageDiagnosisSystemApplication.class, args);
    }

    @Profile("war")
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MedicalImageDiagnosisSystemApplication.class);
    }
}