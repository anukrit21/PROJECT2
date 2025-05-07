# Authentication Service

## Overview
This service handles all authentication-related operations including user registration, login, MFA, OAuth, and role management.

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/validate` - Validate JWT token
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - User logout

### Multi-Factor Authentication (MFA)
- `POST /api/auth/mfa/setup` - Setup MFA
- `POST /api/auth/mfa/verify` - Verify MFA code
- `POST /api/auth/mfa/disable` - Disable MFA

### Password Management
- `POST /api/auth/change-password` - Change password
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password with token

### OAuth
- `POST /api/auth/oauth/login` - OAuth login

### Role Management
- `POST /api/auth/roles/{userId}/add` - Add role to user
- `POST /api/auth/roles/{userId}/remove` - Remove role from user
- `GET /api/auth/roles/{userId}` - Get user roles

### Account Management
- `GET /api/auth/account/lock-status` - Check account lock status
- `POST /api/auth/account/unlock` - Unlock account

## Authentication Flows

### Standard Login Flow
1. Client sends credentials to `/api/auth/login`
2. If MFA is not enabled:
   - Server returns JWT tokens
3. If MFA is enabled:
   - Server returns status "MFA_REQUIRED"
   - Client must complete MFA verification

### MFA Setup Flow
1. Client calls `/api/auth/mfa/setup`
2. Server returns QR code URL and secret key
3. User configures authenticator app
4. Client verifies setup with `/api/auth/mfa/verify`

### Password Reset Flow
1. User requests reset via `/api/auth/forgot-password`
2. User receives reset link via email
3. User submits new password via `/api/auth/reset-password`

### OAuth Flow
1. Client initiates OAuth via `/api/auth/oauth/login`
2. Server validates OAuth token
3. Returns JWT tokens upon success

## Response Objects

### AuthResponse
```json
{
  "status": "SUCCESS|FAILED|MFA_REQUIRED",
  "message": "string",
  "username": "string",
  "role": "string",
  "mfaEnabled": boolean,
  "requiresMfaSetup": boolean,
  "accessToken": "string",
  "refreshToken": "string",
  "expiresAt": "datetime"
}
```

### MfaSetupResponse
```json
{
  "secretKey": "string",
  "qrCodeUrl": "string",
  "message": "string"
}
```

### AccountResponse
```json
{
  "status": "string",
  "message": "string",
  "success": boolean,
  "accountLocked": boolean,
  "username": "string"
}
```

### RoleResponse
```json
{
  "status": "string",
  "message": "string",
  "success": boolean,
  "userId": number,
  "roles": ["string"]
}
```

## Security Considerations
- JWT tokens expire after a configured time
- Failed login attempts trigger account locking
- Password reset tokens are single-use and time-limited
- MFA secrets are stored encrypted
- OAuth tokens are validated with providers

## Error Handling
All endpoints return appropriate HTTP status codes:
- 200: Success
- 201: Created
- 202: Accepted (MFA Required)
- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 500: Internal Server Error 