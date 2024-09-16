package br.com.desouza.unittests.mockito.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.desouza.data.vo.v1.BookVO;
import br.com.desouza.exceptions.RequiredObjectIsNullException;
import br.com.desouza.model.Book;
import br.com.desouza.respositories.BookRepository;
import br.com.desouza.services.BookServices;
import br.com.desouza.unittests.mapper.mocks.MockBook;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class BookServicesTest {
	
	MockBook input;
	
	@InjectMocks
	private BookServices service;
	@Mock
	BookRepository repository;
	
	@BeforeEach
	void setUpMocks() throws Exception {
		input = new MockBook();
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testFindById() {
		Book entity = input.mockEntity(1);
		when(repository.findById(1L)).thenReturn(Optional.of(entity));
		var result = service.findById(1L);
		assertNotNull(result);
		assertNotNull(result.getIdBook());
		assertNotNull(result.getLinks());
		assertTrue(result.toString().contains("links: [</api/book/v1/1>;rel=\"self\"]"));
		assertEquals("Title Test1", result.getTitle());
		assertEquals("Author Test1", result.getAuthor());
		assertNotNull(result.getLaunchDate());
		assertEquals(101.0, result.getPrice());
	}

	@Test
	void testCreate() {
		Book entity = input.mockEntity(1);;
		Book persisted = entity;
		BookVO vo = input.mockVO(1);
		when(repository.save(entity)).thenReturn(persisted);
		var result = service.create(vo);
		assertNotNull(result);
		assertNotNull(result.getIdBook());
		assertNotNull(result.getLinks());
		assertTrue(result.toString().contains("links: [</api/book/v1/1>;rel=\"self\"]"));
		assertEquals("Title Test1", result.getTitle());
		assertEquals("Author Test1", result.getAuthor());
		assertNotNull(result.getLaunchDate());
		assertEquals(101.0, result.getPrice());
	}
	
	
	@Test
	void testCreateWithNullBook() {
		Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> {service.create(null);});
		String expectedMessage = "Not allowed to persist a null object";
		assertTrue(exception.getMessage().contains(expectedMessage));
	}

	@Test
	void testUpdate() {
		Book entity = input.mockEntity(1);;
		Book persisted = entity;
		BookVO vo = input.mockVO(1);
		when(repository.findById(1L)).thenReturn(Optional.of(entity));
		when(repository.save(entity)).thenReturn(persisted);
		var result = service.update(vo);
		assertNotNull(result);
		assertNotNull(result.getIdBook());
		assertNotNull(result.getLinks());
		assertTrue(result.toString().contains("links: [</api/book/v1/1>;rel=\"self\"]"));
		assertEquals("Title Test1", result.getTitle());
		assertEquals("Author Test1", result.getAuthor());
		assertNotNull(result.getLaunchDate());
		assertEquals(101.0, result.getPrice());
	}
	
	@Test
	void testUpdateWithNullBook() {
		Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> {service.update(null);});
		String expectedMessage = "Not allowed to persist a null object";
		assertTrue(exception.getMessage().contains(expectedMessage));
	}

	@Test
	void testDelete() {
		Book entity = input.mockEntity(1);
		when(repository.findById(1L)).thenReturn(Optional.of(entity));
		service.delete(1L);
	}
}
