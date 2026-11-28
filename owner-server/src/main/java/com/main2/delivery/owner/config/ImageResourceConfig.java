package com.main2.delivery.owner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

// 업로드한 이미지를 /images/** 주소로 내보낸다 (로컬 저장소일 때만 필요. 클라우드 저장소로 바꾸면 제거)
// 저장 위치는 LocalImageStorage 와 같은 설정값(app.image.upload-dir)을 쓴다.
@Configuration
public class ImageResourceConfig implements WebMvcConfigurer {

    private final String location;

    public ImageResourceConfig(@Value("${app.image.upload-dir}") String uploadDir) {
        String uri = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();   // file:///.../uploads/images/
        this.location = uri.endsWith("/") ? uri : uri + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**").addResourceLocations(location);
    }
}
