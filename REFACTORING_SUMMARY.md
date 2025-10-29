# Refactoring Summary: Shared Wallets & Real-time Notifications

## Overview

This refactoring transforms the Personal Expense Tracker from a single-user application to a multi-user collaborative platform with real-time updates.

## What Changed?

### 1. Data Model Architecture

**Before:**
- Transactions, Budgets, and Recurring Transactions belonged directly to Users
- Each user had their own isolated data

**After:**
- These entities now belong to Wallets
- Users can be members of multiple Wallets
- Wallets enable shared expense tracking among multiple users

### 2. Key Components Added

#### Wallet Entity (`Wallet.java`)
- Represents a shared or personal wallet
- Has an owner (the user who created it)
- Has members (users who can access and modify data)
- Contains transactions, budgets, and recurring transactions

#### WalletService & WalletController
- Manage wallet creation, member invitations
- Enforce security: users can only access wallets they're members of
- Endpoints:
  - `GET /api/wallets` - List all wallets for current user
  - `POST /api/wallets` - Create a new wallet
  - `POST /api/wallets/{id}/invite` - Invite user by email (owner only)

#### WebSocket Configuration
- Real-time notification system using STOMP over WebSocket
- Endpoint: `/ws`
- Topic pattern: `/topic/wallet/{walletId}`
- Notifies all wallet members when data changes

### 3. Modified Components

All main entities and services were updated:

- **Transaction, Budget, RecurringTransaction**: Changed from `User user` to `Wallet wallet`
- **All Services**: Added `walletId` parameter and security checks
- **All Controllers**: Now require `walletId` in requests
- **All Repositories**: Query methods updated to filter by wallet instead of user
- **AuthController**: Automatically creates default wallet on signup

### 4. Security Enhancements

Every operation now includes:
```java
if (!walletService.isUserMemberOfWallet(walletId, userId)) {
    throw new SecurityException("User is not a member of this wallet");
}
```

This ensures users can only access wallets they belong to.

### 5. Real-time Notifications

Services now send WebSocket messages after operations:
```java
messagingTemplate.convertAndSend("/topic/wallet/" + walletId, 
    Map.of("message", "DATA_UPDATED", "type", "TRANSACTION_CREATED"));
```

## Benefits

### For Users
1. **Shared Expense Tracking**: Families, roommates, or teams can track expenses together
2. **Real-time Updates**: See changes instantly when other members add transactions
3. **Flexible Organization**: Create multiple wallets for different purposes (personal, family, business)

### For Developers
1. **Scalable Architecture**: Easy to add more collaborative features
2. **Maintained Backward Compatibility**: Existing single-user workflows still work via default wallet
3. **Clean Separation**: Wallet-based isolation improves data organization

## Migration Path

### For New Deployments
- No migration needed, start fresh with new schema

### For Existing Deployments
1. Follow `MIGRATION_GUIDE.md` for SQL scripts
2. Update frontend to include `walletId` in API calls
3. Implement WebSocket client for real-time updates

## Testing

All 13 existing tests pass:
- `PersonalExpenseTrackerBackendApplicationTests`
- `RefreshTokenServiceTest`
- `AuthSigninSecurityIntegrationTest`
- `AuthControllerIntegrationTest`

The H2 test database automatically uses the new schema.

## API Usage Examples

### Get User's Wallets
```
GET /api/wallets
Authorization: Bearer <token>

Response:
[
  {
    "id": "uuid",
    "name": "Ví cá nhân",
    "owner": {...},
    "members": [...]
  }
]
```

### Create Transaction in Wallet
```
POST /api/transactions?walletId=<wallet-uuid>
Authorization: Bearer <token>

Body:
{
  "title": "Grocery shopping",
  "amount": 50000,
  "category": "Food",
  "type": "expense",
  "date": "2025-10-29"
}
```

### WebSocket Connection (JavaScript Example)
```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    stompClient.subscribe('/topic/wallet/' + walletId, function(message) {
        const notification = JSON.parse(message.body);
        console.log('Update received:', notification);
        // Refresh UI data
    });
});
```

## Next Steps

Consider implementing:
1. Wallet settings (permissions, currency, categories)
2. Activity log (who added what)
3. Notifications UI component
4. Wallet member management interface
5. Budget alerts via WebSocket

## Questions?

Refer to:
- `MIGRATION_GUIDE.md` for database migration details
- Entity classes for data structure
- Controller classes for API documentation
- Service classes for business logic

---

**Note**: This is a breaking change for existing API clients. Frontend applications must be updated to work with the new API structure.
