-- ================================================================
-- LIBRARY MANAGEMENT SYSTEM - DATABASE SCHEMA
-- Database: PostgreSQL
-- Tạo database: CREATE DATABASE library_db;
-- ================================================================

-- ========================================
-- 1. BẢNG USERS (Tài khoản người dùng)
-- ========================================
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(100)    UNIQUE,
    phone           VARCHAR(20),
    reader_code     VARCHAR(20)     UNIQUE,          -- Mã bạn đọc (chỉ vai trò READER)
    role            VARCHAR(20)     NOT NULL DEFAULT 'READER',  -- ADMIN hoặc READER
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE hoặc LOCKED
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- Index tìm kiếm nhanh theo username, email, reader_code
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_reader_code ON users(reader_code);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- ========================================
-- 2. BẢNG BOOKS (Đầu sách)
-- ========================================
CREATE TABLE IF NOT EXISTS books (
    id              BIGSERIAL       PRIMARY KEY,
    book_code       VARCHAR(20)     NOT NULL UNIQUE,   -- Mã sách duy nhất
    title           VARCHAR(255)    NOT NULL,
    author          VARCHAR(255)    NOT NULL,
    category        VARCHAR(100),                       -- Thể loại
    publisher       VARCHAR(255),                       -- Nhà xuất bản
    publish_year    INT,                                -- Năm xuất bản
    description     TEXT,                               -- Mô tả sách
    cover_image     VARCHAR(500),                       -- Đường dẫn ảnh bìa
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE hoặc INACTIVE
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_books_book_code ON books(book_code);
CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);
CREATE INDEX IF NOT EXISTS idx_books_author ON books(author);
CREATE INDEX IF NOT EXISTS idx_books_category ON books(category);
CREATE INDEX IF NOT EXISTS idx_books_status ON books(status);

-- ========================================
-- 3. BẢNG BOOK_COPIES (Bản sách)
-- ========================================
-- Mỗi đầu sách có thể có nhiều bản sao.
-- Mỗi bản sao có barcode riêng và trạng thái riêng.
CREATE TABLE IF NOT EXISTS book_copies (
    id              BIGSERIAL       PRIMARY KEY,
    book_id         BIGINT          NOT NULL REFERENCES books(id) ON DELETE RESTRICT,
    barcode         VARCHAR(50)     NOT NULL UNIQUE,    -- Barcode / QR duy nhất cho từng bản sách (ví dụ: BS0001)
    location        VARCHAR(100)    DEFAULT 'Kệ A1 - Tầng 1', -- Vị trí giá sách/kệ (ví dụ: Kệ A1 - Tầng 1)
    status          VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
                                    -- AVAILABLE: còn sẵn
                                    -- BORROWED: đang được mượn
                                    -- RESERVED: đã đặt trước
                                    -- LOST: mất
                                    -- DAMAGED: hỏng
    condition       VARCHAR(20)     DEFAULT 'GOOD',     -- GOOD, FAIR, POOR
    notes           TEXT,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_book_copies_book_id ON book_copies(book_id);
CREATE INDEX IF NOT EXISTS idx_book_copies_barcode ON book_copies(barcode);
CREATE INDEX IF NOT EXISTS idx_book_copies_status ON book_copies(status);

-- ========================================
-- 4. BẢNG LOANS (Phiếu mượn)
-- ========================================
-- Mỗi phiếu mượn thuộc về một bạn đọc, do một thủ thư tạo.
-- Một phiếu mượn có thể chứa nhiều bản sách (qua loan_details).
CREATE TABLE IF NOT EXISTS loans (
    id              BIGSERIAL       PRIMARY KEY,
    loan_code       VARCHAR(20)     NOT NULL UNIQUE,    -- Mã phiếu mượn
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    librarian_id    BIGINT          REFERENCES users(id),  -- Thủ thư tạo phiếu
    loan_date       DATE            NOT NULL,
    due_date        DATE            NOT NULL,            -- Hạn trả
    status          VARCHAR(20)     NOT NULL DEFAULT 'BORROWING',
                                    -- BORROWING: đang mượn
                                    -- RETURNED: đã trả hết
                                    -- OVERDUE: quá hạn
    notes           TEXT,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_loans_loan_code ON loans(loan_code);
CREATE INDEX IF NOT EXISTS idx_loans_user_id ON loans(user_id);
CREATE INDEX IF NOT EXISTS idx_loans_status ON loans(status);
CREATE INDEX IF NOT EXISTS idx_loans_due_date ON loans(due_date);

-- ========================================
-- 5. BẢNG LOAN_DETAILS (Chi tiết phiếu mượn)
-- ========================================
-- Mỗi dòng là một bản sách thuộc phiếu mượn.
CREATE TABLE IF NOT EXISTS loan_details (
    id                  BIGSERIAL       PRIMARY KEY,
    loan_id             BIGINT          NOT NULL REFERENCES loans(id) ON DELETE RESTRICT,
    book_copy_id        BIGINT          NOT NULL REFERENCES book_copies(id) ON DELETE RESTRICT,
    return_date         DATE,                               -- Ngày trả thực tế
    condition_on_return VARCHAR(20),                         -- Tình trạng khi trả
    renewal_count       INT             DEFAULT 0,          -- Số lần đã gia hạn
    current_due_date    DATE,                               -- Hạn trả hiện tại (sau gia hạn)
    status              VARCHAR(20)     NOT NULL DEFAULT 'BORROWING',
                                        -- BORROWING, RETURNED, OVERDUE
    notes               TEXT,
    created_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

    -- Ràng buộc: không mượn trùng bản sách trong cùng phiếu
    UNIQUE(loan_id, book_copy_id)
);

CREATE INDEX IF NOT EXISTS idx_loan_details_loan_id ON loan_details(loan_id);
CREATE INDEX IF NOT EXISTS idx_loan_details_book_copy_id ON loan_details(book_copy_id);
CREATE INDEX IF NOT EXISTS idx_loan_details_status ON loan_details(status);

-- ========================================
-- 6. BẢNG RESERVATIONS (Đặt trước sách)
-- ========================================
CREATE TABLE IF NOT EXISTS reservations (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    book_id             BIGINT          NOT NULL REFERENCES books(id) ON DELETE RESTRICT,
    reservation_date    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    expiry_date         TIMESTAMP,                          -- Ngày hết hạn đặt trước
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
                                        -- PENDING: đang chờ
                                        -- FULFILLED: đã nhận sách
                                        -- CANCELLED: đã hủy
                                        -- EXPIRED: hết hạn
    notes               TEXT,
    created_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reservations_user_id ON reservations(user_id);
CREATE INDEX IF NOT EXISTS idx_reservations_book_id ON reservations(book_id);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);

-- ========================================
-- 7. BẢNG LIBRARY_RULES (Quy định thư viện)
-- ========================================
-- Lưu các quy định dưới dạng key-value, có thể cấu hình bởi thủ thư.
CREATE TABLE IF NOT EXISTS library_rules (
    id              BIGSERIAL       PRIMARY KEY,
    rule_key        VARCHAR(50)     NOT NULL UNIQUE,    -- Khóa quy định
    rule_value      VARCHAR(255)    NOT NULL,           -- Giá trị
    description     TEXT,                                -- Mô tả quy định
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- 8. BẢNG LOAN_HISTORY (Lịch sử giao dịch)
-- ========================================
-- Ghi lại mọi thao tác quan trọng: mượn, trả, gia hạn.
CREATE TABLE IF NOT EXISTS loan_history (
    id                  BIGSERIAL       PRIMARY KEY,
    loan_detail_id      BIGINT          REFERENCES loan_details(id),
    loan_id             BIGINT          REFERENCES loans(id),
    action              VARCHAR(50)     NOT NULL,
                                        -- BORROW, RETURN, RENEW, OVERDUE_MARKED
    action_date         TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    performed_by        BIGINT          REFERENCES users(id),   -- Người thực hiện
    old_due_date        DATE,           -- Hạn trả cũ (khi gia hạn)
    new_due_date        DATE,           -- Hạn trả mới (khi gia hạn)
    details             TEXT            -- Ghi chú chi tiết
);

CREATE INDEX IF NOT EXISTS idx_loan_history_loan_id ON loan_history(loan_id);
CREATE INDEX IF NOT EXISTS idx_loan_history_action ON loan_history(action);
CREATE INDEX IF NOT EXISTS idx_loan_history_action_date ON loan_history(action_date);

-- ================================================================
-- DỮ LIỆU KHỞI TẠO
-- ================================================================

-- Quy định mặc định của thư viện
INSERT INTO library_rules (rule_key, rule_value, description) VALUES
    ('MAX_BORROW_COPIES', '5', 'Số bản sách tối đa được mượn cùng lúc'),
    ('LOAN_DURATION_DAYS', '14', 'Số ngày mượn sách'),
    ('RENEWAL_DURATION_DAYS', '7', 'Số ngày được gia hạn mỗi lần'),
    ('MAX_RENEWALS', '2', 'Số lần gia hạn tối đa'),
    ('RESERVATION_EXPIRY_DAYS', '3', 'Số ngày giữ sách đặt trước'),
    ('OVERDUE_FINE_PER_DAY', '2000', 'Mức phạt quá hạn mỗi ngày (VNĐ), 0 nếu không áp dụng'),
    ('ALLOW_RENEWAL_WHEN_OVERDUE', 'false', 'Cho phép gia hạn khi quá hạn (true/false)'),
    ('ALLOW_RENEWAL_WHEN_RESERVED', 'false', 'Cho phép gia hạn khi có người đặt trước (true/false)')
ON CONFLICT (rule_key) DO NOTHING;

-- ================================================================
-- GHI CHÚ QUAN TRỌNG
-- ================================================================
-- 1. Tạo database trước: CREATE DATABASE library_db;
-- 2. Chạy file này trong pgAdmin 4 (Query Tool) sau khi tạo database.
-- 3. Hibernate (ddl-auto=update) sẽ tự tạo bảng từ Entity,
--    nhưng file này giúp hiểu cấu trúc và khởi tạo dữ liệu.
-- 4. Tài khoản admin sẽ được tạo qua DataInitializer ở Phase 3
--    (mật khẩu được mã hóa BCrypt, không lưu plaintext trong SQL).
