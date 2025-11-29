package com.university.library.controller;

import com.university.library.dto.BookRequest;
import com.university.library.entity.Book;
import com.university.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> list(@RequestParam(value = "q", required = false) String keyword) {
        return bookService.search(keyword);
    }

    @GetMapping("/{id}")
    public Book get(@PathVariable Long id) {
        return bookService.getById(id);
    }

    @PostMapping
    public Book create(@Valid @RequestBody BookRequest request) {
        return bookService.create(request);
    }

    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return bookService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        bookService.delete(id);
    }
}
