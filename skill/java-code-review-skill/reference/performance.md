# Java Performance Review Guide

Java/Spring Boot performance optimization review points, covering database, API, concurrency, JVM, etc.

## Database Performance

### N+1 Query Problem

> **See detailed guide:** [jpa-database.md - N+1 Query Problem](jpa-database.md#n1-query-problem)

Quick detection checklist:
- [ ] Are there EAGER fetches?
- [ ] Are associated entities accessed in loops?
- [ ] Is JOIN FETCH or EntityGraph used?

### Index Optimization

```java
// ✅ Add indexes
@Entity
@Table(indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_order_status_date", columnList = "status, createdAt")
})
public class User {
    @Column(unique = true)
    private String email;
}

// ⚠️ Index invalidation scenarios
// 1. Function operations: WHERE YEAR(created_at) = 2024
// 2. Prefix wildcard: WHERE name LIKE '%keyword%'
// 3. Type conversion: WHERE id = '123' (id is numeric)
// 4. OR conditions: WHERE status = 'A' OR name = 'B'
```

### Query Optimization

```java
// ❌ SELECT * getting unnecessary columns
@Query("SELECT u FROM User u WHERE u.active = true")
List<User> findActive();

// ✅ Projection only queries needed fields
public interface UserSummary {
    Long getId();
    String getName();
}
@Query("SELECT u.id as id, u.name as name FROM User u WHERE u.active = true")
List<UserSummary> findActiveSummaries();

// ❌ Large offset pagination
Pageable pageable = PageRequest.of(10000, 20);  // OFFSET 200000

// ✅ Keyset pagination
@Query("SELECT o FROM Order o WHERE o.id > :lastId ORDER BY o.id LIMIT :limit")
List<Order> findAfter(@Param("lastId") Long lastId, @Param("limit") int limit);
```

### Batch Operations

> **See detailed guide:** [jpa-database.md - Batch Operations](jpa-database.md#batch-operations)

Quick checklist:
- [ ] Are batch operations used instead of loops?
- [ ] Is `spring.jpa.properties.hibernate.jdbc.batch_size` configured?

### Review Checklist

- [ ] Are there N+1 queries?
- [ ] Are WHERE condition columns indexed?
- [ ] Is SELECT * avoided?
- [ ] Do large table queries have LIMIT?
- [ ] Are batch operations optimized?

---

## API Performance

### Pagination

```java
// ❌ Returning all data
@GetMapping("/users")
public List<User> getAllUsers() {
    return userRepository.findAll();  // Could be 100k records
}

// ✅ Pagination + max limit
@GetMapping("/users")
public Page<UserResponse> getUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    size = Math.min(size, 100);  // Max 100
    Pageable pageable = PageRequest.of(page, size);
    return userRepository.findAll(pageable).map(UserResponse::from);
}
```

### Caching

```java
// ✅ Method-level caching
@Cacheable(value = "users", key = "#id")
public User getUser(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
}

@CacheEvict(value = "users", key = "#user.id")
public User updateUser(User user) {
    return userRepository.save(user);
}

// ✅ Configure TTL
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30));
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build();
    }
}
```

### Response Compression

```yaml
# application.yml
server:
  compression:
    enabled: true
    mime-types: application/json,text/html,text/xml
    min-response-size: 1024
```

### Rate Limiting

```java
// ✅ Use Bucket4j or Resilience4j
@RestController
public class ApiController {
    private final Bucket bucket;
    
    public ApiController() {
        this.bucket = Bucket.builder()
            .addLimit(Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1))))
            .build();
    }
    
    @GetMapping("/api/resource")
    public ResponseEntity<?> getResource() {
        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok(service.getResource());
        }
        return ResponseEntity.status(429).body("Too many requests");
    }
}
```

### Review Checklist

- [ ] Do list endpoints have pagination?
- [ ] Is there a maximum per-page limit?
- [ ] Is hot data cached?
- [ ] Is response compression enabled?
- [ ] Is there rate limiting?

---

## Concurrency Performance

### Virtual Threads (Java 21+)

```java
// ❌ Traditional thread pool handling I/O blocking
ExecutorService executor = Executors.newFixedThreadPool(100);

// ✅ Virtual threads for I/O-intensive tasks
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

// Spring Boot 3.2+ configuration
spring:
  threads:
    virtual:
      enabled: true
```

### Async Processing

```java
// ❌ Sequential execution of independent tasks
User user = userService.getUser(id);
List<Order> orders = orderService.getOrders(id);
Credit credit = creditService.getCredit(id);
// Total time = user + orders + credit

// ✅ Parallel execution
CompletableFuture<User> userFuture = 
    CompletableFuture.supplyAsync(() -> userService.getUser(id));
CompletableFuture<List<Order>> ordersFuture = 
    CompletableFuture.supplyAsync(() -> orderService.getOrders(id));
CompletableFuture<Credit> creditFuture = 
    CompletableFuture.supplyAsync(() -> creditService.getCredit(id));

CompletableFuture.allOf(userFuture, ordersFuture, creditFuture).join();
// Total time = max(user, orders, credit)
```

### Connection Pool Configuration

```yaml
# HikariCP configuration
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 30000
      max-lifetime: 1200000
```

---

## Algorithm Complexity

### Common Complexities

| Complexity | 10 items | 1000 items | 1M items | Example |
|------------|----------|------------|----------|---------|
| O(1) | 1 | 1 | 1 | HashMap.get() |
| O(log n) | 3 | 10 | 20 | TreeMap.get() |
| O(n) | 10 | 1000 | 1M | Iterate List |
| O(n log n) | 33 | 10000 | 20M | Collections.sort() |
| O(n²) | 100 | 1M | 1T | Nested loops |

### Code Optimization

```java
// ❌ O(n²) - nested loop finding duplicates
for (User u1 : users) {
    for (User u2 : users) {
        if (u1.getEmail().equals(u2.getEmail()) && u1 != u2) {
            duplicates.add(u1);
        }
    }
}

// ✅ O(n) - using Map
Map<String, User> emailToUser = new HashMap<>();
for (User u : users) {
    if (emailToUser.containsKey(u.getEmail())) {
        duplicates.add(u);
    }
    emailToUser.put(u.getEmail(), u);
}

// ❌ O(n) lookup - iterate each time
users.stream().filter(u -> u.getId() == id).findFirst();

// ✅ O(1) lookup - using Map
Map<Long, User> userMap = users.stream()
    .collect(Collectors.toMap(User::getId, Function.identity()));
userMap.get(id);
```

---

## Memory Management

### Object Creation

```java
// ❌ Creating many temporary objects in loop
String result = "";
for (String s : items) {
    result += s;  // Creates new String each time
}

// ✅ Use StringBuilder
StringBuilder sb = new StringBuilder();
for (String s : items) {
    sb.append(s);
}
String result = sb.toString();

// ❌ Frequently creating SimpleDateFormat
public String format(Date date) {
    return new SimpleDateFormat("yyyy-MM-dd").format(date);  // New object each time
}

// ✅ Reuse thread-safe DateTimeFormatter
private static final DateTimeFormatter FORMATTER = 
    DateTimeFormatter.ofPattern("yyyy-MM-dd");
```

### Resource Release

```java
// ❌ Stream not closed
InputStream is = new FileInputStream(file);
byte[] data = is.readAllBytes();
// is not closed!

// ✅ try-with-resources
try (InputStream is = new FileInputStream(file)) {
    byte[] data = is.readAllBytes();
}

// ✅ Connection release
try (Connection conn = dataSource.getConnection();
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery(sql)) {
    // ...
}
```

---

## JVM Tuning

### GC Configuration

```bash
# JDK 11+ defaults to G1GC, usually no adjustment needed

# Large memory applications can consider ZGC
-XX:+UnlockExperimentalVMOptions -XX:+UseZGC

# Common parameters
-Xms2g                    # Initial heap size
-Xmx2g                    # Maximum heap size
-XX:MaxGCPauseMillis=200  # Target GC pause time
```

### Memory Analysis

```bash
# Heap dump
jmap -dump:format=b,file=heap.hprof <pid>

# GC log
-Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10m

# Memory analysis tools
# - VisualVM
# - JProfiler
# - Eclipse MAT
```

---

## Performance Review Checklist

### 🔴 Must Check

- [ ] Are there N+1 queries?
- [ ] Do list endpoints have pagination?
- [ ] Is there SELECT * on large tables?
- [ ] Are there O(n²) nested loops?
- [ ] Are there database queries in loops?

### 🟡 Should Check

- [ ] Are WHERE columns indexed?
- [ ] Is hot data cached?
- [ ] Are I/O-intensive tasks using virtual threads? (Java 21+ / Spring Boot 3.2+)
- [ ] Is there frequent object creation?
- [ ] Is connection pool configured properly?

### 🟢 Optimization Suggestions

- [ ] Is response compression enabled?
- [ ] Is there slow query monitoring?
- [ ] Is there APM monitoring?
- [ ] Is GC configuration appropriate?

---

## Performance Metrics

| Metric | Good | Needs Improvement | Poor |
|--------|------|-------------------|------|
| API response time | < 100ms | 100-500ms | > 500ms |
| Database query | < 50ms | 50-200ms | > 200ms |
| Memory usage | < 70% | 70-85% | > 85% |
| GC pause | < 100ms | 100-500ms | > 500ms |
| CPU usage | < 70% | 70-90% | > 90% |
