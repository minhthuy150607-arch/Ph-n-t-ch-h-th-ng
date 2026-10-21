package com.library.repository;

import com.library.model.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    Optional<BookCopy> findByBarcode(String barcode);
    List<BookCopy> findByBookId(Long bookId);
    List<BookCopy> findByStatus(String status);
    List<BookCopy> findByBookIdAndStatus(Long bookId, String status);
    long countByStatus(String status);

    @Modifying
    @Query("UPDATE BookCopy c SET c.status = :newStatus WHERE c.id = :id AND c.status = :currentStatus")
    int updateStatusAtomic(@Param("id") Long id, @Param("newStatus") String newStatus, @Param("currentStatus") String currentStatus);
}

