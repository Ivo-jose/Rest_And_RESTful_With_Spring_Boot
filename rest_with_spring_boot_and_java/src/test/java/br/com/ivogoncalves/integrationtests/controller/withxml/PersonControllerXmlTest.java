package br.com.ivogoncalves.integrationtests.controller.withxml;

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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import br.com.ivogoncalves.configs.TestConfigs;
import br.com.ivogoncalves.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.ivogoncalves.integrationtests.vo.AccountCredentialsVO;
import br.com.ivogoncalves.integrationtests.vo.PersonVO;
import br.com.ivogoncalves.integrationtests.vo.TokenVO;
import br.com.ivogoncalves.integrationtests.vo.pagedmodels.PagedModelPerson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class PersonControllerXmlTest extends AbstractIntegrationTest {

	private static RequestSpecification specification;
	private static XmlMapper objectMapper;
	
	private static PersonVO person;
	
	@BeforeAll
	public static void setUp() {
		objectMapper = new XmlMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		person = new PersonVO();
	}
	
	@Test
	@Order(0)
	public void authorization() throws JsonMappingException, JsonProcessingException {
		AccountCredentialsVO user = new AccountCredentialsVO("ivolanda", "admin123");
		var accessToken = given()
							.basePath("/auth/signin")
								.port(TestConfigs.SERVER_PORT)
								.contentType(TestConfigs.CONTENT_TYPE_XML)
								.accept(TestConfigs.CONTENT_TYPE_XML)
							.body(user)
								.when()
							.post()
								.then()
								  .statusCode(200)
								  		.extract()
								  			.body()
								  			   .as(TokenVO.class).getAccessToken();
		
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
		var content = given().spec(specification)
				 .contentType(TestConfigs.CONTENT_TYPE_XML)
				 .accept(TestConfigs.CONTENT_TYPE_XML)
				 .body(person)
				 .when()
				 	.post()
				 .then()
				 	.statusCode(200)
				 		.extract()
				 			.body()
				 			  .asString();
		PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
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
		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
				.body(person)
				.when()
					.post()
				.then()
					.statusCode(200)
						.extract()
							.body()
								.asString();
		PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
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
		var content = given().spec(specification)
				 .contentType(TestConfigs.CONTENT_TYPE_XML)
				 .accept(TestConfigs.CONTENT_TYPE_XML)
				 .pathParam("id", person.getId())
				 .when()
				 	.patch("{id}")
				 .then()
				 	.statusCode(200)
				 		.extract()
				 			.body()
				 			  .asString();
		PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
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
		
		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
				.pathParam("id", person.getId())
				.when()
				.get("{id}")
				.then()
				.statusCode(200)
				.extract()
				.body()
				.asString();
		PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
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
				 .pathParam("id", person.getId())
				 .when()
				 	.delete("{id}")
				 .then()
				 	.statusCode(204);
	}
	
	@Test
	@Order(6)
	public void testFindAll() throws JsonMappingException, JsonProcessingException {
		var content = given().spec(specification)
				 .contentType(TestConfigs.CONTENT_TYPE_XML)
				 .queryParams("page",3, "size", 10, "direction", "asc")
				 .accept(TestConfigs.CONTENT_TYPE_XML)
				 .when()
				 	.get()
				 .then()
				 	.statusCode(200)
				 		.extract()
				 			.body()
				 			  .asString();
		PagedModelPerson wrapper = objectMapper.readValue(content, PagedModelPerson.class);
		var people = wrapper.getContent();

		PersonVO personOne = people.get(0);
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
		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.pathParam("firstName", "ayr")
				.queryParams("page",0, "size", 6, "direction", "asc")
				.accept(TestConfigs.CONTENT_TYPE_XML)
				.when()
					.get("/findPersonByName/{firstName}")
				.then()
					.statusCode(200)
						.extract()
							.body()
								.asString();
		PagedModelPerson wrapper = objectMapper.readValue(content, PagedModelPerson.class);
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
			.contentType(TestConfigs.CONTENT_TYPE_XML)
			.when()
				.get()
			.then()
				.statusCode(403);
	}
	
	@Test
	@Order(9)
	public void testHATEOAS() throws JsonMappingException, JsonProcessingException {
		var content = given().spec(specification)
				 .contentType(TestConfigs.CONTENT_TYPE_XML)
				 .queryParams("page",3, "size", 10, "direction", "asc")
				 .accept(TestConfigs.CONTENT_TYPE_XML)
				 .when()
				 	.get()
				 .then()
				 	.statusCode(200)
				 		.extract()
				 			.body()
				 			  .asString();
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/person/v1/677</href></links>"));
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/person/v1/409</href></links>"));
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/person/v1/797</href></links>"));
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/person/v1/687</href></links>"));
		assertTrue(content.contains("<links><rel>first</rel><href>http://localhost:8888/api/person/v1?direction=asc&amp;page=0&amp;size=10&amp;sort=firstName,asc</href></links>"));
		assertTrue(content.contains("<links><rel>prev</rel><href>http://localhost:8888/api/person/v1?direction=asc&amp;page=2&amp;size=10&amp;sort=firstName,asc</href></links>"));
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/person/v1?page=3&amp;size=10&amp;direction=asc</href></links>"));
		assertTrue(content.contains("<links><rel>next</rel><href>http://localhost:8888/api/person/v1?direction=asc&amp;page=4&amp;size=10&amp;sort=firstName,asc</href></links>"));
		assertTrue(content.contains("<links><rel>last</rel><href>http://localhost:8888/api/person/v1?direction=asc&amp;page=100&amp;size=10&amp;sort=firstName,asc</href></links>"));
		assertTrue(content.contains("<page><size>10</size><totalElements>1007</totalElements><totalPages>101</totalPages><number>3</number></page>"));
	}
	
	private void mockPerson() {
		person.setFirstName("Nelson");
		person.setLastName("Piquet");
		person.setAddress("Brasília, DF - Brasil");
		person.setGender("Male");
		person.setEnabled(true);
	}
}