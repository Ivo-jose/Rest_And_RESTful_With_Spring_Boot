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

import br.com.desouza.controllers.BookController;
import br.com.desouza.data.vo.v1.BookVO;
import br.com.desouza.exceptions.RequiredObjectIsNullException;
import br.com.desouza.exceptions.ResourceNotFoundException;
import br.com.desouza.mapper.DozerMapper;
import br.com.desouza.model.Book;
import br.com.desouza.respositories.BookRepository;

@Service
public class BookServices {

	@Autowired
	BookRepository repository;
	@Autowired
	PagedResourcesAssembler<BookVO> assembler;
	private Logger logger = Logger.getLogger(BookServices.class.getName());
	
	public BookVO findById(Long id) {
		logger.info("Finding a Book...");
		var entity = repository.findById(id).orElseThrow(() 
				-> new ResourceNotFoundException("There are no records for this id! Id: " + id));
		BookVO vo = DozerMapper.parseObject(entity, BookVO.class);
		vo.add(linkTo(methodOn(BookController.class).findById(id)).withSelfRel());
		return vo;
		
	}
	
	public PagedModel<EntityModel<BookVO>> findAll(Pageable pageable) {
		logger.info("Finding all Books...");
		var bookPage = repository.findAll(pageable);
		var bookVosPage = bookPage.map(b -> DozerMapper.parseObject(b, BookVO.class));
		bookVosPage.map(b -> b.add(linkTo(methodOn(BookController.class).findById(b.getIdBook())).withSelfRel()));
		Link link = linkTo(methodOn(BookController.class).findAll(pageable.getPageNumber(),
																	pageable.getPageSize(), "asc")).withSelfRel();;
		return assembler.toModel(bookVosPage,link);
	}
	
	public BookVO create(BookVO book) {
		if(book == null) throw new RequiredObjectIsNullException();
		logger.info("Creating a Book...");
		var entity = DozerMapper.parseObject(book, Book.class);
		BookVO vo = DozerMapper.parseObject(repository.save(entity), BookVO.class);
		vo.add(linkTo(methodOn(BookController.class).findById(book.getIdBook())).withSelfRel());
		return vo;	
	}
	
	public BookVO update(BookVO book) {
		if(book == null) throw new RequiredObjectIsNullException();
		logger.info("Updating a Book...");
		var entity = repository.findById(book.getIdBook()).orElseThrow(() 
							  -> new ResourceNotFoundException("There are no records for this id! Id: " + book.getIdBook()));
		entity.setTitle(book.getTitle());
		entity.setAuthor(book.getAuthor());
		entity.setLaunchDate(book.getLaunchDate());
		entity.setPrice(book.getPrice());
		BookVO vo = DozerMapper.parseObject(repository.save(entity), BookVO.class);
		vo.add(linkTo(methodOn(BookController.class).findById(book.getIdBook())).withSelfRel());
		return vo;
	}
	
	public void delete(Long id) {
		logger.info("Deleting a Book...");
		var entity = repository.findById(id).orElseThrow(() 
				   -> new ResourceNotFoundException("There are no records for this id! Id: " + id));
		repository.delete(entity);
	}
}