package com.library.service;

import com.library.dto.BookCopyScanDto;
import com.library.model.*;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanDetailRepository loanDetailRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final LibraryRuleRepository ruleRepository;
    private final LoanHistoryRepository loanHistoryRepository;
    private final ReservationRepository reservationRepository;
    private final QrCodeService qrCodeService;

    public List<Loan> getAllLoans() {
        return loanRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Loan> getLoanById(Long id) {
        return loanRepository.findById(id);
    }

    public List<Loan> getLoansByUserId(Long userId) {
        return loanRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<LoanDetail> getLoanDetailsByUserId(Long userId) {
        return loanDetailRepository.findByUserId(userId);
    }

    public List<LoanDetail> getOverdueLoanDetails() {
        return loanDetailRepository.findOverdueDetails(LocalDate.now());
    }

    public int getRuleIntValue(String key, int defaultValue) {
        return ruleRepository.findByRuleKey(key)
                .map(r -> {
                    try {
                        return Integer.parseInt(r.getRuleValue());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                }).orElse(defaultValue);
    }

    @Transactional
    public Loan createLoan(Long userId, Long librarianId, List<String> copyBarcodes, String notes) {
        User reader = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy độc giả ID: " + userId));

        if ("LOCKED".equalsIgnoreCase(reader.getStatus())) {
            throw new IllegalStateException("Tài khoản độc giả đang bị khóa, không thể mượn sách!");
        }

        int maxCopies = getRuleIntValue("MAX_BORROW_COPIES", 5);
        int durationDays = getRuleIntValue("LOAN_DURATION_DAYS", 14);

        long currentBorrowing = loanDetailRepository.findByUserId(userId).stream()
                .filter(ld -> "BORROWING".equalsIgnoreCase(ld.getStatus()))
                .count();

        if (currentBorrowing + copyBarcodes.size() > maxCopies) {
            throw new IllegalStateException("Độc giả vượt quá số lượng sách được phép mượn tối đa (" + maxCopies + " cuốn)!");
        }

        User librarian = librarianId != null ? userRepository.findById(librarianId).orElse(null) : null;

        LocalDate today = LocalDate.now();
        LocalDate due = today.plusDays(durationDays);

        Loan loan = Loan.builder()
                .loanCode("LN-" + System.currentTimeMillis() % 1000000)
                .user(reader)
                .librarian(librarian)
                .loanDate(today)
                .dueDate(due)
                .status("BORROWING")
                .notes(notes)
                .build();

        List<LoanDetail> details = new ArrayList<>();
        for (String barcode : copyBarcodes) {
            if (barcode == null || barcode.trim().isEmpty()) continue;
            BookCopy copy = bookCopyRepository.findByBarcode(barcode.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản sách với mã: " + barcode));

            if (!"AVAILABLE".equalsIgnoreCase(copy.getStatus())) {
                throw new IllegalStateException("Bản sách " + barcode + " (" + copy.getBook().getTitle() + ") hiện không sẵn sàng để mượn!");
            }

            // Cập nhật trạng thái an toàn chống mượn trùng đồng thời
            int rowsUpdated = bookCopyRepository.updateStatusAtomic(copy.getId(), "BORROWED", "AVAILABLE");
            if (rowsUpdated == 0) {
                throw new IllegalStateException("Bản sách " + barcode + " vừa được người khác mượn hoặc không còn sẵn sàng!");
            }
            copy.setStatus("BORROWED");

            LoanDetail detail = LoanDetail.builder()
                    .loan(loan)
                    .bookCopy(copy)
                    .status("BORROWING")
                    .currentDueDate(due)
                    .renewalCount(0)
                    .build();
            details.add(detail);
        }

        if (details.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 bản sách hợp lệ để mượn!");
        }

        loan.setDetails(details);
        Loan savedLoan = loanRepository.save(loan);

        for (LoanDetail d : savedLoan.getDetails()) {
            loanHistoryRepository.save(LoanHistory.builder()
                    .loan(savedLoan)
                    .loanDetail(d)
                    .action("BORROW")
                    .performedBy(librarian)
                    .details("Mượn bản sách " + d.getBookCopy().getBarcode())
                    .build());
        }

        return savedLoan;
    }

    @Transactional
    public void returnBook(Long detailId, String conditionOnReturn) {
        LoanDetail detail = loanDetailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết phiếu mượn ID: " + detailId));

        if ("RETURNED".equalsIgnoreCase(detail.getStatus())) {
            return;
        }

        detail.setStatus("RETURNED");
        detail.setReturnDate(LocalDate.now());
        if (conditionOnReturn != null) {
            detail.setConditionOnReturn(conditionOnReturn);
        }

        BookCopy copy = detail.getBookCopy();
        copy.setStatus("AVAILABLE");
        if (conditionOnReturn != null) {
            copy.setCondition(conditionOnReturn);
        }
        bookCopyRepository.save(copy);
        loanDetailRepository.save(detail);

        // Kiểm tra xem tất cả các cuốn trong phiếu đã trả chưa
        Loan loan = detail.getLoan();
        boolean allReturned = loan.getDetails().stream().allMatch(d -> "RETURNED".equalsIgnoreCase(d.getStatus()));
        if (allReturned) {
            loan.setStatus("RETURNED");
            loanRepository.save(loan);
        }

        loanHistoryRepository.save(LoanHistory.builder()
                .loan(loan)
                .loanDetail(detail)
                .action("RETURN")
                .details("Trả bản sách " + copy.getBarcode())
                .build());
    }

    @Transactional
    public void renewBook(Long detailId) {
        LoanDetail detail = loanDetailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết mượn ID: " + detailId));

        int maxRenewals = getRuleIntValue("MAX_RENEWALS", 2);
        int renewalDays = getRuleIntValue("RENEWAL_DURATION_DAYS", 7);

        if (detail.getRenewalCount() >= maxRenewals) {
            throw new IllegalStateException("Đã đạt số lần gia hạn tối đa (" + maxRenewals + " lần)!");
        }

        LocalDate currentDue = detail.getCurrentDueDate() != null ? detail.getCurrentDueDate() : detail.getLoan().getDueDate();
        LocalDate newDue = currentDue.plusDays(renewalDays);

        detail.setRenewalCount(detail.getRenewalCount() + 1);
        detail.setCurrentDueDate(newDue);
        loanDetailRepository.save(detail);

        loanHistoryRepository.save(LoanHistory.builder()
                .loan(detail.getLoan())
                .loanDetail(detail)
                .action("RENEW")
                .oldDueDate(currentDue)
                .newDueDate(newDue)
                .details("Gia hạn thêm " + renewalDays + " ngày")
                .build());
    }

    @Transactional
    public Loan borrowBookOnline(Long userId, Long bookId, String notes) {
        User reader = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản độc giả!"));

        if ("LOCKED".equalsIgnoreCase(reader.getStatus())) {
            throw new IllegalStateException("Tài khoản của bạn hiện đang bị khóa. Vui lòng liên hệ thủ thư!");
        }

        List<LoanDetail> userDetails = loanDetailRepository.findByUserId(userId);

        // 1. Kiểm tra xem độc giả có cuốn sách nào quá hạn chưa trả không
        boolean hasOverdue = userDetails.stream()
                .filter(ld -> "BORROWING".equalsIgnoreCase(ld.getStatus()))
                .anyMatch(LoanDetail::isOverdue);
        if (hasOverdue) {
            throw new IllegalStateException("Bạn đang có sách mượn quá hạn chưa trả. Vui lòng hoàn trả sách trước khi mượn cuốn mới!");
        }

        // 2. Kiểm tra giới hạn số lượng sách được mượn
        int maxCopies = getRuleIntValue("MAX_BORROW_COPIES", 5);
        long currentBorrowing = userDetails.stream()
                .filter(ld -> "BORROWING".equalsIgnoreCase(ld.getStatus()))
                .count();

        if (currentBorrowing >= maxCopies) {
            throw new IllegalStateException("Bạn đã đạt giới hạn mượn tối đa (" + maxCopies + " cuốn). Vui lòng trả sách trước khi mượn tiếp!");
        }

        // 3. Kiểm tra xem độc giả đã đang mượn cuốn sách này chưa
        boolean alreadyBorrowing = userDetails.stream()
                .filter(ld -> "BORROWING".equalsIgnoreCase(ld.getStatus()))
                .anyMatch(ld -> ld.getBookCopy().getBook().getId().equals(bookId));
        if (alreadyBorrowing) {
            throw new IllegalStateException("Bạn hiện đang mượn một bản của cuốn sách này rồi!");
        }

        // 4. Tìm 1 bản sách AVAILABLE
        List<BookCopy> availableCopies = bookCopyRepository.findByBookIdAndStatus(bookId, "AVAILABLE");
        if (availableCopies.isEmpty()) {
            throw new IllegalStateException("Rất tiếc! Hiện không còn bản sách nào sẵn sàng. Bạn có thể sử dụng tính năng Đặt trước sách!");
        }

        BookCopy copy = null;
        for (BookCopy candidate : availableCopies) {
            int rowsUpdated = bookCopyRepository.updateStatusAtomic(candidate.getId(), "BORROWED", "AVAILABLE");
            if (rowsUpdated > 0) {
                copy = candidate;
                copy.setStatus("BORROWED");
                break;
            }
        }
        if (copy == null) {
            throw new IllegalStateException("Bản sách vừa được người khác mượn hoặc không còn sẵn sàng!");
        }

        int durationDays = getRuleIntValue("LOAN_DURATION_DAYS", 14);
        LocalDate today = LocalDate.now();
        LocalDate due = today.plusDays(durationDays);

        String noteText = "Mượn trực tuyến qua Web" + (notes != null && !notes.trim().isEmpty() ? " | " + notes.trim() : "");

        Loan loan = Loan.builder()
                .loanCode("ONL-" + (System.currentTimeMillis() % 1000000))
                .user(reader)
                .librarian(null)
                .loanDate(today)
                .dueDate(due)
                .status("BORROWING")
                .notes(noteText)
                .build();

        LoanDetail detail = LoanDetail.builder()
                .loan(loan)
                .bookCopy(copy)
                .status("BORROWING")
                .currentDueDate(due)
                .renewalCount(0)
                .notes("Mượn online")
                .build();

        List<LoanDetail> details = new ArrayList<>();
        details.add(detail);
        loan.setDetails(details);

        Loan savedLoan = loanRepository.save(loan);

        loanHistoryRepository.save(LoanHistory.builder()
                .loan(savedLoan)
                .loanDetail(savedLoan.getDetails().get(0))
                .action("BORROW_ONLINE")
                .performedBy(reader)
                .details("Bạn đọc mượn online bản sách " + copy.getBarcode() + " (" + copy.getBook().getTitle() + ")")
                .build());

        // Nếu có đơn đặt trước PENDING cho sách này, tự động đánh dấu hoàn tất (FULFILLED)
        try {
            List<Reservation> userReservations = reservationRepository.findByUserIdOrderByReservationDateDesc(userId);
            for (Reservation r : userReservations) {
                if ("PENDING".equalsIgnoreCase(r.getStatus()) && r.getBook().getId().equals(bookId)) {
                    r.setStatus("FULFILLED");
                    reservationRepository.save(r);
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        return savedLoan;
    }

    @Transactional
    public void renewBookByReader(Long detailId, Long userId) {
        LoanDetail detail = loanDetailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết mượn ID: " + detailId));

        if (!detail.getLoan().getUser().getId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền thao tác trên phiếu mượn này!");
        }

        if ("RETURNED".equalsIgnoreCase(detail.getStatus())) {
            throw new IllegalStateException("Cuốn sách này đã được trả!");
        }

        if (detail.isOverdue()) {
            throw new IllegalStateException("Sách đã quá hạn trả! Vui lòng mang sách đến thư viện để trả và giải quyết quá hạn.");
        }

        renewBook(detailId);
    }

    /**
     * Tra cứu thông tin đầy đủ của bản sách qua Barcode / QR Code.
     * Bao gồm thông tin sách, ảnh bìa, tình trạng, và thông tin bạn đọc đang mượn (nếu có).
     */
    @Transactional(readOnly = true)
    public BookCopyScanDto getBookCopyScanInfo(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã bản sách không được để trống!");
        }

        BookCopy copy = bookCopyRepository.findByBarcode(barcode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản sách với mã: " + barcode.trim()));

        Book book = copy.getBook();

        BookCopyScanDto.BookCopyScanDtoBuilder builder = BookCopyScanDto.builder()
                .copyId(copy.getId())
                .barcode(copy.getBarcode())
                .status(copy.getStatus())
                .condition(copy.getCondition())
                .notes(copy.getNotes())
                .bookId(book.getId())
                .bookCode(book.getBookCode())
                .title(book.getTitle())
                .author(book.getAuthor())
                .category(book.getCategory())
                .publisher(book.getPublisher())
                .publishYear(book.getPublishYear())
                .coverImage(book.getCoverImage());

        // Tạo sẵn mã QR Base64 an toàn cho bản sách
        try {
            builder.qrCodeBase64(qrCodeService.generateQrCodeBase64(copy.getBarcode(), 250, 250));
        } catch (Exception ignored) {
        }

        // Nếu sách đang được mượn, lấy thông tin phiếu mượn & bạn đọc
        if ("BORROWED".equalsIgnoreCase(copy.getStatus())) {
            loanDetailRepository.findActiveBorrowingByBarcode(copy.getBarcode())
                    .ifPresent(detail -> {
                        Loan loan = detail.getLoan();
                        User reader = loan.getUser();
                        builder.activeLoanId(loan.getId())
                               .activeLoanCode(loan.getLoanCode())
                               .activeDetailId(detail.getId())
                               .readerId(reader.getId())
                               .readerName(reader.getFullName())
                               .readerCode(reader.getReaderCode())
                               .readerPhone(reader.getPhone())
                               .loanDate(loan.getLoanDate())
                               .dueDate(detail.getCurrentDueDate() != null ? detail.getCurrentDueDate() : loan.getDueDate())
                               .isOverdue(detail.isOverdue())
                               .overdueDays(detail.getOverdueDays());
                    });
        }

        return builder.build();
    }

    /**
     * Trả sách nhanh bằng cách quét Barcode / QR Code của bản sách.
     * Tự động tìm chi tiết phiếu mượn đang active, cập nhật trạng thái an toàn với Transaction.
     */
    @Transactional
    public LoanDetail returnBookByBarcode(String barcode, String conditionOnReturn, Long librarianId) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã bản sách không được để trống!");
        }

        LoanDetail detail = loanDetailRepository.findActiveBorrowingByBarcode(barcode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn đang hoạt động cho bản sách mã: " + barcode.trim()));

        returnBook(detail.getId(), conditionOnReturn);

        if (librarianId != null) {
            userRepository.findById(librarianId).ifPresent(librarian -> {
                loanHistoryRepository.save(LoanHistory.builder()
                        .loan(detail.getLoan())
                        .loanDetail(detail)
                        .action("RETURN_QR")
                        .performedBy(librarian)
                        .details("Thủ thư quét QR trả bản sách " + barcode.trim())
                        .build());
            });
        }

        return detail;
    }
}
