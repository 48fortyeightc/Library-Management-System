package com.university.library.service;

import com.university.library.dto.LoanRequest;
import com.university.library.entity.Loan;

import java.util.List;

public interface LoanService {
    List<Loan> listAll();

    List<Loan> listByUser(Long userId);

    Loan createLoan(LoanRequest request);

    Loan returnBook(Long loanId);
}
