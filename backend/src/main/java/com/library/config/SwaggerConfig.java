package com.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 文档配置类。
 * 启动项目后访问 /swagger-ui.html 即可查看所有接口的请求/响应文档。
 * 便于前端开发人员调试接口，无需额外维护接口文档。
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置 OpenAPI 文档的基本信息，包括标题、描述、版本号和联系方式。
     * 该信息会显示在 Swagger UI 页面顶部。
     */
    @Bean
    public OpenAPI libraryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        // API 文档标题
                        .title("图书馆管理系统 API")
                        // API 文档描述，说明技术栈和对接数据库
                        .description("Spring Boot + MyBatis 后端接口文档，对接 library_db 数据库")
                        // 当前 API 版本号
                        .version("1.0.0")
                        // 开发团队联系方式
                        .contact(new Contact().name("成员B").email("library@example.com")));
    }
}
