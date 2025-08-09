package com.young.aicustomer.config;

import com.young.aicustomer.interceptor.LoginInterceptor;
import com.young.aicustomer.interceptor.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final StringRedisTemplate stringRedisTemplate;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:8080"); // 可改为你的前端地址
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

//        config.addExposedHeader("Content-Disposition");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/users/register",
                        "/users/login",
                        "/users/code/**"
                ).order(1);
        registry.addInterceptor(new TokenInterceptor(stringRedisTemplate))
                .addPathPatterns("/**").order(0);   // 全局过滤器-token刷新：优先拦截
//        WebMvcConfigurer.super.addInterceptors(registry);
    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        //跨域配置
////        registry
////                .addMapping("/**")
////                .allowedOrigins("*") // http://localhost:8080", "http://localhost:8889
////                .allowedMethods(new String[]{"GET", "POST", "PUT", "DELETE"})
////                .allowedHeaders("*");
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:8080")
//                .allowedMethods("GET", "POST", "PUT", "DELETE")
//                .allowedHeaders("*")
//                .allowCredentials(true);
//
//
//    }
}
