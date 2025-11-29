package com.university.library.service.impl;

import com.university.library.dto.LoanRequest;
import com.university.library.entity.Book;
import com.university.library.entity.Loan;
import com.university.library.entity.User;
import com.university.library.exception.BadRequestException;
import com.university.library.exception.ResourceNotFoundException;
import com.university.library.model.LoanStatus;
import com.university.library.repository.BookRepository;
import com.university.library.repository.LoanRepository;
import com.university.library.repository.UserRepository;
import com.university.library.service.LoanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public LoanServiceImpl(LoanRepository loanRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public List<Loan> listAll() {
        return refreshStatuses(loanRepository.findAll());
    }

    @Override
    public List<Loan> listByUser(Long userId) {
        return refreshStatuses(loanRepository.findByUserId(userId));
    }

    @Override
    public Loan createLoan(LoanRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + request.getUserId()));
        if (!user.isActive()) {
            throw new BadRequestException("用户已停用，无法借阅");
        }
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到图书: " + request.getBookId()));
        if (book.getAvailableCopies() <= 0) {
            throw new BadRequestException("该图书暂无可借阅库存");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        LocalDate loanDate = LocalDate.now();
        LocalDate dueDate = loanDate.plusDays(request.getLoanDays() == null ? 14 : request.getLoanDays());
        Loan loan = new Loan(user, book, loanDate, dueDate, request.getNote());
        loan.setStatus(LoanStatus.BORROWED);
        loanRepository.save(loan);
        bookRepository.save(book);
        return loan;
    }

    @Override
    public Loan returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到借阅记录: " + loanId));
        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BadRequestException("该借阅已归还");
        }

        Book book = loan.getBook();
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now());
        int newAvailable = Math.min(book.getAvailableCopies() + 1, book.getTotalCopies());
        book.setAvailableCopies(newAvailable);
        loanRepository.save(loan);
        bookRepository.save(book);
        return loan;
    }

    private List<Loan> refreshStatuses(List<Loan> loans) {
        LocalDate today = LocalDate.now();
        List<Loan> updated = loans.stream()
                .peek(loan -> {
                    if (loan.getStatus() == LoanStatus.BORROWED && today.isAfter(loan.getDueDate())) {
                        loan.setStatus(LoanStatus.OVERDUE);
                    }
                }).collect(Collectors.toList());
        loanRepository.saveAll(updated);
        return updated;
    }
}
