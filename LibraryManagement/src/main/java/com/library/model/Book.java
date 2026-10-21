package com.library.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_code", nullable = false, unique = true, length = 20)
    private String bookCode;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(length = 100)
    private String category;

    @Column(length = 255)
    private String publisher;

    @Column(name = "publish_year")
    private Integer publishYear;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE hoặc INACTIVE

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookCopy> copies = new ArrayList<>();

    // Tiện ích: Đếm số bản sách sẵn có để mượn
    @Transient
    public long getAvailableCopiesCount() {
        if (copies == null) return 0;
        return copies.stream().filter(c -> "AVAILABLE".equalsIgnoreCase(c.getStatus())).count();
    }

    @Transient
    public long getTotalCopiesCount() {
        if (copies == null) return 0;
        return copies.size();
    }
}
