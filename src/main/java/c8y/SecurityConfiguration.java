package c8y;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(info = @Info(title = "RESTful Custom API", version = "1.0"), tags = {
    @Tag(name = "Property Definitions") ,
    @Tag(name = "Asset Definitions")
})
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "basic", name = "Basic")
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
@Order(90)
public class SecurityConfiguration {
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
       return web -> web.ignoring().antMatchers("/v3/api-docs", "/v3/api-docs.yaml","/swagger-ui/**","/api");
    }
    
}
