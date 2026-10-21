package com.library.service;

import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }
        return bookRepository.searchBooks(keyword.trim());
    }

    public List<Book> getBooksByCategory(String category) {
        return bookRepository.findByCategoryIgnoreCase(category);
    }

    public List<String> getAllCategories() {
        return bookRepository.findAllCategories();
    }

    @Transactional
    public Book saveBook(Book book) {
        if (book.getBookCode() == null || book.getBookCode().trim().isEmpty()) {
            book.setBookCode("BK-" + System.currentTimeMillis() % 100000);
        }
        return bookRepository.save(book);
    }

    @Transactional
    public Book saveBookWithCover(Book book, org.springframework.web.multipart.MultipartFile coverFile) {
        if (coverFile != null && !coverFile.isEmpty()) {
            try {
                java.nio.file.Path uploadDir = java.nio.file.Paths.get("./uploads/books");
                if (!java.nio.file.Files.exists(uploadDir)) {
                    java.nio.file.Files.createDirectories(uploadDir);
                }
                String filename = System.currentTimeMillis() + "_" + coverFile.getOriginalFilename().replaceAll("\\s+", "_");
                java.nio.file.Path targetPath = uploadDir.resolve(filename);
                java.nio.file.Files.copy(coverFile.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                book.setCoverImage("/uploads/books/" + filename);
            } catch (Exception e) {
                // Nếu lỗi upload thì giữ nguyên coverImage cũ
            }
        }
        return saveBook(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    @Transactional
    public BookCopy addCopy(Long bookId, String barcode, String condition, String notes) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + bookId));

        if (barcode == null || barcode.trim().isEmpty()) {
            barcode = "BC-" + book.getBookCode() + "-" + (book.getCopies().size() + 1);
        }

        BookCopy copy = BookCopy.builder()
                .book(book)
                .barcode(barcode.trim())
                .condition(condition != null ? condition : "GOOD")
                .status("AVAILABLE")
                .notes(notes)
                .build();

        return bookCopyRepository.save(copy);
    }

    @Transactional
    public void deleteCopy(Long copyId) {
        bookCopyRepository.deleteById(copyId);
    }

    @Transactional
    public void updateCopyStatus(Long copyId, String status, String condition, String notes) {
        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản sao với ID: " + copyId));
        if (status != null) copy.setStatus(status);
        if (condition != null) copy.setCondition(condition);
        if (notes != null) copy.setNotes(notes);
        bookCopyRepository.save(copy);
    }
}
