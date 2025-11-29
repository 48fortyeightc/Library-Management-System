package com.university.library.service;

import com.university.library.dto.BookRequest;
import com.university.library.entity.Book;

import java.util.List;

public interface BookService {
    List<Book> listAll();

    List<Book> search(String keyword);

    Book getById(Long id);

    Book create(BookRequest request);

    Book update(Long id, BookRequest request);

    void delete(Long id);
}
