# API Response Library

[![Maven Central](https://img.shields.io/maven-central/v/io.github.pasinduog/api-response.svg)](https://central.sonatype.com/artifact/io.github.pasinduog/api-response)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)

A lightweight and elegant API Response wrapper for Spring Boot applications that provides a consistent structure for all your REST API responses.

## 📋 Table of Contents

- [Features](#-features)
- [Requirements](#-requirements)
- [Installation](#-installation)
- [Quick Start](#-quick-start)
- [Usage Examples](#-usage-examples)
  - [Success Responses](#success-responses)
  - [Created Responses](#created-responses)
  - [Custom Status Responses](#custom-status-responses)
- [API Documentation](#-api-documentation)
- [Response Structure](#-response-structure)
- [Best Practices](#-best-practices)
- [Contributing](#-contributing)
- [License](#-license)
- [Contact](#-contact)

## ✨ Features

- **Consistent Response Structure**: Standardized API response format across your entire application
- **Type-Safe**: Full generic type support for any data payload
- **Automatic Timestamps**: Every response includes an ISO-8601 formatted timestamp
- **Builder Pattern**: Clean and fluent API for creating responses
- **Spring Boot Integration**: Seamlessly integrates with Spring's `ResponseEntity`
- **Zero Configuration**: Works out of the box with no additional setup
- **Lightweight**: Minimal dependencies (Spring Boot Web + Lombok)
- **Well Documented**: Comprehensive JavaDoc and examples

## 📦 Requirements

- Java 17 or higher
- Spring Boot 3.2.0 or higher
- Lombok (provided)

## 🚀 Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.pasinduog</groupId>
    <artifactId>api-response</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

Add the following to your `build.gradle`:

```gradle
implementation 'io.github.pasinduog:api-response:1.0.0'
```

### Gradle (Kotlin DSL)

Add the following to your `build.gradle.kts`:

```kotlin
implementation("io.github.pasinduog:api-response:1.0.0")
```

## 🎯 Quick Start

Here's a simple example to get you started:

```java
import io.github.pasinduog.ApiResponse;
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

## 📖 Usage Examples

### Success Responses

**Simple success message without data:**

```java
@DeleteMapping("/{id}")
public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ApiResponse.success("User deleted successfully");
}
```

**Response:**
```json
{
  "message": "User deleted successfully",
  "data": null,
  "timestamp": "2026-02-01T10:30:45.123"
}
```

**Success with data payload:**

```java
@GetMapping
public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
    List<User> users = userService.findAll();
    return ApiResponse.success("Users retrieved successfully", users);
}
```

**Response:**
```json
{
  "message": "Users retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com"
    },
    {
      "id": 2,
      "name": "Jane Smith",
      "email": "jane@example.com"
    }
  ],
  "timestamp": "2026-02-01T10:30:45.123"
}
```

### Created Responses

Use the `created()` method for resource creation endpoints (HTTP 201):

```java
@PostMapping
public ResponseEntity<ApiResponse<User>> createUser(@RequestBody UserDto userDto) {
    User newUser = userService.create(userDto);
    return ApiResponse.created("User created successfully", newUser);
}
```

**Response (HTTP 201 Created):**
```json
{
  "message": "User created successfully",
  "data": {
    "id": 3,
    "name": "Alice Johnson",
    "email": "alice@example.com"
  },
  "timestamp": "2026-02-01T10:30:45.123"
}
```

### Custom Status Responses

For responses with specific HTTP status codes:

**Without data payload:**

```java
@GetMapping("/check")
public ResponseEntity<ApiResponse<Void>> checkAvailability() {
    boolean available = service.checkAvailability();
    if (!available) {
        return ApiResponse.status("Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }
    return ApiResponse.success("Service is available");
}
```

**With data payload:**

```java
@PutMapping("/{id}")
public ResponseEntity<ApiResponse<User>> updateUser(
        @PathVariable Long id, 
        @RequestBody UserDto userDto) {
    User updatedUser = userService.update(id, userDto);
    return ApiResponse.status("User updated successfully", updatedUser, HttpStatus.OK);
}
```

**Partial content example:**

```java
@GetMapping("/partial")
public ResponseEntity<ApiResponse<UserPreview>> getPartialUser(@PathVariable Long id) {
    UserPreview preview = userService.getPreview(id);
    return ApiResponse.status("Partial user data", preview, HttpStatus.PARTIAL_CONTENT);
}
```

## 📚 API Documentation

### Methods

| Method | Parameters | Return Type | HTTP Status | Description |
|--------|-----------|-------------|-------------|-------------|
| `success(String message)` | message | `ResponseEntity<ApiResponse<Void>>` | 200 OK | Success response without data |
| `success(String message, T data)` | message, data | `ResponseEntity<ApiResponse<T>>` | 200 OK | Success response with data |
| `created(String message, T data)` | message, data | `ResponseEntity<ApiResponse<T>>` | 201 CREATED | Resource creation response |
| `status(String message, HttpStatus status)` | message, status | `ResponseEntity<ApiResponse<Void>>` | Custom | Custom status without data |
| `status(String message, T data, HttpStatus status)` | message, data, status | `ResponseEntity<ApiResponse<T>>` | Custom | Custom status with data |

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `message` | String | A human-readable message describing the response |
| `data` | Generic `<T>` | The response payload (can be any type or null) |
| `timestamp` | LocalDateTime | ISO-8601 formatted timestamp of when the response was created |

## 🔍 Response Structure

All responses follow this consistent structure:

```json
{
  "message": "Description of the operation result",
  "data": { },
  "timestamp": "2026-02-01T10:30:45.123456"
}
```

- **message**: A descriptive message about the operation
- **data**: The actual response payload (can be an object, array, or null)
- **timestamp**: Automatically generated timestamp in ISO-8601 format

## 💡 Best Practices

### 1. Use Appropriate Methods

- Use `success()` for standard GET, PUT, DELETE operations (HTTP 200)
- Use `created()` for POST operations that create resources (HTTP 201)
- Use `status()` for custom HTTP status codes

### 2. Provide Clear Messages

```java
// ✅ Good - Clear and descriptive
return ApiResponse.success("User profile retrieved successfully", user);

// ❌ Avoid - Too generic
return ApiResponse.success("Success", user);
```

### 3. Consistent Error Handling

Use with Spring's `@ControllerAdvice` for global exception handling:

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        return ApiResponse.status(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(ValidationException ex) {
        return ApiResponse.status("Validation failed", ex.getErrors(), HttpStatus.BAD_REQUEST);
    }
}
```

### 4. Type Safety

Leverage generics for type-safe responses:

```java
// Specific type
ResponseEntity<ApiResponse<User>> getUserResponse();

// List type
ResponseEntity<ApiResponse<List<User>>> getUsersResponse();

// Map type
ResponseEntity<ApiResponse<Map<String, Object>>> getMetadataResponse();

// No data
ResponseEntity<ApiResponse<Void>> getVoidResponse();
```

### 5. RESTful Endpoints

Use appropriate HTTP methods and status codes:

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping("/{id}")  // 200 OK
    public ResponseEntity<ApiResponse<Product>> getProduct(@PathVariable Long id) {
        return ApiResponse.success("Product found", productService.findById(id));
    }

    @PostMapping  // 201 CREATED
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody ProductDto dto) {
        return ApiResponse.created("Product created", productService.create(dto));
    }

    @PutMapping("/{id}")  // 200 OK
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long id, @RequestBody ProductDto dto) {
        return ApiResponse.success("Product updated", productService.update(id, dto));
    }

    @DeleteMapping("/{id}")  // 200 OK
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.success("Product deleted");
    }
}
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/pasinduog/api-response.git
   ```

2. Navigate to the project directory:
   ```bash
   cd api-response
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run tests:
   ```bash
   mvn test
   ```

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](http://www.apache.org/licenses/LICENSE-2.0.txt) file for details.

## 📧 Contact

**Pasindu OG**
- Email: pasinduogdev@gmail.com
- GitHub: [@pasinduog](https://github.com/pasinduog)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Project Lombok for reducing boilerplate code

## 📈 Version History

- **1.0.0** (2026) - Initial release
  - Core ApiResponse wrapper
  - Success, Created, and Custom Status methods
  - Full Spring Boot integration
  - Comprehensive documentation

---

Made with ❤️ by [Pasindu OG](https://github.com/pasinduog)
