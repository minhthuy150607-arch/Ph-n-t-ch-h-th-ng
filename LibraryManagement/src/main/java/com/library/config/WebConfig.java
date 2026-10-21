package com.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String dataDir = System.getenv("DATA_DIR");
        Path uploadDir = (dataDir != null && !dataDir.isBlank())
                ? Paths.get(dataDir, "uploads")
                : Paths.get("./uploads");

        String uriStr = uploadDir.toUri().toString();
        if (!uriStr.endsWith("/")) {
            uriStr += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uriStr);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}
