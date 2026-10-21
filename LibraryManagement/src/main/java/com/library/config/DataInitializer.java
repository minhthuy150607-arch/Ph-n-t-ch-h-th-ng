package com.library.config;

import com.library.model.*;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final LoanRepository loanRepository;
    private final LoanDetailRepository loanDetailRepository;
    private final ReservationRepository reservationRepository;
    private final LibraryRuleRepository ruleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initRules();
        initUsers();
        initBooks();
        initLoansAndReservations();
        log.info(">>> Khởi tạo dữ liệu mẫu hoàn tất! Sẵn sàng sử dụng.");
    }

    private void initRules() {
        if (ruleRepository.count() > 0) return;

        List<LibraryRule> rules = List.of(
                LibraryRule.builder().ruleKey("MAX_BORROW_COPIES").ruleValue("5").description("Số bản sách tối đa được mượn cùng lúc").build(),
                LibraryRule.builder().ruleKey("LOAN_DURATION_DAYS").ruleValue("14").description("Số ngày mượn sách tiêu chuẩn").build(),
                LibraryRule.builder().ruleKey("RENEWAL_DURATION_DAYS").ruleValue("7").description("Số ngày được gia hạn mỗi lần").build(),
                LibraryRule.builder().ruleKey("MAX_RENEWALS").ruleValue("2").description("Số lần gia hạn tối đa").build(),
                LibraryRule.builder().ruleKey("RESERVATION_EXPIRY_DAYS").ruleValue("3").description("Số ngày giữ sách đặt trước").build(),
                LibraryRule.builder().ruleKey("OVERDUE_FINE_PER_DAY").ruleValue("2000").description("Mức phạt quá hạn mỗi ngày (VNĐ)").build()
        );
        ruleRepository.saveAll(rules);
    }

    private void initUsers() {
        if (userRepository.count() > 0) return;

        // Admin
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Quản Trị Viên Hệ Thống")
                .email("admin@library.com")
                .phone("0901234567")
                .role("ADMIN")
                .status("ACTIVE")
                .build();

        // Reader 1
        User reader1 = User.builder()
                .username("reader")
                .password(passwordEncoder.encode("reader123"))
                .fullName("Nguyễn Văn An")
                .email("an.nguyen@example.com")
                .phone("0912345678")
                .readerCode("BD001")
                .role("READER")
                .status("ACTIVE")
                .build();

        // Reader 2
        User reader2 = User.builder()
                .username("reader2")
                .password(passwordEncoder.encode("reader123"))
                .fullName("Trần Thị Bích")
                .email("bich.tran@example.com")
                .phone("0987654321")
                .readerCode("BD002")
                .role("READER")
                .status("ACTIVE")
                .build();

        userRepository.saveAll(List.of(admin, reader1, reader2));
    }

    private void initBooks() {
        List<BookData> bookList = List.of(
                new BookData("BK-001", "Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin", "Công nghệ thông tin", "Prentice Hall", 2008,
                        "Cuốn sách kinh điển giúp lập trình viên viết code sạch, dễ bảo trì và mở rộng.",
                        "/images/cover-clean-code.svg"),
                new BookData("BK-002", "Design Patterns: Elements of Reusable Object-Oriented Software", "Erich Gamma, Richard Helm", "Công nghệ thông tin", "Addison-Wesley", 1994,
                        "23 mẫu thiết kế phần mềm kinh điển từ nhóm Gang of Four (GoF).",
                        "/images/cover-design-patterns.svg"),
                new BookData("BK-003", "Spring Boot in Action", "Craig Walls", "Công nghệ thông tin", "Manning Publications", 2016,
                        "Hướng dẫn toàn diện về phát triển ứng dụng Java với Spring Boot hiện đại.",
                        "/images/cover-spring-boot.svg"),
                new BookData("BK-004", "Đắc Nhân Tâm (How to Win Friends and Influence People)", "Dale Carnegie", "Kỹ năng sống", "NXB Tổng Hợp TP.HCM", 2021,
                        "Cuốn sách nổi tiếng nhất về nghệ thuật giao tiếp và đối nhân xử thế.",
                        "/images/cover-dac-nhan-tam.svg"),
                new BookData("BK-005", "Nhà Giả Kim (The Alchemist)", "Paulo Coelho", "Văn học", "NXB Hội Nhà Văn", 2020,
                        "Hành trình theo đuổi vận mệnh và ước mơ của cậu bé chăn cừu Santiago.",
                        "/images/cover-nha-gia-kim.svg"),
                new BookData("BK-006", "Tuổi Trẻ Đáng Giá Bao Nhiêu", "Rosie Nguyễn", "Kỹ năng sống", "NXB Nhã Nam", 2018,
                        "Lời nhắn nhủ nhiệt huyết dành cho các bạn trẻ trên hành trình tự học và trưởng thành.",
                        "/images/cover-tuoi-tre.svg"),
                new BookData("BK-007", "Cha Giàu Cha Nghèo (Rich Dad Poor Dad)", "Robert Kiyosaki", "Kinh tế", "NXB Trẻ", 2019,
                        "Tư duy tài chính cá nhân đột phá và cách biến đồng tiền làm việc cho bạn.",
                        "/images/cover-cha-giau.svg"),
                new BookData("BK-008", "Kinh Tế Học Hài Hước (Freakonomics)", "Steven D. Levitt, Stephen J. Dubner", "Kinh tế", "NXB Thế Giới", 2017,
                        "Khám phá thế giới qua lăng kính kinh tế học độc đáo và bất ngờ.",
                        "/images/cover-freakonomics.svg"),
                new BookData("BK-009", "Sapiens: Lược Sử Loài Người", "Yuval Noah Harari", "Khoa học - Lịch sử", "NXB Tri Thức", 2014,
                        "Cuộc cách mạng nhận thức, nông nghiệp và khoa học đã định hình loài người như thế nào.",
                        "/images/cover-sapiens.svg"),
                new BookData("BK-010", "Dữ Liệu Lớn (Big Data - Cuộc Cách Mạng Về Dữ Liệu)", "Viktor Mayer-Schönberger", "Công nghệ thông tin", "NXB Trẻ", 2013,
                        "Cách mạng dữ liệu lớn sẽ làm thay đổi hoàn toàn cách chúng ta sống, làm việc và tư duy.",
                        "/images/cover-big-data.svg")
        );

        if (bookRepository.count() == 0) {
            for (BookData bd : bookList) {
                Book book = Book.builder()
                        .bookCode(bd.code)
                        .title(bd.title)
                        .author(bd.author)
                        .category(bd.category)
                        .publisher(bd.publisher)
                        .publishYear(bd.year)
                        .description(bd.description)
                        .coverImage(bd.image)
                        .status("ACTIVE")
                        .build();
                bookRepository.save(book);

                // Tạo 3 bản sách cho mỗi đầu sách
                for (int i = 1; i <= 3; i++) {
                    BookCopy copy = BookCopy.builder()
                            .book(book)
                            .barcode("BC-" + bd.code + "-0" + i)
                            .location("Kệ " + (char)('A' + (i % 3)) + (i) + " - Tầng " + (1 + i % 2))
                            .status("AVAILABLE")
                            .condition(i == 3 ? "FAIR" : "GOOD")
                            .notes("Bản sách số " + i)
                            .build();
                    bookCopyRepository.save(copy);
                }
            }
        }

        // Đảm bảo các cuốn sách và bản sách minh họa theo đúng slide chuẩn (BS0001, BS0002, BS0003, BS0004)
        initIllustrativeCopies(bookList);
    }

    private void initIllustrativeCopies(List<BookData> bookList) {
        // Đảm bảo sách Sapiens và Dữ Liệu Lớn có mặt trong DB
        for (BookData bd : bookList) {
            if (bookRepository.findByBookCode(bd.code).isEmpty()) {
                Book book = Book.builder()
                        .bookCode(bd.code)
                        .title(bd.title)
                        .author(bd.author)
                        .category(bd.category)
                        .publisher(bd.publisher)
                        .publishYear(bd.year)
                        .description(bd.description)
                        .coverImage(bd.image)
                        .status("ACTIVE")
                        .build();
                bookRepository.save(book);
            }
        }

        // 1. BS0001: Đắc Nhân Tâm (Dale Carnegie) - Kệ A1 - Tầng 1 (AVAILABLE)
        createOrUpdateCopy("BS0001", "BK-004", "Kệ A1 - Tầng 1", "AVAILABLE", "GOOD");

        // 2. BS0002: Sapiens (Yuval Noah Harari) - Kệ A1 - Tầng 2 (AVAILABLE)
        createOrUpdateCopy("BS0002", "BK-009", "Kệ A1 - Tầng 2", "AVAILABLE", "GOOD");

        // 3. BS0003: Nhà Giả Kim (Paulo Coelho) - Kệ B1 - Tầng 1 (BORROWED để thủ thư quét trả sách)
        createOrUpdateCopy("BS0003", "BK-005", "Kệ B1 - Tầng 1", "BORROWED", "GOOD");

        // 4. BS0004: Dữ Liệu Lớn (Viktor Mayer-Schönberger) - Kệ B1 - Tầng 2 (AVAILABLE)
        createOrUpdateCopy("BS0004", "BK-010", "Kệ B1 - Tầng 2", "AVAILABLE", "GOOD");

        // 5. BS0005: Clean Code (Robert C. Martin) - Kệ A2 - Tầng 1 (AVAILABLE)
        createOrUpdateCopy("BS0005", "BK-001", "Kệ A2 - Tầng 1", "AVAILABLE", "GOOD");

        // 6. BS0006: Design Patterns (GoF) - Kệ A2 - Tầng 2 (AVAILABLE)
        createOrUpdateCopy("BS0006", "BK-002", "Kệ A2 - Tầng 2", "AVAILABLE", "GOOD");

        // 7. BS0007: Spring Boot in Action (Craig Walls) - Kệ B2 - Tầng 1 (AVAILABLE)
        createOrUpdateCopy("BS0007", "BK-003", "Kệ B2 - Tầng 1", "AVAILABLE", "GOOD");

        // 8. BS0008: Tuổi Trẻ Đáng Giá Bao Nhiêu (Rosie Nguyễn) - Kệ B2 - Tầng 2 (AVAILABLE)
        createOrUpdateCopy("BS0008", "BK-006", "Kệ B2 - Tầng 2", "AVAILABLE", "GOOD");

        // 9. BS0009: Cha Giàu Cha Nghèo (Robert Kiyosaki) - Kệ C1 - Tầng 1 (AVAILABLE)
        createOrUpdateCopy("BS0009", "BK-007", "Kệ C1 - Tầng 1", "AVAILABLE", "GOOD");

        // 10. BS0010: Kinh Tế Học Hài Hước (Steven D. Levitt) - Kệ C1 - Tầng 2 (AVAILABLE)
        createOrUpdateCopy("BS0010", "BK-008", "Kệ C1 - Tầng 2", "AVAILABLE", "GOOD");

        // Gán vị trí mặc định cho các bản sách cũ nếu đang null
        for (BookCopy c : bookCopyRepository.findAll()) {
            if (c.getLocation() == null || c.getLocation().trim().isEmpty()) {
                c.setLocation("Kệ A1 - Tầng 1");
                bookCopyRepository.save(c);
            }
        }
    }

    private void createOrUpdateCopy(String barcode, String bookCode, String location, String status, String condition) {
        BookCopy copy = bookCopyRepository.findByBarcodeIgnoreCase(barcode).orElse(null);
        Book book = bookRepository.findByBookCode(bookCode).orElse(null);
        if (book == null) return;

        if (copy == null) {
            copy = BookCopy.builder()
                    .book(book)
                    .barcode(barcode)
                    .location(location)
                    .status(status)
                    .condition(condition)
                    .notes("Bản sách mã chuẩn QR " + barcode)
                    .build();
            bookCopyRepository.save(copy);
        } else {
            copy.setBook(book);
            copy.setLocation(location);
            copy.setStatus(status);
            bookCopyRepository.save(copy);
        }
    }

    private void initLoansAndReservations() {
        // Đảm bảo BS0003 có một phiếu mượn đang active để test quét trả sách
        BookCopy bs0003 = bookCopyRepository.findByBarcodeIgnoreCase("BS0003").orElse(null);
        if (bs0003 != null && "BORROWED".equalsIgnoreCase(bs0003.getStatus())) {
            if (loanDetailRepository.findActiveBorrowingByBarcode("BS0003").isEmpty()) {
                User reader = userRepository.findByUsername("reader").orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));
                User admin = userRepository.findByUsername("admin").orElse(null);
                if (reader != null) {
                    Loan testLoan = Loan.builder()
                            .loanCode("LN-BS0003")
                            .user(reader)
                            .librarian(admin)
                            .loanDate(LocalDate.now().minusDays(3))
                            .dueDate(LocalDate.now().plusDays(11))
                            .status("BORROWING")
                            .notes("Phiếu mượn mẫu quét QR (Nhà Giả Kim - BS0003)")
                            .build();
                    loanRepository.save(testLoan);

                    LoanDetail detail = LoanDetail.builder()
                            .loan(testLoan)
                            .bookCopy(bs0003)
                            .status("BORROWING")
                            .currentDueDate(LocalDate.now().plusDays(11))
                            .renewalCount(0)
                            .build();
                    loanDetailRepository.save(detail);
                }
            }
        }

        if (loanRepository.count() > 1) return;

        User reader = userRepository.findByUsername("reader").orElse(null);
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (reader == null) return;

        List<BookCopy> copies = bookCopyRepository.findAll();
        if (copies.size() < 4) return;

        // 1. Phiếu mượn ĐANG MƯỢN (Active)
        BookCopy copy1 = copies.get(0);
        copy1.setStatus("BORROWED");
        bookCopyRepository.save(copy1);

        Loan activeLoan = Loan.builder()
                .loanCode("LN-20250101")
                .user(reader)
                .librarian(admin)
                .loanDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(9))
                .status("BORROWING")
                .notes("Mượn đọc nghiên cứu môn học")
                .build();
        loanRepository.save(activeLoan);

        LoanDetail detail1 = LoanDetail.builder()
                .loan(activeLoan)
                .bookCopy(copy1)
                .status("BORROWING")
                .currentDueDate(LocalDate.now().plusDays(9))
                .renewalCount(0)
                .build();
        loanDetailRepository.save(detail1);

        // 2. Phiếu mượn QUÁ HẠN (Overdue) để test tính năng Quá hạn
        BookCopy copy2 = copies.get(1);
        copy2.setStatus("BORROWED");
        bookCopyRepository.save(copy2);

        Loan overdueLoan = Loan.builder()
                .loanCode("LN-20241215")
                .user(reader)
                .librarian(admin)
                .loanDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().minusDays(6))
                .status("BORROWING")
                .notes("Chưa trả đúng hạn")
                .build();
        loanRepository.save(overdueLoan);

        LoanDetail detail2 = LoanDetail.builder()
                .loan(overdueLoan)
                .bookCopy(copy2)
                .status("BORROWING")
                .currentDueDate(LocalDate.now().minusDays(6))
                .renewalCount(0)
                .build();
        loanDetailRepository.save(detail2);

        // 3. Một phiếu mượn ĐÃ TRẢ (Returned)
        BookCopy copy3 = copies.get(2);
        Loan returnedLoan = Loan.builder()
                .loanCode("LN-20241101")
                .user(reader)
                .librarian(admin)
                .loanDate(LocalDate.now().minusDays(30))
                .dueDate(LocalDate.now().minusDays(16))
                .status("RETURNED")
                .notes("Đã trả đầy đủ")
                .build();
        loanRepository.save(returnedLoan);

        LoanDetail detail3 = LoanDetail.builder()
                .loan(returnedLoan)
                .bookCopy(copy3)
                .status("RETURNED")
                .returnDate(LocalDate.now().minusDays(18))
                .conditionOnReturn("GOOD")
                .currentDueDate(LocalDate.now().minusDays(16))
                .renewalCount(0)
                .build();
        loanDetailRepository.save(detail3);

        // 4. Đặt trước mẫu
        Book book = copies.get(3).getBook();
        Reservation reservation = Reservation.builder()
                .user(reader)
                .book(book)
                .status("PENDING")
                .expiryDate(LocalDateTime.now().plusDays(3))
                .notes("Đặt trước để làm báo cáo")
                .build();
        reservationRepository.save(reservation);
    }

    private record BookData(String code, String title, String author, String category, String publisher, Integer year, String description, String image) {}
}
