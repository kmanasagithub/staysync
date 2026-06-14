# StaySync – Hotel Reservation & Payment Platform

## Overview

StaySync is a full-featured hotel reservation and booking platform developed using Spring Boot. The application enables users to search hotels, manage bookings, add guests, process secure online payments, and manage hotel inventories through RESTful APIs.

The project demonstrates enterprise-grade backend development practices including JWT Authentication, Refresh Tokens, Spring Security, Stripe Payment Integration, Hibernate/JPA, Design Patterns, Global Exception Handling, DTO Mapping, and API Documentation using Swagger/OpenAPI.

---

# Features

## User Management

* User Registration
* User Login
* User Profile Management
* Role-Based Authorization
* JWT Authentication
* Refresh Token Authentication

## Hotel Management

* Create Hotel
* Update Hotel Details
* View Hotel Information
* Search Hotels by City
* Hotel Contact Information Management

## Room Management

* Create Rooms
* Update Room Details
* Manage Room Inventory
* Room Pricing Configuration

## Inventory Management

* Track Room Availability
* Update Daily Inventory
* Inventory Availability Checks
* Real-Time Room Status Management

## Booking Management

* Initialize Booking
* Add Guests to Booking
* Booking Status Tracking
* Booking Confirmation Workflow
* Guest Information Management

## Payment Processing

* Stripe Checkout Session Creation
* Secure Online Payments
* Webhook Event Handling
* Payment Status Verification
* Booking Confirmation after Successful Payment

## Dynamic Pricing Engine

The pricing engine uses the Strategy Pattern to dynamically calculate room prices.

Supported Pricing Strategies:

* Base Pricing Strategy
* Holiday Pricing Strategy
* Occupancy Pricing Strategy
* Surge Pricing Strategy
* Urgency Pricing Strategy

## Security

* Spring Security
* JWT Access Tokens
* JWT Refresh Tokens
* Stateless Authentication
* Role-Based Access Control

## API Documentation

* Swagger UI
* OpenAPI 3 Specification
* Interactive API Testing

---

# Technology Stack

## Backend

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate

## Database

* MySQL

## Authentication

* JWT (JSON Web Tokens)
* Refresh Token Mechanism

## Payment Gateway

* Stripe API

## Documentation

* Swagger UI
* OpenAPI

## Build Tool

* Maven

## Utilities

* Lombok
* ModelMapper

---

# Project Architecture

```text
Client
   │
   ▼
REST APIs
   │
   ▼
Spring Boot Application
   │
   ├── Controllers
   ├── Services
   ├── Security
   ├── Pricing Engine
   ├── Repositories
   └── Exception Handling
   │
   ▼
MySQL Database
```

---

# Project Structure

```text
com.trip.staysync
│
├── advice
│   ├── GlobalExceptionHandler
│   └── GlobalResponseHandler
│
├── config
│   ├── MapperConfig
│   └── StripeConfig
│
├── controller
│
├── dto
│
├── entity
│
├── exception
│
├── repository
│
├── security
│   ├── JWTAuthFilter
│   ├── JwtService
│   ├── AuthService
│   └── WebSecurityConfig
│
├── service
│
├── strategies
│   ├── PricingStrategy
│   ├── BasePricingStrategy
│   ├── HolidayPricingStrategy
│   ├── OccupancyPricingStrategy
│   ├── SurgePricingStrategy
│   └── UrgencyPricingStrategy
│
├── util
│
└── StaySyncApplication
```

---

# Design Patterns Used

## Strategy Pattern

Dynamic room pricing is implemented using the Strategy Pattern.

Pricing Strategies:

* BasePricingStrategy
* HolidayPricingStrategy
* OccupancyPricingStrategy
* SurgePricingStrategy
* UrgencyPricingStrategy

Benefits:

* Open for Extension
* Closed for Modification
* Easily Add New Pricing Rules
* Cleaner Business Logic

---

# Authentication Flow

## Signup

```http
POST /api/v1/auth/signup
```

Creates a new user account.

## Login

```http
POST /api/v1/auth/login
```

Returns:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token"
}
```

## Refresh Access Token

```http
POST /api/v1/auth/refresh
```

Generates a new access token using a valid refresh token.

---

# Token Configuration

| Token         | Expiry     |
| ------------- | ---------- |
| Access Token  | 15 Minutes |
| Refresh Token | 7 Days     |

---

# Booking Workflow

```text
Search Hotel
      │
      ▼
Initialize Booking
      │
      ▼
Add Guests
      │
      ▼
Create Stripe Checkout Session
      │
      ▼
Complete Payment
      │
      ▼
Stripe Webhook Event
      │
      ▼
Booking Confirmed
```

---

# API Endpoints

## Authentication APIs

```http
POST /api/v1/auth/signup
POST /api/v1/auth/login
POST /api/v1/auth/refresh
```

## User APIs

```http
GET /api/v1/users/profile
PUT /api/v1/users/profile
```

## Hotel APIs

```http
POST /api/v1/hotels
GET  /api/v1/hotels
GET  /api/v1/hotels/{id}
PUT  /api/v1/hotels/{id}
```

## Room APIs

```http
POST /api/v1/rooms
PUT  /api/v1/rooms/{id}
GET  /api/v1/rooms/{id}
```

## Inventory APIs

```http
POST /api/v1/inventory
PUT  /api/v1/inventory
```

## Booking APIs

```http
POST /api/v1/bookings/init
POST /api/v1/bookings/{bookingId}/addGuests
POST /api/v1/bookings/{bookingId}/payments
```

## Stripe Webhook

```http
POST /api/v1/webhooks/stripe
```

---

# API Documentation

## 🔍 Swagger UI

Available after starting the application:

```text
{BASE_URL}/swagger-ui/index.html
```

## 📄 OpenAPI Specification

```text
{BASE_URL}/v3/api-docs
```

Replace {BASE_URL} with your application's host, port, and context path.
---

# Stripe Integration

StaySync integrates with Stripe Checkout for secure payment processing.

## Features

* Checkout Session Creation
* Secure Payment Processing
* Webhook Event Handling
* Booking Confirmation After Payment

## Local Webhook Testing

Install Stripe CLI:

```bash
stripe login
```

Forward Stripe events:

```bash
stripe listen --forward-to localhost:8080/api/v1/webhooks/stripe
```

Copy the generated webhook secret and configure:

```properties
stripe.webhook.event=YOUR_STRIPE_WEBHOOK_SECRET
```

Stripe Documentation:

https://docs.stripe.com/

---

# Environment Configuration

Create:

```text
application-dev.properties
```

Example:

```properties
spring.datasource.url=YOUR_DB_URL
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

jwt.secretKey=YOUR_JWT_SECRET_KEY

stripe.secret.key=YOUR_STRIPE_SECRET_KEY
stripe.webhook.event=YOUR_STRIPE_WEBHOOK_SECRET
```

Sensitive credentials are intentionally excluded from the repository.

---

# Running the Application

## Clone Repository

```bash
git clone https://github.com/kmanasagithub/staysync.git
```

## Navigate to Project

```bash
cd staysync
```

## Build

```bash
mvn clean install
```

## Run

```bash
mvn spring-boot:run
```

# Key Learning Outcomes

* Spring Boot Development
* REST API Design
* Spring Security
* JWT Authentication
* Refresh Token Mechanism
* Hibernate/JPA
* Stripe Payment Integration
* Swagger/OpenAPI Documentation
* Strategy Design Pattern
* Exception Handling
* MySQL Database Design
* Enterprise Backend Architecture

---

# Author

**Manasa Kurella**

Backend Developer | Java | Spring Boot | Spring Security | Hibernate | MySQL | Stripe API
