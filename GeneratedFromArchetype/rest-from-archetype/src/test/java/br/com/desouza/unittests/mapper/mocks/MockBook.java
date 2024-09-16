package br.com.desouza.unittests.mapper.mocks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.desouza.data.vo.v1.BookVO;
import br.com.desouza.model.Book;

public class MockBook {

	public Book mockEntity() {
		return mockEntity(0);
	}
	
	public BookVO mockVO() {
		return mockVO(0);
	}
	
	public List<Book> mockEntityList() {
		List<Book> books = new ArrayList<Book>();
		for(int i=0; i<14; i++) books.add(mockEntity(i));
		return books;
	}
	
	public List<BookVO> mockVOList() {
		List<BookVO> booksVO = new ArrayList<BookVO>();
		for(int i=0; i<14; i++) booksVO.add(mockVO(i));
		return booksVO;
	}

	public Book mockEntity(Integer number) {
		Book book = new Book();
		book.setId(number.longValue());
		book.setTitle("Title Test" + number);
		book.setAuthor("Author Test" + number);
		book.setLaunchDate(new Date());
		book.setPrice(100.0 + number);
		return book;
	}
	
	public BookVO mockVO(Integer number) {
		BookVO bookVO = new BookVO();
		bookVO.setIdBook(number.longValue());
		bookVO.setTitle("Title Test" + number);
		bookVO.setAuthor("Author Test" + number);
		bookVO.setLaunchDate(new Date());
		bookVO.setPrice(100.0 + number);
		return bookVO;
	}
}
