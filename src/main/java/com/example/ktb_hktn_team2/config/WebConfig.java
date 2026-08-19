package com.example.ktb_hktn_team2.config;

import com.example.ktb_hktn_team2.auth.AuthInterceptor;
import com.example.ktb_hktn_team2.auth.LoginMemberArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    /**
     * 토큰 없이 접근할 수 있는 경로. 나머지 /api/** 는 전부 Bearer 토큰이 필요하다.
     */
    private static final String[] PUBLIC_PATHS = {
            "/api/product-key/**",
            "/api/signup",
            "/api/login"
    };

    private final AuthInterceptor authInterceptor;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(PUBLIC_PATHS);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    /**
     * 토큰을 쿠키가 아니라 Authorization 헤더로 주고받으므로 allowCredentials 는 필요 없다.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
