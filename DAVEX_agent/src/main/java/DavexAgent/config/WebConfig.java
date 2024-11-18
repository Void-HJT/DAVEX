package DavexAgent.config;

import DavexBase.common.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")  // 对所有路径进行拦截
                //更改下列用于测试！！！
                .excludePathPatterns("/**")
                .excludePathPatterns("/swagger-ui/**")           // Swagger UI 静态页面
                .excludePathPatterns("/swagger-resources/**")    // Swagger 资源配置
                .excludePathPatterns("/v3/api-docs/**")          // OpenAPI 3.0 文档路径
                .excludePathPatterns("/webjars/**");             // Swagger UI 所需静态资源
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**") // 所有接口
                .allowCredentials(true) // 是否发送 Cookie
                .allowedOriginPatterns("*") // 支持域
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 支持方法
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}

