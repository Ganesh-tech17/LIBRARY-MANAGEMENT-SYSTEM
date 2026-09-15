# Library Management System

A complete Spring Boot REST API for managing a library: books, authors, categories,
members, borrowing/returns, and fines.

**Stack:** Spring Boot 3.3 · Spring Web · Spring Data JPA / Hibernate · MySQL 8 ·
Jakarta Bean Validation · Lombok · springdoc-openapi (Swagger UI)

---

## Project Structure

```
src/main/java/com/library/management/
├── controller/      REST endpoints (request/response handling)
├── service/         Business logic and transaction boundaries
├── repository/      Spring Data JPA repositories + custom queries
├── entity/          JPA entities and relationships
├── dto/
│   ├── request/     Validated request payloads
│   └── response/    Response payloads
├── mapper/          Entity <-> DTO conversion
├── exception/       Custom exceptions + @RestControllerAdvice
└── config/          OpenAPI, CORS and other configuration
```

## Domain Model

| Entity        | Description                                              |
|---------------|-----------------------------------------------------------|
| `Author`      | Book author                                                |
| `Category`    | Book genre/category                                        |
| `Book`        | A catalog title with copies available to borrow            |
| `Member`      | A library member who can borrow books                      |
| `BorrowRecord`| A single borrow transaction (issue → return)                |
| `Fine`        | A late-return fine tied 1:1 to a `BorrowRecord`             |

**Relationships**
- `Category` → `Book` (one-to-many)
- `Author` → `Book` (one-to-many)
- `Member` → `BorrowRecord` (one-to-many)
- `Book` → `BorrowRecord` (one-to-many)
- `BorrowRecord` → `Fine` (one-to-one)

---

## Getting Started

### 1. Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8 running locally (or update the datasource URL)

### 2. Configure the database
Edit `src/main/resources/application.properties` if your MySQL credentials differ
from the defaults (`root` / `root`). The database `library_db` is created
automatically on first run (`createDatabaseIfNotExist=true`), and tables are
created/updated automatically via `spring.jpa.hibernate.ddl-auto=update`.

### 3. Run the app
```bash
mvn spring-boot:run
```
The API starts on **http://localhost:8080**.

### 4. Explore the API
Swagger UI: **http://localhost:8080/swagger-ui.html**
OpenAPI JSON: **http://localhost:8080/api-docs**

---

## REST API Reference

### Authors — `/api/authors`
| Method | Path         | Description       |
|--------|--------------|--------------------|
| POST   | `/`          | Create an author   |
| GET    | `/`          | List all authors   |
| GET    | `/{id}`      | Get author by id    |
| PUT    | `/{id}`      | Update author       |
| DELETE | `/{id}`      | Delete author        |

### Categories — `/api/categories`
Same CRUD pattern as Authors.

### Books — `/api/books`
| Method | Path                | Description                                     |
|--------|---------------------|--------------------------------------------------|
| POST   | `/`                 | Add a book (`authorId`, `categoryId` required)   |
| GET    | `/`                 | List books (paginated)                            |
| GET    | `/search`           | Search by `title`, `author`, `category`, `isbn`    |
| GET    | `/{id}`             | Get book by id                                      |
| PUT    | `/{id}`             | Update book                                          |
| DELETE | `/{id}`             | Delete book (blocked if copies are on loan)           |

### Members — `/api/members`
| Method | Path                | Description                     |
|--------|---------------------|-----------------------------------|
| POST   | `/`                 | Register a member                 |
| GET    | `/`                 | List members (paginated)          |
| GET    | `/{id}`             | Get member by id                    |
| PUT    | `/{id}`             | Update member                        |
| PATCH  | `/{id}/status?active=true|false` | Activate/deactivate    |
| DELETE | `/{id}`             | Delete member                          |

### Borrowing — `/api/borrow`
| Method | Path                        | Description                                             |
|--------|-----------------------------|------------------------------------------------------------|
| POST   | `/issue`                    | Issue a book — body: `{ "bookId": 1, "memberId": 1 }`       |
| POST   | `/return/{borrowRecordId}`  | Return a book; auto-calculates & stores fine if late          |
| GET    | `/{id}`                     | Get a borrow record by id                                       |
| GET    | `/{id}/fine`                | Calculate/retrieve the fine for a record                         |
| GET    | `/overdue`                  | Report of all currently overdue records                           |
| GET    | `/member/{memberId}`        | A member's borrowing history (paginated)                           |

---

## Business Rules

- A book can only be issued if `availableCopies > 0` and the member is `active`.
- Loan period defaults to **14 days** (`library.borrow.loan-period-days`).
- Late fine defaults to **5.00 per day late** (`library.borrow.fine-per-day`),
  both configurable in `application.properties`.
- Returning a book increments `availableCopies` and, if late, creates a `Fine`
  record tied to the `BorrowRecord`.
- A book cannot be deleted while any copies are currently on loan.
- Duplicate ISBNs and duplicate member emails are rejected with a `409 Conflict`.

## Sample Requests

**Create an author**
```bash
curl -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -d '{"name":"George Orwell","nationality":"British"}'
```

**Create a category**
```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Fiction","description":"Fictional works"}'
```

**Add a book**
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"1984","isbn":"9780451524935","publishedYear":1949,"totalCopies":3,"authorId":1,"categoryId":1}'
```

**Register a member**
```bash
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com","phone":"+1-555-0100"}'
```

**Issue a book**
```bash
curl -X POST http://localhost:8080/api/borrow/issue \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"memberId":1}'
```

**Return a book**
```bash
curl -X POST http://localhost:8080/api/borrow/return/1
```

**Overdue report**
```bash
curl http://localhost:8080/api/borrow/overdue
```

## Error Response Format

All errors follow a consistent shape via the global exception handler:

```json
{
  "timestamp": "2026-09-11T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/books",
  "fieldErrors": [
    { "field": "isbn", "message": "ISBN must be a valid 10-20 character ISBN" }
  ]
}
```

| Status | Cause                                                  |
|--------|----------------------------------------------------------|
| 400    | Bean validation failure / bad argument                     |
| 404    | Entity not found                                             |
| 409    | Business rule violation or DB constraint (duplicate, etc.)    |
| 500    | Unexpected server error                                         |

## Running Tests
```bash
mvn test
```
Tests run against an in-memory H2 database configured in MySQL-compatibility mode,
so no external database is needed to run the test suite.
