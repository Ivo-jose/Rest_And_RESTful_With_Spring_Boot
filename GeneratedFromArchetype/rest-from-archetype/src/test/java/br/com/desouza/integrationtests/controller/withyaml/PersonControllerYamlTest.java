package br.com.desouza.integrationtests.controller.withyaml;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import br.com.desouza.configs.TestConfigs;
import br.com.desouza.integrationtests.controller.withyaml.mapper.YamlMapper;
import br.com.desouza.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.desouza.integrationtests.vo.AccountCredentialsVO;
import br.com.desouza.integrationtests.vo.PersonVO;
import br.com.desouza.integrationtests.vo.TokenVO;
import br.com.desouza.integrationtests.vo.pagedmodels.PagedModelPerson;
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
public class PersonControllerYamlTest extends AbstractIntegrationTest {

	private static RequestSpecification specification;
	private static YamlMapper objectMapper;
	
	private static PersonVO person;
	
	@BeforeAll
	public static void setUp() {
		objectMapper = new YamlMapper();
		person = new PersonVO();
	}
	
	@Test
	@Order(0)
	public void authorization() throws JsonMappingException, JsonProcessingException {
		AccountCredentialsVO user = new AccountCredentialsVO("ivolanda", "admin123");
		var accessToken = given()
							.config(RestAssuredConfig
									.config()
									.encoderConfig(EncoderConfig.encoderConfig()
													.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)))
							.basePath("/auth/signin")
								.port(TestConfigs.SERVER_PORT)
								.contentType(TestConfigs.CONTENT_TYPE_YAML)
								.accept(TestConfigs.CONTENT_TYPE_YAML)
							.body(user, objectMapper)
								.when()
							.post()
								.then()
								  .statusCode(200)
								  		.extract()
								  			.body()
								  			   .as(TokenVO.class, objectMapper).getAccessToken();
		
		specification = new RequestSpecBuilder()
							.addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
							.setBasePath("/api/person/v1")
							.setPort(TestConfigs.SERVER_PORT)
								.addFilter(new RequestLoggingFilter(LogDetail.ALL))
								.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
							.build();
							
	}
	
	@Test
	@Order(1)
	public void testCreate() throws JsonMappingException, JsonProcessingException {
		mockPerson();
		var persistedPerson = given().spec(specification)
								.config(RestAssuredConfig
										.config()
										.encoderConfig(EncoderConfig.encoderConfig()
														.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)))
								 .contentType(TestConfigs.CONTENT_TYPE_YAML)
								 .accept(TestConfigs.CONTENT_TYPE_YAML)
								 .body(person, objectMapper)
								 .when()
								 	.post()
								 .then()
								 	.statusCode(200)
								 		.extract()
								 			.body()
								 			  .as(PersonVO.class, objectMapper);
		person = persistedPerson;
		assertNotNull(persistedPerson.getId());
		assertNotNull(persistedPerson.getFirstName());
		assertNotNull(persistedPerson.getLastName());
		assertNotNull(persistedPerson.getAddress());
		assertNotNull(persistedPerson.getGender());
		assertTrue(persistedPerson.getEnabled());
		assertEquals(person.getId(), persistedPerson.getId());
		assertEquals("Nelson", persistedPerson.getFirstName());
		assertEquals("Piquet", persistedPerson.getLastName());
		assertEquals("Brasília, DF - Brasil", persistedPerson.getAddress());
		assertEquals("Male", persistedPerson.getGender());
	}
	
	@Test
	@Order(2)
	public void testUpdate() throws JsonMappingException, JsonProcessingException {
		person.setLastName("Piquet Souto Maior");
		var persistedPerson = given().spec(specification)
								.config(RestAssuredConfig
										.config()
										.encoderConfig(EncoderConfig.encoderConfig()
														.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)))
								.contentType(TestConfigs.CONTENT_TYPE_YAML)
								.accept(TestConfigs.CONTENT_TYPE_YAML)
								.body(person, objectMapper)
								.when()
									.post()
								.then()
									.statusCode(200)
										.extract()
											.body()
											.as(PersonVO.class, objectMapper);
		person = persistedPerson;
		assertNotNull(persistedPerson.getId());
		assertNotNull(persistedPerson.getFirstName());
		assertNotNull(persistedPerson.getLastName());
		assertNotNull(persistedPerson.getAddress());
		assertNotNull(persistedPerson.getGender());
		assertTrue(persistedPerson.getEnabled());
		assertEquals(person.getId(), persistedPerson.getId());
		assertEquals("Nelson", persistedPerson.getFirstName());
		assertEquals("Piquet Souto Maior", persistedPerson.getLastName());
		assertEquals("Brasília, DF - Brasil", persistedPerson.getAddress());
		assertEquals("Male", persistedPerson.getGender());
	}
	
	@Test
	@Order(3)
	public void testDisablePersonById() throws JsonMappingException, JsonProcessingException {
		var persistedPerson = given().spec(specification)
				 .config(RestAssuredConfig
						.config()
						.encoderConfig(EncoderConfig.encoderConfig()
										.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT))) 
				 .contentType(TestConfigs.CONTENT_TYPE_YAML)
				 .accept(TestConfigs.CONTENT_TYPE_YAML)
				 .pathParam("id", person.getId())
				 .when()
				 	.patch("{id}")
				 .then()
				 	.statusCode(200)
				 		.extract()
				 			.body()
				 			  .as(PersonVO.class, objectMapper);
		person = persistedPerson;
		assertNotNull(persistedPerson.getId());
		assertNotNull(persistedPerson.getFirstName());
		assertNotNull(persistedPerson.getLastName());
		assertNotNull(persistedPerson.getAddress());
		assertNotNull(persistedPerson.getGender());
		assertFalse(persistedPerson.getEnabled());
		assertEquals(person.getId(), persistedPerson.getId());
		assertEquals("Nelson", persistedPerson.getFirstName());
		assertEquals("Piquet Souto Maior", persistedPerson.getLastName());
		assertEquals("Brasília, DF - Brasil", persistedPerson.getAddress());
		assertEquals("Male", persistedPerson.getGender());
	}
	
	@Test
	@Order(4)
	public void testFindById() throws JsonMappingException, JsonProcessingException {
		mockPerson();
		
		var persistedPerson = given().spec(specification)
				.config(RestAssuredConfig
						.config()
						.encoderConfig(EncoderConfig.encoderConfig()
								.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT))) 
				.contentType(TestConfigs.CONTENT_TYPE_YAML)
				.accept(TestConfigs.CONTENT_TYPE_YAML)
				.pathParam("id", person.getId())
				.when()
				.get("{id}")
				.then()
				.statusCode(200)
				.extract()
				.body()
				.as(PersonVO.class, objectMapper);
		person = persistedPerson;
		assertNotNull(persistedPerson.getId());
		assertNotNull(persistedPerson.getFirstName());
		assertNotNull(persistedPerson.getLastName());
		assertNotNull(persistedPerson.getAddress());
		assertNotNull(persistedPerson.getGender());
		assertFalse(persistedPerson.getEnabled());
		assertEquals(person.getId(), persistedPerson.getId());
		assertEquals("Nelson", persistedPerson.getFirstName());
		assertEquals("Piquet Souto Maior", persistedPerson.getLastName());
		assertEquals("Brasília, DF - Brasil", persistedPerson.getAddress());
		assertEquals("Male", persistedPerson.getGender());
	}
	
	@Test
	@Order(5)
	public void testDelete() throws JsonMappingException, JsonProcessingException {
		given().spec(specification)
				.config(RestAssuredConfig
						.config()
							.encoderConfig(EncoderConfig.encoderConfig()
										.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT))) 
				 .contentType(TestConfigs.CONTENT_TYPE_YAML)
				 .accept(TestConfigs.CONTENT_TYPE_YAML)	
				 .pathParam("id", person.getId())
				 .when()
				 	.delete("{id}")
				 .then()
				 	.statusCode(204);
	}
	
	@Test
	@Order(6)
	public void testFindAll() throws JsonMappingException, JsonProcessingException {
		var wrapper = given().spec(specification)
				.config(RestAssuredConfig
						.config()
						.encoderConfig(EncoderConfig.encoderConfig()
										.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT))) 
				 .contentType(TestConfigs.CONTENT_TYPE_YAML)
				 .queryParams("page",3, "size", 10, "direction", "asc")
				 .accept(TestConfigs.CONTENT_TYPE_YAML)
				 .when()
					.get()
				.then()
					.statusCode(200)
						.extract()
						  .body()
						     .as(PagedModelPerson.class, objectMapper);
		
		var people = wrapper.getContent();

		PersonVO personOne =  people.get(0);
		assertNotNull(personOne.getId());
		assertNotNull(personOne.getFirstName());
		assertNotNull(personOne.getLastName());
		assertNotNull(personOne.getAddress());
		assertNotNull(personOne.getGender());
		assertTrue(personOne.getEnabled());
		assertEquals(people.get(0).getId(), personOne.getId());
		assertEquals("Alic", personOne.getFirstName());
		assertEquals("Terbrug", personOne.getLastName());
		assertEquals("3 Eagle Crest Court", personOne.getAddress());
		assertEquals("Male", personOne.getGender());
		
		PersonVO personSeven = people.get(5);
		assertNotNull(personSeven.getId());
		assertNotNull(personSeven.getFirstName());
		assertNotNull(personSeven.getLastName());
		assertNotNull(personSeven.getAddress());
		assertNotNull(personSeven.getGender());
		assertTrue(personSeven.getEnabled());
		assertEquals(people.get(5).getId(), personSeven.getId());
		assertEquals("Allegra", personSeven.getFirstName());
		assertEquals("Dome", personSeven.getLastName());
		assertEquals("57 Roxbury Pass", personSeven.getAddress());
		assertEquals("Female", personSeven.getGender());
	}
	
	@Test
	@Order(7)
	public void testFindByName() throws JsonMappingException, JsonProcessingException {
		var wrapper = given().spec(specification)
				.config(RestAssuredConfig
						.config()
						.encoderConfig(EncoderConfig.encoderConfig()
								.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT))) 
				.contentType(TestConfigs.CONTENT_TYPE_YAML)
				.pathParam("firstName", "ayr")
				.queryParams("page",0, "size", 6, "direction", "asc")
				.accept(TestConfigs.CONTENT_TYPE_YAML)
				.when()
					.get("/findPersonByName/{firstName}")
				.then()
					.statusCode(200)
						.extract()
							.body()
								.as(PagedModelPerson.class, objectMapper);
		
		var people = wrapper.getContent();
		
		PersonVO personOne = people.get(0);
		assertNotNull(personOne.getId());
		assertNotNull(personOne.getFirstName());
		assertNotNull(personOne.getLastName());
		assertNotNull(personOne.getAddress());
		assertNotNull(personOne.getGender());
		assertTrue(personOne.getEnabled());
		assertEquals(people.get(0).getId(), personOne.getId());
		assertEquals("Ayrton", personOne.getFirstName());
		assertEquals("Senna", personOne.getLastName());
		assertEquals("São Paulo - Vila Maria", personOne.getAddress());
		assertEquals("Male", personOne.getGender());
	}

	@Test
	@Order(8)
	public void testFindAllWithoutToken() throws JsonMappingException, JsonProcessingException {
		RequestSpecification specificationWithoutToken = new RequestSpecBuilder()
				.setBasePath("/api/person/v1")
				.setPort(TestConfigs.SERVER_PORT)
					.addFilter(new RequestLoggingFilter(LogDetail.ALL))
					.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
				.build();
		
		given().spec(specificationWithoutToken)
			.config(RestAssuredConfig
					.config()
					.encoderConfig(EncoderConfig.encoderConfig()
								.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)))
			.contentType(TestConfigs.CONTENT_TYPE_YAML)
			.when()
				.get()
			.then()
				.statusCode(403);
	}
	
	@Test
	@Order(9)
	public void testHATEOAS() throws JsonMappingException, JsonProcessingException {
		var unthreatedContent = given().spec(specification)
				.config(RestAssuredConfig
						.config()
						.encoderConfig(EncoderConfig.encoderConfig()
										.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT))) 
				 .contentType(TestConfigs.CONTENT_TYPE_YAML)
				 .queryParams("page",3, "size", 10, "direction", "asc")
				 .accept(TestConfigs.CONTENT_TYPE_YAML)
				 .when()
					.get()
				.then()
					.statusCode(200)
						.extract()
						  .body()
						     .asString();
		
		var content = unthreatedContent.replace("\n", "").replace("\r", "");
		
		assertTrue(content.contains("rel: \"self\"    href: \"http://localhost:8888/api/person/v1/677\""));
		assertTrue(content.contains("rel: \"self\"    href: \"http://localhost:8888/api/person/v1/409\""));
		assertTrue(content.contains("rel: \"self\"    href: \"http://localhost:8888/api/person/v1/797\""));
		assertTrue(content.contains("rel: \"self\"    href: \"http://localhost:8888/api/person/v1/687\""));
		assertTrue(content.contains("rel: \"first\"  href: \"http://localhost:8888/api/person/v1?direction=asc&page=0&size=10&sort=firstName,asc\""));
		assertTrue(content.contains("rel: \"prev\"  href: \"http://localhost:8888/api/person/v1?direction=asc&page=2&size=10&sort=firstName,asc\""));
		assertTrue(content.contains("rel: \"self\"  href: \"http://localhost:8888/api/person/v1?page=3&size=10&direction=asc\""));
		assertTrue(content.contains("rel: \"next\"  href: \"http://localhost:8888/api/person/v1?direction=asc&page=4&size=10&sort=firstName,asc\""));
		assertTrue(content.contains("rel: \"last\"  href: \"http://localhost:8888/api/person/v1?direction=asc&page=100&size=10&sort=firstName,asc\""));
		assertTrue(content.contains("page:  size: 10  totalElements: 1007  totalPages: 101  number: 3"));
	}
	
	private void mockPerson() {
		person.setFirstName("Nelson");
		person.setLastName("Piquet");
		person.setAddress("Brasília, DF - Brasil");
		person.setGender("Male");
		person.setEnabled(true);
	}
}