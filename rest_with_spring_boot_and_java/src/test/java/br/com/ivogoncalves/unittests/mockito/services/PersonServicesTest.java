package br.com.ivogoncalves.unittests.mockito.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;
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

import br.com.ivogoncalves.data.vo.v1.PersonVO;
import br.com.ivogoncalves.exceptions.RequiredObjectIsNullException;
import br.com.ivogoncalves.model.Person;
import br.com.ivogoncalves.respositories.PersonRepository;
import br.com.ivogoncalves.services.PersonServices;
import br.com.ivogoncalves.unittests.mapper.mocks.MockPerson;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class PersonServicesTest {
	
	MockPerson input;
	
	@InjectMocks
	private PersonServices service;
	@Mock
	PersonRepository repository;
	
	@BeforeEach
	void setUpMocks() throws Exception {
		input = new MockPerson();
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testFindById() {
		Person entity = input.mockEntity(1);
		when(repository.findById(1L)).thenReturn(Optional.of(entity));
		var result = service.findById(1L);
		assertNotNull(result);
		assertNotNull(result.getIdPerson());
		assertNotNull(result.getLinks());
		assertTrue(result.toString().contains("links: [</api/person/v1/1>;rel=\"self\"]"));
		assertEquals("First Name Test1", result.getFirstName());
		assertEquals("Last Name Test1", result.getLastName());
		assertEquals("Address Test1", result.getAddress());
		assertEquals("Female", result.getGender());
	}

	@Test
	void testCreate() {
		Person entity = input.mockEntity(1);;
		Person persisted = entity;
		PersonVO vo = input.mockVO(1);
		when(repository.save(entity)).thenReturn(persisted);
		var result = service.create(vo);
		assertNotNull(result);
		assertNotNull(result.getIdPerson());
		assertNotNull(result.getLinks());
		assertTrue(result.toString().contains("links: [</api/person/v1/1>;rel=\"self\"]"));
		assertEquals("First Name Test1", result.getFirstName());
		assertEquals("Last Name Test1", result.getLastName());
		assertEquals("Address Test1", result.getAddress());
		assertEquals("Female", result.getGender());
	}
	
	
	@Test
	void testCreateWithNullPerson() {
		Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> {service.create(null);});
		String expectedMessage = "Not allowed to persist a null object";
		assertTrue(exception.getMessage().contains(expectedMessage));
	}

	@Test
	void testUpdate() {
		Person entity = input.mockEntity(1);;
		Person persisted = entity;
		PersonVO vo = input.mockVO(1);
		when(repository.findById(1L)).thenReturn(Optional.of(entity));
		when(repository.save(entity)).thenReturn(persisted);
		var result = service.update(vo);
		assertNotNull(result);
		assertNotNull(result.getIdPerson());
		assertNotNull(result.getLinks());
		assertTrue(result.toString().contains("links: [</api/person/v1/1>;rel=\"self\"]"));
		assertEquals("First Name Test1", result.getFirstName());
		assertEquals("Last Name Test1", result.getLastName());
		assertEquals("Address Test1", result.getAddress());
		assertEquals("Female", result.getGender());
	}
	
	@Test
	void testUpdateWithNullPerson() {
		Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> {service.update(null);});
		String expectedMessage = "Not allowed to persist a null object";
		assertTrue(exception.getMessage().contains(expectedMessage));
	}

	@Test
	void testDelete() {
		Person entity = input.mockEntity(1);
		when(repository.findById(1L)).thenReturn(Optional.of(entity));
		service.delete(1L);
	}

	@Test
	void testFindAll() {
		List<Person> list = input.mockEntityList();
		when(repository.findAll()).thenReturn(list);
		var people = service.findAll();
		assertNotNull(people);
		assertEquals(14, people.size());
		
		var personOne = people.get(1);
		assertNotNull(personOne);
		assertNotNull(personOne.getIdPerson());
		assertNotNull(personOne.getLinks());
		assertTrue(personOne.toString().contains("links: [</api/person/v1/1>;rel=\"self\"]"));
		assertEquals("First Name Test1", personOne.getFirstName());
		assertEquals("Last Name Test1", personOne.getLastName());
		assertEquals("Address Test1", personOne.getAddress());
		assertEquals("Female", personOne.getGender());
		
		var personFour = people.get(4);
		assertNotNull(personFour);
		assertNotNull(personFour.getIdPerson());
		assertNotNull(personFour.getLinks());
		assertTrue(personFour.toString().contains("links: [</api/person/v1/4>;rel=\"self\"]"));
		assertEquals("First Name Test4", personFour.getFirstName());
		assertEquals("Last Name Test4", personFour.getLastName());
		assertEquals("Address Test4", personFour.getAddress());
		assertEquals("Male", personFour.getGender());
		
		var personSeven = people.get(7);
		assertNotNull(personSeven);
		assertNotNull(personSeven.getIdPerson());
		assertNotNull(personSeven.getLinks());
		assertTrue(personSeven.toString().contains("links: [</api/person/v1/7>;rel=\"self\"]"));
		assertEquals("First Name Test7", personSeven.getFirstName());
		assertEquals("Last Name Test7", personSeven.getLastName());
		assertEquals("Address Test7", personSeven.getAddress());
		assertEquals("Female", personSeven.getGender());
	}
}
