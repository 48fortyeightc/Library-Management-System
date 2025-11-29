package com.university.library.controller;

import com.university.library.dto.LoanRequest;
import com.university.library.entity.Loan;
import com.university.library.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public List<Loan> list(@RequestParam(value = "userId", required = false) Long userId) {
        if (userId != null) {
            return loanService.listByUser(userId);
        }
        return loanService.listAll();
    }

    @PostMapping
    public Loan create(@Valid @RequestBody LoanRequest request) {
        return loanService.createLoan(request);
    }

    @PostMapping("/{id}/return")
    public Loan returnBook(@PathVariable Long id) {
        return loanService.returnBook(id);
    }
}
