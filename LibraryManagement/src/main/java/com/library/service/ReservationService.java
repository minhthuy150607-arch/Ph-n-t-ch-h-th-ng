package com.library.service;

import com.library.model.Book;
import com.library.model.Reservation;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAllByOrderByReservationDateDesc();
    }

    public List<Reservation> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserIdOrderByReservationDateDesc(userId);
    }

    @Transactional
    public Reservation reserveBook(Long userId, Long bookId, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách ID: " + bookId));

        Reservation reservation = Reservation.builder()
                .user(user)
                .book(book)
                .status("PENDING")
                .expiryDate(LocalDateTime.now().plusDays(3))
                .notes(notes)
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu đặt trước ID: " + reservationId));
        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);
    }
}
