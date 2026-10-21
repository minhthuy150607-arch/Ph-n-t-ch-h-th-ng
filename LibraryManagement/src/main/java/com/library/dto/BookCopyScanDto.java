package com.library.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCopyScanDto {

    // Thông tin bản sách
    private Long copyId;
    private String barcode;
    private String status;       // AVAILABLE, BORROWED, RESERVED, LOST, DAMAGED
    private String condition;    // GOOD, FAIR, POOR
    private String notes;
    private String qrCodeBase64;

    // Thông tin đầu sách
    private Long bookId;
    private String bookCode;
    private String title;
    private String author;
    private String category;
    private String publisher;
    private Integer publishYear;
    private String coverImage;

    // Thông tin giao dịch mượn đang diễn ra (nếu có)
    private Long activeLoanId;
    private String activeLoanCode;
    private Long activeDetailId;
    private Long readerId;
    private String readerName;
    private String readerCode;
    private String readerPhone;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private boolean isOverdue;
    private long overdueDays;
}
