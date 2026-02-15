package io.github.pasinduog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

/**
 * Standard API Response wrapper for Spring Boot Applications.
 * <p>
 * This class provides a consistent structure for all API responses, ensuring that
 * clients always receive a predictable format including a status code, message,
 * content payload, and metadata like timestamps for debugging.
 * </p>
 * <p>
 * <b>Important:</b> This class is used for <b>success responses only</b>. Error responses
 * use Spring's ProblemDetail format (RFC 9457) which includes trace IDs. Success responses
 * do not include trace IDs in the response body, but trace IDs can be propagated via
 * HTTP headers using {@link io.github.pasinduog.filter.TraceIdFilter} for distributed tracing.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Consistent response structure across all API endpoints</li>
 *   <li>ISO-8601 UTC timestamps for all responses</li>
 *   <li>Null-safe with Jackson's @JsonInclude for clean JSON output</li>
 *   <li>Fluent builder pattern via Lombok @Builder</li>
 *   <li>Static factory methods for common HTTP status codes</li>
 *   <li>Type-safe generic content payload</li>
 * </ul>
 *
 * <h2>Response Structure:</h2>
 * <pre>
 * {
 *   "status": 200,
 *   "message": "Operation successful",
 *   "content": { ... },
 *   "timestamp": "2026-02-15T10:30:45.123Z"
 * }
 * </pre>
 *
 * <h2>Usage Examples:</h2>
 * <pre>
 * {@code
 * // Simple success response with content
 * return ApiResponse.success("User fetched successfully", userDto);
 *
 * // Created response (201)
 * return ApiResponse.created("User created successfully", savedUser);
 *
 * // Success without content
 * return ApiResponse.success("User deleted successfully");
 *
 * // Custom status code
 * return ApiResponse.status("Custom message", content, HttpStatus.ACCEPTED);
 *
 * // With list content
 * List<UserDto> users = userService.findAll();
 * return ApiResponse.success("Users fetched successfully", users);
 * }
 * </pre>
 *
 * <h2>Distributed Tracing:</h2>
 * <p>
 * For distributed tracing support, use {@link io.github.pasinduog.filter.TraceIdFilter}
 * to add trace IDs to HTTP response headers (e.g., X-Trace-Id). Trace IDs are also
 * automatically included in error responses via the GlobalExceptionHandler.
 * </p>
 *
 * @author Pasindu OG
 * @version 2.0.1
 * @since 1.0.0
 * @param <T> The type of the content object included in the response body.
 * @see org.springframework.http.ResponseEntity
 * @see io.github.pasinduog.exception.GlobalExceptionHandler
 * @see io.github.pasinduog.filter.TraceIdFilter
 */
@Getter
@Builder
@SuppressWarnings("unused")
public class ApiResponse<T> {

    /**
     * The HTTP status code of the response (e.g., 200, 201, 400).
     * This mirrors the HTTP status but is included in the body for easier client-side parsing.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer status;

    /**
     * A descriptive message about the API result.
     * <p>
     * This message provides human-readable context about the operation result,
     * such as "User created successfully" or "Operation completed".
     * </p>
     * <p>
     * Best practices:
     * </p>
     * <ul>
     *   <li>Use clear, user-friendly language</li>
     *   <li>Be consistent across similar operations</li>
     *   <li>Avoid technical jargon when possible</li>
     *   <li>Include relevant entity names (e.g., "User", "Order")</li>
     * </ul>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String message;

    /**
     * The actual payload of the response.
     * <p>
     * Can be any type including:
     * </p>
     * <ul>
     *   <li>A single DTO object (e.g., {@code UserDto})</li>
     *   <li>A collection (e.g., {@code List&lt;UserDto&gt;})</li>
     *   <li>{@code null} if no content is returned (e.g., DELETE operations)</li>
     *   <li>Primitive wrappers (e.g., {@code Boolean}, {@code Integer})</li>
     *   <li>Complex nested structures</li>
     * </ul>
     * <p>
     * The content is serialized to JSON automatically by Jackson.
     * Use {@code @JsonInclude(JsonInclude.Include.NON_NULL)} on your DTOs to exclude null fields.
     * </p>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T content;

    /**
     * The UTC timestamp when the response was generated.
     * <p>
     * Defaults to the current system time in UTC using {@link Instant#now()}.
     * The timestamp is in ISO-8601 format when serialized to JSON.
     * </p>
     * <p>
     * Example JSON output: {@code "timestamp": "2026-02-15T10:30:45.123Z"}
     * </p>
     * <p>
     * This field is automatically populated during construction. If not explicitly provided,
     * it uses the current system time. The timestamp helps with:
     * </p>
     * <ul>
     *   <li>Request debugging and log correlation</li>
     *   <li>Performance monitoring and SLA tracking</li>
     *   <li>Cache validation and staleness detection</li>
     *   <li>Audit trails and compliance reporting</li>
     * </ul>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Instant timestamp;

    /**
     * Constructs an ApiResponse with all parameters.
     * <p>
     * This constructor is used internally by Lombok's {@code @Builder} annotation.
     * It is recommended to use static factory methods like {@link #success(String, Object)},
     * {@link #created(String, Object)}, or the builder pattern instead of calling this directly.
     * </p>
     *
     * @param status The HTTP status code (e.g., 200, 201, 400). Should match the HTTP response status.
     * @param message A descriptive message about the result (e.g., "User created successfully").
     * @param content The response payload content. Can be any type or null.
     * @param timestamp The timestamp when the response was created. If null, defaults to {@link Instant#now()}.
     */
    @SuppressWarnings("java:S107") // Constructor generated by Lombok @Builder, not called directly
    public ApiResponse(Integer status, String message, T content, Instant timestamp) {
        this.status = status;
        this.message = message;
        this.content = content;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    /**
     * Creates a response with HTTP 201 Created status.
     * <p>
     * This is typically used for POST requests where a new resource has been successfully created.
     * </p>
     * <p>
     * Example usage:
     * </p>
     * <pre>
     * {@code
     * @PostMapping("/users")
     * public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody UserDto userDto) {
     *     UserDto savedUser = userService.save(userDto);
     *     return ApiResponse.created("User created successfully", savedUser);
     * }
     * }
     * </pre>
     *
     * @param <T> The type of the content.
     * @param message The success message to be displayed to the client.
     * @param content The created content object (e.g., the saved User DTO).
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with HTTP 201 status.
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T content) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<T>builder()
                        .status(HttpStatus.CREATED.value())
                        .message(message)
                        .content(content)
                        .build());
    }

    /**
     * Creates a success response with HTTP 200 OK status without a content payload.
     * <p>
     * Useful for operations like DELETE or PUT where the action is successful but there is no
     * specific content to return.
     * </p>
     * <p>
     * Example usage:
     * </p>
     * <pre>
     * {@code
     * @DeleteMapping("/users/{id}")
     * public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
     *     userService.delete(id);
     *     return ApiResponse.success("User deleted successfully");
     * }
     * }
     * </pre>
     *
     * @param message The success message.
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with HTTP 200 status.
     */
    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .build());
    }

    /**
     * Creates a success response with HTTP 200 OK status and a content payload.
     * <p>
     * This is the most common method used for GET requests to return requested content.
     * </p>
     * <p>
     * Example usage:
     * </p>
     * <pre>
     * {@code
     * @GetMapping("/users/{id}")
     * public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id) {
     *     UserDto user = userService.findById(id);
     *     return ApiResponse.success("User fetched successfully", user);
     * }
     *
     * @GetMapping("/users")
     * public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
     *     List<UserDto> users = userService.findAll();
     *     return ApiResponse.success("Users fetched successfully", users);
     * }
     * }
     * </pre>
     *
     * @param <T> The type of the content.
     * @param message The success message.
     * @param content The content object to return (e.g., a UserDto or List&lt;UserDto&gt;).
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with HTTP 200 status.
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T content) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<T>builder()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .content(content)
                        .build());
    }

    /**
     * Creates a response with a custom HTTP status and no content payload.
     * <p>
     * Useful for error handling or specific status codes not covered by standard success methods.
     * </p>
     * <p>
     * Example usage:
     * </p>
     * <pre>
     * {@code
     * @PostMapping("/process")
     * public ResponseEntity<ApiResponse<Void>> process() {
     *     // Queue the processing job
     *     jobService.queueJob();
     *     return ApiResponse.status("Processing queued", HttpStatus.ACCEPTED);
     * }
     * }
     * </pre>
     *
     * @param message The message to include in the response.
     * @param status The specific {@link HttpStatus} to return.
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with the specified status.
     */
    public static ResponseEntity<ApiResponse<Void>> status(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.<Void>builder()
                        .status(status.value())
                        .message(message)
                        .build());
    }

    /**
     * Creates a response with a custom HTTP status and a content payload.
     * <p>
     * Provides maximum flexibility for custom response scenarios with any HTTP status code.
     * </p>
     * <p>
     * Example usage:
     * </p>
     * <pre>
     * {@code
     * @PutMapping("/users/{id}")
     * public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Long id, @RequestBody UserDto dto) {
     *     UserDto updated = userService.update(id, dto);
     *     return ApiResponse.status("User updated and queued for sync", updated, HttpStatus.ACCEPTED);
     * }
     * }
     * </pre>
     *
     * @param <T> The type of the content.
     * @param message The message to include in the response.
     * @param content The content object to return.
     * @param status The specific {@link HttpStatus} to return.
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with the specified status.
     */
    public static <T> ResponseEntity<ApiResponse<T>> status(String message, T content, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.<T>builder()
                        .status(status.value())
                        .message(message)
                        .content(content)
                        .build());
    }
}