# MindSync

Mental health screening, advising, and tracking application.

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Build and Run](#build-and-run)

## Overview

MindSync is a Spring Boot application designed for mental health screening, advising, and tracking. It provides user authentication, profile management, and OTP-based password reset functionality.

## Technology Stack

- Java 21
- Spring Boot 3.5.7
- Spring Web
- Spring Security
- PostgreSQL
- JWT (JSON Web Tokens)
- Maven

## Project Structure

```
mindsync-auth/
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── logs/
├── src/
│   ├── main/
│   │   ├── java/com/jitech/mindsync/
│   │   │   ├── MindSyncApplication.java          Main application entry point
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java          Security configuration
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java          Authentication endpoints
│   │   │   │   ├── AuthControllerTest.java      Auth controller tests
│   │   │   │   ├── EmailTestController.java     Email testing endpoint
│   │   │   │   ├── HelloWorldController.java    Health check endpoint
│   │   │   │   └── ProfileController.java       Profile management endpoints
│   │   │   ├── dto/
│   │   │   │   ├── ChangePasswordRequest.java   Change password request DTO
│   │   │   │   ├── JwtResponse.java             JWT response DTO
│   │   │   │   ├── LoginRequest.java            Login request DTO
│   │   │   │   ├── OtpRequest.java              OTP request DTO
│   │   │   │   ├── OtpVerifyRequest.java        OTP verification request DTO
│   │   │   │   ├── ProfileResponse.java         Profile response DTO
│   │   │   │   ├── ProfileUpdateRequest.java    Profile update request DTO
│   │   │   │   └── RegisterRequest.java         Registration request DTO
│   │   │   ├── model/
│   │   │   │   ├── FactorAdvices.java           Factor advice entity
│   │   │   │   ├── Factors.java                 Factors entity
│   │   │   │   ├── Genders.java                 Genders entity
│   │   │   │   ├── GuestUsers.java              Guest users entity
│   │   │   │   ├── Occupations.java             Occupations entity
│   │   │   │   ├── OtpToken.java                OTP token entity
│   │   │   │   ├── PredictedFactors.java        Predicted factors entity
│   │   │   │   ├── PredictedFactorsId.java      Composite ID for predicted factors
│   │   │   │   ├── Predictions.java             Predictions entity
│   │   │   │   ├── Users.java                   Users entity
│   │   │   │   └── WorkRemotes.java             Work remote settings entity
│   │   │   ├── repository/
│   │   │   │   ├── GendersRepository.java       Genders data repository
│   │   │   │   ├── OccupationsRepository.java   Occupations data repository
│   │   │   │   ├── OtpTokenRepository.java      OTP token repository
│   │   │   │   └── UserRepository.java          Users data repository
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java JWT authentication filter
│   │   │   │   └── JwtProvider.java             JWT token provider
│   │   │   └── service/
│   │   │       ├── AuthService.java             Authentication service
│   │   │       ├── EmailService.java            Email sending service
│   │   │       ├── OtpService.java              OTP generation and validation
│   │   │       └── ProfileService.java          Profile management service
│   │   └── resources/
│   │       ├── application.properties           Main configuration
│   │       └── application-local.properties     Local environment configuration
│   └── test/
│       └── java/com/jitech/mindsync/
│           └── MindSyncApplicationTests.java    Application tests
└── target/                                       Build output directory
```

## API Endpoints

### Authentication Endpoints

#### Register User

- **Endpoint:** `POST /register`
- **Description:** Register a new user account
- **Request Body:**
  ```json
  {
    "email": "user@example.com",
    "password": "password123",
    "name": "John Doe"
  }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "message": "Registration successful",
    "data": {
      "email": "user@example.com",
      "name": "John Doe"
    }
  }
  ```
- **Status Codes:** 200 (Success), 400 (Bad Request)

#### Login

- **Endpoint:** `POST /login`
- **Description:** Authenticate user and receive JWT token via HttpOnly cookie
- **Request Body:**
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "message": "Login successful",
    "user": {
      "userId": 1,
      "email": "user@example.com",
      "name": "John Doe",
      "dob": "1990-01-01",
      "gender": "Male",
      "occupation": "Engineer"
    }
  }
  ```
- **Cookies:** JWT token is automatically set as an HttpOnly, Secure, SameSite=Strict cookie
- **Status Codes:** 200 (Success), 401 (Unauthorized)

#### Logout

- **Endpoint:** `POST /logout`
- **Description:** Logout user and clear authentication cookie
- **Response:**
  ```json
  {
    "success": true,
    "message": "Logout successful"
  }
  ```
- **Status Codes:** 200 (Success)

### Profile Endpoints

#### Get Profile

- **Endpoint:** `GET /profile`
- **Description:** Retrieve current user's profile information
- **Authentication:** Required (JWT cookie)
- **Credentials:** Include `credentials: 'include'` when making fetch requests
- **Response:**
  ```json
  {
    "success": true,
    "data": {
      "userId": 1,
      "email": "user@example.com",
      "name": "John Doe",
      "dob": "1990-01-01",
      "gender": "Male",
      "occupation": "Engineer"
    }
  }
  ```
- **Status Codes:** 200 (Success), 400 (Bad Request)

#### Update Profile

- **Endpoint:** `PUT /profile`
- **Description:** Update user's profile information (name, gender, occupation)
- **Authentication:** Required (JWT cookie)
- **Credentials:** Include `credentials: 'include'` when making fetch requests
- **Request Body:**
  ```json
  {
    "name": "Jane Doe",
    "gender": "Female",
    "occupation": "Doctor"
  }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "message": "Profile updated successfully",
    "data": {
      "userId": 1,
      "email": "user@example.com",
      "name": "Jane Doe",
      "dob": "1990-01-01",
      "gender": "Female",
      "occupation": "Doctor"
    }
  }
  ```
- **Status Codes:** 200 (Success), 400 (Bad Request)

#### Request OTP

- **Endpoint:** `POST /profile/request-otp`
- **Description:** Request OTP code for password reset
- **Authentication:** Not required
- **Request Body:**
  ```json
  {
    "email": "user@example.com"
  }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "message": "OTP has been sent to your email"
  }
  ```
- **Status Codes:** 200 (Success), 400 (Bad Request), 500 (Internal Server Error)

#### Verify OTP

- **Endpoint:** `POST /profile/verify-otp`
- **Description:** Verify OTP code
- **Authentication:** Not required
- **Request Body:**
  ```json
  {
    "email": "user@example.com",
    "otp": "123456"
  }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "message": "OTP verified successfully"
  }
  ```
- **Status Codes:** 200 (Success), 400 (Bad Request)

#### Change Password

- **Endpoint:** `POST /profile/change-password`
- **Description:** Change user password after OTP verification
- **Authentication:** Not required
- **Request Body:**
  ```json
  {
    "email": "user@example.com",
    "otp": "123456",
    "newPassword": "newpassword123"
  }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "message": "Password changed successfully"
  }
  ```
- **Status Codes:** 200 (Success), 400 (Bad Request)

### Utility Endpoints

#### Health Check

- **Endpoint:** `GET /hello`
- **Description:** Health check endpoint
- **Authentication:** Not required
- **Response:**
  ```json
  {
    "message": "Hello World"
  }
  ```
- **Status Codes:** 200 (Success)

#### Test Email

- **Endpoint:** `GET /test-email`
- **Description:** Test email sending functionality
- **Authentication:** Not required
- **Query Parameters:**
  - `to` (required): Email address to send test email to
- **Example:** `GET /test-email?to=user@example.com`
- **Response:** Plain text message indicating success or failure
- **Status Codes:** 200 (Success)

## Getting Started

### Prerequisites

- Java 21
- Maven 3.6+
- PostgreSQL (local or Docker)
- Git

### Installation

1. Clone the repository:

   ```bash
   git clone <repository-url>
   cd mindsync-backend
   ```

2. Set up PostgreSQL (if using Docker):

   ```bash
   docker run --name mindsync-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=demo_mindsync -p 5432:5432 -d postgres:latest
   ```

3. Build the project:
   ```bash
   ./mvnw clean install
   ```

## Configuration

### Environment Variables

The following environment variables can be configured:

| Variable         | Default                                                 | Description                                                        |
| ---------------- | ------------------------------------------------------- | ------------------------------------------------------------------ |
| `JWT_SECRET`     | `ThisIsASecretKeyForJWTGenerationInMindsyncApplication` | Secret key for JWT signing (use strong random value in production) |
| `RESEND_API`     | (none)                                                  | Resend API key for email functionality                             |
| `EMAIL_USERNAME` | (none)                                                  | Email sender username                                              |
| `EMAIL_PASSWORD` | (none)                                                  | Email sender password                                              |

**Generate a strong JWT secret:**

```bash
openssl rand -base64 32
```

### Application Properties

- **Production:** `src/main/resources/application.properties`
  - Uses `https://mindsync.my` CORS origin
  - Production database configuration
  - Uses environment variables for secrets

- **Local Development:** `src/main/resources/application-local.properties`
  - Uses localhost CORS origins (3000, 5173, 8080, 8081)
  - Local PostgreSQL database
  - Runs on port 8081

## Build and Run

### Local Development with Maven

1. Set environment variables (optional, uses defaults if not set):

   ```bash
   # Linux/Mac
   export JWT_SECRET=$(openssl rand -base64 32)
   export RESEND_API="your-resend-api-key"

   # Windows PowerShell
   $env:JWT_SECRET = "your-jwt-secret"
   $env:RESEND_API = "your-resend-api-key"
   ```

2. Run with local profile:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
   ```

   Or set the profile in `application-local.properties` (already set to `dev` by default)

3. Application will be available at `http://localhost:8081`

### Production Build with Maven

1. Set required environment variables:

   ```bash
   export JWT_SECRET="your-strong-secret-key"
   export RESEND_API="your-resend-api-key"
   export EMAIL_USERNAME="your-email"
   export EMAIL_PASSWORD="your-email-password"
   ```

2. Build the project:

   ```bash
   ./mvnw clean package -DskipTests
   ```

3. Run the application:
   ```bash
   java -jar target/mindsync-*.jar
   ```

### Using Docker

Build Docker image:

```bash
docker build -t mindsync-auth .
```

Run Docker container with environment variables:

```bash
docker run -p 8080:8080 \
  -e JWT_SECRET="your-jwt-secret" \
  -e RESEND_API="your-resend-api-key" \
  -e EMAIL_USERNAME="your-email" \
  -e EMAIL_PASSWORD="your-email-password" \
  mindsync-auth
```

### Docker Compose (Recommended for Local Development)

Create a `docker-compose.yml` file:

```yaml
version: "3.8"
services:
  db:
    image: postgres:latest
    environment:
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: demo_mindsync
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8081:8081"
    environment:
      JWT_SECRET: ${JWT_SECRET}
      RESEND_API: ${RESEND_API}
      EMAIL_USERNAME: ${EMAIL_USERNAME}
      EMAIL_PASSWORD: ${EMAIL_PASSWORD}
      SPRING_PROFILES_ACTIVE: local
    depends_on:
      - db

volumes:
  postgres_data:
```

Run with docker-compose:

```bash
docker-compose up
```

### Default Ports

- **Local Development:** `http://localhost:8081`
- **Production:** `http://localhost:8080`

## Security Features

This application implements the following security measures:

- **HttpOnly Cookies:** JWT tokens stored as HttpOnly, Secure, SameSite=Strict cookies to prevent XSS attacks
- **CSRF Protection:** SameSite attribute on cookies prevents Cross-Site Request Forgery
- **Input Validation:** @Valid annotations on DTOs with @NotBlank, @Email, @Size constraints
- **Security Headers:**
  - HSTS (HTTP Strict Transport Security)
  - XSS Protection
  - Content Security Policy
  - Frame Options (Clickjacking protection)
- **Environment-based Secrets:** JWT secret loaded from environment variables
- **Password Requirements:** Minimum 8 characters enforced at registration
- **CORS Configuration:** Environment-specific allowed origins
- **BCrypt Password Hashing:** Passwords hashed with BCrypt before storage

## Frontend Integration

### HttpOnly Cookie Authentication

This API uses HttpOnly cookies for authentication instead of Bearer tokens. The JWT token is automatically set in a secure cookie on login.

#### Fetch API

```javascript
// Login
const response = await fetch("http://localhost:8081/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  credentials: "include", // Important: send cookies with request
  body: JSON.stringify({
    email: "user@example.com",
    password: "password123",
  }),
});

// Authenticated requests
const profileResponse = await fetch("http://localhost:8081/profile", {
  method: "GET",
  credentials: "include", // Cookie is automatically sent
});
```

#### Axios

```javascript
// Configure axios to send cookies with requests
axios.defaults.withCredentials = true;

// Login
const response = await axios.post("http://localhost:8081/login", {
  email: "user@example.com",
  password: "password123",
});

// Authenticated requests automatically include the cookie
const profile = await axios.get("http://localhost:8081/profile");

// Logout
await axios.post("http://localhost:8081/logout");
```

**Important:** Always use `credentials: 'include'` (Fetch) or `withCredentials: true` (Axios) for cookie-based authentication to work.

## CORS Configuration

### Local Development

```
http://127.0.0.1:3000
http://127.0.0.1:5173
http://localhost:3000
http://localhost:5173
```

### Production

```
https://mindsync.my
```

Update `src/main/resources/application.properties` and `src/main/resources/application-local.properties` to add/modify allowed origins.
