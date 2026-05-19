---
name: java-development
version: "1.0.0"
tags: [java, spring-boot, development, best-practices, patterns]
description: |
  Java/Spring Boot development skill for modern Java (17/21) and Spring Boot 3.x.
  Provides development patterns, best practices, and code templates.
  For code review, use the java-code-review skill.
allowed-tools:
  - Read
  - Write
  - Edit
  - Grep
  - Glob
  - Bash
  - WebFetch
---

# Java Development Skill (Modern)

A development skill designed for **Java 17/21** and **Spring Boot 3.x** projects.

> **Note:** For Java 8 / Spring Boot 2 legacy projects, use the `java-development-legacy` skill.

## Use Cases

- Implement Java/Spring Boot features
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
    // TODO: add validation
    // TODO: handle null
    User user = repo.findById(id).get();
    // ...
}

// ✅ Write production-ready code
public UserResponse process(@NotBlank String id) {
    return repo.findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new UserNotFoundException(id));
}
```

### 2. Follow SOLID Principles

| Principle | During Development |
|-----------|-------------------|
| SRP | One class = one responsibility |
| OCP | Extend via interfaces, not modifications |
| LSP | Subclasses must be substitutable |
| ISP | Split large interfaces |
| DIP | Depend on abstractions, not implementations |

### 3. Prefer Composition Over Inheritance

```java
// ❌ Inheritance for code reuse
public class AdminUser extends User {
    public boolean canManageUsers() { return true; }
}

// ✅ Composition with roles
public record User(String id, Set<Role> roles) {
    public boolean canManageUsers() {
        return roles.contains(Role.ADMIN);
    }
}
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
4. **Handle Errors** - Use ProblemDetail for API errors
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
            .created(URI.create("/api/users/" + created.id()))
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
        validateUniqueEmail(request.email());
        User user = userMapper.toEntity(request);
        return userMapper.toResponse(userRepository.save(user));
    }
}
```

### Repository Layer

```java
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesById(String id);
    
    boolean existsByEmail(String email);
}
```

### Error Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(EntityNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Entity Not Found");
        problem.setDetail(e.getMessage());
        return ResponseEntity.of(problem).build();
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", extractErrors(e));
        return ResponseEntity.of(problem).build();
    }
}
```

### DTO Pattern with Records

```java
// Request DTO
public record CreateUserRequest(
    @NotBlank String name,
    @Email String email,
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$") String phone
) {}

// Response DTO
public record UserResponse(
    String id,
    String name,
    String email,
    Instant createdAt
) {}

// Mapper
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(CreateUserRequest request);
}
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

### Common Patterns

```java
// Optional handling
return optional.map(this::transform)
    .orElseThrow(() -> new NotFoundException(id));

// Stream to collection
List<UserResponse> responses = users.stream()
    .map(mapper::toResponse)
    .toList();

// Map operations
Map<String, User> userMap = users.stream()
    .collect(Collectors.toMap(User::getId, Function.identity()));

// Grouping
Map<Role, List<User>> byRole = users.stream()
    .collect(Collectors.groupingBy(User::getRole));

// Async operations
CompletableFuture<User> future = CompletableFuture.supplyAsync(
    () -> userService.getUser(id),
    asyncExecutor
);
```

## Detailed Guides

### Modern Java & Spring Boot 3.x (Version-Specific)

| Topic | Document |
|-------|----------|
| **Java 17/21 Features** | [../java-code-review-skill/reference/java-17-21-features.md](../java-code-review-skill/reference/java-17-21-features.md) |
| **Spring Boot 3 Best Practices** | [../java-code-review-skill/reference/spring-boot-3.md](../java-code-review-skill/reference/spring-boot-3.md) |
| **Concurrency & Virtual Threads** | [../java-code-review-skill/reference/concurrency-modern.md](../java-code-review-skill/reference/concurrency-modern.md) |

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

See [templates/](templates/) for:
- [Entity Template](templates/entity-template.java)
- [Service Template](templates/service-template.java)
- [Controller Template](templates/controller-template.java)
- [Repository Template](templates/repository-template.java)
- [Test Template](templates/test-template.java)

## Development Checklist

See [assets/checklist.md](assets/checklist.md) for pre-commit checklist.
