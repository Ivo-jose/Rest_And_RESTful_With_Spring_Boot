package br.com.desouza.integrationtests.controller.withxml;

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
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import br.com.desouza.configs.TestConfigs;
import br.com.desouza.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.desouza.integrationtests.vo.AccountCredentialsVO;
import br.com.desouza.integrationtests.vo.BookVO;
import br.com.desouza.integrationtests.vo.TokenVO;
import br.com.desouza.integrationtests.vo.pagedmodels.PagedModelBook;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class BookControllerXmlTest extends AbstractIntegrationTest {

	private static RequestSpecification specification;
	private static XmlMapper objectMapper;
	
	private static BookVO book;
	
	@BeforeAll
	public static void setUp() {
		objectMapper = new XmlMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		book = new BookVO();
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
				 .contentType(TestConfigs.CONTENT_TYPE_XML)
				 .accept(TestConfigs.CONTENT_TYPE_XML)
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
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
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
				 .contentType(TestConfigs.CONTENT_TYPE_XML)
				 .accept(TestConfigs.CONTENT_TYPE_XML)
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
				 .contentType(TestConfigs.CONTENT_TYPE_XML)
				 .accept(TestConfigs.CONTENT_TYPE_XML)
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
		PagedModelBook wrapper = objectMapper.readValue(content, PagedModelBook.class);
		var people = wrapper.getContent();
		
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
			.contentType(TestConfigs.CONTENT_TYPE_XML)
			.when()
				.get()
			.then()
				.statusCode(403);
	}
	
	@Test
	@Order(7)
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
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/book/v1/527</href></links>"));
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/book/v1/249</href></links>"));
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/book/v1/341</href></links>"));
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/book/v1/887</href></links>"));
		assertTrue(content.contains("<links><rel>first</rel><href>http://localhost:8888/api/book/v1?direction=asc&amp;page=0&amp;size=10&amp;sort=title,asc</href></links>"));
		assertTrue(content.contains("<links><rel>prev</rel><href>http://localhost:8888/api/book/v1?direction=asc&amp;page=2&amp;size=10&amp;sort=title,asc</href></links>"));
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/book/v1?page=3&amp;size=10&amp;direction=asc</href></links>"));
		assertTrue(content.contains("<links><rel>next</rel><href>http://localhost:8888/api/book/v1?direction=asc&amp;page=4&amp;size=10&amp;sort=title,asc</href></links>"));
		assertTrue(content.contains("<links><rel>last</rel><href>http://localhost:8888/api/book/v1?direction=asc&amp;page=101&amp;size=10&amp;sort=title,asc</href></links>"));
		assertTrue(content.contains("<page><size>10</size><totalElements>1015</totalElements><totalPages>102</totalPages><number>3</number></page>"));
		
	}
	
	private void mockBook() {
        book.setTitle("Docker Deep Dive");
        book.setAuthor("Nigel Poulton");
        book.setPrice(Double.valueOf(55.99));
        book.setLaunchDate(new Date());
    }  
}