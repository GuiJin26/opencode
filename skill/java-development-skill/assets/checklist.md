# Java Development Checklist

Pre-commit checklist for Java 17/21 / Spring Boot 3 development.

## Before Starting

- [ ] Requirements understood
- [ ] API design completed
- [ ] Edge cases identified
- [ ] Existing code patterns reviewed

## Code Quality

- [ ] Clear naming (classes, methods, variables)
- [ ] Single responsibility per class
- [ ] Method parameters ≤ 4
- [ ] Nesting depth ≤ 3 levels
- [ ] No magic values/strings
- [ ] No commented-out code

## Java 17/21 Features

- [ ] Use `record` for DTOs/POJOs
- [ ] Use switch expressions
- [ ] Use text blocks for multi-line strings
- [ ] Use pattern matching for instanceof
- [ ] Use sealed classes when appropriate

## Spring Boot 3

- [ ] Constructor injection (no @Autowired)
- [ ] @ConfigurationProperties for config
- [ ] @Transactional at service layer
- [ ] ProblemDetail for API errors
- [ ] Validation annotations on request DTOs

## Database

- [ ] @Transactional(readOnly = true) for reads
- [ ] Avoid N+1 (use JOIN FETCH)
- [ ] Entity has proper equals/hashCode
- [ ] No business logic in entities

## Security

- [ ] Input validation with Bean Validation
- [ ] Parameterized queries (no SQL injection)
- [ ] No sensitive data in logs
- [ ] Authorization checks present

## Performance

- [ ] No database queries in loops
- [ ] Pagination for list endpoints
- [ ] Caching for hot data
- [ ] Virtual threads for I/O operations

## Testing

- [ ] Unit tests for business logic
- [ ] Edge case tests
- [ ] Exception path tests
- [ ] Test names describe scenario

## Documentation

- [ ] Javadoc for public APIs
- [ ] README updated if needed
- [ ] API documentation updated

---

## Quick Patterns

### Constructor Injection

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
}
```

### Optional Handling

```java
return repo.findById(id)
    .map(mapper::toResponse)
    .orElseThrow(() -> new NotFoundException(id));
```

### Validation

```java
public record CreateUserRequest(
    @NotBlank String name,
    @Email String email,
    @Size(min = 8) String password
) {}
```

### Error Response

```java
@ExceptionHandler(EntityNotFoundException.class)
public ResponseEntity<ProblemDetail> handleNotFound(EntityNotFoundException e) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle("Not Found");
    problem.setDetail(e.getMessage());
    return ResponseEntity.of(problem).build();
}
```

### Async Processing

```java
@Async
public CompletableFuture<Order> processOrderAsync(String orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    // Process order
    return CompletableFuture.completedFuture(order);
}
```

## Common Mistakes to Avoid

| ❌ Avoid | ✅ Use Instead |
|----------|----------------|
| `@Autowired` field injection | Constructor injection |
| `@Data` on Entity | `@Getter` + `@Setter` + manual equals/hashCode |
| `SimpleDateFormat` | `DateTimeFormatter` |
| `HashMap` in concurrent context | `ConcurrentHashMap` |
| `SELECT *` on large tables | Select specific columns |
| Catching and swallowing exceptions | Proper exception handling |
| `==` for string comparison | `.equals()` |
| Returning entities from controllers | DTOs |
