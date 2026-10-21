package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByBookCode(String bookCode);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(b.category) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(b.bookCode) LIKE LOWER(CONCAT('%', :kw, '%'))")
    List<Book> searchBooks(@Param("kw") String keyword);

    List<Book> findByCategoryIgnoreCase(String category);

    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL ORDER BY b.category")
    List<String> findAllCategories();

    long countByStatus(String status);
}
