# Shopping Cart Line Item Manager — Full Stack

A production-quality full-stack shopping cart application: **Spring Boot 3 + PostgreSQL** backend with JWT auth, and a **React 18 + Vite + Tailwind** frontend.

## Features

**Catalog & Search** — predefined multi-category catalog, search by name/SKU, filter by category, sort by name/price/stock/category, recently-viewed tracking.

**Cart** — SKU-based quantity merging, inline quantity update, remove, clear, sort cart view, **undo last cart operation** (Stack/Command pattern, per-user, server-side).

**Checkout** — configurable GST, percentage/flat discount coupons, delivery charge rule (free above threshold), UPI/Card/Cash payment simulation, auto-generated invoice numbers (`INV-YYYYMMDD-000123`), stock deduction, invoice history.

**Wishlist** — add/remove/move-to-cart.

**Admin Panel** — product & inventory CRUD, low-stock alerts, coupon management, GST rate configuration, sales analytics (revenue, order count, AOV, top-selling products).

**Invoice Export** — TXT and CSV download per order, generated server-side.

**Auth** — JWT-based registration/login, `CUSTOMER` / `ADMIN` roles, role-gated routes on both API and frontend.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.3 (Web, Data JPA, Security), Maven |
| Auth | JWT (jjwt), BCrypt password hashing |
| Database | PostgreSQL 16 (Docker) |
| Frontend | React 18, Vite, React Router, Axios, Tailwind CSS |

## Architecture

```
React SPA (5173)  ──HTTP/JSON+JWT──>  Spring Boot API (8080)  ──JDBC──>  PostgreSQL (5432, Docker)
```

Backend layers: `controller → service → repository → entity`, with `dto`/`mapper` keeping entities off the wire, `security` for JWT + Spring Security, `exception` for a centralized `@RestControllerAdvice`, and `service/pricing` holding a `DiscountStrategy` interface (percentage/flat) for Open/Closed-friendly coupon math.

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+ / npm
- Docker (for PostgreSQL)

## Setup & Run

### 1. Start PostgreSQL

```bash
docker compose up -d postgres
```

This starts Postgres on `localhost:5432` with database `shoppingcart` / user `shoppingcart` / password `shoppingcart` (see `docker-compose.yml`).

### 2. Start the backend

```bash
cd backend
mvn spring-boot:run
```

Runs on `http://localhost:8080`. On first boot, `DataSeeder` populates:
- An admin account: **admin@shoppingcart.local / Admin@123**
- 24 starter products across 8 categories
- 3 coupons: `SAVE10` (10%), `FLAT100` (₹100 flat), `WELCOME20` (20%)
- Default GST rate: 18%

Config lives in `backend/src/main/resources/application.yml` (JWT secret, delivery charge, free-delivery threshold, DB connection — all overridable via env vars).

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173`. API base URL is set via `frontend/.env` (`VITE_API_BASE_URL`).

### 4. Use the app

- Register a new customer account, or log in as the seeded admin to access `/admin`.
- Browse the catalog, add items to cart, apply a coupon at checkout, place an order, then view/export the invoice from Order History.

## API Overview

```
POST   /api/auth/register              POST /api/auth/login
GET    /api/products                   GET  /api/products/search?q=&category=&sortBy=
GET    /api/products/{sku}             POST /api/products/{sku}/view   GET /api/products/recently-viewed
GET    /api/cart?sortBy=               POST /api/cart/items            PUT/DELETE /api/cart/items/{sku}
DELETE /api/cart                       POST /api/cart/undo
GET    /api/wishlist                   POST/DELETE /api/wishlist/{sku}
GET    /api/coupons/validate?code=&subtotal=
POST   /api/orders/checkout            GET  /api/orders                GET /api/orders/{invoiceNumber}
GET    /api/orders/{invoiceNumber}/export?format=csv|txt

# Admin (ROLE_ADMIN required)
GET/POST/PUT/DELETE  /api/admin/products[/{sku}]      PATCH /api/admin/products/{sku}/stock
GET    /api/admin/products/low-stock?threshold=
GET/POST/PUT/DELETE  /api/admin/coupons[/{code}]
GET/PUT /api/admin/gst
GET    /api/admin/analytics
```

## Project Structure

```
SHOPPINGCART/
├── docker-compose.yml
├── backend/
│   └── src/main/java/com/shoppingcart/
│       ├── config/        (SecurityConfig, DataSeeder)
│       ├── security/      (JWT filter/service, UserDetails)
│       ├── controller/    (REST endpoints)
│       ├── service/       (interfaces + impl/, pricing/ discount strategies)
│       ├── repository/    (Spring Data JPA)
│       ├── entity/        (JPA entities + enums)
│       ├── dto/            (request/, response/)
│       ├── mapper/        (entity ↔ DTO)
│       └── exception/     (custom exceptions + GlobalExceptionHandler)
└── frontend/
    └── src/
        ├── api/            (axios client + endpoint modules)
        ├── context/        (Auth, Cart, Notification)
        ├── components/     (Navbar, ProductCard, CartTable, InvoiceView, ...)
        ├── pages/          (Catalog, Cart, Checkout, Orders, Wishlist, admin/)
        └── routes/         (ProtectedRoute, AdminRoute)
```

## Notes

- Cart data is persisted per-user in Postgres (survives logout/refresh); the cart **undo stack** is kept server-side in memory per user (cleared after checkout), implementing the Stack/Command pattern from the original spec.
- Product categories are a fixed enum (`ELECTRONICS`, `GROCERY`, `FASHION`, `HOME_APPLIANCES`, `BOOKS`, `TOYS`, `BEAUTY`, `SPORTS`) rather than an editable table, keeping catalog management simple.
- `spring.jpa.open-in-view` is disabled; read-only service methods that touch lazy associations are explicitly `@Transactional(readOnly = true)`, and order queries use `JOIN FETCH` to avoid N+1s / `LazyInitializationException`.
