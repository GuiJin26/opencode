# Java Core Features Review Guide

Best practices and anti-pattern recognition for Java 17/21 features.

## Record

### Basic Usage

```java
// ❌ Traditional POJO/DTO: lots of boilerplate
public class UserDto {
    private final String name;
    private final int age;

    public UserDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
    public String getName() { return name; }
    public int getAge() { return age; }
    @Override public boolean equals(Object o) { ... }
    @Override public int hashCode() { return ...; }
    @Override public String toString() { return ...; }
}

// ✅ Record: concise, immutable, clear semantics
public record UserDto(String name, int age) {
    // Compact constructor for validation
    public UserDto {
        if (age < 0) throw new IllegalArgumentException("Age cannot be negative");
    }
}
```

### Review Points

| Check Item | Description |
|------------|-------------|
| Can DTO be replaced with Record | Prefer Record for data transfer objects |
| Immutability requirement | Record is automatically immutable, suitable for value objects |
| Serialization compatibility | Jackson 2.12+ supports Record serialization |
| JPA Entity | Not recommended (JPA requires no-arg constructor and mutable fields) |

### Record Anti-patterns

```java
// ❌ Defining mutable state in Record
public record Counter(int value) {
    private int count; // Compile error! Record doesn't support instance fields
}

// ❌ Using Record as JPA Entity
@Entity
public record User(...) { } // Not supported!

// ✅ Use Record for DTO/value objects/response objects
public record UserResponse(Long id, String name, String email) {}
```

---

## Switch Expressions

### Traditional vs Expression

```java
// ❌ Traditional switch: easy to miss break, no return value
String type = "";
switch (obj) {
    case Integer i:
        type = "int";
        break;
    case String s:
        type = "string";
        break;
    default:
        type = "unknown";
}

// ✅ Switch expression: no fall-through risk, forced return
String type = switch (obj) {
    case Integer i -> "int %d".formatted(i);
    case String s  -> "string %s".formatted(s);
    case null      -> "null value";  // Java 21
    default        -> "unknown";
};
```

### Pattern Matching (Java 21)

```java
// ❌ Traditional instanceof + cast
if (obj instanceof String) {
    String s = (String) obj;
    return s.length();
}

// ✅ Pattern matching
if (obj instanceof String s) {
    return s.length();
}

// ✅ Switch pattern matching
return switch (shape) {
    case Circle c    -> c.radius() * 2 * Math.PI;
    case Rectangle r -> 2 * (r.width() + r.height());
    case Square s    -> 4 * s.side();
};
```

### Review Points

- [ ] Is there a traditional switch that can be changed to an expression?
- [ ] Is null handling missing?
- [ ] Are there redundant type checks and casts?

---

## Text Blocks

```java
// ❌ String concatenation for SQL/JSON
String json = "{\n" +
              "  \"name\": \"Alice\",\n" +
              "  \"age\": 20\n" +
              "}";

// ✅ Text block: WYSIWYG
String json = """
    {
      "name": "Alice",
      "age": 20
    }
    """;

// ✅ Use formatted() for dynamic content
String sql = """
    SELECT * FROM users
    WHERE status = '%s'
    """.formatted(status);
```

### Review Points

- [ ] Are multi-line strings using text blocks?
- [ ] Is indentation handled correctly?
- [ ] Is formatted() or string templates used for dynamic content?

---

## Sealed Classes

```java
// ✅ Restrict inheritance hierarchy
public sealed interface Shape 
    permits Circle, Rectangle, Square {
    
    double area();
}

public final class Circle implements Shape {
    private final double radius;
    
    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

// non-sealed allows further extension
public non-sealed class AbstractShape implements Shape { }
```

### Review Points

- [ ] Is there a need to limit the number of subclasses?
- [ ] Use with Pattern Matching
- [ ] Is final / sealed / non-sealed used correctly

---

## Stream API

### Avoid Overuse

```java
// ❌ Using Stream for simple loops (performance overhead)
items.stream().forEach(item -> process(item));

// ✅ Use for-each for simple cases
for (var item : items) {
    process(item);
}

// ❌ Overly complex Stream chain
List<Dto> result = list.stream()
    .filter(...)
    .map(...)
    .peek(...)
    .sorted(...)
    .collect(...);

// ✅ Split into meaningful steps
var filtered = list.stream().filter(...).toList();
var mapped = filtered.stream().map(...).toList();
```

### Common Pitfalls

```java
// ❌ Using peek to modify state (side effects)
users.stream()
    .peek(u -> u.setActive(true))
    .collect(toList());

// ✅ Use peek only for debug logging
users.stream()
    .peek(u -> log.debug("Processing: {}", u))
    .collect(toList());

// ❌ Misusing parallel streams
list.parallelStream().forEach(item -> {
    // Non-thread-safe operation
    sharedList.add(item);
});

// ✅ Clarify thread safety requirements
list.parallelStream()
    .collect(toList()); // Thread-safe collection
```

---

## Optional

### Correct Usage

```java
// ❌ Optional as parameter or field
public void process(Optional<String> name) { }
public class User {
    private Optional<String> email; // Not recommended
}

// ✅ Optional only for return values
public Optional<User> findUser(String id) { }

// ❌ isPresent + get anti-pattern
Optional<User> userOpt = findUser(id);
if (userOpt.isPresent()) {
    return userOpt.get().getName();
}
return "Unknown";

// ✅ Functional API
return findUser(id)
    .map(User::getName)
    .orElse("Unknown");

// ✅ orElseThrow for explicit exception
User user = findUser(id)
    .orElseThrow(() -> new UserNotFoundException(id));
```

### Review Points

- [ ] Is Optional only used for return values?
- [ ] Is isPresent + get used instead of functional API?
- [ ] Is the empty case handled correctly?

---

## Deprecated API Replacements

| Deprecated API | Replacement |
|----------------|-------------|
| `Date` | `java.time.LocalDateTime` / `Instant` |
| `Calendar` | `java.time.ZonedDateTime` |
| `SimpleDateFormat` | `DateTimeFormatter` |
| `Vector` | `ArrayList` |
| `Hashtable` | `HashMap` / `ConcurrentHashMap` |
| `StringBuffer` | `StringBuilder` (non-thread-safe scenarios) |

```java
// ❌ Deprecated API
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
Date date = sdf.parse("2024-01-01");

// ✅ Java 8+ API
DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
LocalDate date = LocalDate.parse("2024-01-01", dtf);
```
