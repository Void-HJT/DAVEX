package DavexAgent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableOpenApi
public class Swagger3Config {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .enable(true) // true 启用Swagger3.0， false 禁用（生产环境要禁用）
                .select()
                // 扫描的路径使用@Api的controller
                .paths(PathSelectors.any()) // 指定路径处理PathSelectors.any()代表所有的路径
                .build()
                .globalRequestParameters(globalHeaderParameters());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("DAVEX-agent 文档")
                .description("DAVEX-agent 文档")
                .version("0.0.1")
                .build();
    }

    private List<RequestParameter> globalHeaderParameters() {
        List<RequestParameter> parameters = new ArrayList<>();
        parameters.add(new RequestParameterBuilder()
                .name("Token")
                .description("用户认证令牌")
                .in(ParameterType.HEADER) // 指定参数放在 Header 中
                .required(false) // 是否为必填
                .query(q -> q.defaultValue("Bearer [token-value]"))
                .build());

        parameters.add(new RequestParameterBuilder()
                .name("Authentication-ID")
                .description("用户认证ID")
                .in(ParameterType.HEADER) // 指定参数放在 Header 中
                .required(false) // 是否为必填
                .query(q -> q.defaultValue("[auth-id-value]"))
                .build());

        return parameters;
    }
}
