# API Response Library

[![Maven Central](https://img.shields.io/maven-central/v/io.github.pasinduog/api-response.svg)](https://central.sonatype.com/artifact/io.github.pasinduog/api-response)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0+-brightgreen.svg)](https://spring.io/projects/spring-boot)

A lightweight, type-safe API Response wrapper for Spring Boot applications. Standardize your REST API responses with consistent structure, automatic timestamps, and clean factory methods.

## ✨ Features

- 🎯 **Consistent Structure** - All responses follow the same format: `message`, `data`, `timestamp`
- 🔒 **Type-Safe** - Full generic type support with compile-time type checking
- ⏰ **Auto Timestamps** - Automatic ISO-8601 formatted timestamps on every response
- 🏭 **Factory Methods** - Clean static methods: `success()`, `created()`, `status()`
- 🚀 **Zero Config** - Works immediately with no configuration required
- 🪶 **Lightweight** - Only ~15KB with minimal dependencies (Spring Web + Lombok)
- 📦 **Immutable** - Thread-safe with final fields
- 🔌 **Spring Native** - Built on `ResponseEntity` and `HttpStatus`

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
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.github.pasinduog:api-response:1.0.0'
```

### Gradle Kotlin DSL

```kotlin
implementation("io.github.pasinduog:api-response:1.0.0")
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
  "message": "User retrieved successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "timestamp": "2026-02-01T10:30:45.123"
}
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

### Error Handling with @ControllerAdvice

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.status(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ApiResponse.status("Invalid request: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        return ApiResponse.status("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
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
| `message` | `String` | Human-readable message describing the response |
| `data` | `T` (Generic) | Response payload (can be any type or null) |
| `timestamp` | `LocalDateTime` | ISO-8601 formatted timestamp (auto-generated) |

## 🔍 Response Structure

All responses follow this consistent structure:

```json
{
  "message": "string",
  "data": {},
  "timestamp": "2026-02-01T10:30:45.123456"
}
```

### Examples

**Single Object:**
```json
{
  "message": "Product found",
  "data": {
    "id": 1,
    "name": "Laptop",
    "price": 999.99
  },
  "timestamp": "2026-02-01T10:30:45.123"
}
```

**Array/List:**
```json
{
  "message": "Products retrieved",
  "data": [
    {"id": 1, "name": "Laptop"},
    {"id": 2, "name": "Mouse"}
  ],
  "timestamp": "2026-02-01T10:30:45.123"
}
```

**No Data (Void):**
```json
{
  "message": "Product deleted successfully",
  "data": null,
  "timestamp": "2026-02-01T10:30:45.123"
}
```

**Error Response:**
```json
{
  "message": "Product not found with ID: 123",
  "data": null,
  "timestamp": "2026-02-01T10:30:45.123"
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

### 4. Global Exception Handling

Always use `@ControllerAdvice` for consistent error responses:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.status(ex.getMessage(), HttpStatus.NOT_FOUND);
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
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("John Doe"))
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
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.id").value(1));
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
| More boilerplate | ✅ Concise |
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
- Validation error helpers
- Pagination metadata support
- OpenAPI schema generation

---

**⭐ If you find this library helpful, please give it a star on GitHub!**

Made with ❤️ by [Pasindu OG](https://github.com/pasinduog)
