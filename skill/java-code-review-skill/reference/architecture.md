# Architecture Design Review Guide

Java/Spring Boot architecture design review points, covering SOLID principles, layered architecture, design pattern evaluation, etc.

## SOLID Principles

### S - Single Responsibility Principle (SRP)

**Warning Signs:**
```
⚠️ Class name contains "Manager", "Handler", "Service", "Util"
⚠️ A class exceeds 200-300 lines
⚠️ Class has more than 5-7 public methods
⚠️ Different methods operate on completely different data
```

```java
// ❌ Violating SRP: UserService takes on too many responsibilities
@Service
public class UserService {
    public User createUser(UserDto dto) { ... }
    public void sendWelcomeEmail(User user) { ... }  // Email responsibility
    public void validatePassword(String password) { ... }  // Validation responsibility
    public UserPreferences loadPreferences(Long userId) { ... }  // Preferences responsibility
    public void auditUserAction(User user, String action) { ... }  // Audit responsibility
}

// ✅ Responsibility separation
@Service
public class UserService {
    private final EmailService emailService;
    private final UserPreferencesService prefsService;
    private final AuditService auditService;
    
    public User createUser(UserDto dto) {
        User user = saveUser(dto);
        emailService.sendWelcome(user);
        auditService.logUserCreation(user);
        return user;
    }
}
```

### O - Open-Closed Principle (OCP)

**Warning Signs:**
```
⚠️ switch/if-else chains handling different types
⚠️ Adding new features requires modifying core classes
⚠️ instanceof type checks scattered throughout code
```

```java
// ❌ Violating OCP: Must modify this class for every new payment method
@Service
public class PaymentService {
    public Payment processPayment(String type, BigDecimal amount) {
        if ("credit".equals(type)) {
            return processCreditPayment(amount);
        } else if ("paypal".equals(type)) {
            return processPaypalPayment(amount);
        } else if ("crypto".equals(type)) {
            return processCryptoPayment(amount);
        }
        throw new IllegalArgumentException("Unknown payment type");
    }
}

// ✅ Use Strategy pattern, new payment methods don't require modification
public interface PaymentStrategy {
    Payment process(BigDecimal amount);
    String getType();
}

@Service
public class PaymentService {
    private final Map<String, PaymentStrategy> strategies;
    
    public PaymentService(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(PaymentStrategy::getType, Function.identity()));
    }
    
    public Payment processPayment(String type, BigDecimal amount) {
        PaymentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown payment type: " + type);
        }
        return strategy.process(amount);
    }
}
```

### L - Liskov Substitution Principle (LSP)

```java
// ❌ Violating LSP: Subclass changes parent's behavior contract
public class Bird {
    public void fly() { ... }
}

public class Penguin extends Bird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException("Penguins can't fly");
    }
}

// ✅ Correct abstraction level
public interface Bird { }

public interface FlyingBird extends Bird {
    void fly();
}

public class Eagle implements FlyingBird { }
public class Penguin implements Bird { }
```

### I - Interface Segregation Principle (ISP)

```java
// ❌ Violating ISP: Interface too large
public interface UserService {
    User createUser(UserDto dto);
    User findById(Long id);
    void deleteUser(Long id);
    void sendEmail(Long userId, String message);
    void resetPassword(Long userId);
    void updatePreferences(Long userId, Preferences prefs);
    void auditAction(Long userId, String action);
}

// ✅ Interface separation
public interface UserRepository {
    User create(UserDto dto);
    Optional<User> findById(Long id);
    void delete(Long id);
}

public interface UserEmailService {
    void sendWelcome(User user);
    void sendPasswordReset(User user);
}

public interface UserPreferencesService {
    Preferences getPreferences(Long userId);
    void updatePreferences(Long userId, Preferences prefs);
}
```

### D - Dependency Inversion Principle (DIP)

```java
// ❌ Violating DIP: High-level module depends on concrete implementation
@Service
public class OrderService {
    private final MySQLOrderRepository orderRepo = new MySQLOrderRepository();
    private final S3FileStorage fileStorage = new S3FileStorage();
}

// ✅ Depend on abstractions
@Service
public class OrderService {
    private final OrderRepository orderRepo;
    private final FileStorage fileStorage;
    
    public OrderService(OrderRepository orderRepo, FileStorage fileStorage) {
        this.orderRepo = orderRepo;
        this.fileStorage = fileStorage;
    }
}
```

---

## Layered Architecture

### Standard Layers

```
┌─────────────────────────────────────┐
│         Presentation Layer          │ ← Controller, REST API
├─────────────────────────────────────┤
│         Application Layer           │ ← Service, Use Cases
├─────────────────────────────────────┤
│            Domain Layer             │ ← Entity, Domain Service
├─────────────────────────────────────┤
│         Infrastructure Layer        │ ← Repository, External Services
└─────────────────────────────────────┘
           ↑ Dependencies only point inward ↑
```

### Layer Boundary Checks

```java
// ❌ Layer violation: Entity directly depends on JPA annotations and HTTP
@Entity
public class User {
    @Id
    private Long id;
    
    // Entity shouldn't contain HTTP-related logic
    public ResponseEntity<Void> toResponse() { ... }
}

// ❌ Layer violation: Service directly operates HttpServletRequest
@Service
public class UserService {
    public void createUser(HttpServletRequest request) {
        String name = request.getParameter("name");
    }
}

// ✅ Correct layering: Entity is pure, Controller handles HTTP
@Entity
public class User {
    @Id
    private Long id;
    private String name;
}

@RestController
public class UserController {
    @PostMapping("/users")
    public ResponseEntity<Void> createUser(@RequestBody UserDto dto) {
        userService.createUser(dto);
        return ResponseEntity.ok().build();
    }
}
```

### Review Checklist

- [ ] Does Controller only handle HTTP layer (parameter binding, response formatting)?
- [ ] Does Service only contain business logic?
- [ ] Is Repository only responsible for data access?
- [ ] Is Entity decoupled from framework (pure POJO/Record)?
- [ ] Are there cross-layer calls (Controller directly calling Repository)?

---

## Architecture Anti-patterns

### Fatal Anti-patterns

| Anti-pattern | Warning Signs | Impact |
|--------------|---------------|--------|
| **God Class** | Single class > 1000 lines, > 10 dependencies | Hard to test, hard to maintain |
| **Circular Dependency** | A → B → C → A | Startup failure, hard to understand |
| **Big Ball of Mud** | No clear module boundaries | Can't extend, hard to modify |

### Design Anti-patterns

```java
// ❌ Over-engineering: Complex patterns for simple needs
// Only one payment method, but implementing full strategy pattern factory

// ❌ Golden Hammer: Using inheritance for everything
public class AdminUser extends User { }
public class PremiumUser extends User { }
public class TrialUser extends User { }
// Should use composition or state pattern

// ❌ Boat Anchor: Code written for "might need in future"
public interface FutureFeature {
    void methodNoOneUses();
}
```

---

## Coupling Assessment

### Coupling Types (Best to Worst)

| Type | Description | Java Example |
|------|-------------|--------------|
| **Message Coupling** ✅ | Pass through parameters | `calculate(price, quantity)` |
| **Data Coupling** ✅ | Shared DTO | `processOrder(orderDTO)` |
| **Stamp Coupling** ⚠️ | Pass large object but use only part | Pass entire User but only use name |
| **Control Coupling** ⚠️ | Pass control flags | `process(data, isAdmin)` |
| **Common Coupling** ❌ | Static variables | `public static Map cache` |
| **Content Coupling** ❌ | Access private members | Reflection to access private fields |

### Metrics

```yaml
Coupling Metrics:
  CBO (Coupling Between Objects):
    Good: < 5
    Warning: 5-10
    Dangerous: > 10

Cohesion Metrics:
  LCOM4 (Lack of Cohesion of Methods):
    1: Single responsibility ✅
    2-3: May need splitting ⚠️
    >3: Should split ❌
```

---

## Design Pattern Evaluation

### When to Use

| Pattern | Use Case | Java Example |
|---------|----------|--------------|
| **Strategy** | Algorithm switches at runtime | Payment methods, sorting algorithms |
| **Factory** | Complex creation logic | Multiple database connections |
| **Builder** | Multi-parameter construction | Complex object building |
| **Template Method** | Fixed process with varying details | Report generation |
| **Decorator** | Dynamically add responsibilities | HTTP interceptor chain |
| **Observer** | Event-driven | Spring Event |

### Over-design Warning

```
⚠️ Patternitis warning signs:
1. Interface with only one implementation
2. Simple if/else replaced by strategy+factory+registry
3. Adding abstraction layers for "might need in future"
4. New developers need long time to understand code structure
```

---

## Code Structure

### Package Organization

```
// ✅ Organize by domain (recommended)
com.example.
├── user/
│   ├── domain/        # User Entity, Value Objects
│   ├── application/   # UserService, CreateUserUseCase
│   ├── infrastructure/# UserRepositoryImpl
│   └── presentation/  # UserController, UserDto
├── order/
│   └── ...
└── shared/           # Shared utilities

// ⚠️ Organize by technical layer (not recommended)
com.example.
├── controller/       # All Controllers mixed together
├── service/
├── repository/
└── entity/
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Class name | PascalCase, noun | `UserService`, `OrderRepository` |
| Method name | camelCase, verb prefix | `createUser`, `findOrderById` |
| Interface name | Adjective or noun | `Runnable`, `UserRepository` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Package name | lowercase, singular | `com.example.user` |

### Size Guidelines

```yaml
Recommended Limits:
  Single file: < 300 lines
  Single method: < 30 lines
  Single class: < 200 lines
  Method parameters: < 4
  Nesting depth: < 4 levels
  Class dependencies: < 5
```

---

## Review Checklist

### 🔴 Blocking Level

- [ ] Are there circular dependencies?
- [ ] God class (> 1000 lines or > 10 dependencies)?
- [ ] Does Entity depend on framework implementation?
- [ ] Are there hardcoded configurations and secrets?

### 🟡 Important Level

- [ ] Class coupling (CBO) > 10?
- [ ] Method parameters more than 5?
- [ ] Nesting depth more than 4 levels?
- [ ] Interface with only one implementation (over-design)?
- [ ] Duplicate code blocks > 10 lines?

### 🟢 Suggestion Level

- [ ] Is package structure organized by domain?
- [ ] Are naming conventions followed?
- [ ] Is there unused code?
