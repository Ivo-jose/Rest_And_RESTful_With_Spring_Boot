package br.com.desouza.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
					.title("RESTful API with Java 19 and Spring Boot 3.1.1 - Doc.")
					.version("v1")
					.description("For skill development")
					.termsOfService("https://github.com/Ivo-jose/Rest_and_RESTful_With_Spring_Boot/tree/main")
					.license(new License()
							.name("Apache 2.0")
							.url("https://www.apache.org/licenses/LICENSE-2.0")));	
	}
}
