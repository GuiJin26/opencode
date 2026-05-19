# Java Code Review Checklist

Quick review checklist, sorted by priority.

## Pre-Review (2 min)

- [ ] Read PR description and linked Issues
- [ ] Check PR size (>400 lines suggests splitting)
- [ ] Verify CI status (tests passing)
- [ ] Understand business context

## Architecture Design (5 min)

- [ ] Does the solution match the problem scale
- [ ] Class/package structure is reasonable
- [ ] Dependencies are clear with no cycles
- [ ] Responsibilities are well-defined (SRP)
- [ ] Correct layering (Controller → Service → Repository)

## Java Features (3 min)

- [ ] DTOs use Record
- [ ] Switch uses expressions
- [ ] Optional only for return values
- [ ] Avoid deprecated APIs (Date, SimpleDateFormat)
- [ ] Stream not overused

## Spring Boot (5 min)

- [ ] Constructor injection (not @Autowired)
- [ ] @ConfigurationProperties for configuration
- [ ] Controller only handles HTTP layer
- [ ] Service manages transactions
- [ ] Global exception handling

## Database (5 min)

- [ ] No N+1 queries
- [ ] Read operations `readOnly = true`
- [ ] Entity doesn't use @Data
- [ ] Indexes cover queries
- [ ] Batch operations optimized

## Concurrency Safety (3 min)

- [ ] Shared variables are thread-safe
- [ ] DateTimeFormatter instead of SimpleDateFormat
- [ ] ConcurrentHashMap instead of HashMap
- [ ] Lock granularity is reasonable

## Performance (3 min)

- [ ] No O(n²) nested loops
- [ ] List endpoints paginated
- [ ] No SELECT * on large tables
- [ ] Hot data cached
- [ ] No database queries in loops

## Security (5 min)

- [ ] No SQL injection (parameterized queries)
- [ ] Input validation (Bean Validation)
- [ ] Password encryption (BCrypt)
- [ ] Sensitive data not logged
- [ ] Permission controls complete

## Code Quality (3 min)

- [ ] Clear naming
- [ ] No magic values/strings
- [ ] Method parameters ≤ 4
- [ ] No duplicate code
- [ ] Nesting depth ≤ 3 levels

## Testing (5 min)

- [ ] Unit tests cover core logic
- [ ] Edge case tests
- [ ] Exception path tests
- [ ] Clear test naming

---

## Severity Markers

| Marker | Meaning | Action |
|--------|---------|--------|
| 🔴 `[blocking]` | Must fix | Blocks merge |
| 🟡 `[important]` | Should fix | Recommend fix |
| 🟢 `[nit]` | Minor issue | Non-blocking |
| 💡 `[suggestion]` | Suggestion | Optional |
| 🎉 `[praise]` | Excellent | Praise |

## Time Budget

| PR Size | Suggested Time |
|---------|----------------|
| < 100 lines | 10-15 min |
| 100-400 lines | 20-40 min |
| > 400 lines | Suggest splitting |

## Red Flags

### Architecture Level
- Circular dependencies
- God class (> 1000 lines or > 10 dependencies)
- Entity depends on framework implementation
- Hardcoded passwords/API keys

### Code Level
- `@Autowired` field injection
- `@Data` on Entity
- `SimpleDateFormat` static instance
- `HashMap` shared across threads
- Empty catch blocks
- Database calls in loops
- `==` for string comparison

### Performance Level
- N+1 queries
- Unpaginated large lists
- O(n²) algorithms
- SELECT * on large tables

### Security Level
- SQL string concatenation
- Plaintext passwords
- Sensitive info in logs
- Sensitive endpoints without permission control

## Quick Reference

### Dependency Injection

```java
// ✅ Constructor injection
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
}
```

### Record

```java
// ✅ DTO
public record UserDto(String name, int age) {}

// ❌ Entity shouldn't use Record
@Entity
public class User { ... }
```

### Avoid N+1

```java
// ✅ JOIN FETCH
@Query("SELECT u FROM User u JOIN FETCH u.orders")
List<User> findAllWithOrders();
```

### Thread Safety

```java
// ✅ DateTimeFormatter
private static final DateTimeFormatter DTF = 
    DateTimeFormatter.ofPattern("yyyy-MM-dd");

// ✅ ConcurrentHashMap
private static final Map<String, String> CACHE = new ConcurrentHashMap<>();
```

### SOLID Check

| Principle | Check Question |
|-----------|----------------|
| SRP | How many reasons does this class have to change? |
| OCP | Does adding a new type require modifying this class? |
| LSP | Can subclasses fully replace the parent class? |
| ISP | Are all interface methods being used? |
| DIP | Does it depend on abstractions or concrete implementations? |
