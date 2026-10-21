package com.library.service;

import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final LoanDetailRepository loanDetailRepository;
    private final ReservationRepository reservationRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalBooks = bookRepository.count();
        long totalCopies = bookCopyRepository.count();
        long totalReaders = userRepository.countByRole("READER");
        long totalBorrowing = bookCopyRepository.countByStatus("BORROWED");
        long overdueCount = loanDetailRepository.findOverdueDetails(LocalDate.now()).size();
        long pendingReservations = reservationRepository.countByStatus("PENDING");

        stats.put("totalBooks", totalBooks);
        stats.put("totalCopies", totalCopies);
        stats.put("totalReaders", totalReaders);
        stats.put("totalBorrowing", totalBorrowing);
        stats.put("overdueCount", overdueCount);
        stats.put("pendingReservations", pendingReservations);

        List<Object[]> topBooks = loanDetailRepository.findTopBorrowedBooks();
        stats.put("topBooks", topBooks);

        return stats;
    }
}
