# Java 8 Core Features Review Guide

Best practices and anti-pattern recognition for Java 8 features in legacy projects.

## Lambda Expressions

### Basic Usage

```java
// ❌ Anonymous inner class
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};

// ✅ Lambda expression
Runnable r = () -> System.out.println("Hello");

// ❌ Anonymous Comparator
Collections.sort(users, new Comparator<User>() {
    @Override
    public int compare(User u1, User u2) {
        return u1.getName().compareTo(u2.getName());
    }
});

// ✅ Lambda + method reference
users.sort(Comparator.comparing(User::getName));
```

### Review Points

| Check Item | Description |
|------------|-------------|
| Lambda readability | If lambda body > 3 lines, extract to method |
| Parameter types | Omit types when compiler can infer |
| Capturing variables | Avoid modifying outer variables in lambda |
| This reference | `this` in lambda refers to enclosing class, not lambda itself |

### Anti-patterns

```java
// ❌ Complex logic inside lambda
users.forEach(u -> {
    if (u.isActive()) {
        if (u.getAge() > 18) {
            if (u.getEmail() != null) {
                sendEmail(u.getEmail(), buildWelcomeMessage(u));
            }
        }
    }
});

// ✅ Extract to method, use Stream
users.stream()
    .filter(User::isActive)
    .filter(u -> u.getAge() > 18)
    .map(User::getEmail)
    .filter(Objects::nonNull)
    .forEach(email -> sendEmail(email, WELCOME_MSG));

// ❌ Lambda with side effects modifying external state
List<String> result = new ArrayList<>();
users.forEach(u -> result.add(u.getName()));

// ✅ Use collect
List<String> result = users.stream()
    .map(User::getName)
    .collect(Collectors.toList());
```

---

## Functional Interfaces

### Built-in Interfaces

| Interface | Method | Use Case |
|-----------|--------|----------|
| `Predicate<T>` | `boolean test(T)` | Filtering |
| `Function<T,R>` | `R apply(T)` | Transformation |
| `Consumer<T>` | `void accept(T)` | Side effects (logging) |
| `Supplier<T>` | `T get()` | Lazy creation |
| `BiFunction<T,U,R>` | `R apply(T, U)` | Two-arg transform |
| `UnaryOperator<T>` | `T apply(T)` | Same type transform |

### Custom Functional Interface

```java
// ❌ Creating new interface when built-in exists
@FunctionalInterface
public interface StringChecker {
    boolean check(String s);
}

// ✅ Use Predicate<String> instead
Predicate<String> checker = s -> s.length() > 5;

// ✅ Only create custom when meaningful name adds clarity
@FunctionalInterface
public interface RetryPolicy {
    boolean shouldRetry(int attempt, Exception lastError);
}
```

### Review Points

- [ ] Is there an unnecessary custom functional interface?
- [ ] Are method references used where possible (`User::getName` vs `u -> u.getName()`)?
- [ ] Is `Optional::isPresent` with `get()` used instead of functional style?

---

## Stream API

### Proper Usage

```java
// ✅ Short-circuit operations
boolean exists = users.stream()
    .anyMatch(u -> u.getEmail().equals(email));

// ✅ Collectors
Map<Long, User> userMap = users.stream()
    .collect(Collectors.toMap(User::getId, Function.identity()));

// ✅ Grouping
Map<Department, List<User>> byDept = users.stream()
    .collect(Collectors.groupingBy(User::getDepartment));

// ✅ Partitioning
Map<Boolean, List<User>> activeInactive = users.stream()
    .collect(Collectors.partitioningBy(User::isActive));

// ✅ Joining
String names = users.stream()
    .map(User::getName)
    .collect(Collectors.joining(", "));
```

### Common Pitfalls

```java
// ❌ Using Stream for simple single-iteration
items.stream().forEach(item -> process(item));

// ✅ Use enhanced for-loop for simple cases
for (Item item : items) {
    process(item);
}

// ❌ Reusing Stream (Streams are single-use)
Stream<User> stream = users.stream();
stream.count();
stream.collect(Collectors.toList());  // IllegalStateException!

// ✅ Create new Stream
long count = users.stream().count();
List<User> list = users.stream().collect(Collectors.toList());

// ❌ Using peek for side effects
users.stream()
    .peek(u -> u.setActive(true))
    .collect(Collectors.toList());

// ✅ Use map for transformation
List<User> activated = users.stream()
    .map(u -> {
        u.setActive(true);
        return u;
    })
    .collect(Collectors.toList());

// ❌ Misusing parallel streams
list.parallelStream().forEach(item -> {
    sharedList.add(item);  // Non-thread-safe!
});

// ✅ Use parallel only for CPU-intensive operations on large datasets
// ✅ Use thread-safe collectors
List<Result> results = largeList.parallelStream()
    .map(this::cpuIntensiveTransform)
    .collect(Collectors.toList());
```

### Stream vs For-Loop Decision

| Scenario | Recommendation |
|----------|----------------|
| Simple iteration | for-each loop |
| Filtering / mapping / collecting | Stream |
| Nested loops | Stream (flatter) |
| Need index | for-loop or `IntStream.range` |
| Modifying external state | for-loop |
| Performance-critical inner loop | for-loop |
| Complex data pipeline | Stream |

### Review Points

- [ ] Are Streams used for data transformation pipelines?
- [ ] Are side effects avoided in Streams?
- [ ] Is `parallelStream()` used only when appropriate?
- [ ] Are Streams not reused?

---

## Optional

### Correct Usage (Java 8 Style)

```java
// ❌ Optional as parameter or field
public void process(Optional<String> name) { }
public class User {
    private Optional<String> email;  // Not recommended
}

// ✅ Optional only for return values
public Optional<User> findUser(String id) { }

// ❌ isPresent + get anti-pattern
Optional<User> userOpt = findUser(id);
if (userOpt.isPresent()) {
    return userOpt.get().getName();
}
return "Unknown";

// ✅ Functional API (orElse, orElseGet, map)
return findUser(id)
    .map(User::getName)
    .orElse("Unknown");

// ✅ orElseGet for lazy evaluation
return findUser(id)
    .map(User::getName)
    .orElseGet(() -> fetchDefaultName());

// ✅ orElseThrow for explicit exception
User user = findUser(id)
    .orElseThrow(() -> new UserNotFoundException(id));

// ❌ orElse with expensive computation
return findUser(id).orElse(createNewUser());  // createNewUser() always called!
```

### Java 8 Limitations

```java
// ⚠️ Java 8 Optional lacks ifPresentOrElse, or, stream (added in 9/10/11)
// Workaround for ifPresentOrElse:
opt.ifPresent(value -> process(value));
if (!opt.isPresent()) {
    handleEmpty();
}

// ⚠️ No Optional.stream() in Java 8
// Workaround: filter empty values from stream
List<User> users = ids.stream()
    .map(this::findUser)
    .filter(Optional::isPresent)
    .map(Optional::get)
    .collect(Collectors.toList());
```

### Review Points

- [ ] Is Optional only used for return values?
- [ ] Is `isPresent + get` used instead of functional API?
- [ ] Is the empty case handled correctly?
- [ ] Is `orElse` used instead of `orElseGet` for expensive operations?

---

## Default Methods

### Interface Evolution

```java
// ✅ Adding methods to existing interfaces without breaking implementations
public interface Loggable {
    default void logInfo(String msg) {
        getLogger().info(msg);
    }
    
    default void logError(String msg, Throwable t) {
        getLogger().error(msg, t);
    }
    
    Logger getLogger();
}

// ⚠️ Use sparingly - default methods can indicate interface is too large
// ❌ Don't use default methods to add business logic
public interface UserService {
    default User createUser(String name) {
        // This is business logic, should be in implementation
    }
}
```

### Diamond Problem

```java
// ❌ Class implements two interfaces with same default method
public interface A {
    default void hello() { System.out.println("A"); }
}
public interface B {
    default void hello() { System.out.println("B"); }
}

// ✅ Must explicitly resolve conflict
public class C implements A, B {
    @Override
    public void hello() {
        A.super.hello();  // Choose one
    }
}
```

---

## Method References

### Types

| Type | Example | Equivalent Lambda |
|------|---------|-------------------|
| Static method | `Math::abs` | `x -> Math.abs(x)` |
| Instance method | `String::toUpperCase` | `s -> s.toUpperCase()` |
| Instance of object | `System.out::println` | `s -> System.out.println(s)` |
| Constructor | `ArrayList::new` | `() -> new ArrayList()` |

```java
// ❌ Lambda when method reference is clearer
users.stream().map(u -> u.getName()).collect(Collectors.toList());

// ✅ Method reference
users.stream().map(User::getName).collect(Collectors.toList());

// ❌ Lambda for simple delegation
list.forEach(item -> System.out.println(item));

// ✅ Method reference
list.forEach(System.out::println);
```

---

## Date/Time API (JSR-310)

### Modern Date/Time Usage

```java
// ❌ Legacy Date/Calendar
Date now = new Date();
Calendar cal = Calendar.getInstance();
cal.set(2024, Calendar.JANUARY, 1);
Date date = cal.getTime();

SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
String formatted = sdf.format(date);

// ✅ java.time API
LocalDate date = LocalDate.of(2024, 1, 1);
LocalDateTime now = LocalDateTime.now();
Instant instant = Instant.now();
ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));

DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
String formatted = date.format(formatter);

// ✅ Date math
LocalDate tomorrow = LocalDate.now().plusDays(1);
Duration duration = Duration.between(startTime, endTime);
Period period = Period.between(startDate, endDate);
```

### Legacy Interoperability

```java
// ✅ Converting between old and new API
// Date -> Instant
Instant instant = oldDate.toInstant();

// Instant -> Date
Date date = Date.from(instant);

// Calendar -> Instant
Instant instant = calendar.toInstant();

// Calendar -> ZonedDateTime
ZonedDateTime zdt = GregorianCalendar.from(calendar).toZonedDateTime();
```

### Review Points

- [ ] Is `java.time` used instead of `Date`/`Calendar`?
- [ ] Is `DateTimeFormatter` used instead of `SimpleDateFormat`?
- [ ] Is time zone handling correct for cross-region applications?
- [ ] Are timestamps stored as `Instant` (UTC) in the database?

---

## CompletableFuture (Java 8)

### Async Orchestration

```java
// ❌ Sequential calls to independent services
User user = userService.getUser(id);           // 200ms
List<Order> orders = orderService.getOrders(id); // 300ms
Credit credit = creditService.getCredit(id);   // 100ms
// Total: 600ms

// ✅ Parallel calls with CompletableFuture
CompletableFuture<User> userFuture = 
    CompletableFuture.supplyAsync(() -> userService.getUser(id));
CompletableFuture<List<Order>> ordersFuture = 
    CompletableFuture.supplyAsync(() -> orderService.getOrders(id));
CompletableFuture<Credit> creditFuture = 
    CompletableFuture.supplyAsync(() -> creditService.getCredit(id));

CompletableFuture.allOf(userFuture, ordersFuture, creditFuture).join();
// Total: 300ms (takes longest)

// ✅ Chained calls
CompletableFuture.supplyAsync(() -> userService.getUser(id))
    .thenCompose(user -> orderService.getOrdersAsync(user.getId()))
    .thenAccept(orders -> process(orders));

// ✅ Exception handling
CompletableFuture.supplyAsync(() -> riskyOperation())
    .exceptionally(ex -> {
        log.error("Operation failed", ex);
        return defaultValue;
    });

// ✅ Combine two futures
CompletableFuture<String> future1 = getUserIdAsync();
CompletableFuture<String> future2 = getUserNameAsync();
CompletableFuture<String> combined = future1.thenCombine(future2, 
    (id, name) -> id + ":" + name);
```

### Thread Pool Selection

```java
// ❌ Using default ForkJoinPool (shared, limited threads)
CompletableFuture.supplyAsync(() -> blockingIO());

// ✅ Specify custom thread pool for blocking I/O
ExecutorService ioExecutor = Executors.newFixedThreadPool(20);
CompletableFuture.supplyAsync(() -> blockingIO(), ioExecutor);

// ⚠️ Always shut down executor
ioExecutor.shutdown();
```

### Java 8 Limitations

```java
// ⚠️ No orTimeout/completeOnTimeout (added in Java 9)
// Workaround:
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
CompletableFuture<User> future = CompletableFuture.supplyAsync(() -> getUser(id));
scheduler.schedule(() -> future.cancel(true), 5, TimeUnit.SECONDS);

// ⚠️ No exceptionallyAsync (added in Java 12)
// Use handle() instead
future.handle((result, ex) -> {
    if (ex != null) {
        log.error("Failed", ex);
        return fallback();
    }
    return result;
});
```

### Review Points

- [ ] Are independent operations executed in parallel?
- [ ] Are exceptions handled correctly?
- [ ] Is a custom thread pool used for blocking operations?
- [ ] Is the executor properly shut down?

---

## Deprecated API Replacements

| Deprecated API | Java 8 Replacement |
|----------------|-------------------|
| `Date` | `java.time.LocalDateTime` / `Instant` |
| `Calendar` | `java.time.ZonedDateTime` |
| `SimpleDateFormat` | `DateTimeFormatter` |
| `Vector` | `ArrayList` |
| `Hashtable` | `HashMap` / `ConcurrentHashMap` |
| `StringBuffer` | `StringBuilder` (non-thread-safe scenarios) |
| `Collections.sort(list, cmp)` | `list.sort(cmp)` |
| `String.format()` | `String.format()` (still ok, but prefer concatenation for simple cases) |

```java
// ❌ Legacy API
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
Date date = sdf.parse("2024-01-01");

// ✅ Java 8 API
DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
LocalDate date = LocalDate.parse("2024-01-01", dtf);
```

---

## Migration Awareness

When reviewing Java 8 code, identify opportunities for future migration:

| Java 8 Pattern | Modern Equivalent | Benefit |
|----------------|-------------------|---------|
| POJO/DTO with Lombok | `record` (Java 16+) | Immutable, concise |
| Traditional switch | Switch expression (Java 14+) | No fall-through, returns value |
| String concatenation for multi-line | Text block (Java 15+) | Readable |
| instanceof + cast | Pattern matching (Java 16+) | Eliminates cast |
| `var` not available | `var` (Java 10+) | Reduces boilerplate |
| `Collectors.toList()` | `.toList()` (Java 16+) | Shorter |
| CompletableFuture chains | Structured Concurrency (Java 21+) | Scoped lifetime |

Mark these as `💡 [suggestion]` with a note about future migration rather than blocking issues.
