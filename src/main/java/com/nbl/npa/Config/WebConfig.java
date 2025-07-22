package com.nbl.npa.Config;

import jakarta.servlet.SessionTrackingMode;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Collections;


@Configuration
public class WebConfig implements WebMvcConfigurer {



    private final SSOInterceptor ssoInterceptor;

    public WebConfig(SSOInterceptor ssoInterceptor) {
        this.ssoInterceptor = ssoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ssoInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/npa")
                .excludePathPatterns("/token")
                .excludePathPatterns("/paymentstatus")
                .excludePathPatterns("/change-password")


        ;
    }
    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> {
            servletContext.setSessionTrackingModes(
                    Collections.singleton(SessionTrackingMode.COOKIE));
        };
    }



}

