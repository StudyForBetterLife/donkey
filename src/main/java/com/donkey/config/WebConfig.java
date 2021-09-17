package com.donkey.config;

import com.donkey.security.LoginUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("uploads")
    private String path;

    private final LoginUserArgumentResolver loginUserArgumentResolver;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        long MAX_AGE_SECS = 3600;
        registry
                // CORS 적용할 URL 패턴
                .addMapping("/**")
                // 자원을 공유할 오리진 지정
                .allowedOrigins("http://localhost:8081")
                // 요청 허용 메서드
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                // 요청 허용 헤더
                .allowedHeaders("*")
                // 쿠키 허용
                .allowCredentials(true)
                .maxAge(MAX_AGE_SECS);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // HandleMethodArgumentResolver는 항상 WebMvcConfigurer의 addArgumentResolvers()를 통해 추가해야한다.
        resolvers.add(loginUserArgumentResolver);
    }

    // https://subji.github.io/posts/2019/11/21/springboot-external-reources-solve-copy-2https://subji.github.io/posts/2019/11/21/springboot-external-reources-solve-copy-2
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler(path + "/**") // url 접근 경로
                .addResourceLocations("file:" + path + "/"); // 디렉토리 경로 (반드시 file: 을 붙여주어야 한다.)
    }

}
