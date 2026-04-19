# 💰 Centralised Personal Finance Tracker

**UE23CS352B – Object Oriented Analysis & Design | Mini Project**
PES University | January – May 2026

---

## Team Members

| Name               | SRN              |
|--------------------|------------------|
| Shagal             | PES1UG23CS531    |
| Shourya Rai        | PES1UG23CS552    |
| Suyash Shewale     | PES1UG23CS545    |
| Shaurya Pratap Rana| PES1UG23CS544    |

**Semester / Section:** 6th Sem – I Section
**Faculty:** Prof. Shridevi A Sawant

---

## Problem Statement

Managing personal finances across income, expenses, budgets, and recurring payments
is fragmented and error-prone when done manually.  This project provides a
**Centralised Personal Finance Tracker** — a full-stack application (Java REST backend
+ React frontend + SQLite database) where multiple users can independently register,
log transactions, set category-wise monthly budgets, schedule recurring payments, and
export financial reports — all from a clean web interface.

---

## Key Features

1. **Multi-user Authentication** — Signup/login with SHA-256 salted passwords and
   session-based tokens (Bearer auth).
2. **Transaction Management** — Add, view, and delete income/expense transactions
   with category, description, and date fields.
3. **Category-wise Summary** — Monthly totals for income, expense, net balance, and
   a per-category expense breakdown.
4. **Budget Alerts** — Set spending limits per category per month; real-time progress
   bars and alerts when a budget is exceeded.
5. **Recurring Transactions** — Schedule Daily / Weekly / Monthly auto-transactions
   that are applied automatically when their due date arrives.
6. **Report Export** — Generate reports for any date range as a downloadable CSV file
   or an interactive JSON summary with charts.
7. **Interactive Dashboard** — Pie charts (expense breakdown) and budget progress bars
   with instant budget-exceeded alerts.

---

## Tech Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Backend    | Java 17, `com.sun.net.httpserver`   |
| Database   | SQLite via JDBC (`sqlite-jdbc`)     |
| JSON       | Google Gson                         |
| Frontend   | React 18, Vite, Recharts            |
| Build tool | Apache Maven                        |

---

## Design Patterns Used

### 1. Singleton (`DatabaseConnection`)
`DatabaseConnection.getInstance()` guarantees exactly one SQLite connection is
created for the lifetime of the server.  Using multiple connections to SQLite can
cause file-locking conflicts; the Singleton prevents that while also saving the
overhead of repeated connection setup.

### 2. Factory (`TransactionFactory`)
`TransactionFactory.create(type, …)` centralises the construction and validation
of `Transaction` objects.  Callers never call `new Transaction()` directly; they ask
the factory, which validates the type, amount, category, and date before returning a
correctly-initialised object.  Adding a new transaction type (e.g. "TRANSFER") in
the future requires a change in one place only.

### 3. Builder (`Report.Builder`)
A financial `Report` has many optional fields (username, date range, totals, category
breakdown, transaction list, format, generated timestamp).  The Builder pattern lets
`FinanceFacade.buildReport()` construct the object step-by-step without telescoping
constructors, making the intent at each step clear.

### 4. Command (`AddTransactionCommand`, `DeleteTransactionCommand`)
Each mutation of the transaction table is encapsulated as a `Command` object with
`execute()` and `undo()` methods.  This decouples the _what_ (add/delete) from the
_how_ (DAO persistence), enables future undo/redo support, and makes operations
auditable or queueable.

### 5. Proxy — Protection Proxy (`AuthProxy`)
`AuthProxy` sits in front of every HTTP handler.  Before any business logic runs,
the handler calls `authProxy.isAuthenticated(token)`.  If the session token is
invalid or missing, the proxy rejects the request with HTTP 401 — the real service
never executes.  This keeps authentication cleanly separated from business logic.

### 6. Facade (`FinanceFacade`)
All HTTP handlers go through a single `FinanceFacade` object.  The facade hides the
complexity of coordinating the Factory, DAOs, Command execution, Iterator traversal,
and Builder construction behind a simple, coarse-grained interface.  Handlers stay
thin; all orchestration lives in one place.

### 7. Iterator (`TransactionIterator`)
`TransactionIterator` traverses a `List<Transaction>` with an optional type-filter
("INCOME" or "EXPENSE").  `FinanceFacade` uses two iterators simultaneously to
compute total income and total expense in a single pass over the same list — without
exposing the underlying collection structure.

---

## Design Principles Used

### Single Responsibility Principle (SRP)
Every class has one reason to change.  `TransactionFactory` only creates transactions;
`TransactionDAO` only performs DB I/O; `AuthProxy` only checks authentication;
`FinanceFacade` only orchestrates; HTTP handlers only parse/respond.

### Open/Closed Principle (OCP)
`TransactionFactory` can support new transaction types without modifying existing
callers.  New report formats can be added to `ReportHandler` without touching the
`Report` builder or the `FinanceFacade`.

### Dependency Inversion Principle (DIP)
HTTP handlers depend on `FinanceFacade` (a high-level abstraction), not on
individual DAOs or SQL queries.  `Command` handlers depend on the `Command` interface,
not on concrete DAO implementations.

### DRY (Don't Repeat Yourself)
All CORS headers, body reading, token extraction, and error responses are in
`BaseHandler`.  All DB connection access is routed through the Singleton.

### Law of Demeter
Handlers only call `FinanceFacade` methods — they never reach through the facade to
call DAO or factory methods directly.

---

## MVC Architecture

**Yes** — the project follows MVC:

| Layer      | Components                                        |
|------------|---------------------------------------------------|
| Model      | `User`, `Transaction`, `Budget`, `RecurringTransaction`, `Report` (+ DAOs) |
| View       | React frontend (components in `frontend/src/components/`) |
| Controller | HTTP Handlers (`AuthHandler`, `TransactionHandler`, etc.) via `FinanceFacade` |

---

## Individual Contributions

| Name                | Module Worked On                                          |
|---------------------|-----------------------------------------------------------|
| Shagal              | Backend architecture, Design Patterns, FinanceFacade, DAOs |
| Shourya Rai         | React Frontend (Dashboard, Reports, Recharts integration) |
| Suyash Shewale      | Auth system (Signup/Login/Session), AuthProxy, Database schema |
| Shaurya Pratap Rana | Transactions, Budget, Recurring modules (frontend + handler) |

---

## GitHub Repository

https://github.com/](https://github.com/ShagalVerma/FinTracker

---

## How to Run

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+

### Backend
```bash
# From project root
mvn package -q
java -jar target/finance-tracker-1.0-SNAPSHOT-jar-with-dependencies.jar
# API is now live at http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# Open http://localhost:5173 in your browser
```

### API Endpoints Summary

| Method | Endpoint                    | Description                          |
|--------|-----------------------------|--------------------------------------|
| POST   | /api/auth/signup            | Register a new user                  |
| POST   | /api/auth/login             | Login, returns Bearer token          |
| POST   | /api/auth/logout            | Invalidate session                   |
| GET    | /api/transactions           | List all transactions for user       |
| POST   | /api/transactions           | Add transaction (Factory + Command)  |
| DELETE | /api/transactions/{id}      | Delete transaction (Command + undo)  |
| GET    | /api/budgets                | List budgets with live spent amount  |
| POST   | /api/budgets                | Set/update a monthly budget          |
| DELETE | /api/budgets/{id}           | Delete a budget                      |
| GET    | /api/summary?month=YYYY-MM  | Monthly summary + category breakdown |
| GET    | /api/report?from=&to=&format= | Generate report (JSON or CSV)      |
| GET    | /api/recurring              | List recurring transactions          |
| POST   | /api/recurring              | Add recurring transaction            |
| DELETE | /api/recurring/{id}         | Delete recurring transaction         |
