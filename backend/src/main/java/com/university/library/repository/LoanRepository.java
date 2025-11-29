package com.university.library.repository;

import com.university.library.entity.Loan;
import com.university.library.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByStatus(LoanStatus status);

    List<Loan> findByUserId(Long userId);
}
