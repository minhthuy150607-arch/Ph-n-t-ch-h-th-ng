package com.library.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_copy_id", nullable = false)
    private BookCopy bookCopy;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "condition_on_return", length = 20)
    private String conditionOnReturn;

    @Column(name = "renewal_count")
    @Builder.Default
    private Integer renewalCount = 0;

    @Column(name = "current_due_date")
    private LocalDate currentDueDate;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "BORROWING"; // BORROWING, RETURNED, OVERDUE

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Kiểm tra xem cuốn sách này có bị quá hạn không
    @Transient
    public boolean isOverdue() {
        if ("RETURNED".equalsIgnoreCase(status)) return false;
        LocalDate effectiveDue = currentDueDate != null ? currentDueDate : (loan != null ? loan.getDueDate() : null);
        return effectiveDue != null && LocalDate.now().isAfter(effectiveDue);
    }

    // Số ngày quá hạn
    @Transient
    public long getOverdueDays() {
        if (!isOverdue()) return 0;
        LocalDate effectiveDue = currentDueDate != null ? currentDueDate : loan.getDueDate();
        return java.time.temporal.ChronoUnit.DAYS.between(effectiveDue, LocalDate.now());
    }
}
