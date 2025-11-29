package com.university.library.service.impl;

import com.university.library.dto.BookRequest;
import com.university.library.entity.Book;
import com.university.library.exception.BadRequestException;
import com.university.library.exception.ResourceNotFoundException;
import com.university.library.repository.BookRepository;
import com.university.library.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public List<Book> listAll() {
        return bookRepository.findAll();
    }

    @Override
    public List<Book> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return listAll();
        }
        return bookRepository.search(keyword.trim());
    }

    @Override
    public Book getById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到图书: " + id));
    }

    @Override
    public Book create(BookRequest request) {
        Book book = new Book();
        apply(request, book);
        return bookRepository.save(book);
    }

    @Override
    public Book update(Long id, BookRequest request) {
        Book book = getById(id);
        apply(request, book);
        return bookRepository.save(book);
    }

    @Override
    public void delete(Long id) {
        Book book = getById(id);
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new BadRequestException("有借阅记录的图书无法删除");
        }
        bookRepository.delete(book);
    }

    private void apply(BookRequest request, Book book) {
        int total = request.getTotalCopies();
        if (total < 1) {
            throw new BadRequestException("总册数必须大于等于 1");
        }

        int borrowed = book.getTotalCopies() - book.getAvailableCopies();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setDescription(request.getDescription());
        book.setTotalCopies(total);
        int newAvailable = total - borrowed;
        if (newAvailable < 0) {
            throw new BadRequestException("总册数不能少于已借出的数量");
        }
        book.setAvailableCopies(newAvailable);
    }
}
