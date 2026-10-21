package com.library.repository;

import com.library.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    Optional<Loan> findByLoanCode(String loanCode);
    List<Loan> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Loan> findByStatus(String status);
    List<Loan> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user.id = :userId AND l.status = 'BORROWING'")
    long countActiveLoansByUserId(Long userId);
}
