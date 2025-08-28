# Personal Expense Tracker

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x%20%7C%203.x-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-2023-61DAFB?logo=react&logoColor=white)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/Authentication-JWT-F0C808?logo=jsonwebtokens&logoColor=white)](https://jwt.io/)

This is the backend project for the **Expense Tracker** application, built with **Spring Boot**. The system provides a set of secure REST APIs to handle business logic, user management, authentication, and data storage.

The backend is designed to work with any frontend (React, Vue, Angular, mobile, etc.) that can communicate via HTTP.

## ✨ Key Features

- **REST APIs:** Provides RESTful APIs for resource management.
- **Comprehensive Security:** Integrates **Spring Security 6** to protect endpoints.
- **JWT Authentication:** Uses JSON Web Tokens (JWT) with **Access Token** and **Refresh Token** mechanisms for secure and flexible authentication.
- **User Management:** Offers full functionality for user registration, login, and logout.
- **Persistence:** Uses **Spring Data JPA** and **Hibernate** to interact with the database.
- **Centralized Error Handling:** Defines custom Exceptions (ResourceNotFoundException, BadRequestException, TokenRefreshException) to return clear, contextual HTTP error codes.
- **Validation:** Uses `jakarta.validation` to validate input data.

## 🛠️ Technologies Used

- **Language:** [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **Framework:** [Spring Boot 3.x](https://spring.io/projects/spring-boot)
- **Security:** [Spring Security 6.x](https://spring.io/projects/spring-security)
- **Database Persistence:** [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- **JWT Library:** `io.jsonwebtoken`
- **Database:** [PostgreSQL](https://www.postgresql.org/) (can be easily switched to MySQL, H2, etc.)
- **Build Tool:** [Apache Maven](https://maven.apache.org/)

## 🚀 Setup and Running the Project

### Prerequisites

- JDK 17 or higher.
- Apache Maven 3.6+
- A running instance of PostgreSQL.

### Installation Steps

1. **Clone the repository:**

   ```bash
   git clone [https://github.com/thanhpro0802/personal-expense-tracker-backend.git](https://github.com/thanhpro0802/personal-expense-tracker-backend.git)
   cd personal-expense-tracker-backend
    ```

2.  **Configure the Database:**
    - Open PostgreSQL and create a new database, for example: `expense_tracker_db`.

3.  **Configure the Application:**
    - Open the src/main/resources/application.properties file..
    - Update the database connection information and the secret keys for JWT. Below is a sample configuration:

    ```properties name=src/main/resources/application.properties
    # Server Port
    server.port=8081
    
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
    # --- IMPORTANT: Change this secret string to your own random and secure string! ---
    expensetracker.app.jwtSecret======================ThanhProJWTSecret======================
    # Access Token expiration time (e.g., 15 minutes)
    expensetracker.app.jwtExpirationMs=900000
    # Refresh Token expiration time (e.g., 7 days)
    expensetracker.app.jwtRefreshExpirationMs=604800000
    ```

4.  **Build the Project:**
   Use Maven to build and package the application:
    ```bash
    mvn clean install
    ```

5.  **Run the Application:**
    ```bash
    mvn spring-boot:run
    ```
    The backend will start and run on port 8080.

## 📖 API Endpoints

### **Authentication (`/api/auth`)**

| Method | Endpoint          | Description                                                            |
| :----- | :---------------- | :--------------------------------------------------------------------- |
| `POST` | `/signup`         | Registers a new user account.                                          |
| `POST` | `/signin`         | Logs in and receives an Access Token.                                  |                   
| `POST` | `/refreshtoken`   | Submits a Refresh Token to get a new Access Token.                     |
| `POST` | `/signout`        | Logs out the user.                                                     |

### **User Management (`/api/users`)**

_Note: These endpoints typically require administrative privileges (ADMIN) in a real-world application._

| Method | Endpoint   | Description                          |
| :----- | :--------- | :----------------------------------- |
| `GET`  | `/`        | Retrieves a list of all users.       |
| `GET`  | `/{id}`    | Retrieves user information by ID.    |
| `PUT`  | `/{id}`    | Updates user information.            |
| `DELETE`| `/{id}`   | Deletes a user.                      |

## ✨ Author

**Nguyễn Tuấn Thành**

- 🔗 GitHub: - 🔗 GitHub: [https://github.com/thanhpro0802](https://github.com/thanhpro0802)
- 🎓 Hanoi University of Science and Technology (HUST)
- 📚 Major: Information Technology – Việt Nhật Program
- 📧 Email: tuanthanh.work@gmail.com 

---
