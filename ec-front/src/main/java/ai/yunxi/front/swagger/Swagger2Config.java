package ai.yunxi.front.swagger;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration  //让Spring来加载该类配置
@EnableWebMvc   //非SpringBoot需启用
@EnableSwagger2 //启用Swagger2
public class Swagger2Config {
    static final Logger log = LoggerFactory.getLogger(Swagger2Config.class);

    @Bean
    public Docket createRestApi() {
        log.info("开始加载Swagger2...");
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo()).select()
                //扫描指定包中的swagger注解
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Api Documentation")
                .description("前台API接口文档")
                .termsOfServiceUrl("")
                .contact(new Contact("EC", "", ""))
                .version("1.0.0")
                .build();
    }
}
