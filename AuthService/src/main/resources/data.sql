-- Seed data for Postgres. Uses quoted table names to avoid reserved keywords.

INSERT INTO "USER" (id, email, password, first_name, last_name, role, enabled, created_at, updated_at)
VALUES
  (1, 'admin@wallet.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5X4s2c9ZwTe74MkRUYw35vj0IwyK2', 'Admin', 'User', 'ADMIN', true, now(), now()),
  (2, 'alice@wallet.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5X4s2c9ZwTe74MkRUYw35vj0IwyK2', 'Alice', 'Nguyen', 'USER', true, now(), now()),
  (3, 'bob@wallet.local',   '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5X4s2c9ZwTe74MkRUYw35vj0IwyK2', 'Bob',   'Tran',   'USER', true, now(), now());

SELECT setval('user_seq', (SELECT COALESCE(MAX(id), 0) FROM "USER"));

INSERT INTO "WALLET" (wallet_address, status, user_id, created_at)
VALUES
  ('WALLET-ALICE-0001', 'ACTIVE', 2, now()),
  ('WALLET-BOB-0001',   'ACTIVE', 3, now());

INSERT INTO "TRANSACTION" (transaction_id, amount, type, status, balance_before, balance_after, wallet_id, created_at)
VALUES
  ('TXN-ALICE-0001', 1000.00, 'DEPOSIT',     'SUCCESS', 0.00,    1000.00, 'WALLET-ALICE-0001', now()),
  ('TXN-ALICE-0002', 150.00,  'PAYMENT',     'SUCCESS', 1000.00, 850.00,  'WALLET-ALICE-0001', now()),
  ('TXN-BOB-0001',   500.00,  'DEPOSIT',     'SUCCESS', 0.00,    500.00,  'WALLET-BOB-0001',   now()),
  ('TXN-BOB-0002',   75.00,   'WITHDRAW',    'SUCCESS', 500.00,  425.00,  'WALLET-BOB-0001',   now()),
  ('TXN-ALICE-0003', 50.00,   'TRANSFER_OUT','SUCCESS', 850.00,  800.00,  'WALLET-ALICE-0001', now()),
  ('TXN-BOB-0003',   50.00,   'TRANSFER_IN', 'SUCCESS', 425.00,  475.00,  'WALLET-BOB-0001',   now());
