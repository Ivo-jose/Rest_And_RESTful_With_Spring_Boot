package br.com.desouza.integrationtests.controller.withjson;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.desouza.configs.TestConfigs;
import br.com.desouza.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.desouza.integrationtests.vo.AccountCredentialsVO;
import br.com.desouza.integrationtests.vo.BookVO;
import br.com.desouza.integrationtests.vo.TokenVO;
import br.com.desouza.integrationtests.vo.wrappers.WrapperBookVO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class BookControllerJsonTest extends AbstractIntegrationTest {

	private static RequestSpecification specification;
	private static ObjectMapper objectMapper;
	
	private static BookVO book;
	
	@BeforeAll
	public static void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		book = new BookVO();
	}
	
	@Test
	@Order(0)
	public void authorization() throws JsonMappingException, JsonProcessingException {
		AccountCredentialsVO user = new AccountCredentialsVO("ivolanda", "admin123");
		var accessToken = given()
							.basePath("/auth/signin")
								.port(TestConfigs.SERVER_PORT).contentType(TestConfigs.CONTENT_TYPE_JSON)
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
							.setBasePath("/api/book/v1")
							.setPort(TestConfigs.SERVER_PORT)
								.addFilter(new RequestLoggingFilter(LogDetail.ALL))
								.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
							.build();
							
	}
	
	@Test
	@Order(1)
	public void testCreate() throws JsonMappingException, JsonProcessingException {
		mockBook();
		var content = given().spec(specification)
				 .contentType(TestConfigs.CONTENT_TYPE_JSON)
				 .body(book)
				 .when()
				 	.post()
				 .then()
				 	.statusCode(200)
				 		.extract()
				 			.body()
				 			  .asString();
		BookVO persistedBook = objectMapper.readValue(content, BookVO.class);
		book = persistedBook;
		assertNotNull(persistedBook.getId());
        assertNotNull(persistedBook.getTitle());
        assertNotNull(persistedBook.getAuthor());
        assertNotNull(persistedBook.getPrice());
        assertNotNull(persistedBook.getLaunchDate());
        assertTrue(persistedBook.getId() > 0);
        assertEquals(book.getId(), persistedBook.getId());
        assertEquals("Docker Deep Dive", persistedBook.getTitle());
        assertEquals("Nigel Poulton", persistedBook.getAuthor());
        assertEquals(55.99, persistedBook.getPrice());
	}
	
	@Test
	@Order(2)
	public void testUpdate() throws JsonMappingException, JsonProcessingException {
		book.setTitle("Docker Deep Dive - Updated");
		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
				.body(book)
				.when()
					.post()
				.then()
					.statusCode(200)
						.extract()
							.body()
								.asString();
		BookVO persistedBook = objectMapper.readValue(content, BookVO.class);
		book = persistedBook;
		assertNotNull(persistedBook.getId());
        assertNotNull(persistedBook.getTitle());
        assertNotNull(persistedBook.getAuthor());
        assertNotNull(persistedBook.getPrice());
        assertNotNull(persistedBook.getLaunchDate());
        assertTrue(persistedBook.getId() > 0);
        assertEquals(book.getId(), persistedBook.getId());
        assertEquals("Docker Deep Dive - Updated", persistedBook.getTitle());
        assertEquals("Nigel Poulton", persistedBook.getAuthor());
        assertEquals(55.99, persistedBook.getPrice());
	}
	
	@Test
	@Order(3)
	public void testFindById() throws JsonMappingException, JsonProcessingException {
		mockBook();
		
		var content = given().spec(specification)
				 .contentType(TestConfigs.CONTENT_TYPE_JSON)
				 .pathParam("id", book.getId())
				 .when()
				 	.get("{id}")
				 .then()
				 	.statusCode(200)
				 		.extract()
				 			.body()
				 			  .asString();
		BookVO persistedBook = objectMapper.readValue(content, BookVO.class);
		book = persistedBook;
		assertNotNull(persistedBook.getId());
        assertNotNull(persistedBook.getTitle());
        assertNotNull(persistedBook.getAuthor());
        assertNotNull(persistedBook.getPrice());
        assertNotNull(persistedBook.getLaunchDate());
        assertTrue(persistedBook.getId() > 0);
        assertEquals(book.getId(), persistedBook.getId());
        assertEquals("Docker Deep Dive - Updated", persistedBook.getTitle());
        assertEquals("Nigel Poulton", persistedBook.getAuthor());
        assertEquals(55.99, persistedBook.getPrice());
	}
	
	@Test
	@Order(4)
	public void testDelete() throws JsonMappingException, JsonProcessingException {
		given().spec(specification)
				 .contentType(TestConfigs.CONTENT_TYPE_JSON)	
				 .pathParam("id", book.getId())
				 .when()
				 	.delete("{id}")
				 .then()
				 	.statusCode(204);
	}
	
	@Test
	@Order(5)
	public void testFindAll() throws JsonMappingException, JsonProcessingException {
		var content = given().spec(specification)
				 .contentType(TestConfigs.CONTENT_TYPE_JSON)
				 .queryParams("page",3, "size", 10, "direction", "asc")
				 .when()
				 	.get()
				 .then()
				 	.statusCode(200)
				 		.extract()
				 			.body()
				 			  .asString();
		WrapperBookVO wrapper = objectMapper.readValue(content, WrapperBookVO.class);
		var people = wrapper.getEmbedded().getBooks();

		BookVO bookOne = people.get(0);
		assertNotNull(bookOne.getId());
        assertNotNull(bookOne.getTitle());
        assertNotNull(bookOne.getAuthor());
        assertNotNull(bookOne.getPrice());
        assertNotNull(bookOne.getLaunchDate());
        assertTrue(bookOne.getId() > 0);
        assertEquals(people.get(0).getId(), bookOne.getId());
        assertEquals("Account Representative IV", bookOne.getTitle());
        assertEquals("Leonid Buckthorp", bookOne.getAuthor());
        assertEquals(74.36, bookOne.getPrice());
		
		BookVO bookSix = people.get(5);
		assertNotNull(bookSix.getId());
        assertNotNull(bookSix.getTitle());
        assertNotNull(bookSix.getAuthor());
        assertNotNull(bookSix.getPrice());
        assertNotNull(bookSix.getLaunchDate());
        assertTrue(bookSix.getId() > 0);
        assertEquals(people.get(5).getId(), bookSix.getId());
        assertEquals("Accountant III", bookSix.getTitle());
        assertEquals("Gloriana Deathridge", bookSix.getAuthor());
        assertEquals(476.03, bookSix.getPrice());
	}

	@Test
	@Order(6)
	public void testFindAllWithoutToken() throws JsonMappingException, JsonProcessingException {
		RequestSpecification specificationWithoutToken = new RequestSpecBuilder()
				.setBasePath("/api/book/v1")
				.setPort(TestConfigs.SERVER_PORT)
					.addFilter(new RequestLoggingFilter(LogDetail.ALL))
					.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
				.build();
		
		given().spec(specificationWithoutToken)
			.contentType(TestConfigs.CONTENT_TYPE_JSON)
			.when()
				.get()
			.then()
				.statusCode(403);
	}
	
	@Test
	@Order(7)
	public void testHATEOAS() throws JsonMappingException, JsonProcessingException {
		var content = given().spec(specification)
				 .contentType(TestConfigs.CONTENT_TYPE_JSON)
				 .queryParams("page",3, "size", 10, "direction", "asc")
				 .when()
				 	.get()
				 .then()
				 	.statusCode(200)
				 		.extract()
				 			.body()
				 			  .asString();
		assertTrue(content.contains("\"_links\":{\"self\":{\"href\":\"http://localhost:8888/api/book/v1/527\"}}}"));
		assertTrue(content.contains("\"_links\":{\"self\":{\"href\":\"http://localhost:8888/api/book/v1/249\"}}}"));
		assertTrue(content.contains("\"_links\":{\"self\":{\"href\":\"http://localhost:8888/api/book/v1/341\"}}}"));
		assertTrue(content.contains("\"_links\":{\"self\":{\"href\":\"http://localhost:8888/api/book/v1/887\"}}}"));
		assertTrue(content.contains("\"_links\":{\"first\":{\"href\":\"http://localhost:8888/api/book/v1?direction=asc&page=0&size=10&sort=title,asc\"}"));
		assertTrue(content.contains("\"prev\":{\"href\":\"http://localhost:8888/api/book/v1?direction=asc&page=2&size=10&sort=title,asc\"}"));
		assertTrue(content.contains("\"self\":{\"href\":\"http://localhost:8888/api/book/v1?page=3&size=10&direction=asc\"}"));
		assertTrue(content.contains("\"next\":{\"href\":\"http://localhost:8888/api/book/v1?direction=asc&page=4&size=10&sort=title,asc\"}"));
		assertTrue(content.contains("\"last\":{\"href\":\"http://localhost:8888/api/book/v1?direction=asc&page=101&size=10&sort=title,asc\"}}"));
		assertTrue(content.contains("\"page\":{\"size\":10,\"totalElements\":1015,\"totalPages\":102,\"number\":3}}"));
	}

	
	private void mockBook() {
        book.setTitle("Docker Deep Dive");
        book.setAuthor("Nigel Poulton");
        book.setPrice(Double.valueOf(55.99));
        book.setLaunchDate(new Date());
    }  
}