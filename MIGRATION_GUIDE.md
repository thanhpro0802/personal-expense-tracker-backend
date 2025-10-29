# Database Migration Guide

## Overview

This refactoring changes the data model from user-centric to wallet-centric. Previously, `Transaction`, `Budget`, and `RecurringTransaction` entities belonged directly to a `User`. Now they belong to a `Wallet`, and users can be members of multiple wallets.

## Schema Changes

### New Tables

#### 1. `wallets`
- `id` (UUID, primary key)
- `name` (VARCHAR)
- `owner_id` (UUID, foreign key to users)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

#### 2. `wallet_members` (join table)
- `wallet_id` (UUID, foreign key to wallets)
- `user_id` (UUID, foreign key to users)
- Primary key: (`wallet_id`, `user_id`)

### Modified Tables

#### `transactions`
- **REMOVED**: `user_id` column
- **ADDED**: `wallet_id` (UUID, foreign key to wallets, NOT NULL)

#### `budgets`
- **REMOVED**: `user_id` column
- **ADDED**: `wallet_id` (UUID, foreign key to wallets, NOT NULL)
- **CHANGED**: Unique constraint from `(user_id, category, month, year)` to `(wallet_id, category, "month", "year")`
- **NOTE**: Column names `month` and `year` are now quoted to avoid conflicts with SQL reserved keywords

#### `recurring_transactions`
- **REMOVED**: `user_id` column
- **ADDED**: `wallet_id` (UUID, foreign key to wallets, NOT NULL)

## Migration Strategy

### For Development/Testing (Fresh Start)

If your database can be recreated:

1. Drop all existing tables
2. Run the application - Hibernate will create the new schema automatically
3. The application will work with the new schema

### For Production (Data Preservation Required)

**⚠️ IMPORTANT: Backup your database before running any migration!**

You'll need to run SQL migration scripts to:

1. Create the new `wallets` and `wallet_members` tables
2. Create a default wallet for each existing user
3. Migrate data from `user_id` to `wallet_id` in transactions, budgets, and recurring_transactions
4. Drop the old `user_id` columns

#### Sample Migration SQL (PostgreSQL)

```sql
BEGIN;

-- Step 1: Create new tables
CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE wallet_members (
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    user_id UUID NOT NULL REFERENCES users(id),
    PRIMARY KEY (wallet_id, user_id)
);

-- Step 2: Create a default wallet for each user
INSERT INTO wallets (id, name, owner_id, created_at, updated_at)
SELECT 
    gen_random_uuid() as id,
    'Ví cá nhân' as name,
    id as owner_id,
    NOW() as created_at,
    NOW() as updated_at
FROM users;

-- Step 3: Add owner to wallet_members
INSERT INTO wallet_members (wallet_id, user_id)
SELECT id as wallet_id, owner_id as user_id
FROM wallets;

-- Step 4: Add wallet_id column to transactions (temporarily nullable)
ALTER TABLE transactions ADD COLUMN wallet_id UUID;

-- Step 5: Migrate transaction data
UPDATE transactions t
SET wallet_id = w.id
FROM wallets w
WHERE t.user_id = w.owner_id;

-- Step 6: Make wallet_id NOT NULL and add foreign key
ALTER TABLE transactions ALTER COLUMN wallet_id SET NOT NULL;
ALTER TABLE transactions ADD CONSTRAINT fk_transaction_wallet 
    FOREIGN KEY (wallet_id) REFERENCES wallets(id);

-- Step 7: Drop old user_id column
ALTER TABLE transactions DROP COLUMN user_id;

-- Step 8: Repeat for budgets
ALTER TABLE budgets ADD COLUMN wallet_id UUID;

UPDATE budgets b
SET wallet_id = w.id
FROM wallets w
WHERE b.user_id = w.owner_id;

ALTER TABLE budgets ALTER COLUMN wallet_id SET NOT NULL;
ALTER TABLE budgets ADD CONSTRAINT fk_budget_wallet 
    FOREIGN KEY (wallet_id) REFERENCES wallets(id);

-- Drop old unique constraint and add new one
ALTER TABLE budgets DROP CONSTRAINT IF EXISTS budgets_user_id_category_month_year_key;
ALTER TABLE budgets ADD CONSTRAINT budgets_wallet_category_month_year_unique 
    UNIQUE (wallet_id, category, "month", "year");

ALTER TABLE budgets DROP COLUMN user_id;

-- Step 9: Repeat for recurring_transactions
ALTER TABLE recurring_transactions ADD COLUMN wallet_id UUID;

UPDATE recurring_transactions rt
SET wallet_id = w.id
FROM wallets w
WHERE rt.user_id = w.owner_id;

ALTER TABLE recurring_transactions ALTER COLUMN wallet_id SET NOT NULL;
ALTER TABLE recurring_transactions ADD CONSTRAINT fk_recurring_transaction_wallet 
    FOREIGN KEY (wallet_id) REFERENCES wallets(id);

ALTER TABLE recurring_transactions DROP COLUMN user_id;

COMMIT;
```

## API Changes

All endpoints now require a `walletId` parameter:

### Transactions
- `GET /api/transactions?walletId={walletId}&...`
- `POST /api/transactions?walletId={walletId}`
- `PUT /api/transactions/{id}?walletId={walletId}`
- `DELETE /api/transactions/{id}?walletId={walletId}`

### Budgets
- `GET /api/budgets?walletId={walletId}&month={month}&year={year}`
- `POST /api/budgets?walletId={walletId}`
- `DELETE /api/budgets/{id}?walletId={walletId}`

### Recurring Transactions
- `GET /api/recurring-transactions?walletId={walletId}`
- `POST /api/recurring-transactions?walletId={walletId}`
- `PUT /api/recurring-transactions/{id}?walletId={walletId}`
- `DELETE /api/recurring-transactions/{id}?walletId={walletId}`

### Dashboard
- `GET /api/dashboard/stats?walletId={walletId}&month={month}&year={year}`

### New Wallet Endpoints
- `GET /api/wallets` - Get all wallets for the current user
- `POST /api/wallets` - Create a new wallet
- `POST /api/wallets/{walletId}/invite` - Invite a user to a wallet (owner only)

## WebSocket Integration

Real-time notifications are sent to `/topic/wallet/{walletId}` when:
- A transaction is created, updated, or deleted
- A budget is created, updated, or deleted
- A recurring transaction is created, updated, or deleted

Notification format:
```json
{
    "message": "DATA_UPDATED",
    "type": "TRANSACTION_CREATED" // or TRANSACTION_UPDATED, BUDGET_CREATED, etc.
}
```

Frontend clients should:
1. Connect to WebSocket endpoint: `/ws`
2. Subscribe to: `/topic/wallet/{walletId}`
3. Handle incoming messages to refresh data

## Security

All endpoints now verify that the authenticated user is a member of the requested wallet before allowing any operations. This is handled automatically by the service layer.

## Automatic Wallet Creation

When a new user registers via `/api/auth/signup`, a default wallet named "Ví cá nhân" is automatically created and the user is added as both the owner and a member.

## Testing

All existing tests pass with the new schema. The H2 in-memory database automatically creates the new schema during test runs.
