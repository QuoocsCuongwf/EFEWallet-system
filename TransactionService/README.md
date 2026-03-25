# EFEWallet Transaction Service

Production-grade financial transaction processing system supporting transfer, deposit, and withdrawal operations with full ACID guarantees, idempotency, and concurrency safety.

---

## Table of Contents

1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Business Logic](#business-logic)
4. [Technical Guarantees](#technical-guarantees)
5. [Idempotency Implementation](#idempotency-implementation)
6. [Transaction Flow](#transaction-flow)
7. [Rollback Strategy](#rollback-strategy)
8. [Performance and Scaling](#performance-and-scaling)
9. [Security](#security)
10. [Testing](#testing)
11. [Benchmark and Stress Test](#benchmark-and-stress-test)
12. [Common Pitfalls](#common-pitfalls)
13. [Tech Stack](#tech-stack)
14. [Architecture Levels](#architecture-levels)
15. [Future Improvements](#future-improvements)
16. [Conclusion](#conclusion)

---

## Overview

EFEWallet Transaction Service is a financial-grade backend system designed around four non-negotiable guarantees:

- No money is ever lost due to system failure or concurrent access
- No transaction is ever processed more than once
- Concurrent requests cannot corrupt wallet balances
- The service scales horizontally without sacrificing data consistency

---

## System Architecture

```
                    CLIENT LAYER
          Mobile App / Web App / Third-party API
                          |
                          | HTTPS + JWT + Idempotency-Key
                          v
              API GATEWAY / LOAD BALANCER
           Rate Limiting  |  Auth  |  Routing
                  |                    |
         Service Node 1        Service Node 2
           (Stateless)           (Stateless)
                  |                    |
                  +--------+  +--------+
                           |  |
                      REDIS CLUSTER
              Idempotency Key Store (TTL 24h)
                           |
                     KAFKA CLUSTER
              Topic: wallet.transaction.events
              Partitioned by user_id
                           |
                   WORKER POOL
          Consume -> Lock -> Debit/Credit -> Save
                           |
               POSTGRESQL PRIMARY + REPLICA
               wallets table | transactions table
```

The service is intentionally stateless. No in-memory session or balance state is kept on any service node. All shared mutable state lives in PostgreSQL. All ephemeral lookup state lives in Redis. This design allows any number of service nodes to be added or removed without coordination.

### Request Lifecycle

A transfer request passes through the following layers in order:

1. JWT authentication at the gateway
2. Idempotency key extraction and Redis lookup
3. Input validation (amount, receiver, self-transfer check)
4. Mark request as PROCESSING in Redis
5. Begin database transaction
6. Acquire row-level locks on both wallets
7. Assert sender balance is sufficient
8. Deduct sender balance, add to receiver balance
9. Insert transaction record with status SUCCESS
10. Commit database transaction
11. Update Redis entry to SUCCESS with cached response
12. Publish event to Kafka for audit and notifications
13. Return HTTP 200 to client

If any step between 5 and 10 fails, PostgreSQL rolls back the entire unit. Redis is cleaned so the client can safely retry.

---

## Business Logic

### Request Validation

Every request must pass three checks before any database operation begins:

- The amount must be a positive number greater than zero. Zero and negative values are rejected immediately.
- The receiver must exist in the system. Transferring to a non-existent user is rejected.
- The sender and receiver must be different users. Self-transfers are rejected.

These validations happen before any lock is acquired and before any transaction opens, keeping the critical section as short as possible.

### Balance Check

The sender balance check occurs inside the database transaction, after the wallet row lock has been acquired. Checking before the lock would introduce a time-of-check to time-of-use race condition where two concurrent requests both pass the balance check but collectively overdraw the wallet.

### Transaction State Machine

A transaction begins in PENDING state when first created. It moves to SUCCESS after the database transaction commits, or to FAILED if any step in the processing chain throws an exception. State transitions are strictly one-way. A FAILED or SUCCESS transaction record is never mutated. Any retry creates a new transaction record.

```
PENDING  -->  SUCCESS
         -->  FAILED
```

### Atomic Transfer

A transfer involves three writes that must succeed together or not at all:

- Deducting the amount from the sender wallet
- Adding the amount to the receiver wallet
- Inserting the transaction record

If any one of these writes fails, the database transaction rolls back all three. The system never reaches a state where the sender has been debited but the receiver has not been credited.

### Audit Logging

Every transaction record stores the sender, receiver, amount, currency, timestamps for creation and completion, the final status, the idempotency key used, and a hash of the original request payload. This supports both operational debugging and financial reconciliation.

---

## Technical Guarantees

### Idempotency

Idempotency prevents duplicate processing caused by double-clicks, client-side timeouts that trigger retries, or network failures where the client cannot determine if the original request succeeded.

The client generates a unique key per logical operation and sends it as a request header. The server stores the outcome of each key and returns the cached result on any repeat request with the same key, without re-executing the business logic.

The server distinguishes three states for any given key. If the key is not found, the request is new and proceeds normally. If the key is found with status SUCCESS, the cached response is returned immediately without touching the database. If the key is found with status PROCESSING, the request is still in-flight and a conflict response is returned so the client waits before retrying.

The idempotency store has a TTL of 24 hours. After expiry, the key is no longer recognized and the same key would be treated as a new request. Clients should generate a new key for each new logical operation.

### Database Transactions (ACID)

All balance mutations and transaction record inserts are wrapped in a single database transaction with READ COMMITTED isolation. This ensures that no partial write is ever visible to other transactions, that balance constraints are always enforced at commit time, and that committed data persists even if the service crashes immediately after commit.

### Concurrency Control

The core concurrency problem is the lost update: two requests both read the same balance, both pass the check, and both apply their deduction, resulting in less money being removed than intended or a negative balance.

The solution is a pessimistic write lock acquired on the wallet row at the start of the transaction. No other transaction can read or write that row until the lock is released at commit. This serializes all updates to a given wallet.

To prevent deadlocks between two transfers that involve the same pair of wallets in opposite directions, locks are always acquired in a deterministic order based on the user identifier. Both transfers lock the same wallet first, so neither can hold one lock and wait for the other indefinitely.

Optimistic locking using a version column is an alternative for lower-contention scenarios. It does not block readers, but requires retry logic because concurrent writers will receive a version conflict and must re-attempt the entire operation from the beginning.

### Unique Constraint

A database-level unique constraint on the combination of idempotency key and user identifier serves as a final defense layer. If the application-level idempotency check is bypassed due to a bug or race condition, the database will reject the duplicate insert rather than creating a duplicate record.

### Retry Mechanism

Retries apply only to transient infrastructure failures such as deadlocks and temporary database unavailability. Business logic failures such as insufficient balance or invalid receiver are not retried, as the outcome would not change. Retries use exponential backoff to avoid thundering herd behavior during a recovery period.

### Message Queue

For systems requiring high throughput, the synchronous path where the API waits for the full database transaction to complete before responding can be replaced with an asynchronous model. The API layer validates the request, saves a PENDING record, and places a message on a Kafka topic. A separate worker pool consumes messages, performs locking and balance mutation, and updates the transaction to SUCCESS. The API returns PENDING immediately, and the client polls or receives a webhook when processing completes.

Kafka partitions messages by user identifier, guaranteeing that a single user's transactions are always processed sequentially within one partition, preserving ordering without global locking.

### Consistency Strategy

Balance-affecting operations always use strong consistency. The sender must see the exact current balance before any deduction, and the deduction must be immediately visible to all subsequent reads. No caching or eventual consistency is acceptable for balance writes.

Downstream systems such as audit logs, notification services, and analytics can use eventual consistency. These systems read from Kafka events and may be slightly behind the source of truth, which is acceptable because they do not participate in the approval decision for a transfer.

### Error Handling

Exceptions must never be caught and silently discarded inside a transaction. A swallowed exception allows the transaction to continue and eventually commit in a broken state. Every exception caught inside a transactional method must be re-thrown so that Spring's transaction manager can issue a rollback before the database session is released.

---

## Idempotency Implementation

### Storage

Idempotency records are stored in Redis with a 24-hour TTL. Each record contains the idempotency key, user identifier, request payload hash, current status, and the serialized response body. The key structure in Redis namespaces records per user to prevent collisions across different users who might generate the same UUID independently.

The payload hash is used to detect cases where a client reuses the same idempotency key with a different request body. This is treated as an error because the key is supposed to uniquely represent one specific logical operation.

### Annotation

A custom annotation is placed on service methods that require idempotency protection. The annotation declares the transaction type and whether payload hash validation should be enforced. This separates idempotency concerns from business logic entirely.

### AOP Interceptor

An Aspect-Oriented Programming interceptor wraps each annotated method. Before executing, it reads the idempotency key from the request context, hashes the payload, and checks Redis. If a cached result exists, it is returned immediately. If a PROCESSING entry exists, a conflict is returned. If nothing is found, the request proceeds and the interceptor marks it as PROCESSING before delegating to the business method.

After successful execution, the interceptor updates the Redis entry to SUCCESS and stores the serialized response. If the method throws, the interceptor clears the PROCESSING entry so the client can retry safely.

This design means that business logic methods have no knowledge of idempotency mechanics. They receive a validated request and focus only on the transfer logic.

---

## Transaction Flow

The following sequence represents the production-safe execution order for a transfer:

| Step | Action | Component |
|------|--------|-----------|
| 1 | Receive HTTP request | API Controller |
| 2 | Authenticate JWT, extract userId from token | Security Filter |
| 3 | Extract Idempotency-Key from request header | AOP Interceptor |
| 4 | Validate input: amount, receiverId, self-transfer | Validation Layer |
| 5 | Check idempotency key in Redis | Redis (O(1) lookup) |
| 6 | Save PROCESSING status to Redis | Redis |
| 7 | Begin database transaction | Spring / JDBC |
| 8 | Acquire pessimistic write lock on first wallet | PostgreSQL |
| 9 | Acquire pessimistic write lock on second wallet | PostgreSQL |
| 10 | Assert sender balance is greater than or equal to amount | Domain Logic |
| 11 | Deduct sender balance | Wallet Entity |
| 12 | Add to receiver balance | Wallet Entity |
| 13 | Insert transaction record with status SUCCESS | Transaction Repository |
| 14 | Commit | PostgreSQL |
| 15 | Update Redis entry to SUCCESS with response body | Redis |
| 16 | Publish TransferEvent to Kafka | Kafka Producer |
| 17 | Return HTTP 200 response | API Controller |

Steps 7 through 14 form the critical section. Any failure in this range triggers a full rollback of all balance and transaction writes.

---

## Rollback Strategy

### Automatic Rollback

Spring's transaction manager automatically rolls back on any unchecked exception. For checked exceptions, rollback must be declared explicitly in the transaction annotation. The recommendation is to always declare rollback for all Exception types so that no domain exception accidentally escapes without triggering a rollback.

### Failure Scenarios

| Failure Point | Database Outcome | Client Outcome |
|---|---|---|
| Balance check fails | Nothing written, clean rollback | 422 Unprocessable Entity |
| DB write fails mid-transaction | All writes in the unit rolled back | 500, safe to retry with same key |
| Kafka publish fails after commit | DB is consistent, event not published | Worker retry via dead letter queue |
| Redis update fails after DB commit | DB is consistent, key not cached | Client may timeout; retry is safe because DB unique constraint prevents duplicate |
| Service crash after commit | DB is consistent | Client retries; idempotency key not in Redis but DB constraint prevents duplicate |

### The Rule on Exception Handling

Inside any transactional method, an exception must always propagate upward. Catching it to log and then silently continuing causes the transaction to commit in whatever partial state it reached before the exception. This is the most dangerous failure mode in financial systems because it is often not detected until reconciliation reveals missing or extra funds.

---

## Performance and Scaling

### Redis as Idempotency Store

Storing idempotency records in Redis rather than PostgreSQL has a significant performance advantage. A Redis lookup is O(1) and typically completes in under one millisecond. A duplicate request such as a double-click is resolved without opening a database connection or acquiring any lock. Under high retry traffic, this protects the database from unnecessary load.

Keys are set with a 24-hour TTL so that Redis memory usage is bounded and does not grow indefinitely with historical records.

### Database Indexes

Every query path in the transfer flow must be backed by an index. The wallet lookup by user identifier, the idempotency key lookup by key and user, and the transaction history queries by sender and receiver each require dedicated indexes. Missing indexes cause sequential table scans that degrade severely under load.

### Connection Pool Sizing

The database connection pool should be sized proportionally to the number of CPU cores available to the database server. A common starting point is two connections per core. Oversizing the pool leads to excessive context switching inside PostgreSQL. Undersizing causes connection wait time that inflates request latency across the board.

### Horizontal Scaling

Adding service nodes has no coordination cost because the service carries no in-memory state. A new node connects to the same PostgreSQL primary and the same Redis cluster and begins handling traffic immediately. The load balancer distributes requests across nodes without stickiness requirements.

Kafka partitioning by user identifier ensures that even when multiple worker nodes consume from the same topic, all events for a single user are processed by the same worker in sequence.

---

## Security

### Authentication

All endpoints require a valid JWT Bearer token in the Authorization header. The authenticated user identifier is extracted exclusively from the token payload. The request body may contain a user identifier for informational purposes, but the system never trusts client-supplied identifiers for authorization decisions.

### Input Validation

All request fields are validated before any business logic executes. The amount must be a positive decimal within an acceptable range. The receiver identifier must be a non-empty string of bounded length. The currency must match a supported ISO 4217 code. Requests that fail validation are rejected with a 422 status and never reach the database.

### Principle of Least Trust

The service does not trust any value that originates from the client for security decisions. The sender identifier is always taken from the authenticated JWT subject. The service verifies that the authenticated user owns the wallet being debited before any lock is acquired.

---

## Testing

### Unit Tests

Unit tests validate individual business rules in isolation. Each test covers a single condition: an amount of zero is rejected, a transfer to a non-existent user is rejected, a self-transfer is rejected, a balance below the transfer amount throws the correct exception. These tests run without a database and complete in milliseconds.

### Integration Tests

Integration tests verify the full stack behavior including actual database transactions. The most important integration test is the rollback test: inject a failure at a specific point in the transfer flow and assert that both wallet balances are unchanged after the exception is thrown. This confirms that the transaction annotation is correctly configured and that no exception is being swallowed.

A second important integration test verifies idempotency behavior: send the same request twice with the same idempotency key and assert that exactly one transaction record exists, exactly one deduction occurred, and the second response is identical to the first.

### Concurrency Tests

Concurrency tests simulate the double-spend scenario using multiple threads. The test creates a sender with a fixed balance, launches a high number of concurrent transfer requests each for an amount that would overdraw the wallet if all succeeded, and then asserts that the total amount debited equals the sender's original balance and that the combined balance across both wallets is unchanged. This test must pass consistently across many runs under different threading conditions.

---

## Benchmark and Stress Test

### Test Environment

A representative stress test environment for a target of 10,000 requests per second uses two service nodes with four CPU cores and eight gigabytes of RAM each, a PostgreSQL primary with eight cores and thirty-two gigabytes of RAM on NVMe storage, and a three-node Redis cluster.

### Test Tool

k6 is the recommended load testing tool. It supports staged load profiles covering ramp-up, sustained peak, and ramp-down phases. It allows threshold assertions on latency percentiles and error rates, and supports parameterization for different endpoints and authentication tokens.

A representative test scenario ramps to one thousand concurrent users over one minute, sustains ten thousand requests per second for three minutes, and then ramps down. Thresholds should require that the ninety-ninth percentile response time stays below five hundred milliseconds and that the error rate remains below 0.1 percent.

### Expected Results

| Metric | Target |
|---|---|
| Throughput | 10,000 requests per second |
| p50 latency | Below 50ms |
| p95 latency | Below 200ms |
| p99 latency | Below 500ms |
| Error rate | Below 0.1% |
| Duplicate debit rate | Zero |

Duplicate requests resolved from Redis cache complete significantly faster than fresh requests because they skip the entire database path. Realistic traffic with some retry behavior will therefore show better average latency than a purely synthetic workload of unique requests.

### Tuning for Higher Throughput

When throughput targets exceed what the synchronous database path can sustain, the following changes improve capacity in order of impact. Increasing the database connection pool size up to the saturation point of the database server removes connection wait time. Moving to a Redis-based distributed lock replaces database row locking with a faster in-memory alternative, reducing lock hold time. Introducing Kafka workers with asynchronous settlement decouples API throughput from database write throughput entirely. Adding read replicas and routing balance queries to replicas reduces load on the primary. Caching wallet balances in Redis with a very short TTL serves read-heavy patterns without introducing meaningful stale balance risk.

---

## Common Pitfalls

| Issue | Impact | Resolution |
|---|---|---|
| No idempotency | Duplicate transactions; money created or destroyed twice | Mandatory Idempotency-Key header with Redis cache |
| No database transaction | Sender debited but receiver never credited | All writes must be in a single transactional boundary |
| No concurrency control | Race condition allows balance to go negative | Pessimistic lock with deterministic acquisition order |
| Swallowing exceptions | Partial writes committed without rollback | Always re-throw inside transactional methods |
| No unique constraint in DB | Duplicate records if application layer is bypassed | UNIQUE constraint on idempotency_key and user_id |
| Publishing to Kafka inside the transaction | Message sent but DB rolls back; events become misleading | Publish after commit using a post-transaction hook |
| Non-deterministic lock order | Deadlock between two transfers on the same wallet pair | Always acquire locks in the same order based on user ID |
| Trusting client-supplied userId | Authorization bypass; user A can debit user B's wallet | Extract userId exclusively from the authenticated JWT |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21, Spring Boot 3.x |
| Persistence | Spring Data JPA, PostgreSQL 15 |
| Caching | Redis 7.2 |
| Messaging | Apache Kafka 3.x |
| Security | Spring Security, JWT (RS256) |
| Observability | Micrometer, Prometheus, Grafana |
| Tracing | OpenTelemetry, Jaeger |
| Containerization | Docker, Docker Compose |
| Orchestration | Kubernetes |
| Load Testing | k6 |

---

## Architecture Levels

| Level | Characteristics |
|---|---|
| Basic | CRUD operations, no transaction guarantees, no idempotency |
| Intermediate | Transactional writes, optimistic locking, basic error handling |
| Advanced | DB-backed idempotency, pessimistic locking, retry on deadlock |
| Production | Redis idempotency, deterministic lock ordering, Kafka async processing, distributed tracing |
| Distributed | Saga pattern, distributed locks via Redisson, event-driven architecture, multi-region replication |

---

## Future Improvements

| Improvement | Description |
|---|---|
| Distributed Lock via Redisson | Replace database row locking with Redis-based distributed locks to reduce database pressure at scale |
| Saga Pattern | Decompose transfers into compensatable steps for multi-service transaction choreography |
| Outbox Pattern | Guarantee exactly-once Kafka publishing by persisting events to an outbox table in the same database transaction as the balance update |
| Rate Limiting | Per-user and per-IP rate limiting at the gateway to prevent abuse and protect downstream services |
| Currency Exchange | Multi-currency support with real-time FX rate integration and currency conversion audit trail |
| Fraud Detection | Real-time rule engine to flag and hold suspicious transaction patterns before settlement |
| CQRS | Separate read and write models to allow query performance optimization independently of write throughput |
| Multi-region Replication | Active-active PostgreSQL replication for disaster recovery and geographic latency reduction |

---

## Conclusion

A production-grade transaction system requires more than correct business logic. The following properties must hold simultaneously and reinforce each other:

- Correct Logic: amount validation, self-transfer prevention, balance enforcement before deduction
- Strong Consistency: ACID transactions ensuring no partial writes ever reach a committed state
- Idempotency: every operation is safe to retry an unlimited number of times without side effects
- Concurrency Safety: deterministic locking eliminates race conditions and deadlocks under any load
- Operational Clarity: structured logging, distributed tracing, and metrics at every layer for debugging and audit

Each property depends on the others. Idempotency without transactions is incomplete because a cached success response can coexist with a rolled-back database state. Transactions without locking are unsafe under concurrency because two transactions can both read a consistent snapshot and then both write conflicting updates. Locking without a correct acquisition order causes deadlocks that degrade availability. All five layers must be implemented together as a cohesive system, not as independent features bolted on at different stages of development.