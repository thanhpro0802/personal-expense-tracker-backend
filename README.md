# Personal Expense Tracker

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x%20%7C%203.x-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-2023-61DAFB?logo=react&logoColor=white)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/Authentication-JWT-F0C808?logo=jsonwebtokens&logoColor=white)](https://jwt.io/)

Đây là dự án backend cho ứng dụng Quản lý Chi tiêu (Expense Tracker), được xây dựng bằng **Spring Boot**. Hệ thống cung cấp một bộ các REST API an toàn để xử lý logic nghiệp vụ, quản lý người dùng, xác thực và lưu trữ dữ liệu.

Backend được thiết kế để hoạt động với bất kỳ frontend nào (React, Vue, Angular, mobile...) có khả năng giao tiếp qua HTTP.

## ✨ Các tính năng chính

- **REST APIs:** Cung cấp các API theo chuẩn RESTful để quản lý tài nguyên.
- **Bảo mật toàn diện:** Tích hợp **Spring Security 6** để bảo vệ các endpoint.
- **Xác thực bằng JWT:** Sử dụng JSON Web Token (JWT) với cơ chế **Access Token** và **Refresh Token** để đảm bảo xác thực an toàn và linh hoạt.
- **Quản lý người dùng:** Cung cấp đầy đủ các chức năng đăng ký, đăng nhập, đăng xuất.
- **Persistence:** Sử dụng **Spring Data JPA** và **Hibernate** để tương tác với cơ sở dữ liệu.
- **Xử lý lỗi tập trung:** Định nghĩa các Exception tùy chỉnh (`ResourceNotFoundException`, `BadRequestException`, `TokenRefreshException`) để trả về các mã lỗi HTTP có ngữ cảnh rõ ràng.
- **Validation:** Sử dụng `jakarta.validation` để kiểm tra tính hợp lệ của dữ liệu đầu vào.

## 🛠️ Công nghệ sử dụng

- **Ngôn ngữ:** [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **Framework:** [Spring Boot 3.x](https://spring.io/projects/spring-boot)
- **Bảo mật:** [Spring Security 6.x](https://spring.io/projects/spring-security)
- **Cơ sở dữ liệu:** [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- **JWT Library:** `io.jsonwebtoken`
- **Database:** [PostgreSQL](https://www.postgresql.org/) (Có thể dễ dàng chuyển sang MySQL, H2,...)
- **Build Tool:** [Apache Maven](https://maven.apache.org/)

## 🚀 Cài đặt và Chạy dự án

### Yêu cầu tiên quyết

- JDK 17 hoặc cao hơn.
- Apache Maven 3.6+
- Một instance của PostgreSQL đang chạy.

### Các bước cài đặt

1.  **Clone repository về máy:**
    ```bash
     git clone https://github.com/thanhpro0802/personal-expense-tracker-backend.git
    cd personal-expense-tracker-backend
    ```

2.  **Cấu hình Cơ sở dữ liệu:**
    - Mở PostgreSQL và tạo một database mới, ví dụ: `expense_tracker_db`.

3.  **Cấu hình ứng dụng:**
    - Mở file `src/main/resources/application.properties`.
    - Cập nhật thông tin kết nối database và các khóa bí mật cho JWT. Dưới đây là một mẫu cấu hình:

    ```properties name=src/main/resources/application.properties
    # Server Port
    server.port=8080

    # PostgreSQL Database Connection
    spring.datasource.url=jdbc:postgresql://localhost:5432/expense_tracker_db
    spring.datasource.username=your_postgres_username
    spring.datasource.password=your_postgres_password
    spring.datasource.driver-class-name=org.postgresql.Driver

    # JPA and Hibernate Configuration
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
    spring.jpa.properties.hibernate.format_sql=true

    # JWT Secret and Expiration
    # --- QUAN TRỌNG: Hãy thay đổi chuỗi secret này thành một chuỗi ngẫu nhiên và an toàn của riêng bạn! ---
    expensetracker.app.jwtSecret======================ThanhProJWTSecret======================
    # Thời gian hết hạn của Access Token (ví dụ: 15 phút)
    expensetracker.app.jwtExpirationMs=900000
    # Thời gian hết hạn của Refresh Token (ví dụ: 7 ngày)
    expensetracker.app.jwtRefreshExpirationMs=604800000
    ```

4.  **Build dự án:**
    Sử dụng Maven để build và đóng gói ứng dụng:
    ```bash
    mvn clean install
    ```

5.  **Chạy ứng dụng:**
    ```bash
    mvn spring-boot:run
    ```
    Backend sẽ khởi động và chạy trên cổng `8080`.

## 📖 Danh sách API Endpoints

Dưới đây là các API chính được cung cấp bởi hệ thống.

### **Authentication (`/api/auth`)**

| Method | Endpoint          | Mô tả                                                              |
| :----- | :---------------- | :----------------------------------------------------------------- |
| `POST` | `/signup`         | Đăng ký một tài khoản người dùng mới.                              |
| `POST` | `/signin`         | Đăng nhập và nhận về Access Token và Refresh Token.                |
| `POST` | `/refreshtoken`   | Gửi Refresh Token để nhận một Access Token mới.                    |
| `POST` | `/signout`        | Đăng xuất người dùng (vô hiệu hóa Refresh Token).                  |

### **User Management (`/api/users`)**

_Lưu ý: Các endpoint này thường yêu cầu quyền quản trị (ADMIN) trong một ứng dụng thực tế._

| Method | Endpoint   | Mô tả                                |
| :----- | :--------- | :----------------------------------- |
| `GET`  | `/`        | Lấy danh sách tất cả người dùng.     |
| `GET`  | `/{id}`    | Lấy thông tin người dùng theo ID.    |
| `PUT`  | `/{id}`    | Cập nhật thông tin người dùng.       |
| `DELETE`| `/{id}`   | Xóa một người dùng.                  |

## ✨ Author

**Nguyễn Tuấn Thành**

- 🔗 GitHub: - 🔗 GitHub: [https://github.com/thanhpro0802](https://github.com/thanhpro0802)
- 🎓 Hanoi University of Science and Technology (HUST)
- 📚 Major: Information Technology – Việt Nhật Program
- 📧 Email: tuanthanh.work@gmail.com 

---
