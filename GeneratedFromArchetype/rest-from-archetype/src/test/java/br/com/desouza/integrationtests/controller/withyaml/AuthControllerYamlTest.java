package br.com.desouza.integrationtests.controller.withyaml;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import br.com.desouza.configs.TestConfigs;
import br.com.desouza.integrationtests.controller.withyaml.mapper.YamlMapper;
import br.com.desouza.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.desouza.integrationtests.vo.AccountCredentialsVO;
import br.com.desouza.integrationtests.vo.TokenVO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class AuthControllerYamlTest extends AbstractIntegrationTest{

	private static TokenVO tokenVO;
	private static YamlMapper objectMapper;
	
	@BeforeAll
	public static void setup() {
		objectMapper = new YamlMapper();
	}
	
	
	@Test
	@Order(1)
	public void testSignin() throws JsonMappingException, JsonProcessingException {
		AccountCredentialsVO user = new AccountCredentialsVO("ivolanda", "admin123");
		RequestSpecification specification = new RequestSpecBuilder()
				.addFilter(new RequestLoggingFilter(LogDetail.ALL))
				.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
			.build();
		
		tokenVO = given().spec(specification)
				.config(RestAssuredConfig
						.config()
						.encoderConfig(EncoderConfig.encoderConfig()
										.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)))
					.accept(TestConfigs.CONTENT_TYPE_YAML)
					.basePath("/auth/signin")
						.port(TestConfigs.SERVER_PORT).contentType(TestConfigs.CONTENT_TYPE_YAML)
					.body(user, objectMapper)
						.when()
					.post()
						.then()
							.statusCode(200)
								.extract()
									.body()
										.as(TokenVO.class, objectMapper);
		assertNotNull(tokenVO.getAccessToken());
		assertNotNull(tokenVO.getRefreshToken());
	}
	
	@Test
	@Order(2)
	public void testRefresh() throws JsonMappingException, JsonProcessingException {
		var newTokenVO = given()
							.config(RestAssuredConfig
									.config()
									.encoderConfig(EncoderConfig.encoderConfig()
													.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)))
							.accept(TestConfigs.CONTENT_TYPE_YAML)
							.basePath("/auth/refresh")
				.port(TestConfigs.SERVER_PORT).contentType(TestConfigs.CONTENT_TYPE_YAML)
					.pathParam("username", tokenVO.getUsername())
					.header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenVO.getRefreshToken())
				.when()
				.put("{username}")
					.then()
						.statusCode(200)
							.extract()
								.body()
									.as(TokenVO.class, objectMapper);
		assertNotNull(newTokenVO.getAccessToken());
		assertNotNull(newTokenVO.getRefreshToken());
	}
}