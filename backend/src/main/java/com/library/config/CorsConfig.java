package com.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域（CORS）全局配置类。
 * 开发阶段允许前端 dev server 跨域访问后端接口，解决前后端分离开发的同源限制。
 * 生产部署时建议将 allowedOriginPattern 改为具体域名以提高安全性。
 */
@Configuration
public class CorsConfig {

    /**
     * 注册全局 CORS 过滤器，对所有请求路径生效。
     * 允许任意来源、任意请求头、任意 HTTP 方法，并携带 Cookie 凭证。
     */
    @Bean
    public CorsFilter corsFilter() {
        // 创建跨域配置对象
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源域名访问（开发环境用 *，生产环境应指定具体域名）
        config.addAllowedOriginPattern("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有 HTTP 方法（GET/POST/PUT/DELETE 等）
        config.addAllowedMethod("*");
        // 允许携带 Cookie 等凭证信息
        config.setAllowCredentials(true);

        // 基于 URL 路径注册跨域配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径（/**）应用上述跨域规则
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
