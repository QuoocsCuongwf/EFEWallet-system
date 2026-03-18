# EFEWallet System

A microservice-based E-Wallet system built with **Spring Boot**, designed for secure and scalable digital payment processing.

---

## Overview

EFEWallet is a backend system that supports:

- Wallet-to-wallet transfer
- Secure authentication using JWT
- Idempotent transaction handling (prevent duplicate requests)
- Microservice architecture
- Event-driven communication using RabbitMQ

---

## Architecture

```mermaid
graph TD
    Client["Client"]
    Gateway["API Gateway\n(Authentication · JWT)"]
    Wallet["Wallet Service"]
    Transaction["Transaction Service"]
    Notification["Notification Service\n(optional)"]
    DB["PostgreSQL"]
    MQ["RabbitMQ\n(Message Queue)"]

    Client --> Gateway
    Gateway --> Wallet
    Gateway --> Transaction
    Gateway --> Notification
    Wallet --> DB
    Transaction --> DB
    Transaction --> MQ
    MQ --> Notification
```

---

## Request Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant G as API Gateway
    participant T as Transaction Service
    participant W as Wallet Service
    participant DB as PostgreSQL
    participant MQ as RabbitMQ

    C->>G: POST /api/v1/transfer + JWT + Idempotency-Key
    G->>G: Validate JWT, extract user identity
    G->>T: Forward request
    T->>DB: Check Idempotency-Key (scoped to user)

    alt Key already exists
        DB-->>T: Return stored response
        T-->>C: 200 OK (cached response)
    else New request
        T->>W: Validate balance
        W->>DB: Check wallet balance
        DB-->>W: Balance sufficient
        W-->>T: Approved
        T->>DB: Begin DB transaction (ACID)
        T->>DB: Save transaction record (PENDING)
        T->>DB: Debit source wallet
        T->>DB: Credit destination wallet
        T->>DB: Update status (SUCCESS)
        T->>DB: Save Idempotency-Key + response
        T->>DB: Commit transaction
        T->>MQ: Publish TransactionCompleted event
        MQ-->>Notification: Notify user (email/push)
        T-->>C: 200 OK (transactionId, amount)
    end

    alt Error / Insufficient balance
        T->>DB: Rollback transaction
        T->>DB: Update status (FAILED)
        T-->>C: 400 Bad Request
    end
```

---

## Database Design

```mermaid
erDiagram
    WALLET {
        uuid id PK
        uuid user_id
        decimal balance
        string currency
    }

    TRANSACTION {
        uuid id PK
        uuid from_wallet_id FK
        uuid to_wallet_id FK
        decimal amount
        string status
        timestamp created_at
    }

    IDEMPOTENCY_KEY {
        uuid id PK
        uuid user_id FK
        string idempotency_key
        string request_hash
        string status
        text response_body
        timestamp created_at
    }

    WALLET ||--o{ TRANSACTION : "sends"
    WALLET ||--o{ TRANSACTION : "receives"
    WALLET ||--o{ IDEMPOTENCY_KEY : "scoped to user"
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot (Java 17) |
| Database | PostgreSQL |
| ORM | JPA / Hibernate |
| Messaging | RabbitMQ |
| Security | Spring Security + JWT |
| Build Tool | Maven |
| Containerization | Docker |

---

## Core Features

### 1. Transfer Money

- Transfer between wallets
- Validate balance before transaction
- Ensure atomic transaction (ACID)

### 2. Transaction Handling

- Uses database transaction to ensure atomicity
- Debit and credit operations are executed within a single DB transaction
- Rollback is triggered automatically on any failure
- Isolation level: `READ COMMITTED` to prevent dirty reads

### 3. Idempotency

```mermaid
flowchart LR
    A["Client sends request\n+ Idempotency-Key"] --> B{Key exists\nfor this user?}
    B -- Yes --> C["Return stored\nresponse"]
    B -- No --> D["Process\ntransaction"]
    D --> E["Save key + response\nscoped to user"]
    E --> F["Return new\nresponse"]
```

- `Idempotency-Key` is scoped per user — the same key from different users is treated as distinct
- Key is stored alongside a hash of the request to prevent misuse (same key, different payload = rejected)
- Guarantees exactly-once processing for retried requests

### 4. Authentication

- JWT-based authentication via API Gateway
- User identity is extracted from the token server-side
- All APIs are secured — unauthenticated requests are rejected at the gateway

### 5. Transaction Status

```mermaid
stateDiagram-v2
    [*] --> PENDING : Request received
    PENDING --> SUCCESS : Debit + credit committed
    PENDING --> FAILED : Error or insufficient balance
    SUCCESS --> [*]
    FAILED --> [*]
```

### 6. Event-driven Communication

- After a successful transaction, Transaction Service publishes a `TransactionCompleted` event to RabbitMQ
- Notification Service consumes this event to send user notifications (email, push)
- Designed for extensibility: analytics, audit logging, and fraud detection can subscribe independently

---

## API Design

### Transfer

```
POST /api/v1/transfer
```

**Headers:**

```
Authorization: Bearer <token>
Idempotency-Key: <uuid>
```

**Request Body:**

```json
{
  "toWalletId": "wallet_123",
  "amount": 200000,
  "currency": "VND",
  "description": "Transfer money"
}
```

> **Note:** `fromWalletId` is intentionally absent from the request body.
> The source wallet is derived from the authenticated user's JWT token — clients cannot control the source wallet.

**Response:**

```json
{
  "status": "SUCCESS",
  "data": {
    "transactionId": "txn_123",
    "amount": 200000
  }
}
```

---

## Security

- All endpoints require valid JWT authentication
- Source wallet is resolved from the authenticated user — not from client input
- Idempotency keys are scoped per user to prevent cross-user replay attacks

---

## Error Handling

Centralized using `@RestControllerAdvice`. Standard error response format:

```json
{
  "error": "INSUFFICIENT_BALANCE",
  "message": "Not enough balance",
  "status": 400
}
```

---

## Getting Started

**Run with Docker:**

```bash
docker compose up --build
```

**Run locally:**

```bash
mvn clean install
mvn spring-boot:run
```

---

## Future Improvements

- Saga pattern for distributed transactions
- Anti race condition: optimistic locking with `@Version` / pessimistic locking
- Retry mechanism with dead-letter queue (DLQ) for failed events
- Ledger with double-entry accounting
- Monitoring: Prometheus + Grafana
- API Gateway: Spring Cloud Gateway
- Integration testing

---

## Author

GitHub: [QuoocsCuongwf](https://github.com/QuoocsCuongwf)

---

## License

This project is for educational and research purposes.
