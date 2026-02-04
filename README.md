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
- **Description:** Authenticate user and receive JWT token
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
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
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
- **Status Codes:** 200 (Success), 401 (Unauthorized)

### Profile Endpoints

#### Get Profile
- **Endpoint:** `GET /profile`
- **Description:** Retrieve current user's profile information
- **Authentication:** Required (Bearer token)
- **Headers:**
  ```
  Authorization: Bearer <token>
  ```
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
- **Authentication:** Required (Bearer token)
- **Headers:**
  ```
  Authorization: Bearer <token>
  ```
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
- PostgreSQL
- Git

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd mindsync-auth
   ```

2. Configure application properties:
   ```bash
   # Edit src/main/resources/application.properties
   # Set up database connection, email configuration, JWT secret
   ```

3. Build the project:
   ```bash
   ./mvnw clean install
   ```

## Build and Run

### Using Maven

Build the project:
```bash
./mvnw clean package
```

Run the application:
```bash
./mvnw spring-boot:run
```

### Using Docker

Build Docker image:
```bash
docker build -t mindsync-auth .
```

Run Docker container:
```bash
docker run -p 8080:8080 mindsync-auth
```

### Default Port

The application runs on `http://localhost:8080` by default.

## CORS Configuration

The application supports CORS requests from the following origins:
- http://165.22.63.100
- http://139.59.109.5
- http://165.22.246.95
- http://localhost:3000
- http://localhost:5173
- http://localhost:8080
- http://localhost:8081
- http://127.0.0.1:3000
- http://127.0.0.1:5173
- http://127.0.0.1:8080
- http://127.0.0.1:8081
