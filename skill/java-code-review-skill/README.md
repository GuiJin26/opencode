/# Java Code Review Skill

A code review skill designed for Java developers, focusing on Java 17/21 features and Spring Boot 3 ecosystem best practices.

## Features

### Java/Spring Boot Specific
- **Java 17/21 Features** - Records, Switch expressions, Pattern Matching, Text blocks
- **Spring Boot 3 Best Practices** - Dependency injection, Configuration management, Exception handling
- **JPA Performance Optimization** - N+1 issues, Entity design, Batch operations
- **Concurrency Programming** - Virtual threads, Thread safety, Lock optimization
- **Security Review** - SQL injection, XSS, Authentication/Authorization, Sensitive data
- **Testing Standards** - Unit tests, Integration tests, Testcontainers

### Architecture & Quality (General)
- **Architecture Design Review** - SOLID principles, Layered architecture, Anti-pattern recognition
- **Code Quality Anti-patterns** - Parameter bloat, Leaky abstractions, TOCTOU, Redundant state
- **Performance Review** - Database optimization, API performance, Algorithm complexity

## Directory Structure

```
java-code-review-skill/
├── SKILL.md                    # Main skill file
├── README.md
├── reference/
│   ├── java-features.md        # Java 17/21 features
│   ├── spring-boot.md          # Spring Boot best practices
│   ├── jpa-database.md         # JPA & Database
│   ├── concurrency.md          # Concurrency & Virtual threads
│   ├── security.md             # Security review
│   ├── common-bugs.md          # Common bug list
│   ├── testing.md              # Testing standards
│   ├── architecture.md         # Architecture design review
│   ├── code-quality.md         # Code quality anti-patterns
│   └── performance.md          # Performance review
├── templates/
│   └── review-template.md      # PR review output template
└── assets/
    └── checklist.md            # Quick checklist
```

## Installation

Copy this directory to opencode skills directory:

```bash
cp -r java-code-review-skill ~/.config/opencode/skills/
```

## Usage

Activate the skill in opencode:

```
Use java-code-review to review this PR
```

## Review Process

1. **Phase 1** - Context understanding (2-3 min)
2. **Phase 2** - High-level design review (5-10 min)
3. **Phase 3** - Line-by-line review (10-20 min)
4. **Phase 4** - Summary & decision (2-3 min)

## Severity Markers

| Marker | Meaning |
|--------|---------|
| 🔴 `[blocking]` | Must fix |
| 🟡 `[important]` | Should fix |
| 🟢 `[nit]` | Minor issue |
| 💡 `[suggestion]` | Improvement suggestion |
| 🎉 `[praise]` | Excellent code |

## Review Dimensions

### Java Features

| Check Item | Description |
|------------|-------------|
| Record | Use Record for DTOs/value objects |
| Switch Expression | Avoid traditional switch break pitfalls |
| Optional | Only for return values, use functional API |
| Stream | Don't overuse, use for-each for simple loops |

### Spring Boot

| Check Item | Description |
|------------|-------------|
| Dependency Injection | Constructor injection, avoid @Autowired |
| Configuration | @ConfigurationProperties type-safe |
| Transactions | Managed at Service layer, readOnly for reads |
| Exceptions | @ControllerAdvice global handling |

### Database

| Check Item | Description |
|------------|-------------|
| N+1 | JOIN FETCH / @EntityGraph |
| Entity | Don't use @Data, correct equals/hashCode |
| Indexes | WHERE condition columns indexed |
| Batch | saveAll / batch update JPQL |

### Architecture

| Check Item | Description |
|------------|-------------|
| SOLID | Single responsibility, Open-closed, etc. |
| Layers | Controller → Service → Repository |
| Coupling | Inter-class coupling < 5 |
| Cohesion | LCOM4 = 1 |

### Performance

| Check Item | Description |
|------------|-------------|
| Algorithm | Avoid O(n²) nested loops |
| Pagination | List endpoints must paginate |
| Cache | Use cache for hot data |
| Connection Pool | Configure HikariCP properly |

### Security

| Check Item | Description |
|------------|-------------|
| SQL Injection | Parameterized queries |
| Authentication | Spring Security configuration |
| Passwords | BCrypt encryption |
| Logging | Mask sensitive information |

## Quick Reference

### Spring Boot Dependency Injection

```java
// ✅ Recommended: Constructor injection
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
}

// ❌ Avoid: Field injection
@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;
}
```

### Record Usage

```java
// ✅ Use Record for DTOs
public record UserDto(String name, int age) {}

// ❌ Don't use Record for Entities
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

### SOLID Quick Check

| Principle | Check Question |
|-----------|----------------|
| SRP | How many reasons does this class have to change? |
| OCP | Does adding a new type require modifying this class? |
| LSP | Can subclasses fully replace the parent class? |
| ISP | Are all interface methods being used? |
| DIP | Does it depend on abstractions or concrete implementations? |

## License

MIT
