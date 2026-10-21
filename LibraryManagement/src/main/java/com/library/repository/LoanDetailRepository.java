package com.library.repository;

import com.library.model.LoanDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanDetailRepository extends JpaRepository<LoanDetail, Long> {
    List<LoanDetail> findByLoanId(Long loanId);
    List<LoanDetail> findByStatus(String status);

    @Query("SELECT ld FROM LoanDetail ld WHERE ld.status = 'BORROWING' AND " +
           "( (ld.currentDueDate IS NOT NULL AND ld.currentDueDate < :today) OR (ld.currentDueDate IS NULL AND ld.loan.dueDate < :today) )")
    List<LoanDetail> findOverdueDetails(@Param("today") LocalDate today);

    @Query("SELECT ld FROM LoanDetail ld WHERE ld.loan.user.id = :userId ORDER BY ld.createdAt DESC")
    List<LoanDetail> findByUserId(@Param("userId") Long userId);

    @Query("SELECT ld.bookCopy.book.title, COUNT(ld) FROM LoanDetail ld GROUP BY ld.bookCopy.book.title ORDER BY COUNT(ld) DESC")
    List<Object[]> findTopBorrowedBooks();

    @Query("SELECT ld FROM LoanDetail ld WHERE ld.bookCopy.barcode = :barcode AND ld.status = 'BORROWING'")
    java.util.Optional<LoanDetail> findActiveBorrowingByBarcode(@Param("barcode") String barcode);

    @Query("SELECT ld FROM LoanDetail ld WHERE ld.bookCopy.id = :copyId AND ld.status = 'BORROWING'")
    java.util.Optional<LoanDetail> findActiveBorrowingByCopyId(@Param("copyId") Long copyId);
}
