## 🎯 Overview
InsightFlow implements a **JWT-based authentication system** with Spring Security, featuring user registration, login, profile management, and secure API access. The system supports both email and username-based authentication with MongoDB as the data store.

---
## 🎯 Key Authentication Features

### **Security Features**
- ✅ JWT-based stateless authentication
- ✅ BCrypt password encryption
- ✅ CORS configuration for frontend integration
- ✅ Custom error handling with JSON responses
- ✅ Token expiration validation
- ✅ Dual username/email authentication support

### **User Management Features**
- ✅ User registration with profile image support
- ✅ User profile management and updates
- ✅ Analysis history tracking and pagination
- ✅ Last login timestamp tracking
- ✅ Automatic avatar generation

### **API Security Features**
- ✅ Protected endpoints with JWT validation
- ✅ Request filtering for authentication
- ✅ Security context management
- ✅ Authentication and authorization separation
- ✅ Comprehensive error handling


## 🔧 External Tools & Technologies Used

### **Security & Authentication**
- **Spring Security 6.x** - Core security framework
- **JWT (JSON Web Tokens)** - Stateless authentication tokens
- **JJWT Library** - JWT creation and validation
- **BCrypt** - Password hashing algorithm

### **Database & Persistence**
- **MongoDB** - NoSQL document database
- **Spring Data MongoDB** - Data access layer
- **MongoDB Atlas** - Cloud database service

### **Web Framework**
- **Spring Boot 3.x** - Application framework
- **Spring Web MVC** - REST API framework
- **Jackson** - JSON serialization/deserialization

### **Testing**
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing

### **External Services**
- **DiceBear API** - Avatar generation service
- **CORS Configuration** - Cross-origin resource sharing


## 📁 Files Involved in Authentication

### **Core Authentication Files**
1. **Controllers**
   - `AuthController.java` - Main authentication endpoints
   
2. **Services**  
   - `UserService.java` - User business logic and authentication operations
   - `UserDetailsServiceImpl.java` - Spring Security user details implementation

3. **Security Configuration**
   - `SecurityConfig.java` - Main security configuration
   - `JwtUtil.java` - JWT token generation and validation
   - `JwtAuthenticationFilter.java` - JWT token processing filter
   - `CustomAuthenticationEntryPoint.java` - Unauthorized access handler
   - `CustomAccessDeniedHandler.java` - Access denied handler

4. **Models & Repositories**
   - `User.java` - User entity model
   - `UserRepository.java` - MongoDB user data access


---

## 🔄 Authentication Flow & File Execution Order

### **1. Registration Flow**
```
Client Request → AuthController.signup() → UserService.signup() → UserRepository.save() 
→ JwtUtil.generateToken() → Response with JWT Token
```

### **2. Login Flow**  
```
Client Request → AuthController.login() → UserService.login() → AuthenticationManager.authenticate() 
→ UserDetailsServiceImpl.loadUserByUsername() → UserRepository.findByEmail/Username() 
→ JwtUtil.generateToken() → Response with JWT Token
```

### **3. Secured API Access Flow**
```
Client Request → JwtAuthenticationFilter.doFilterInternal() → JwtUtil.extractUsername() 
→ JwtUtil.validateToken() → SecurityContextHolder.setAuthentication() → Controller Method
```

### **4. Error Handling Flow**
```
Authentication Failure → CustomAuthenticationEntryPoint.commence() → JSON Error Response
Access Denied → CustomAccessDeniedHandler.handle() → JSON Error Response
```

---

## 🔐 Function Call Sequence

### **Registration Sequence**
1. `AuthController.signup()` receives registration request
2. Validates input data and checks for existing users
3. Calls `UserService.signup()` with user details
4. `UserService` creates User entity and encrypts password with BCrypt
5. `UserRepository.save()` persists user to MongoDB
6. `JwtUtil.generateToken()` creates JWT token
7. Returns success response with token

### **Login Sequence**
1. `AuthController.login()` receives credentials
2. Calls `UserService.login()` with username/email and password
3. `AuthenticationManager.authenticate()` validates credentials
4. `UserDetailsServiceImpl.loadUserByUsername()` loads user from database
5. `UserRepository.findByEmail()` or `findByUsername()` queries MongoDB
6. Password verification using BCrypt
7. `JwtUtil.generateToken()` creates JWT token
8. Updates user's last login timestamp
9. Returns success response with token

### **API Request Sequence (Protected Endpoints)**
1. `JwtAuthenticationFilter.doFilterInternal()` intercepts request
2. Extracts JWT token from Authorization header
3. `JwtUtil.extractUsername()` extracts username from token
4. `JwtUtil.validateToken()` validates token signature and expiration
5. Creates `UsernamePasswordAuthenticationToken`
6. Sets authentication in `SecurityContextHolder`
7. Proceeds to controller method

---

## 🛠️ Fallback Actions

### **Registration Fallbacks**
- **Duplicate Email/Username**: Returns 400 Bad Request with specific error message
- **Invalid Input**: Returns 400 Bad Request with validation errors
- **Database Error**: Returns 500 Internal Server Error with generic message
- **JWT Generation Failure**: Returns 500 Internal Server Error

### **Login Fallbacks**
- **Invalid Credentials**: Returns 401 Unauthorized via `AuthenticationException`
- **User Not Found**: `UserDetailsServiceImpl` throws `UsernameNotFoundException`
- **Database Connection Error**: Returns 500 Internal Server Error
- **JWT Generation Failure**: Returns 500 Internal Server Error

### **API Access Fallbacks**
- **Missing Token**: `CustomAuthenticationEntryPoint` returns 401 Unauthorized JSON
- **Invalid Token**: `JwtAuthenticationFilter` logs error and continues without authentication
- **Expired Token**: Token validation fails, request treated as unauthenticated
- **Malformed Token**: Exception handling in filter continues request without authentication

---

## 📋 Detailed Component Documentation

## 🎮 **AuthController.java**
*Main REST controller handling authentication endpoints*

### **Class Dependencies**
- `@Autowired UserService userService` - User business logic
- `@Autowired JwtUtil jwtUtil` - JWT token operations

### **Key Methods**

#### **• signup(@RequestBody Map<String, String> request)**
- **Purpose**: Register new user with basic information
- **Process**: 
  - Validates input data
  - Checks for existing users via UserService
  - Creates new user with encrypted password
  - Generates JWT token
- **Returns**: JSON response with JWT token and user info
- **Fallback**: Returns error response for validation failures or duplicates
- **Tools Used**: BCrypt for password encryption, JWT for token generation

#### **• signupWithImage(@RequestParam MultipartFile profileImage, ...)**
- **Purpose**: Register new user with optional profile image
- **Process**:
  - Handles file upload for profile image
  - Generates avatar URL or uses uploaded image
  - Creates user account with image
- **Returns**: JWT token and user profile with avatar
- **Fallback**: Falls back to generated avatar if image upload fails
- **Tools Used**: Spring Multipart, image processing utilities

#### **• login(@RequestBody Map<String, String> request)**
- **Purpose**: Authenticate user and provide JWT token
- **Input**: JSON with username/email and password
- **Process**:
  - Delegates to UserService for authentication
  - Updates last login timestamp
  - Generates fresh JWT token
- **Returns**: JWT token and user information
- **Fallback**: Returns 401 for invalid credentials
- **Tools Used**: Spring Security AuthenticationManager, JWT

#### **• getUserProfile(@RequestHeader("Authorization") String token)**
- **Purpose**: Retrieve current user's profile information
- **Input**: JWT token in Authorization header
- **Process**:
  - Extracts username from JWT token
  - Looks up user by username or email
  - Includes analysis statistics
- **Returns**: Complete user profile with statistics
- **Fallback**: Returns 401 for invalid tokens, 404 for user not found
- **Tools Used**: JWT token parsing

#### **• updateUserProfile(@RequestHeader("Authorization") String token, ...)**
- **Purpose**: Update user profile with new information
- **Input**: JWT token and form data with profile updates
- **Process**:
  - Validates JWT token
  - Updates user fields selectively
  - Handles profile image uploads
- **Returns**: Updated user profile
- **Fallback**: Maintains existing data if updates fail
- **Tools Used**: Spring Multipart, MongoDB updates

#### **• getUserAnalyses(@RequestHeader("Authorization") String token, @RequestParam pagination)**
- **Purpose**: Retrieve user's analysis history with pagination
- **Input**: JWT token and pagination parameters
- **Process**:
  - Authenticates user via JWT
  - Queries user's analysis history
  - Implements pagination logic
- **Returns**: Paginated list of user analyses
- **Fallback**: Returns empty list if no analyses found
- **Tools Used**: MongoDB queries, pagination logic

#### **• saveUserAnalysis(@RequestHeader("Authorization") String token, @RequestBody analysisData)**
- **Purpose**: Save new analysis to user's history
- **Input**: JWT token and analysis data
- **Process**:
  - Validates user authentication
  - Creates UserAnalysis entity
  - Persists to database
- **Returns**: Success confirmation with analysis ID
- **Fallback**: Returns error if save fails
- **Tools Used**: MongoDB persistence

#### **• deleteUserAnalysis(@RequestHeader("Authorization") String token, @PathVariable analysisId)**
- **Purpose**: Delete specific analysis from user's history
- **Input**: JWT token and analysis ID
- **Process**:
  - Validates user ownership of analysis
  - Removes from database
- **Returns**: Success confirmation
- **Fallback**: Returns 403 if user doesn't own analysis
- **Tools Used**: MongoDB deletion operations

---

## 🔧 **UserService.java**
*Business logic service for user operations*

### **Class Dependencies**
- `@Autowired UserRepository userRepository` - Data access
- `@Autowired BCryptPasswordEncoder passwordEncoder` - Password encryption
- `@Autowired AuthenticationManager authenticationManager` - Spring Security auth
- `@Autowired JwtUtil jwtUtil` - JWT operations

### **Key Methods**

#### **• signup(String username, String password, String firstName, String lastName, String email)**
- **Purpose**: Create new user account with validation
- **Process**:
  - Validates email uniqueness
  - Encrypts password with BCrypt
  - Creates User entity with generated avatar
  - Saves to MongoDB
  - Generates JWT token
- **Returns**: JWT token string
- **Fallback**: Throws RuntimeException for duplicate users
- **Tools Used**: BCrypt, MongoDB, JWT, Avatar generation API

#### **• signupWithImage(String username, String password, String firstName, String lastName, String email, String avatarUrl)**
- **Purpose**: Create user account with custom avatar
- **Process**: Same as signup but uses provided avatar URL
- **Returns**: JWT token string
- **Fallback**: Falls back to generated avatar if URL invalid
- **Tools Used**: BCrypt, MongoDB, JWT

#### **• login(String username, String password)**
- **Purpose**: Authenticate user credentials
- **Process**:
  - Uses AuthenticationManager for credential validation
  - Updates user's lastLogin timestamp
  - Generates new JWT token
- **Returns**: JWT token string
- **Fallback**: Throws RuntimeException for invalid credentials
- **Tools Used**: Spring Security AuthenticationManager, JWT, MongoDB updates

#### **• findByUsername(String username)**
- **Purpose**: Find user by username
- **Input**: Username string
- **Process**: Queries UserRepository
- **Returns**: Optional<User>
- **Fallback**: Returns empty optional if not found
- **Tools Used**: MongoDB queries

#### **• findByEmail(String email)**
- **Purpose**: Find user by email address
- **Input**: Email string
- **Process**: Queries UserRepository
- **Returns**: Optional<User>
- **Fallback**: Returns empty optional if not found
- **Tools Used**: MongoDB queries

#### **• getUserAnalysisCount(String userId)**
- **Purpose**: Count total analyses for user
- **Input**: User ID
- **Process**: Queries UserAnalysisRepository
- **Returns**: Long count
- **Fallback**: Returns 0 if user has no analyses
- **Tools Used**: MongoDB aggregation

---

## 🔐 **SecurityConfig.java**
*Main Spring Security configuration*

### **Class Dependencies**
- `@Autowired JwtAuthenticationFilter jwtAuthenticationFilter`
- `@Autowired UserDetailsService userDetailsService`
- `@Autowired CustomAuthenticationEntryPoint customAuthenticationEntryPoint`
- `@Autowired CustomAccessDeniedHandler customAccessDeniedHandler`

### **Key Methods**

#### **• securityFilterChain(HttpSecurity http)**
- **Purpose**: Configure HTTP security settings
- **Input**: HttpSecurity configuration object
- **Process**:
  - Enables CORS with custom configuration
  - Disables CSRF for stateless JWT authentication
  - Configures public endpoints (/api/signup, /api/login, /health)
  - Requires authentication for all other endpoints
  - Sets stateless session management
  - Adds JWT filter before username/password filter
- **Returns**: SecurityFilterChain bean
- **Fallback**: Uses default Spring Security settings if configuration fails
- **Tools Used**: Spring Security DSL, JWT filter chain

#### **• corsConfigurationSource()**
- **Purpose**: Configure Cross-Origin Resource Sharing
- **Input**: None
- **Process**:
  - Allows specific origins (localhost:3000, localhost:5173, production URL)
  - Permits all HTTP methods
  - Allows all headers
  - Enables credential sharing
- **Returns**: CorsConfigurationSource bean
- **Fallback**: Blocks all CORS requests if not configured
- **Tools Used**: Spring CORS configuration

#### **• authenticationManager(AuthenticationConfiguration config)**
- **Purpose**: Create authentication manager bean
- **Input**: AuthenticationConfiguration
- **Process**: Delegates to Spring Security's configuration
- **Returns**: AuthenticationManager bean
- **Fallback**: Uses default authentication manager
- **Tools Used**: Spring Security configuration

#### **• authenticationProvider()**
- **Purpose**: Configure DAO authentication provider
- **Input**: None
- **Process**:
  - Sets UserDetailsService for user lookup
  - Sets BCrypt password encoder
- **Returns**: DaoAuthenticationProvider bean
- **Fallback**: Uses default authentication provider
- **Tools Used**: Spring Security DAO provider, BCrypt

#### **• passwordEncoder()**
- **Purpose**: Create BCrypt password encoder bean
- **Input**: None
- **Process**: Instantiates BCryptPasswordEncoder
- **Returns**: BCryptPasswordEncoder bean
- **Fallback**: No fallback - required for security
- **Tools Used**: BCrypt algorithm

---

## 🎫 **JwtUtil.java**
*JWT token generation and validation utility*

### **Class Dependencies**
- `@Value("${jwt.secret}")` - JWT signing secret from properties
- `@Value("${jwt.expiration}")` - Token expiration time

### **Key Methods**

#### **• generateToken(String username)**
- **Purpose**: Create JWT token for authenticated user
- **Input**: Username/email string
- **Process**:
  - Creates token with username as subject
  - Sets issue date and expiration
  - Signs with secret key using HS256
- **Returns**: JWT token string
- **Fallback**: No specific fallback - throws exception if fails
- **Tools Used**: JJWT library, HMAC-SHA256 signing

#### **• extractUsername(String token)**
- **Purpose**: Extract username from JWT token
- **Input**: JWT token string
- **Process**:
  - Parses token and extracts subject claim
- **Returns**: Username string
- **Fallback**: Returns null if token parsing fails
- **Tools Used**: JJWT claims extraction

#### **• extractExpiration(String token)**
- **Purpose**: Get token expiration date
- **Input**: JWT token string
- **Process**: Extracts expiration claim from token
- **Returns**: Date object
- **Fallback**: Returns null if extraction fails
- **Tools Used**: JJWT claims extraction

#### **• isTokenExpired(String token)**
- **Purpose**: Check if token has expired
- **Input**: JWT token string
- **Process**: Compares expiration date with current time
- **Returns**: Boolean
- **Fallback**: Returns true (expired) if check fails
- **Tools Used**: Date comparison

#### **• validateToken(String token, String username)**
- **Purpose**: Validate token signature and user match
- **Input**: JWT token and expected username
- **Process**:
  - Extracts username from token
  - Checks username match
  - Verifies token not expired
- **Returns**: Boolean validation result
- **Fallback**: Returns false if any validation step fails
- **Tools Used**: JJWT validation, signature verification

---

## 🛡️ **JwtAuthenticationFilter.java**
*Filter for processing JWT tokens on each request*

### **Class Dependencies**
- `@Autowired JwtUtil jwtUtil` - JWT operations

### **Key Methods**

#### **• doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)**
- **Purpose**: Process JWT token for each HTTP request
- **Input**: HTTP request, response, and filter chain
- **Process**:
  - Extracts Authorization header
  - Validates Bearer token format
  - Extracts username from JWT
  - Validates token if user not already authenticated
  - Sets authentication in SecurityContextHolder
  - Continues filter chain
- **Returns**: Void (filter operation)
- **Fallback**: Continues without authentication if token invalid
- **Tools Used**: JWT validation, Spring Security context

---

## 👤 **UserDetailsServiceImpl.java**
*Spring Security user details service implementation*

### **Class Dependencies**
- `@Autowired UserRepository userRepository` - User data access

### **Key Methods**

#### **• loadUserByUsername(String username)**
- **Purpose**: Load user details for Spring Security authentication
- **Input**: Username or email string
- **Process**:
  - Searches user by email first, then by username
  - Creates Spring Security UserDetails object
  - Uses email as principal for consistency
- **Returns**: UserDetails object
- **Fallback**: Throws UsernameNotFoundException if user not found
- **Tools Used**: MongoDB queries, Spring Security UserDetails

---

## 🏗️ **User.java** (Model)
*User entity model for MongoDB*

### **Class Annotations**
- `@Document(collection = "users")` - MongoDB collection mapping
- `@Id` - MongoDB document ID
- `@Indexed(unique = true)` - Unique constraints

### **Key Fields**
- `String id` - MongoDB object ID
- `String email` - User email (unique, used as primary identifier)
- `String username` - Optional username (for backward compatibility)
- `String firstName, lastName` - User names
- `String password` - BCrypt encrypted password
- `String role` - User role (default "USER")
- `String avatar` - Profile image URL
- `String bio` - User biography
- `LocalDateTime createdAt` - Account creation timestamp
- `LocalDateTime lastLogin` - Last login timestamp
- `List<String> analysisHistoryIds` - References to user's analyses

### **Key Methods**

#### **• generateAvatar(String firstName, String lastName)**
- **Purpose**: Generate avatar URL using external service
- **Input**: User's first and last names
- **Process**: Creates URL for DiceBear avatar service
- **Returns**: Avatar URL string
- **Fallback**: Provides default avatar if generation fails
- **Tools Used**: DiceBear API

---

## 🗄️ **UserRepository.java**
*MongoDB data access interface*

### **Interface Extension**
- `extends MongoRepository<User, String>` - Basic CRUD operations

### **Custom Query Methods**

#### **• findByUsername(String username)**
- **Purpose**: Find user by username
- **Input**: Username string
- **Returns**: Optional<User>
- **Tools Used**: MongoDB Spring Data queries

#### **• findByEmail(String email)**
- **Purpose**: Find user by email
- **Input**: Email string
- **Returns**: Optional<User>
- **Tools Used**: MongoDB Spring Data queries

#### **• existsByUsername(String username)**
- **Purpose**: Check if username exists
- **Input**: Username string
- **Returns**: Boolean
- **Tools Used**: MongoDB existence queries

#### **• existsByEmail(String email)**
- **Purpose**: Check if email exists
- **Input**: Email string
- **Returns**: Boolean
- **Tools Used**: MongoDB existence queries

---

## 🚨 **CustomAuthenticationEntryPoint.java**
*Handler for unauthorized access attempts*

### **Class Dependencies**
- `ObjectMapper objectMapper` - JSON serialization

### **Key Methods**

#### **• commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)**
- **Purpose**: Handle unauthorized access attempts
- **Input**: HTTP request, response, and authentication exception
- **Process**:
  - Sets HTTP status to 401 Unauthorized
  - Creates JSON error response
  - Includes error details and request path
- **Returns**: Void (writes to response)
- **Fallback**: Provides generic error message if details unavailable
- **Tools Used**: Jackson JSON, HTTP response manipulation

---

## 🔒 **CustomAccessDeniedHandler.java**
*Handler for access denied scenarios*

### **Class Dependencies**
- `ObjectMapper objectMapper` - JSON serialization

### **Key Methods**

#### **• handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)**
- **Purpose**: Handle access denied scenarios
- **Input**: HTTP request, response, and access denied exception
- **Process**:
  - Sets HTTP status to 403 Forbidden
  - Creates JSON error response
  - Includes error details and request path
- **Returns**: Void (writes to response)
- **Fallback**: Provides generic error message
- **Tools Used**: Jackson JSON, HTTP response manipulation

---

## 🧪 **AuthControllerTest.java**
*Unit tests for authentication endpoints*

### **Test Configuration**
- `@WebMvcTest(AuthController.class)` - Web layer testing
- `@AutoConfigureMockMvc(addFilters = false)` - Disable security for testing
- `@MockBean UserService userService` - Mock user service

### **Key Test Methods**

#### **• testSignup()**
- **Purpose**: Test user registration endpoint
- **Process**:
  - Mocks UserService.signup() method
  - Sends POST request to /api/signup
  - Verifies 200 OK response and JWT token
- **Tools Used**: MockMvc, Mockito, JSON assertions

#### **• testLogin()**
- **Purpose**: Test user login endpoint
- **Process**:
  - Mocks UserService.login() method
  - Sends POST request to /api/login
  - Verifies 200 OK response and JWT token
- **Tools Used**: MockMvc, Mockito, JSON assertions