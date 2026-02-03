# API Response Library

[![Maven Central](https://img.shields.io/maven-central/v/io.github.pasinduog/api-response.svg)](https://central.sonatype.com/artifact/io.github.pasinduog/api-response)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0+-brightgreen.svg)](https://spring.io/projects/spring-boot)

A lightweight, type-safe API Response wrapper for Spring Boot applications. Standardize your REST API responses with consistent structure, automatic timestamps, and clean factory methods.

## ✨ Features

- 🎯 **Consistent Structure** - All responses follow the same format: `status`, `traceId`, `message`, `data`, `timestamp`
- 🔒 **Type-Safe** - Full generic type support with compile-time type checking
- 🔍 **Distributed Tracing** - Auto-generated UUID trace IDs for request tracking *(New in v1.2.0)*
- ⏰ **Auto Timestamps** - Automatic ISO-8601 UTC formatted timestamps on every response
- 🏭 **Factory Methods** - Clean static methods: `success()`, `created()`, `status()`
- 🚀 **Zero Config** - Spring Boot Auto-Configuration for instant setup *(Enhanced in v1.3.0)*
- 🪶 **Lightweight** - Only ~15KB with minimal dependencies (Spring Web + Lombok)
- 📦 **Immutable** - Thread-safe with final fields
- 🔌 **Spring Native** - Built on `ResponseEntity` and `HttpStatus`
- 🛡️ **Global Exception Handler** - Built-in ProblemDetail RFC 7807 error handling
- 🎭 **Custom Business Exceptions** - Abstract `ApiException` class for domain-specific errors *(New in v1.2.0)*
- ✅ **Validation Support** - Automatic `@Valid` annotation error handling

## 📦 Requirements

- Java 17 or higher
- Spring Boot 3.2.0 or higher
- Lombok (compile-time only, provided scope)

## 🚀 Installation

### Maven

```xml
<dependency>
    <groupId>io.github.pasinduog</groupId>
    <artifactId>api-response</artifactId>
    <version>1.3.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.github.pasinduog:api-response:1.3.0'
```

### Gradle Kotlin DSL

```kotlin
implementation("io.github.pasinduog:api-response:1.3.0")
```

## 🎯 Quick Start

```java
import io.github.pasinduog.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ApiResponse.success("User retrieved successfully", user);
    }
}
```

**Response:**
```json
{
  "status": 200,
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "User retrieved successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "timestamp": "2026-02-01T10:30:45.123Z"
}
```

## ⚙️ Auto-Configuration (New in v1.3.0)

The library now features **Spring Boot Auto-Configuration** for truly zero-config setup! Simply add the dependency and everything works automatically.

### What Gets Auto-Configured

When you include this library in your Spring Boot application, the following components are automatically registered:

✅ **GlobalExceptionHandler** - Automatic exception handling with RFC 7807 ProblemDetail format  
✅ **Component Scanning** - All library components are automatically discovered  
✅ **Bean Registration** - No manual @ComponentScan or @Import required

### How It Works

The library includes `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` which Spring Boot 3.x automatically detects and loads the `ApiResponseAutoConfiguration` class.

**No configuration needed!** Just add the dependency:

```xml
<dependency>
    <groupId>io.github.pasinduog</groupId>
    <artifactId>api-response</artifactId>
    <version>1.3.0</version>
</dependency>
```

### Disabling Auto-Configuration (Optional)

If you need to customize or disable the auto-configuration:

```java
@SpringBootApplication(exclude = ApiResponseAutoConfiguration.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Or in `application.properties`:
```properties
spring.autoconfigure.exclude=io.github.pasinduog.config.ApiResponseAutoConfiguration
```

## 🛡️ Built-in Exception Handling (Enhanced in v1.2.0)

The library includes a **production-ready `GlobalExceptionHandler`** that automatically handles common exceptions using Spring Boot's **ProblemDetail (RFC 7807)** standard.

### What's Included

✅ **General Exception Handler** - Catches all unhandled exceptions  
✅ **Validation Error Handler** - Automatically processes `@Valid` annotation failures  
✅ **Null Pointer Handler** - Specific handling for NullPointerException  
✅ **Custom ApiException Handler** - Handles custom business exceptions extending `ApiException` *(New in v1.2.0)*  
✅ **Automatic Logging** - SLF4J integration for all errors  
✅ **Timestamp Support** - All error responses include timestamps

### Custom Business Exceptions (New in v1.2.0)

Instead of throwing generic exceptions, you can now extend the **abstract `ApiException` class** to create domain-specific exceptions with automatic exception handling:

```java
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with ID: %d", resource, id), HttpStatus.NOT_FOUND);
    }
}

public class UnauthorizedAccessException extends ApiException {
    public UnauthorizedAccessException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}

public class BusinessRuleViolationException extends ApiException {
    public BusinessRuleViolationException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
```

**Usage in Controllers:**

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
    User user = userService.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", id));
    return ApiResponse.success("User retrieved successfully", user);
}
```

**Automatic Error Response (RFC 7807):**
```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "User not found with ID: 123",
  "timestamp": "2026-02-02T10:30:45.123Z"
}
```

The `GlobalExceptionHandler` automatically:
- Extracts the HTTP status from your custom exception
- Formats it as a ProblemDetail response
- Logs the error with appropriate severity
- Includes timestamps for traceability

### Example: Validation Errors

Add validation to your DTOs:

```java
public class UserDto {
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Min(value = 18, message = "Age must be at least 18")
    private Integer age;
}
```

Use `@Valid` in your controller:

```java
@PostMapping
public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserDto dto) {
    User newUser = userService.create(dto);
    return ApiResponse.created("User created successfully", newUser);
}
```

**Automatic Error Response:**
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation Failed",
  "errors": {
    "email": "Email must be valid",
    "name": "Name is required",
    "age": "Age must be at least 18"
  },
  "timestamp": "2026-02-02T10:30:45.123Z"
}
```

### Logging

All exceptions are automatically logged:
- **ERROR level** - General exceptions and null pointer exceptions
- **WARN level** - Validation errors

```
2026-02-02 10:30:45.123 WARN  c.e.e.GlobalExceptionHandler - Validation error: {email=Email must be valid, name=Name is required}
2026-02-02 10:30:45.456 ERROR c.e.e.GlobalExceptionHandler - An unexpected error occurred: 
java.lang.RuntimeException: Database connection failed
    at com.example.service.UserService.findById(UserService.java:42)
```

## 📖 Usage

### Success Responses (HTTP 200)

**With data:**
```java
@GetMapping
public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
    List<User> users = userService.findAll();
    return ApiResponse.success("Users retrieved successfully", users);
}
```

**Without data:**
```java
@DeleteMapping("/{id}")
public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ApiResponse.success("User deleted successfully");
}
```

### Created Responses (HTTP 201)

```java
@PostMapping
public ResponseEntity<ApiResponse<User>> createUser(@RequestBody UserDto dto) {
    User newUser = userService.create(dto);
    return ApiResponse.created("User created successfully", newUser);
}
```

### Custom Status Responses

**Without data:**
```java
@GetMapping("/health")
public ResponseEntity<ApiResponse<Void>> healthCheck() {
    if (!service.isHealthy()) {
        return ApiResponse.status("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }
    return ApiResponse.success("Service is healthy");
}
```

**With data:**
```java
@PutMapping("/{id}")
public ResponseEntity<ApiResponse<User>> updateUser(
        @PathVariable Long id, 
        @RequestBody UserDto dto) {
    User updated = userService.update(id, dto);
    return ApiResponse.status("User updated", updated, HttpStatus.OK);
}
```

### Error Handling with GlobalExceptionHandler

**Version 1.1.0+ includes a built-in `GlobalExceptionHandler`** with Spring Boot's `ProblemDetail` for standardized error responses:

```java
package io.github.pasinduog.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllExceptions(Exception ex) {
        log.error("An unexpected error occurred: ", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "Internal Server Error. Please contact technical support"
        );
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errorMessage = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMessage.merge(fieldError.getField(), fieldError.getDefaultMessage(),
                    (msg1, msg2) -> msg1 + "; " + msg2);
        }
        log.warn("Validation error: {}", errorMessage);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, 
            "Validation Failed"
        );
        problemDetail.setProperty("errors", errorMessage);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(NullPointerException.class)
    public ProblemDetail handleNullPointerExceptions(NullPointerException ex) {
        log.error("Null pointer exception occurred: ", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "A null pointer exception occurred."
        );
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
```

**The GlobalExceptionHandler provides:**
- 🛡️ **ProblemDetail RFC 7807** - Standard error format
- ✅ **Validation Error Handling** - Automatic `@Valid` annotation support
- 📝 **Comprehensive Logging** - SLF4J integration
- ⏰ **Automatic Timestamps** - On all error responses
- 🔍 **Null Pointer Protection** - Dedicated NullPointerException handling

**Example Validation Error Response:**
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation Failed",
  "errors": {
    "email": "must be a well-formed email address",
    "name": "must not be blank"
  },
  "timestamp": "2026-02-02T10:30:45.123Z"
}
```

You can also create custom exception handlers using ApiResponse:

```java
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.status(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ApiResponse.status("Invalid request: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
```

## 📚 API Reference

### Static Factory Methods

| Method | Parameters | Return Type | HTTP Status | Description |
|--------|-----------|-------------|-------------|-------------|
| `success(String message)` | message | `ResponseEntity<ApiResponse<Void>>` | 200 OK | Success without data |
| `success(String message, T data)` | message, data | `ResponseEntity<ApiResponse<T>>` | 200 OK | Success with data |
| `created(String message, T data)` | message, data | `ResponseEntity<ApiResponse<T>>` | 201 CREATED | Resource creation |
| `status(String message, HttpStatus status)` | message, status | `ResponseEntity<ApiResponse<Void>>` | Custom | Custom status without data |
| `status(String message, T data, HttpStatus status)` | message, data, status | `ResponseEntity<ApiResponse<T>>` | Custom | Custom status with data |

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `status` | `Integer` | HTTP status code (e.g., 200, 201, 404) - *Added in v1.2.0* |
| `traceId` | `UUID` | Unique identifier for request tracing and log correlation - *Added in v1.2.0* |
| `message` | `String` | Human-readable message describing the response |
| `data` | `T` (Generic) | Response payload (can be any type or null) |
| `timestamp` | `Instant` | ISO-8601 formatted UTC timestamp (auto-generated) |

## 🔍 Response Structure

All responses follow this consistent structure:

```json
{
  "status": 200,
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "string",
  "data": {},
  "timestamp": "2026-02-01T10:30:45.123456Z"
}
```

### Examples

**Single Object:**
```json
{
  "status": 200,
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Product found",
  "data": {
    "id": 1,
    "name": "Laptop",
    "price": 999.99
  },
  "timestamp": "2026-02-01T10:30:45.123Z"
}
```

**Array/List:**
```json
{
  "status": 200,
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Products retrieved",
  "data": [
    {"id": 1, "name": "Laptop"},
    {"id": 2, "name": "Mouse"}
  ],
  "timestamp": "2026-02-01T10:30:45.123Z"
}
```

**No Data (Void):**
```json
{
  "status": 200,
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Product deleted successfully",
  "data": null,
  "timestamp": "2026-02-01T10:30:45.123Z"
}
```

**Error Response:**
```json
{
  "status": 404,
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Product not found with ID: 123",
  "data": null,
  "timestamp": "2026-02-01T10:30:45.123Z"
}
```

## 💡 Best Practices

### 1. Use Appropriate Methods

```java
// ✅ Use success() for standard operations
return ApiResponse.success("Retrieved", data);

// ✅ Use created() for resource creation
return ApiResponse.created("Created", newResource);

// ✅ Use status() for custom status codes
return ApiResponse.status("Accepted", HttpStatus.ACCEPTED);
```

### 2. Write Clear Messages

```java
// ✅ Good - Descriptive and specific
return ApiResponse.success("User profile updated successfully", user);

// ❌ Avoid - Too generic
return ApiResponse.success("Success", user);
```

### 3. Leverage Generics

```java
// Specific types
ResponseEntity<ApiResponse<User>> getUser();
ResponseEntity<ApiResponse<List<Product>>> getProducts();
ResponseEntity<ApiResponse<Map<String, Object>>> getMetadata();
ResponseEntity<ApiResponse<Void>> deleteResource();
```

### 4. Custom Business Exceptions (New in v1.2.0)

**Version 1.2.0+ includes an abstract `ApiException` class** for creating domain-specific exceptions. The built-in `GlobalExceptionHandler` automatically handles them with the correct HTTP status:

```java
// Define custom exceptions
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with ID: %d", resource, id), HttpStatus.NOT_FOUND);
    }
}

public class InsufficientBalanceException extends ApiException {
    public InsufficientBalanceException(String accountId) {
        super("Insufficient balance in account: " + accountId, HttpStatus.PAYMENT_REQUIRED);
    }
}

// Use them in your service/controller
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", id));
    return ApiResponse.success("User found", user);
}
```

**Benefits:**
- ✅ No need to manually create `@ExceptionHandler` methods
- ✅ Automatic RFC 7807 ProblemDetail formatting
- ✅ Type-safe with compile-time checking
- ✅ Clean, readable code

You can still create additional custom exception handlers if needed:

```java
@ControllerAdvice
public class CustomExceptionHandler {
    
    @ExceptionHandler(ThirdPartyApiException.class)
    public ProblemDetail handleThirdPartyError(ThirdPartyApiException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_GATEWAY, ex.getMessage()
        );
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
```

### 5. RESTful Conventions

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping("/{id}")  // 200 OK
    public ResponseEntity<ApiResponse<Product>> get(@PathVariable Long id) {
        return ApiResponse.success("Product found", productService.findById(id));
    }

    @PostMapping  // 201 CREATED
    public ResponseEntity<ApiResponse<Product>> create(@RequestBody ProductDto dto) {
        return ApiResponse.created("Product created", productService.create(dto));
    }

    @PutMapping("/{id}")  // 200 OK
    public ResponseEntity<ApiResponse<Product>> update(
            @PathVariable Long id, @RequestBody ProductDto dto) {
        return ApiResponse.success("Product updated", productService.update(id, dto));
    }

    @DeleteMapping("/{id}")  // 200 OK
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.success("Product deleted");
    }
}
```

## 🧪 Testing

### Unit Testing with MockMvc

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void shouldReturnUserSuccessfully() throws Exception {
        User user = new User(1L, "John Doe", "john@example.com");
        when(userService.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("John Doe"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnCreatedStatus() throws Exception {
        User newUser = new User(1L, "Jane Doe", "jane@example.com");
        when(userService.create(any())).thenReturn(newUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Jane Doe\",\"email\":\"jane@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.traceId").exists());
    }
}
```

## 🔌 OpenAPI/Swagger Integration

Works seamlessly with SpringDoc OpenAPI:

```java
@Operation(summary = "Get user by ID", description = "Returns a single user")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "User found"),
    @ApiResponse(responseCode = "404", description = "User not found")
})
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return ApiResponse.success("User retrieved successfully", user);
}
```

**Add SpringDoc dependency:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

## ❓ FAQ

### How do I use trace IDs for debugging? *(New in v1.2.0)*

Every response includes an auto-generated UUID `traceId`. You can use it to correlate logs across your distributed system:

**Response:**
```json
{
  "status": 200,
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "User created",
  "data": {...},
  "timestamp": "2026-02-01T10:30:45.123Z"
}
```

**In your logs:**
```java
@Service
@Slf4j
public class UserService {
    public User createUser(UserDto dto) {
        log.info("Creating user with email: {}", dto.getEmail());
        // ... business logic
        return user;
    }
}
```

You can also pass the trace ID to downstream services for end-to-end tracing. To use a custom trace ID instead of auto-generated:

```java
UUID customTraceId = UUID.fromString(request.getHeader("X-Trace-Id"));
ApiResponse<User> response = ApiResponse.<User>builder()
    .status(HttpStatus.OK.value())
    .traceId(customTraceId)
    .message("User found")
    .data(user)
    .build();
return ResponseEntity.ok(response);
```

### How do I create custom business exceptions? *(New in v1.2.0)*

Extend the abstract `ApiException` class to create domain-specific exceptions:

```java
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with ID: %d", resource, id), HttpStatus.NOT_FOUND);
    }
}

public class DuplicateResourceException extends ApiException {
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
```

Then throw them in your code - the `GlobalExceptionHandler` will automatically convert them to RFC 7807 ProblemDetail responses:

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", id));
    return ApiResponse.success("User found", user);
}
```

### How do I use the built-in GlobalExceptionHandler?

**As of v1.3.0**, the `GlobalExceptionHandler` is **automatically configured** via Spring Boot Auto-Configuration. No manual setup is required!

Simply add the library dependency, and the exception handler will be active immediately. The auto-configuration mechanism automatically registers the handler when the library is detected on the classpath.

**For versions prior to 1.3.0**, you needed to ensure component scanning:

```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.yourapp", "io.github.pasinduog.exception"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Can I customize or extend the GlobalExceptionHandler?

Yes! You can add your own `@ControllerAdvice` handlers alongside the built-in one:

```java
@ControllerAdvice
public class CustomExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.status(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, ex.getMessage()
        );
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
```

### How do I disable the GlobalExceptionHandler?

**As of v1.3.0**, you can disable it by excluding the auto-configuration:

```java
@SpringBootApplication(exclude = ApiResponseAutoConfiguration.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Or in `application.properties`:
```properties
spring.autoconfigure.exclude=io.github.pasinduog.config.ApiResponseAutoConfiguration
```

**For versions prior to 1.3.0**, you needed to use component scanning filters:

```java
@SpringBootApplication
@ComponentScan(basePackages = "com.yourapp",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalExceptionHandler.class
    ))
public class Application {
    // ...
}
```

### How do I customize the timestamp format?

Configure Jackson's ObjectMapper:

```java
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}
```

### Can I add custom fields?

The library provides a standard structure. To add custom fields, wrap the response:

```java
public class ExtendedResponse<T> {
    private ApiResponse<T> response;
    private String requestId;
    private Map<String, String> metadata;
    // getters and setters
}
```

### Does this work with Spring WebFlux?

Currently designed for Spring MVC. For WebFlux, you'd need to adapt it to work with `Mono<ResponseEntity<ApiResponse<T>>>`.

### How do I handle paginated responses?

Use Spring's `Page` as the data type:

```java
@GetMapping
public ResponseEntity<ApiResponse<Page<User>>> getUsers(Pageable pageable) {
    Page<User> users = userService.findAll(pageable);
    return ApiResponse.success("Users retrieved", users);
}
```

### Why use this over plain ResponseEntity?

| Plain ResponseEntity | ApiResponse |
|---------------------|-------------|
| Inconsistent structure | ✅ Standardized everywhere |
| Manual timestamps | ✅ Automatic |
| No trace IDs | ✅ Built-in UUID trace IDs *(v1.2.0)* |
| No status in body | ✅ Status code included in response *(v1.2.0)* |
| Manual exception handling | ✅ Custom ApiException support *(v1.2.0)* |
| More boilerplate | ✅ Concise factory methods |
| No message field | ✅ Always includes message |

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup

```bash
# Clone the repository
git clone https://github.com/pasinduog/api-response.git
cd api-response

# Build the project
mvn clean install

# Run tests (when available)
mvn test

# Generate JavaDoc
mvn javadoc:javadoc
```

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](http://www.apache.org/licenses/LICENSE-2.0.txt) file for details.

## 📧 Contact

**Pasindu OG**
- 📧 Email: pasinduogdev@gmail.com
- 🐙 GitHub: [@pasinduog](https://github.com/pasinduog)
- 💻 Repository: [github.com/pasinduog/api-response](https://github.com/pasinduog/api-response)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Project Lombok for compile-time code generation
- The open-source community

## 📈 Version History

### 1.3.0 (February 2026) - Auto-Configuration & Stability Release

✅ **New Features:**
- **Spring Boot Auto-Configuration** - Added `ApiResponseAutoConfiguration` with automatic component registration
- **META-INF Auto-Configuration File** - Included `org.springframework.boot.autoconfigure.AutoConfiguration.imports` for Spring Boot 3.x
- **Zero Manual Configuration** - No more need for @ComponentScan or @Import annotations

🔧 **Improvements:**
- Updated Lombok version to 1.18.42 for improved compatibility and bug fixes
- Enhanced project stability and dependency management
- Improved JavaDoc documentation across all classes with comprehensive examples
- Added @since tags to all classes for better version tracking
- Refined build process and artifact generation

📝 **Documentation:**
- Added comprehensive auto-configuration documentation
- Updated FAQ section with auto-configuration details
- Enhanced all JavaDoc comments with detailed descriptions and examples
- Added migration notes for users upgrading from previous versions

🔧 **Technical Updates:**
- Maintained compatibility with Java 17+ and Spring Boot 3.2.0+
- Enhanced Maven Central publishing workflow
- Improved package structure and organization

### 1.2.0 (February 2026) - Enhanced Response & Custom Exceptions

✅ **New Features:**
- **Custom ApiException Support** - Abstract base class for creating domain-specific business exceptions
- **Automatic ApiException Handling** - GlobalExceptionHandler now catches and formats custom ApiException instances
- **Response Status Field** - Added `status` field to ApiResponse for explicit HTTP status code in response body
- **Trace ID Support** - Added `traceId` (UUID) field for distributed tracing and log correlation
- **Improved Timestamp Format** - Changed from `LocalDateTime` to `Instant` (UTC) for consistent timezone handling

🔧 **Improvements:**
- Better support for microservices architecture with trace IDs
- Enhanced debugging capabilities with status codes in response body
- Cleaner exception handling pattern for business logic errors
- More consistent timestamp format across all responses

📝 **Documentation:**
- Added comprehensive examples for custom ApiException usage
- Updated all response examples to include new fields
- Enhanced best practices section

### 1.1.0 (February 2026) - Exception Handling Update

✅ **New Features:**
- Built-in `GlobalExceptionHandler` with ProblemDetail (RFC 7807) support
- Automatic validation error handling for `@Valid` annotations
- Comprehensive exception logging with SLF4J
- Null pointer exception handling
- Standardized error response format with timestamps

🔧 **Improvements:**
- Enhanced error responses with structured format
- Better integration with Spring Boot validation
- Automatic error field aggregation for validation failures

### 1.0.0 (February 2026) - Initial Release

✅ **Core Features:**
- Standard API Response wrapper with generic type support
- Five static factory methods: `success()`, `created()`, `status()`
- Automatic ISO-8601 timestamp generation
- Full Spring Boot 3.2.0+ integration
- Immutable, thread-safe design
- Comprehensive JavaDoc documentation

🎯 **Roadmap:**
- Spring WebFlux support (reactive)
- Pagination metadata support
- OpenAPI schema generation
- Additional exception handlers

---

**⭐ If you find this library helpful, please give it a star on GitHub!**

Made with ❤️ by [Pasindu OG](https://github.com/pasinduog)