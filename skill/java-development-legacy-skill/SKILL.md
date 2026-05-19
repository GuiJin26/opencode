---
name: java-development-legacy
version: "1.0.0"
tags: [java, spring-boot, development, best-practices, patterns, java-8, spring-boot-2]
description: |
  Java/Spring Boot development skill for legacy Java 8 and Spring Boot 2.x projects.
  Provides development patterns, best practices, and code templates.
  For code review, use the java-code-review-legacy skill.
allowed-tools:
  - Read
  - Write
  - Edit
  - Grep
  - Glob
  - Bash
  - WebFetch
---

# Java Development Skill (Legacy)

A development skill designed for **Java 8** and **Spring Boot 2.x** projects.

> **Note:** For Java 17/21 / Spring Boot 3 projects, use the `java-development` skill.

## Use Cases

- Implement Java/Spring Boot features for legacy projects
- Apply best practices and patterns
- Write clean, maintainable code
- Follow security guidelines
- Optimize performance during development

## AI Security Rules

> **Prerequisite:** Follow `ai-security` skill rules when using AI tools.

When AI generates code:
- Never generate hardcoded secrets (`apiKey = "sk-..."`)
- Always use environment variables (`${API_KEY}`)
- Use safe test data (`user@example.com`, `555-555-5555`)
- Validate AI output before using

Never let AI access:
- Production credentials
- Customer data
- Secret management systems

See: [ai-security skill](../../../ai-security-skill/SKILL.md) for full rules.

## Development Principles

### 1. Write Review-Ready Code

Code should pass code review standards from the start:

```java
// ❌ Write first, fix later
public void process(String id) {
    User user = repo.findById(id).get();
    // ...
}

// ✅ Write production-ready code
public UserResponse process(String id) {
    return repo.findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new UserNotFoundException(id));
}
```

### 2. Use Java 8 Features Effectively

```java
// Stream API for transformations
List<UserResponse> responses = users.stream()
    .map(this::toResponse)
    .collect(Collectors.toList());

// Optional for null safety
public User findUser(String id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
}

// CompletableFuture for async
CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(
    () -> userService.getUser(id)
);
```

## Development Workflow

### Phase 1: Design (Before Coding)

1. **Understand Requirements** - Read user story/issue carefully
2. **Design API First** - Define interfaces before implementation
3. **Plan Data Model** - Entity relationships, DTOs, mappers
4. **Consider Edge Cases** - Null handling, validation, error states
5. **Check Existing Code** - Reuse patterns, avoid duplication

### Phase 2: Implementation

1. **Write Tests First** - TDD when possible
2. **Implement Core Logic** - Service layer first
3. **Add Controllers** - Thin controllers, delegate to services
4. **Handle Errors** - Use custom error responses
5. **Add Logging** - Use SLF4J, log meaningful events

### Phase 3: Refinement

1. **Run Tests** - Unit and integration tests
2. **Check Performance** - N+1 queries, unnecessary loops
3. **Review Security** - Input validation, SQL injection
4. **Clean Up** - Remove commented code, fix warnings
5. **Document** - Javadoc for public APIs

## Core Development Patterns

### Controller Layer

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUser(id));
    }
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity
            .created(URI.create("/api/users/" + created.getId()))
            .body(created);
    }
}
```

### Service Layer

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Transactional(readOnly = true)
    public UserResponse getUser(String id) {
        return userRepository.findById(id)
            .map(userMapper::toResponse)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
    
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        validateUniqueEmail(request.getEmail());
        User user = userMapper.toEntity(request);
        return userMapper.toResponse(userRepository.save(user));
    }
}
```

### Error Handling (Spring Boot 2)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException e) {
        ErrorResponse error = new ErrorResponse(
            "ENTITY_NOT_FOUND",
            e.getMessage(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.toList());
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_FAILED",
            "Validation failed",
            errors,
            Instant.now()
        );
        return ResponseEntity.badRequest().body(error);
    }
}

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
    private Instant timestamp;
    
    // Overloaded constructor for validation errors
    private List<String> details;
    public ErrorResponse(String code, String message, List<String> details, Instant timestamp) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }
}
```

### DTO Pattern (Java 8)

```java
// Request DTO
@Data
public class CreateUserRequest {
    @NotBlank
    private String name;
    
    @Email
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    private String phone;
}

// Response DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private Instant createdAt;
}

// Mapper (MapStruct)
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(CreateUserRequest request);
}
```

## Java 8 Specific Patterns

### Optional Usage

```java
// ✅ Return Optional for nullable values
public Optional<User> findUser(String id) {
    return userRepository.findById(id);
}

// ✅ Map and transform
public Optional<String> findUserName(String id) {
    return userRepository.findById(id)
        .map(User::getName);
}

// ✅ Provide default
public User getUserOrDefault(String id) {
    return userRepository.findById(id)
        .orElse(defaultUser);
}

// ✅ Throw exception
public User getUserOrThrow(String id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
}

// ❌ Avoid isPresent + get
if (opt.isPresent()) {
    User user = opt.get();  // Bad pattern
}
```

### Stream API

```java
// Filter and collect
List<User> activeUsers = users.stream()
    .filter(User::isActive)
    .collect(Collectors.toList());

// Map to another type
List<UserResponse> responses = users.stream()
    .map(mapper::toResponse)
    .collect(Collectors.toList());

// Group by
Map<String, List<User>> byDepartment = users.stream()
    .collect(Collectors.groupingBy(User::getDepartment));

// Find first match
Optional<User> firstAdmin = users.stream()
    .filter(u -> u.getRoles().contains(Role.ADMIN))
    .findFirst();

// Parallel stream for CPU-intensive operations
long count = users.parallelStream()
    .filter(this::isEligible)
    .count();
```

### CompletableFuture

```java
// Async operation
CompletableFuture<User> future = CompletableFuture.supplyAsync(() -> 
    userRepository.findById(id).orElse(null)
);

// Combine multiple async operations
CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(
    () -> userService.getUser(userId)
);
CompletableFuture<List<Order>> ordersFuture = CompletableFuture.supplyAsync(
    () -> orderService.getOrders(userId)
);

CompletableFuture<UserWithOrders> result = userFuture.thenCombine(
    ordersFuture,
    (user, orders) -> new UserWithOrders(user, orders)
);

// Wait for all
CompletableFuture.allOf(future1, future2, future3).join();
```

### Date/Time API

```java
// Current time
Instant now = Instant.now();
LocalDate today = LocalDate.now();
LocalDateTime dateTime = LocalDateTime.now();

// Parse and format
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
LocalDateTime parsed = LocalDateTime.parse("2024-01-15 10:30:00", formatter);
String formatted = dateTime.format(formatter);

// Thread-safe formatter (can be static)
private static final DateTimeFormatter DATE_FORMATTER = 
    DateTimeFormatter.ofPattern("yyyy-MM-dd");

// Duration and Period
Duration duration = Duration.between(startTime, endTime);
Period period = Period.between(startDate, endDate);
```

## Quick Reference

### Annotations

| Annotation | When to Use |
|------------|-------------|
| `@Service` | Service layer classes |
| `@Repository` | Repository interfaces |
| `@RestController` | REST API controllers |
| `@Transactional` | Service methods modifying data |
| `@Transactional(readOnly = true)` | Read operations |
| `@RequiredArgsConstructor` | Constructor injection with Lombok |
| `@Valid` | Request body validation |

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Class | PascalCase | `UserService` |
| Method | camelCase (verb) | `getUserById` |
| Variable | camelCase | `userRepository` |
| Constant | SCREAMING_SNAKE | `MAX_RETRY_COUNT` |
| Package | lowercase | `com.example.user` |
| Entity | Singular noun | `User` (not `Users`) |
| Repository | Entity + Repository | `UserRepository` |
| Service | Entity + Service | `UserService` |
| Controller | Entity + Controller | `UserController` |

## Detailed Guides

### Java 8 & Spring Boot 2.x (Version-Specific)

| Topic | Document |
|-------|----------|
| **Java 8 Features** | [../java-code-review-legacy-skill/reference/java-8-features.md](../java-code-review-legacy-skill/reference/java-8-features.md) |
| **Spring Boot 2 Best Practices** | [../java-code-review-legacy-skill/reference/spring-boot-2.md](../java-code-review-legacy-skill/reference/spring-boot-2.md) |
| **Concurrency (Java 8)** | [../java-code-review-legacy-skill/reference/concurrency.md](../java-code-review-legacy-skill/reference/concurrency.md) |

### General Best Practices (Shared)

| Topic | Document |
|-------|----------|
| **Architecture Design** | [../java-code-review-skill/reference/architecture.md](../java-code-review-skill/reference/architecture.md) |
| **Code Quality Patterns** | [../java-code-review-skill/reference/code-quality.md](../java-code-review-skill/reference/code-quality.md) |
| **JPA & Database** | [../java-code-review-skill/reference/jpa-database.md](../java-code-review-skill/reference/jpa-database.md) |
| **Security Guidelines** | [../java-code-review-skill/reference/security.md](../java-code-review-skill/reference/security.md) |
| **Common Bugs to Avoid** | [../java-code-review-skill/reference/common-bugs.md](../java-code-review-skill/reference/common-bugs.md) |
| **Testing Standards** | [../java-code-review-skill/reference/testing.md](../java-code-review-skill/reference/testing.md) |
| **Performance Patterns** | [../java-code-review-skill/reference/performance.md](../java-code-review-skill/reference/performance.md) |

## Code Templates

See [templates/](templates/) for Java 8 / Spring Boot 2 specific templates.

## Development Checklist

See [assets/checklist.md](assets/checklist.md) for pre-commit checklist.
