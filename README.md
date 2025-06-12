# Personal Expense Tracker

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x%20%7C%203.x-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-2023-61DAFB?logo=react&logoColor=white)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/Authentication-JWT-F0C808?logo=jsonwebtokens&logoColor=white)](https://jwt.io/)

## Mục lục

1.  [Giới thiệu](#1-giới-thiệu)
2.  [Tính năng](#2-tính-năng)
3.  [Công nghệ sử dụng](#3-công-nghệ-sử-dụng)
    * [Backend](#backend)
    * [Frontend](#frontend)
4.  [Yêu cầu hệ thống](#4-yêu-cầu-hệ-thống)


---

## 1. Giới thiệu

**Personal Expense Tracker** là một ứng dụng quản lý chi tiêu cá nhân giúp người dùng theo dõi và quản lý các khoản thu chi của mình một cách hiệu quả. Dự án này được phát triển với kiến trúc microservices (hoặc monolithic với tách biệt rõ ràng giữa Backend và Frontend), sử dụng Spring Boot cho Backend API và ReactJS cho Frontend UI.

## 2. Tính năng

### Backend (API)
* **Xác thực & Ủy quyền (Authentication & Authorization):**
    * Đăng ký người dùng mới.
    * Đăng nhập với JWT (JSON Web Token) để xác thực phiên làm việc.
    * Bảo vệ các API endpoints dựa trên vai trò (USER, ADMIN).
* **Quản lý Người dùng (User Management):**
    * Xem thông tin người dùng (chỉ admin).
    * Xem thông tin người dùng hiện tại (chỉ user đã đăng nhập).
* **Quản lý Danh mục (Category Management):**
    * Thêm, sửa, xóa các danh mục chi tiêu/thu nhập (ví dụ: Ăn uống, Đi lại, Lương...).
    * Mỗi danh mục có thể liên kết với một người dùng cụ thể (hoặc là danh mục mặc định).
* **Quản lý Chi tiêu (Expense/Transaction Management):**
    * Thêm, sửa, xóa các khoản chi tiêu/giao dịch.
    * Mỗi khoản chi tiêu liên kết với một danh mục và người dùng cụ thể.
    * Ghi lại số tiền, mô tả và ngày giao dịch.
* **Quản lý Ngân sách (Budget Management):**
    * Thiết lập ngân sách theo danh mục hoặc tổng thể cho một khoảng thời gian cụ thể.
    * Theo dõi giới hạn chi tiêu.

### Frontend (UI)
* Giao diện đăng ký và đăng nhập thân thiện.
* Dashboard tổng quan về tình hình tài chính.
* Các trang quản lý riêng biệt cho Danh mục, Chi tiêu và Ngân sách.
* Chức năng thêm/sửa/xóa dữ liệu thông qua các biểu mẫu và bảng.
* Bảo vệ định tuyến (route protection) dựa trên trạng thái đăng nhập.

## 3. Công nghệ sử dụng

### Backend
* **Ngôn ngữ:** Java 17+
* **Framework:** Spring Boot 3.x
* **Quản lý phụ thuộc:** Maven
* **Database:** PostgreSQL
* **ORM:** Spring Data JPA / Hibernate
* **Bảo mật:** Spring Security, JWT (JSON Web Token)
* **Utility:** Lombok (để giảm boilerplate code)
* **Validation:** Jakarta Bean Validation

### Frontend
* **Ngôn ngữ:** JavaScript (ES6+)
* **Thư viện:** ReactJS (v18+)
* **Công cụ build:** Vite
* **Routing:** React Router DOM
* **HTTP Client:** Axios
* **Quản lý trạng thái:** (Có thể thêm Context API hoặc Redux/Zustand sau này)
* **UI Library/Styling:** (Tùy chọn: Tailwind CSS, Material-UI, Ant Design hoặc CSS thuần)

## 4. Yêu cầu hệ thống

* **Java Development Kit (JDK):** Phiên bản 17 trở lên.
* **Maven:** Phiên bản 3.x trở lên.
* **Node.js:** Phiên bản 16 trở lên (bao gồm npm).
* **PostgreSQL:** Phiên bản 12 trở lên.
* **IDE:** IntelliJ IDEA (cho Backend) và Visual Studio Code (cho Frontend) hoặc WebStorm.


