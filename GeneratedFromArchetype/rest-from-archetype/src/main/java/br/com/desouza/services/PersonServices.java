package br.com.desouza.services;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import br.com.desouza.controllers.PersonController;
import br.com.desouza.data.vo.v1.PersonVO;
import br.com.desouza.exceptions.RequiredObjectIsNullException;
import br.com.desouza.exceptions.ResourceNotFoundException;
import br.com.desouza.mapper.DozerMapper;
import br.com.desouza.model.Person;
import br.com.desouza.respositories.PersonRepository;
import jakarta.transaction.Transactional;

@Service
public class PersonServices {

	@Autowired
	PersonRepository repository;
	@Autowired
	PagedResourcesAssembler<PersonVO> assembler;
	private Logger logger = Logger.getLogger(PersonServices.class.getName());
	
	public PersonVO findById(Long id) {
		logger.info("Finding a Person...");
		var entity = repository.findById(id).orElseThrow(() 
				-> new ResourceNotFoundException("There are no records for this id! Id: " + id));
		PersonVO vo = DozerMapper.parseObject(entity, PersonVO.class);
		vo.add(linkTo(methodOn(PersonController.class).findById(id)).withSelfRel());
		return vo;
		
	}
	
	public PagedModel<EntityModel<PersonVO>> findAll(Pageable pageable) {
		logger.info("Finding all People...");
		var personPage = repository.findAll(pageable); 
		var personVosPage = personPage.map(p -> DozerMapper.parseObject(p, PersonVO.class));
		personVosPage.map(p -> p.add(linkTo(methodOn(PersonController.class).findById(p.getIdPerson())).withSelfRel()));
		Link link = linkTo(methodOn(PersonController.class).findAll(pageable.getPageNumber(),
																	pageable.getPageSize(), "asc")).withSelfRel();
		return assembler.toModel(personVosPage, link);
	}
	
	
	public PagedModel<EntityModel<PersonVO>> findPersonsByName(String firstName, Pageable pageable) {
		logger.info("Finding all People...");
		var personPage = repository.findPersonsByName(firstName,pageable); 
		var personVosPage = personPage.map(p -> DozerMapper.parseObject(p, PersonVO.class));
		personVosPage.map(p -> p.add(linkTo(methodOn(PersonController.class).findById(p.getIdPerson())).withSelfRel()));
		Link link = linkTo(methodOn(PersonController.class).findAll(pageable.getPageNumber(),
				pageable.getPageSize(), "asc")).withSelfRel();
		return assembler.toModel(personVosPage, link);
	}
	
	public PersonVO create(PersonVO person) {
		if(person == null) throw new RequiredObjectIsNullException();
		logger.info("Creating a Person...");
		var entity = DozerMapper.parseObject(person, Person.class);
		PersonVO vo = DozerMapper.parseObject(repository.save(entity), PersonVO.class);
		vo.add(linkTo(methodOn(PersonController.class).findById(person.getIdPerson())).withSelfRel());
		return vo;	
	}
	
	public PersonVO update(PersonVO person) {
		if(person == null) throw new RequiredObjectIsNullException();
		logger.info("Updating a Person...");
		var entity = repository.findById(person.getIdPerson()).orElseThrow(() 
							  -> new ResourceNotFoundException("There are no records for this id! Id: " + person.getIdPerson()));
		entity.setFirstName(person.getFirstName());
		entity.setLastName(person.getLastName());
		entity.setAddress(person.getAddress());
		entity.setGender(person.getGender());
		PersonVO vo = DozerMapper.parseObject(repository.save(entity), PersonVO.class);
		vo.add(linkTo(methodOn(PersonController.class).findById(person.getIdPerson())).withSelfRel());
		return vo;
	}
	
	@Transactional
	public PersonVO disablePerson(Long id) {
		logger.info("Disabling a Person...");
		repository.disablePerson(id);
		var entity = repository.findById(id).orElseThrow(() 
				-> new ResourceNotFoundException("There are no records for this id! Id: " + id));
		PersonVO vo = DozerMapper.parseObject(entity, PersonVO.class);
		vo.add(linkTo(methodOn(PersonController.class).findById(id)).withSelfRel());
		return vo;
		
	}
	
	
	public void delete(Long id) {
		logger.info("Deleting a Person...");
		var entity = repository.findById(id).orElseThrow(() 
				   -> new ResourceNotFoundException("There are no records for this id! Id: " + id));
		repository.delete(entity);
	}
}