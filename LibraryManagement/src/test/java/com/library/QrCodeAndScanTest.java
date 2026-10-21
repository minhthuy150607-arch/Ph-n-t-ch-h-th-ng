package com.library;

import com.library.dto.BookCopyScanDto;
import com.library.model.*;
import com.library.repository.*;
import com.library.service.LoanService;
import com.library.service.QrCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QrCodeAndScanTest {

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanDetailRepository loanDetailRepository;

    private User testReader;
    private Book testBook;
    private BookCopy testCopy;

    @BeforeEach
    void setUp() {
        // Tạo bạn đọc mẫu nếu chưa có
        testReader = userRepository.findByUsername("test_reader_qr").orElseGet(() -> {
            return userRepository.save(User.builder()
                    .username("test_reader_qr")
                    .password("$2a$10$abcdefghijklmnopqrstuvwxyz1234567890")
                    .fullName("Độc giả Test QR")
                    .email("reader_qr@test.com")
                    .readerCode("BD-TEST-QR")
                    .role("READER")
                    .status("ACTIVE")
                    .build());
        });

        // Tạo sách mẫu
        String bookCode = "BK-QR-" + System.currentTimeMillis() % 10000;
        testBook = bookRepository.save(Book.builder()
                .bookCode(bookCode)
                .title("Lập Trình Web Hiện Đại")
                .author("Nguyễn Văn A")
                .category("Công nghệ thông tin")
                .status("ACTIVE")
                .build());

        // Tạo bản sách mẫu với barcode duy nhất
        String barcode = "BC-QR-" + System.currentTimeMillis() % 10000;
        testCopy = bookCopyRepository.save(BookCopy.builder()
                .book(testBook)
                .barcode(barcode)
                .status("AVAILABLE")
                .condition("GOOD")
                .build());
    }

    @Test
    @DisplayName("Kiểm tra QrCodeService tạo ảnh PNG và Base64 hợp lệ")
    void testQrCodeGeneration() {
        byte[] qrBytes = qrCodeService.generateQrCodeImage(testCopy.getBarcode(), 250, 250);
        assertNotNull(qrBytes);
        assertTrue(qrBytes.length > 0);

        String base64 = qrCodeService.generateQrCodeBase64(testCopy.getBarcode(), 250, 250);
        assertNotNull(base64);
        assertTrue(base64.startsWith("data:image/png;base64,"));
    }

    @Test
    @DisplayName("Kiểm tra tra cứu thông tin bản sách qua Barcode")
    void testGetBookCopyScanInfo() {
        BookCopyScanDto info = loanService.getBookCopyScanInfo(testCopy.getBarcode());
        assertNotNull(info);
        assertEquals(testCopy.getBarcode(), info.getBarcode());
        assertEquals("AVAILABLE", info.getStatus());
        assertEquals(testBook.getTitle(), info.getTitle());
        assertEquals(testBook.getAuthor(), info.getAuthor());
        assertNotNull(info.getQrCodeBase64());
    }

    @Test
    @Transactional
    @DisplayName("Kiểm tra mượn và trả sách qua mã Barcode / QR Code an toàn")
    void testBorrowAndReturnByBarcode() {
        // 1. Mượn sách
        Loan loan = loanService.createLoan(testReader.getId(), null, List.of(testCopy.getBarcode()), "Test mượn QR");
        assertNotNull(loan);
        assertEquals("BORROWING", loan.getStatus());

        // Kiểm tra bản sách đã chuyển sang BORROWED
        BookCopy borrowedCopy = bookCopyRepository.findByBarcode(testCopy.getBarcode()).orElseThrow();
        assertEquals("BORROWED", borrowedCopy.getStatus());

        // 2. Tra cứu bản sách khi đang mượn -> phải có thông tin bạn đọc và phiếu
        BookCopyScanDto borrowedInfo = loanService.getBookCopyScanInfo(testCopy.getBarcode());
        assertEquals("BORROWED", borrowedInfo.getStatus());
        assertEquals(testReader.getFullName(), borrowedInfo.getReaderName());
        assertEquals(loan.getLoanCode(), borrowedInfo.getActiveLoanCode());

        // 3. Trả sách bằng Barcode
        LoanDetail returnedDetail = loanService.returnBookByBarcode(testCopy.getBarcode(), "GOOD", null);
        assertNotNull(returnedDetail);
        assertEquals("RETURNED", returnedDetail.getStatus());

        // Kiểm tra bản sách đã quay lại AVAILABLE
        BookCopy returnedCopy = bookCopyRepository.findByBarcode(testCopy.getBarcode()).orElseThrow();
        assertEquals("AVAILABLE", returnedCopy.getStatus());
    }

    @Test
    @Transactional
    @DisplayName("Chống mượn trùng: không cho phép mượn bản sách đã ở trạng thái BORROWED")
    void testPreventDoubleBorrowing() {
        // Mượn lần 1
        loanService.createLoan(testReader.getId(), null, List.of(testCopy.getBarcode()), "Mượn lần 1");

        // Cố tình mượn lần 2 cùng bản sách đó
        assertThrows(IllegalStateException.class, () -> {
            loanService.createLoan(testReader.getId(), null, List.of(testCopy.getBarcode()), "Mượn trùng lần 2");
        });
    }
}
