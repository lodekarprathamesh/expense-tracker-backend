# Expense Tracker Backend — Phase 1 + Phase 2

Spring Boot 3 + PostgreSQL backend for the expense tracker app.
Phase 1: entities, repositories, services, controllers, Flyway migrations.
Phase 2: JWT authentication — register/login, and every `/api/users/{userId}/**`
endpoint now requires a valid token belonging to that exact user.

## Prerequisites
- Java 17+
- Maven (or your IDE's bundled Maven)
- A running local PostgreSQL instance with a database named `expense_tracker`

## Run it

1. Make sure Postgres is running and `expense_tracker` database exists.
2. Run the app (IntelliJ: run `ExpenseTrackerApplication`, or via terminal: `mvn spring-boot:run`).
3. Flyway creates the schema and seeds default categories automatically on first boot.

The app starts on `http://localhost:8080`.

## Auth flow (PowerShell examples)

### 1. Register
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"email": "you@example.com", "password": "supersecret123", "displayName": "Prathamesh"}'
```
Response includes a `token` — copy it.

### 2. Login (once already registered)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email": "you@example.com", "password": "supersecret123"}'
```

### 3. Call a protected endpoint with the token
```powershell
$token = "PASTE_YOUR_TOKEN_HERE"
Invoke-RestMethod -Uri "http://localhost:8080/api/users/1/transactions" -Method Get -Headers @{ Authorization = "Bearer $token" }
```

Without the `Authorization` header (or with a token belonging to a different user id
in the URL), you'll get a `401` or `403` instead of data.

## What's next
- **Phase 3**: Android app skeleton (Kotlin + Retrofit) — login screen, token storage, transaction list
- **Phase 4**: `NotificationListenerService` + parsing → posts to `/auto-capture`
- **Phase 5**: Categorization UI, spend insights
- **Phase 6**: Python microservice for AI-based categorization
