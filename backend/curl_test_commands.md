# InsightFlow Backend Testing Commands

## 1. Sign Up with New Fields

curl -X POST http://localhost:8000/api/signup ^
-H "Content-Type: application/json" ^
-d "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\",\"password\":\"password123\"}"

## Expected Response:
```json
{
"message":"User created successfully",
"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTc1NzUxNjU3OSwiZXhwIjoxNzU3NjAyOTc5fQ.HNkEYYUMJCWecdY-NzRuOzMpzuafPRBXzOfHeLSQRjw"
}
```
## 2. Login with Email
```bash
curl -X POST http://localhost:8000/api/login ^
-H "Content-Type: application/json" ^
-d "{\"email\":\"john.doe@example.com\",\"password\":\"password123\"}"
```
## Expected Response:
```json
{
"message":"Login successful",
"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTc1NzUxODA1NSwiZXhwIjoxNzU3NjA0NDU1fQ.dYHBwL9CIa0udWU0mAQIo6p9lHOOFq7f65Ng2MWdQZw"
}
```
## 3. Get User Profile (Replace TOKEN with actual JWT from login)
```bash
curl -X GET http://localhost:8000/api/user/profile ^
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTc1NzUxODA1NSwiZXhwIjoxNzU3NjA0NDU1fQ.dYHBwL9CIa0udWU0mAQIo6p9lHOOFq7f65Ng2MWdQZw"
```
## Expected Response:

```json
{
    "firstName":"John",
    "lastName":"Doe",
    "createdAt":"2025-09-10T21:02:59.4",
    "lastLogin":"2025-09-10T21:03:35.557",
    "role":"USER",
    "totalAnalyses":0,
    "id":"68c19323e4b5493cd3b95051",
    "avatar":"https://ui-avatars.com/api/?name=John+Doe&background=0D8ABC&color=fff",
    "successfulAnalyses":0,
    "email":"john.doe@example.com"
}
```
## 4. Get User Analysis History (Replace TOKEN with actual JWT)
```bash
curl -X GET http://localhost:8000/api/user/analyses ^
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTc1NzUxODA1NSwiZXhwIjoxNzU3NjA0NDU1fQ.dYHBwL9CIa0udWU0mAQIo6p9lHOOFq7f65Ng2MWdQZw"
```
## Expected Response:

```json
{
"analyses":[],
"total":0
}
```
## 5. Test Error Cases

### Signup with existing email
```bash
curl -X POST http://localhost:8000/api/signup ^
-H "Content-Type: application/json" ^
-d "{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"email\":\"john.doe@example.com\",\"password\":\"password456\"}"
```
## Expected Response:

```json
{
"error":"Username already exists"
}
```
### Login with wrong password

```bash
curl -X POST http://localhost:8000/api/login ^
-H "Content-Type: application/json" ^
-d "{\"email\":\"john.doe@example.com\",\"password\":\"wrongpassword\"}"
```

## Expected Response:
```json
{
    "error":"Invalid username or password"
}
```
### Access protected endpoint without token
```bash
curl -X GET http://localhost:8000/api/user/profile
```
## Expected Response:

```json
{
  "error": "Unauthorized",
  "message": "Authentication required. Please provide a valid token.",
  "status": 401,
  "path": "/api/user/profile"
}
```

### Access protected endpoint with invalid token
```bash
curl -X GET http://localhost:8000/api/user/profile ^
-H "Authorization: Bearer invalid-token"
```
## Expected Response:

```json
{
  "error": "Unauthorized",
  "message": "Authentication required. Please provide a valid token.",
  "status": 401,
  "path": "/api/user/profile"
}
```

## Testing Workflow

1. Start the backend server: `mvn spring-boot:run`
2. Run the signup command to create a user
3. Copy the token from the response
4. Replace "TOKEN" in the profile and analyses commands with the actual token
5. Test all endpoints to verify functionality

## Database Verification

To verify data in MongoDB:

```javascript
// Connect to MongoDB
use insightflow

// Check users collection
db.users.find().pretty()

// Check user_analyses collection
db.user_analyses.find().pretty()
```

## Notes

- All endpoints return JSON responses
- Tokens are valid for the duration specified in JWT configuration
- The signup endpoint creates a user with a generated avatar URL
- Email field is now indexed and unique
- Analysis history is initially empty but will be populated when users run analyses
- Error responses include descriptive messages for debugging
