# Code Quality Review Guide

Java general code quality anti-patterns and best practices.

## Code Reuse Review

### Search Existing Utilities

```java
// ❌ Reinventing the wheel - project already has StringUtils
public String capitalize(String str) {
    if (str == null || str.isEmpty()) return str;
    return str.substring(0, 1).toUpperCase() + str.substring(1);
}

// ✅ Use existing utility class
String result = StringUtils.capitalize(str);
```

```java
// ❌ Hand-written date formatting - project already has DateUtils
String formatted = new SimpleDateFormat("yyyy-MM-dd").format(date);

// ✅ Use existing utility class
String formatted = DateUtils.formatStandard(date);
```

**Review Points:**
- Do new methods duplicate or overlap with existing utilities?
- Check `src/main/java/**/util/` and `common/` directories
- Can Apache Commons / Guava / Hutool be used?

---

## Parameter Bloat

### Too Many Function Parameters

```java
// ❌ Parameters keep growing
public User createUser(String name, String email, String role, 
                       String team, boolean active, String avatarUrl,
                       String timezone, String locale) {
    // ...
}

// ✅ Use parameter object
public record CreateUserParams(
    String name,
    String email,
    String role,
    String team,
    boolean active,
    String avatarUrl,
    String timezone,
    String locale
) {}

public User createUser(CreateUserParams params) {
    // ...
}

// ✅ Or use Builder pattern
User user = User.builder()
    .name("Alice")
    .email("alice@example.com")
    .role("admin")
    .build();
```

**Review Points:**
- Method parameters ≥ 4? Consider parameter object or Builder
- Is the new parameter just a boolean flag? Consider enum
- Are there mutually exclusive parameters like `enableX`, `disableY`?

---

## Leaky Abstraction

### Exposing Internal Implementation

```java
// ❌ Service returns JPA Entity - caller forced to know about Hibernate
public User getUser(Long id) {
    return userRepository.findById(id).orElse(null);
}
// Problem: Entity may have lazy loading, may be accidentally modified

// ✅ Return DTO, hide persistence details
public UserDto getUser(Long id) {
    return userRepository.findById(id)
        .map(UserDto::from)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
}

// ❌ Method signature exposes concrete implementation
public List<User> getUsersFromMySQL() { }

// ✅ Signature hides implementation details
public List<User> getUsers() { }
```

**Review Points:**
- Does Service directly return Entity?
- Does method name expose concrete implementation (MySQL, Redis, S3)?
- Does return type expose third-party library types?

---

## String Typing

### Using Strings Instead of Enums/Constants

```java
// ❌ Magic strings scattered everywhere
if ("active".equals(user.getStatus())) { }
if ("admin".equals(user.getRole())) { }
if ("USD".equals(currency)) { }

// ✅ Use enum
public enum UserStatus {
    ACTIVE, SUSPENDED, ARCHIVED
}

public enum UserRole {
    ADMIN, MANAGER, VIEWER
}

if (user.getStatus() == UserStatus.ACTIVE) { }
if (user.getRole() == UserRole.ADMIN) { }

// ✅ International currency using enum
public enum Currency {
    USD, EUR, GBP, CNY
}
```

```java
// ❌ String event name - typos won't error
applicationEventPublisher.publishEvent("userCreated", user);
@EventListener("usercreated")  // typo!

// ✅ Type-safe event class
public class UserCreatedEvent {
    private final User user;
}

@EventListener
public void onUserCreated(UserCreatedEvent event) { }
```

**Review Points:**
- Are status/type fields using strings instead of enum?
- Are event names, message types using strings?
- Is string comparison case-sensitive but not validated?

---

## Nested Conditional Expressions

### Deep Nesting

```java
// ❌ Chained ternary hard to read
String label = role.equals("admin") ? "Admin" :
               role.equals("manager") ? "Manager" :
               role.equals("viewer") ? "Viewer" : "Unknown";

// ✅ Use Map or enum
private static final Map<String, String> ROLE_LABELS = Map.of(
    "admin", "Admin",
    "manager", "Manager",
    "viewer", "Viewer"
);
String label = ROLE_LABELS.getOrDefault(role, "Unknown");

// ❌ Nested if 3+ levels
public void process(Order order) {
    if (order != null) {
        if (order.getItems() != null) {
            for (Item item : order.getItems()) {
                if (item.getPrice() > 0) {
                    if (item.isAvailable()) {
                        // Processing logic
                    }
                }
            }
        }
    }
}

// ✅ Early return + Guard clauses
public void process(Order order) {
    if (order == null || order.getItems() == null) {
        return;
    }
    
    for (Item item : order.getItems()) {
        if (item.getPrice() <= 0 || !item.isAvailable()) {
            continue;
        }
        // Processing logic
    }
}
```

**Review Points:**
- Are ternary expressions nested ≥ 2 levels?
- Is if/else nesting ≥ 3 levels?
- Can Map lookup, early return, or switch expressions be used?

---

## Copy-Paste Variations

### Nearly Duplicate Code

```java
// ❌ Two methods nearly identical, only field names differ
public String formatUser(User user) {
    return String.format("%s %s (%s)", 
        user.getFirstName(), user.getLastName(), user.getEmail());
}

public String formatEmployee(Employee emp) {
    return String.format("%s %s (%s)", 
        emp.getFirstName(), emp.getLastName(), emp.getWorkEmail());
}

// ✅ Unified abstraction
public String formatPerson(String firstName, String lastName, String email) {
    return String.format("%s %s (%s)", firstName, lastName, email);
}
```

```java
// ❌ Copy-paste handler only changed endpoint
public User getUser(Long id) {
    return restTemplate.getForObject("/api/users/" + id, User.class);
}

public Order getOrder(Long id) {
    return restTemplate.getForObject("/api/orders/" + id, Order.class);
}

// ✅ Parameterize
public <T> T getResource(String resource, Long id, Class<T> type) {
    return restTemplate.getForObject("/api/" + resource + "/" + id, type);
}
```

**Review Points:**
- Are there ≥ 2 code segments that differ only in variable names/URLs?
- Can a parameterized shared method be extracted?
- Can template method pattern eliminate variations?

---

## No-Op Updates

### Ineffective Duplicate Updates

```java
// ❌ Every poll triggers update - even when data unchanged
@Scheduled(fixedRate = 5000)
public void syncStatus() {
    Status newStatus = externalService.getStatus();
    statusRepository.save(newStatus);  // Writes to DB even if value is same
}

// ✅ Only update on change
@Scheduled(fixedRate = 5000)
public void syncStatus() {
    Status newStatus = externalService.getStatus();
    Status current = statusRepository.findCurrent();
    if (!newStatus.equals(current)) {
        statusRepository.save(newStatus);
    }
}
```

```java
// ❌ Unconditional property setting - triggers dirty check
@Transactional
public void updateUser(UserDto dto) {
    User user = userRepository.findById(dto.getId());
    user.setName(dto.getName());      // Triggers update even if value is same
    user.setEmail(dto.getEmail());    // Triggers update even if value is same
    // Executes UPDATE even with no actual changes
}

// ✅ Only set on change
@Transactional
public void updateUser(UserDto dto) {
    User user = userRepository.findById(dto.getId());
    if (!user.getName().equals(dto.getName())) {
        user.setName(dto.getName());
    }
    if (!user.getEmail().equals(dto.getEmail())) {
        user.setEmail(dto.getEmail());
    }
}
```

**Review Points:**
- Do scheduled tasks write unconditionally?
- Do update operations check for actual changes?
- Do JPA Entities avoid unnecessary setter calls?

---

## TOCTOU Race Conditions

### Time-of-Check-to-Time-of-Use

```java
// ❌ Check then operate - may be modified by another thread in between
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepository.findById(fromId);
    if (from.getBalance().compareTo(amount) >= 0) {
        from.setBalance(from.getBalance().subtract(amount));
        Account to = accountRepository.findById(toId);
        to.setBalance(to.getBalance().add(amount));
    }
}

// ✅ Use database lock or optimistic locking
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepository.findWithLockingById(fromId);
    if (from.getBalance().compareTo(amount) < 0) {
        throw new InsufficientFundsException();
    }
    from.setBalance(from.getBalance().subtract(amount));
    Account to = accountRepository.findById(toId);
    to.setBalance(to.getBalance().add(amount));
}

// ✅ Or use optimistic lock version number
@Entity
public class Account {
    @Version
    private Long version;
}
```

```java
// ❌ Check file then read
if (Files.exists(path)) {
    String content = Files.readString(path);  // File may have been deleted
}

// ✅ Read directly + handle exception
try {
    String content = Files.readString(path);
} catch (NoSuchFileException e) {
    // Handle file not found
}
```

**Review Points:**
- Can `if condition → operate` pattern be replaced with atomic operation?
- Are multi-step state changes within transaction/lock?
- Do file operations have TOCTOU risk?

---

## Overly Broad Operations

### Reading Too Much Data

```java
// ❌ Query entire table then filter
List<User> allUsers = userRepository.findAll();
List<User> activeUsers = allUsers.stream()
    .filter(User::isActive)
    .collect(toList());

// ✅ Database-level filtering
List<User> activeUsers = userRepository.findByActiveTrue();

// ❌ Read entire file to get first line
String content = Files.readString(path);
String firstLine = content.split("\n")[0];

// ✅ Only read what's needed
String firstLine = Files.lines(path).findFirst().orElse("");
```

**Review Points:**
- Is all data queried then filtered in memory?
- Is entire file/collection read just to get a small portion?
- Do API calls support pagination?

---

## Redundant State

### Derivable State

```java
// ❌ Storing both fullName and firstName + lastName
@Entity
public class User {
    private String firstName;
    private String lastName;
    private String fullName;  // Redundant! May become inconsistent
}

// ✅ fullName is a computed property
@Entity
public class User {
    private String firstName;
    private String lastName;
    
    @Transient  // Don't persist
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

// ❌ Cached value may be stale when source data changes
public class Order {
    private BigDecimal total;
    private List<Item> items;
    
    public void addItem(Item item) {
        items.add(item);
        // Forgot to update total!
    }
}

// ✅ Derive or update promptly
public class Order {
    private List<Item> items;
    
    public BigDecimal getTotal() {
        return items.stream()
            .map(Item::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

**Review Points:**
- Are there fields that can be derived from other fields?
- Do cached values have invalidation mechanism?
- Is there data consistency risk?

---

## General Quality Review Checklist

- [ ] **Reuse Review**: Searched existing utility/helper, not reinventing the wheel?
- [ ] **Parameter Count**: Method parameters ≤ 3? Use parameter object/Builder if more?
- [ ] **Abstraction Boundary**: Return type doesn't expose internal implementation details (Entity, ORM)?
- [ ] **Type Safety**: No magic strings instead of enum/constant?
- [ ] **Condition Depth**: Ternary nesting ≤ 1 level? if/else nesting ≤ 2 levels?
- [ ] **DRY**: No copy-paste-with-variation (≥ 2 similar code segments)?
- [ ] **No-Op Guard**: Scheduled tasks/updates have change-detection guard?
- [ ] **TOCTOU**: `if condition → operate` replaced with atomic operation?
- [ ] **Data Precision**: Not querying all data just to filter a subset?
- [ ] **Redundant State**: No stored fields that can be derived from other fields?
