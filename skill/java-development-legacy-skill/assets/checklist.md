# Java Development Checklist (Legacy)

Pre-commit checklist for Java 8 / Spring Boot 2 development.

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

## Java 8 Features

- [ ] Use Optional for nullable returns
- [ ] Use Stream API for collections
- [ ] Use DateTimeFormatter instead of SimpleDateFormat
- [ ] Use java.time instead of Date/Calendar
- [ ] Use CompletableFuture for async operations

## Spring Boot 2

- [ ] Constructor injection (no @Autowired)
- [ ] @ConfigurationProperties for config
- [ ] @Transactional at service layer
- [ ] Custom ErrorResponse for API errors
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
- [ ] Thread pools for async operations

## Testing

- [ ] Unit tests for business logic
- [ ] Edge case tests
- [ ] Exception path tests
- [ ] Test names describe scenario

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

### Stream Operations

```java
List<UserResponse> responses = users.stream()
    .map(mapper::toResponse)
    .collect(Collectors.toList());
```

### Date/Time

```java
private static final DateTimeFormatter DTF = 
    DateTimeFormatter.ofPattern("yyyy-MM-dd");

LocalDate date = LocalDate.parse("2024-01-15", DTF);
String formatted = date.format(DTF);
```

### Async with CompletableFuture

```java
CompletableFuture<User> future = CompletableFuture.supplyAsync(
    () -> userService.getUser(id)
);

// Combine multiple futures
CompletableFuture.allOf(future1, future2).join();
```

## Common Mistakes to Avoid

| ❌ Avoid | ✅ Use Instead |
|----------|----------------|
| `@Autowired` field injection | Constructor injection |
| `@Data` on Entity | `@Getter` + `@Setter` + manual equals/hashCode |
| `SimpleDateFormat` | `DateTimeFormatter` |
| `HashMap` in concurrent context | `ConcurrentHashMap` |
| `Date` / `Calendar` | `java.time` API |
| `SELECT *` on large tables | Select specific columns |
| Catching and swallowing exceptions | Proper exception handling |
| `==` for string comparison | `.equals()` |
| Returning entities from controllers | DTOs |
| `isPresent()` + `get()` | `map()`, `orElse()`, `orElseThrow()` |
