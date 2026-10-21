# 📚 Library Management System
## Hệ thống quản lý thư viện - Đồ án môn học

### Công nghệ sử dụng
| Thành phần | Công nghệ |
|---|---|
| Backend | Java 21, Spring Boot 3.2.5 |
| Kiến trúc | MVC (Model-View-Controller) |
| Database | PostgreSQL |
| ORM | Spring Data JPA + Hibernate |
| Frontend | HTML5, CSS3, JavaScript, Thymeleaf |
| Bảo mật | Spring Security, BCrypt |
| Build tool | Maven |

---

### Yêu cầu hệ thống
- **Java JDK 21** trở lên
- **PostgreSQL 15+** (quản lý bằng pgAdmin 4)
- **Maven 3.8+** (hoặc sử dụng Maven Wrapper)
- **Visual Studio Code** với các extension:
  - Extension Pack for Java
  - Spring Boot Extension Pack

---

### Hướng dẫn cài đặt và chạy

#### Bước 1: Tạo database PostgreSQL
1. Mở **pgAdmin 4**
2. Kết nối đến PostgreSQL server
3. Click chuột phải vào **Databases** → **Create** → **Database**
4. Nhập tên: `library_db` → click **Save**
5. (Tùy chọn) Mở **Query Tool** trên database `library_db`, chạy file `database/schema.sql`

#### Bước 2: Cấu hình kết nối database
Mở file `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/library_db
spring.datasource.username=postgres
spring.datasource.password=123456
```
Thay `postgres` và `123456` bằng username/password PostgreSQL của bạn.

**Cách an toàn hơn:** Dùng biến môi trường:
- Mở PowerShell (hoặc Terminal trong VS Code)
- Chạy:
```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"
```

#### Bước 3: Chạy ứng dụng
Mở Terminal trong VS Code (Ctrl + `), chạy:

```bash
cd LibraryManagement
mvn spring-boot:run
```

Hoặc nếu chưa cài Maven:
```bash
cd LibraryManagement
mvnw spring-boot:run
```

#### Bước 4: Truy cập website
- **Trang chủ:** http://localhost:8080
- **Đăng nhập:** http://localhost:8080/login

**Tài khoản test (Phase 1 - tạm thời):**
| Vai trò | Username | Password |
|---|---|---|
| Quản trị viên | admin | admin123 |
| Bạn đọc | reader | reader123 |

> ⚠️ Tài khoản tạm thời chỉ dùng trong phát triển. Sẽ thay bằng tài khoản database ở Phase 3.

---

### Cấu trúc thư mục
```
LibraryManagement/
├── pom.xml                          # Cấu hình Maven
├── database/
│   └── schema.sql                   # Script tạo database
├── src/main/
│   ├── java/com/library/
│   │   ├── LibraryApplication.java  # Điểm khởi động
│   │   ├── config/
│   │   │   └── SecurityConfig.java  # Cấu hình bảo mật
│   │   ├── controller/              # Xử lý request (Controller)
│   │   ├── model/                   # Entity JPA (Model)
│   │   ├── repository/              # Truy vấn database
│   │   ├── service/                 # Logic nghiệp vụ
│   │   └── dto/                     # Data Transfer Object
│   └── resources/
│       ├── application.properties   # Cấu hình ứng dụng
│       ├── templates/               # Giao diện Thymeleaf (View)
│       │   ├── layouts/             # Layout dùng chung
│       │   ├── client/              # Trang bạn đọc
│       │   └── admin/               # Trang quản trị
│       └── static/
│           ├── css/                 # Stylesheets
│           ├── js/                  # JavaScript
│           └── images/              # Hình ảnh
└── src/test/                        # Unit test
```

---

### Tiến độ phát triển
- [x] **Phase 1:** Project setup, Maven, Security cơ bản, Layout
- [x] **Phase 2:** Entity, Repository, Service, Database Layer (JPA + Hibernate)
- [x] **Phase 3:** Đăng nhập, đăng xuất, phân quyền động từ Database (BCrypt)
- [x] **Phase 4:** Quản lý sách, quản lý bản sao (Barcode), Quản lý bạn đọc
- [x] **Phase 5:** Mượn trả sách, gia hạn, đặt trước (Reservation), Quá hạn & tính phạt
- [x] **Phase 6:** Dashboard thống kê trực quan, biểu đồ Chart.js, Báo cáo
- [x] **Phase 7:** Khởi tạo dữ liệu mẫu tự động (Data Initializer) sẵn sàng demo ngay
