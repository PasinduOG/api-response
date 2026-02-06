package io.github.pasinduog.dto;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Paginated API Response wrapper for Spring Boot Applications.
 * <p>
 * This record provides a standardized structure for paginated API responses, ensuring consistent
 * pagination metadata across all endpoints. It includes page information, total counts, and the
 * actual data content for the current page.
 * </p>
 * <p>
 * <b>New in v2.0.0:</b> Introduced as part of the pagination support feature to handle large
 * datasets efficiently. Supports both in-memory pagination and database-backed pagination scenarios.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Consistent pagination structure across all paginated endpoints</li>
 *   <li>In-memory pagination with automatic slicing via {@link #of(List, Integer, Integer)}</li>
 *   <li>Database pagination support with {@link #ofJDBC(List, Integer, Integer, Long)}</li>
 *   <li>Null-safe with automatic defaults (page=1, size=10)</li>
 *   <li>Empty-safe handling for null or empty content lists</li>
 *   <li>Immutable record type for thread safety</li>
 *   <li>Builder pattern support via Lombok @Builder</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 *
 * <h3>In-Memory Pagination (Auto-Slice):</h3>
 * <pre>
 * {@code
 * @GetMapping("/users")
 * public PagedApiResponse<UserDto> getUsers(
 *         @RequestParam(defaultValue = "1") Integer page,
 *         @RequestParam(defaultValue = "10") Integer size) {
 *
 *     List<UserDto> allUsers = userService.findAll();
 *     return PagedApiResponse.of(allUsers, page, size);
 *     // Automatically slices the list and calculates pagination metadata
 * }
 * }
 * </pre>
 *
 * <h3>Database Pagination (Pre-Fetched):</h3>
 * <pre>
 * {@code
 * @GetMapping("/products")
 * public PagedApiResponse<ProductDto> getProducts(
 *         @RequestParam(defaultValue = "1") Integer page,
 *         @RequestParam(defaultValue = "20") Integer size) {
 *
 *     // Fetch only the required page from database
 *     List<ProductDto> products = productRepository.findPage(page, size);
 *     Long totalCount = productRepository.count();
 *
 *     return PagedApiResponse.ofJDBC(products, page, size, totalCount);
 * }
 * }
 * </pre>
 *
 * <h3>Simple Pagination (Default Page & Size):</h3>
 * <pre>
 * {@code
 * @GetMapping("/items")
 * public PagedApiResponse<ItemDto> getItems() {
 *     List<ItemDto> items = itemService.findAll();
 *     return PagedApiResponse.of(items);
 *     // Uses default: page=1, size=10
 * }
 * }
 * </pre>
 *
 * <h2>Response JSON Structure:</h2>
 * <pre>
 * {
 *   "status": 200,
 *   "message": "Success",
 *   "content": [...],
 *   "page": 1,
 *   "size": 10,
 *   "totalElements": 150,
 *   "totalPages": 15
 * }
 * </pre>
 *
 * @param <T> The type of elements in the paginated content list.
 * @param status The HTTP status code (typically 200 for successful pagination).
 * @param message A descriptive message about the response (e.g., "Success").
 * @param content The list of items for the current page.
 * @param page The current page number (1-based indexing).
 * @param size The number of items per page.
 * @param totalElements The total number of items across all pages.
 * @param totalPages The total number of pages available.
 *
 * @author Pasindu OG
 * @version 2.0.0
 * @since 2.0.0
 * @see ApiResponse
 */
@Builder
@SuppressWarnings("unused")
public record PagedApiResponse<T>(Integer status, String message, List<T> content, Integer page, Integer size,
                                  Long totalElements, Integer totalPages) {

    /**
     * Creates a paginated response with default pagination parameters.
     * <p>
     * This is a convenience method that uses default values for page (1) and size (10).
     * The entire content list is automatically sliced to return only the first page.
     * </p>
     * <p>
     * <b>Use Case:</b> Quick pagination when you want to display the first page with standard settings.
     * </p>
     *
     * <h3>Example Usage:</h3>
     * <pre>
     * {@code
     * @GetMapping("/users")
     * public PagedApiResponse<UserDto> getUsers() {
     *     List<UserDto> allUsers = userService.findAll();
     *     return PagedApiResponse.of(allUsers);
     *     // Returns: page 1, size 10, auto-calculated totals
     * }
     * }
     * </pre>
     *
     * @param content The complete list of items to paginate (will be sliced automatically).
     * @param <T>     The type of elements in the content list.
     * @return A {@link PagedApiResponse} with page=1, size=10, and the first 10 items from the content list.
     */
    public static <T> PagedApiResponse<T> of(List<T> content) {
        List<T> safeContent = (content == null) ? List.of() : content;
        return PagedApiResponse.of(content, null, null);
    }

    /**
     * Creates a paginated response with automatic in-memory slicing.
     * <p>
     * This method takes the complete content list and automatically slices it to return only
     * the requested page. It calculates pagination metadata (totalElements, totalPages) based
     * on the full content list size.
     * </p>
     * <p>
     * <b>Best for:</b> Small to medium datasets where loading all data into memory is acceptable.
     * For large datasets, consider using {@link #ofJDBC(List, Integer, Integer, Long)} with
     * database-level pagination.
     * </p>
     *
     * <h3>Behavior:</h3>
     * <ul>
     *   <li>Null content is treated as an empty list</li>
     *   <li>Null or invalid page defaults to 1</li>
     *   <li>Null or invalid size defaults to 10</li>
     *   <li>Pages beyond available data return empty content with correct totals</li>
     *   <li>Automatically calculates totalPages based on content.size() / size</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>
     * {@code
     * @GetMapping("/products")
     * public PagedApiResponse<ProductDto> getProducts(
     *         @RequestParam(defaultValue = "1") Integer page,
     *         @RequestParam(defaultValue = "20") Integer size) {
     *
     *     List<ProductDto> allProducts = productService.findAll();
     *     return PagedApiResponse.of(allProducts, page, size);
     *     // Automatically slices to return items for the requested page
     * }
     *
     * // Example: page=2, size=10, content.size()=45
     * // Returns: items 11-20, totalElements=45, totalPages=5
     * }
     * </pre>
     *
     * @param content The complete list of items to paginate (will be sliced to the requested page).
     * @param page    The page number to retrieve (1-based). Defaults to 1 if null or &lt; 1.
     * @param size    The number of items per page. Defaults to 10 if null or &lt; 1.
     * @param <T>     The type of elements in the content list.
     * @return A {@link PagedApiResponse} containing the requested page slice with complete pagination metadata.
     */
    public static <T> PagedApiResponse<T> of(List<T> content, Integer page, Integer size) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : size;

        if (content == null || content.isEmpty()) {
            return PagedApiResponse.<T>builder()
                    .status(HttpStatus.OK.value())
                    .message("Success")
                    .content(List.of())
                    .page(p).size(s).totalElements(0L).totalPages(0)
                    .build();
        }

        long totalElements = content.size();
        int totalPages = (int) Math.ceil((double) totalElements / s);

        int fromIndex = (p - 1) * s;

        if (fromIndex >= totalElements) {
            return PagedApiResponse.<T>builder()
                    .status(HttpStatus.OK.value())
                    .message("Success")
                    .content(List.of())
                    .page(p).size(s)
                    .totalElements(totalElements).totalPages(totalPages)
                    .build();
        }

        int toIndex = Math.min(fromIndex + s, (int) totalElements);
        List<T> pagedContent = content.subList(fromIndex, toIndex);

        return PagedApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .content(pagedContent)
                .page(p).size(s)
                .totalElements(totalElements).totalPages(totalPages)
                .build();
    }

    /**
     * Creates a paginated response for database-backed pagination with default parameters.
     * <p>
     * This is a convenience method that uses default values for page (1) and size (10).
     * Unlike {@link #of(List)}, this method expects pre-paginated content from the database
     * and requires the total count to be provided separately.
     * </p>
     * <p>
     * <b>Use Case:</b> When you've fetched the first page from the database and have the total count.
     * </p>
     *
     * <h3>Example Usage:</h3>
     * <pre>
     * {@code
     * @GetMapping("/orders")
     * public PagedApiResponse<OrderDto> getOrders() {
     *     // Fetch first 10 orders from database
     *     List<OrderDto> orders = orderRepository.findPage(1, 10);
     *     Long totalCount = orderRepository.count();
     *
     *     return PagedApiResponse.ofJDBC(orders, totalCount);
     *     // Returns: page 1, size 10, with provided totalElements
     * }
     * }
     * </pre>
     *
     * @param content       The pre-paginated list of items for the current page (already fetched from database).
     * @param totalElements The total number of items across all pages (from COUNT query).
     * @param <T>           The type of elements in the content list.
     * @return A {@link PagedApiResponse} with page=1, size=10, and the provided content and total count.
     */
    public static <T> PagedApiResponse<T> ofJDBC(List<T> content, Long totalElements) {
        List<T> safeContent = (content == null) ? List.of() : content;
        return ofJDBC(safeContent, null, null, totalElements);
    }

    /**
     * Creates a paginated response for database-backed pagination with custom parameters.
     * <p>
     * This method is designed for scenarios where pagination is handled at the database level
     * (e.g., using SQL LIMIT/OFFSET or JPA Pageable). The content should already be pre-fetched
     * for the specific page, and the total count should be obtained from a separate COUNT query.
     * </p>
     * <p>
     * <b>Best for:</b> Large datasets where loading all data into memory is not feasible.
     * Database-level pagination is more efficient for millions of records.
     * </p>
     *
     * <h3>Performance Benefits:</h3>
     * <ul>
     *   <li>Only fetches data for the current page from database</li>
     *   <li>Reduces memory footprint significantly</li>
     *   <li>Faster response times for large datasets</li>
     *   <li>Scalable for production environments</li>
     * </ul>
     *
     * <h3>Behavior:</h3>
     * <ul>
     *   <li>Null content is treated as an empty list</li>
     *   <li>Null or invalid page defaults to 1</li>
     *   <li>Null or invalid size defaults to 10</li>
     *   <li>Uses provided totalElements for calculating totalPages</li>
     *   <li>No automatic slicing - content is used as-is</li>
     * </ul>
     *
     * <h3>Example Usage with JPA:</h3>
     * <pre>
     * {@code
     * @GetMapping("/customers")
     * public PagedApiResponse<CustomerDto> getCustomers(
     *         @RequestParam(defaultValue = "1") Integer page,
     *         @RequestParam(defaultValue = "50") Integer size) {
     *
     *     // Create Spring Data Pageable
     *     Pageable pageable = PageRequest.of(page - 1, size); // Spring uses 0-based
     *
     *     // Fetch only the current page from database
     *     Page<Customer> customerPage = customerRepository.findAll(pageable);
     *
     *     // Convert to DTOs
     *     List<CustomerDto> customers = customerPage.getContent()
     *         .stream()
     *         .map(CustomerMapper::toDto)
     *         .toList();
     *
     *     // Return paginated response
     *     return PagedApiResponse.ofJDBC(
     *         customers,
     *         page,
     *         size,
     *         customerPage.getTotalElements()
     *     );
     * }
     * }
     * </pre>
     *
     * <h3>Example Usage with Native SQL:</h3>
     * <pre>
     * {@code
     * @GetMapping("/transactions")
     * public PagedApiResponse<TransactionDto> getTransactions(
     *         @RequestParam(defaultValue = "1") Integer page,
     *         @RequestParam(defaultValue = "100") Integer size) {
     *
     *     // Calculate offset for SQL query
     *     int offset = (page - 1) * size;
     *
     *     // Fetch current page with LIMIT/OFFSET
     *     List<TransactionDto> transactions = jdbcTemplate.query(
     *         "SELECT * FROM transactions ORDER BY created_at DESC LIMIT ? OFFSET ?",
     *         new Object[]{size, offset},
     *         new TransactionRowMapper()
     *     );
     *
     *     // Get total count with separate query
     *     Long totalCount = jdbcTemplate.queryForObject(
     *         "SELECT COUNT(*) FROM transactions",
     *         Long.class
     *     );
     *
     *     return PagedApiResponse.ofJDBC(transactions, page, size, totalCount);
     * }
     * }
     * </pre>
     *
     * @param content       The pre-paginated list of items for the current page (already fetched from database).
     * @param page          The current page number (1-based). Defaults to 1 if null or &lt; 1.
     * @param size          The number of items per page. Defaults to 10 if null or &lt; 1.
     * @param totalElements The total number of items across all pages (from COUNT query).
     * @param <T>           The type of elements in the content list.
     * @return A {@link PagedApiResponse} containing the provided content with calculated pagination metadata.
     */
    public static <T> PagedApiResponse<T> ofJDBC(List<T> content, Integer page, Integer size, Long totalElements) {

        int finalPage = (page == null || page < 1) ? 1 : page;
        int finalSize = (size == null || size < 1) ? 10 : size;
        List<T> safeContent = (content == null) ? List.of() : content;

        int totalPages = (int) Math.ceil((double) totalElements / finalSize);

        return PagedApiResponse.<T>builder()
                .status(200)
                .message("Success")
                .content(safeContent)
                .page(finalPage)
                .size(finalSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}