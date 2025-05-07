package com.example.luna.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/", "classpath:/resources/", "classpath:/META-INF/resources/");

        registry.addResourceHandler("/upload/content/**")
                .addResourceLocations("file:///C:/upload/content/");

        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///C:/upload/");

    }
}