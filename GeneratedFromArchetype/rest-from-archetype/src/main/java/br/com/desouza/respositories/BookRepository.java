package br.com.desouza.respositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.desouza.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {}
