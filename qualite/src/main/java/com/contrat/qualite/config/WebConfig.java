package com.contrat.qualite.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // T'appliquer CORS 3la ga3 les APIs
                .allowedOrigins("http://localhost:3000") // L'port dyal Next.js
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // OPTIONS hya li katji m3a CORS
                .allowedHeaders("*") // N9blou ga3 les headers
                .allowCredentials(true);
    }
}