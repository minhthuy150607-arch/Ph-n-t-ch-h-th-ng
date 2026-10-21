package com.library.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_detail_id")
    private LoanDetail loanDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @Column(nullable = false, length = 50)
    private String action; // BORROW, RETURN, RENEW, OVERDUE_MARKED

    @CreationTimestamp
    @Column(name = "action_date", updatable = false)
    private LocalDateTime actionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Column(name = "old_due_date")
    private LocalDate oldDueDate;

    @Column(name = "new_due_date")
    private LocalDate newDueDate;

    @Column(columnDefinition = "TEXT")
    private String details;
}
