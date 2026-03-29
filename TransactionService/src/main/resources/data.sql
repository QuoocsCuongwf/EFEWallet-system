-- Shared relationship IDs:
-- users: 33333333-3333-3333-3333-333333333001(alice), 33333333-3333-3333-3333-333333333002(bob)
-- wallets: 44444444-4444-4444-4444-444444444001(alice), 44444444-4444-4444-4444-444444444002(bob)

TRUNCATE TABLE idempotency_keys, transactions;

INSERT INTO transactions (
  id,
  wallet_id,
  reference_id,
  type,
  status,
  amount,
  fee,
  description,
  external_transaction_id,
  created_at,
  updated_at
) VALUES
  (
    '55555555-5555-5555-5555-555555555001',
    '44444444-4444-4444-4444-444444444001',
    '44444444-4444-4444-4444-444444444002',
    'TRANSFER',
    'SUCCESS',
    50000.0000,
    1000.0000,
    'Alice transferred to Bob',
    'EXT-TRF-000001',
    '2026-01-02 09:00:00',
    '2026-01-02 09:00:00'
  ),
  (
    '55555555-5555-5555-5555-555555555002',
    '44444444-4444-4444-4444-444444444002',
    '44444444-4444-4444-4444-444444444001',
    'RECEIVE',
    'SUCCESS',
    50000.0000,
    0.0000,
    'Bob received from Alice',
    'EXT-RCV-000002',
    '2026-01-02 09:00:01',
    '2026-01-02 09:00:01'
  ),
  (
    '55555555-5555-5555-5555-555555555003',
    '44444444-4444-4444-4444-444444444001',
    'BANK-DEPOSIT-0001',
    'DEPOSIT',
    'PENDING',
    200000.0000,
    0.0000,
    'Deposit pending confirmation',
    'EXT-DEP-000003',
    '2026-01-02 10:00:00',
    '2026-01-02 10:00:00'
  );

INSERT INTO idempotency_keys (
  id,
  idempotency_key,
  request_hash,
  user_id,
  status,
  response_body,
  response_status,
  resource_type,
  resource_id,
  created_at,
  updated_at,
  expired_at
) VALUES
  (
    '66666666-6666-6666-6666-666666666001',
    'idem-alice-transfer-0001',
    'hash-alice-transfer-0001',
    '33333333-3333-3333-3333-333333333001',
    'SUCCESS',
    '{"transactionId":"55555555-5555-5555-5555-555555555001","status":"SUCCESS"}',
    200,
    'TRANSFER',
    '55555555-5555-5555-5555-555555555001',
    '2026-01-02 09:00:00',
    '2026-01-02 09:00:00',
    '2026-01-02 09:10:00'
  ),
  (
    '66666666-6666-6666-6666-666666666002',
    'idem-alice-deposit-0002',
    'hash-alice-deposit-0002',
    '33333333-3333-3333-3333-333333333001',
    'PROCESSING',
    null,
    null,
    'DEPOSIT',
    '55555555-5555-5555-5555-555555555003',
    '2026-01-02 10:00:00',
    '2026-01-02 10:00:00',
    '2026-01-02 10:10:00'
  );
