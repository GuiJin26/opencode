# JPA & Database Review Guide

JPA performance optimization, N+1 issues, Entity design, transaction management review points.

## N+1 Query Problem

### Problem Identification

```java
// ❌ FetchType.EAGER
@Entity
public class User {
    @OneToMany(fetch = FetchType.EAGER)  // Dangerous!
    private List<Order> orders;
}

// ❌ Triggering lazy loading in a loop
List<User> users = userRepository.findAll();  // 1 SQL
for (User user : users) {
    int orderCount = user.getOrders().size();  // N SQLs!
}

// ❌ Using Lombok @Data (toString triggers lazy loading)
@Entity
@Data  // toString will load all associations
public class User {
    @OneToMany
    private List<Order> orders;
}
```

### Solutions

```java
// ✅ Solution 1: JOIN FETCH
@Query("SELECT u FROM User u JOIN FETCH u.orders WHERE u.id = :id")
User findByIdWithOrders(@Param("id") Long id);

// ✅ Solution 2: @EntityGraph
@EntityGraph(attributePaths = {"orders"})
List<User> findAll();

// ✅ Solution 3: Batch fetching
@BatchSize(size = 100)
@OneToMany
private List<Order> orders;

// ✅ Solution 4: Subquery
@Query("SELECT u FROM User u WHERE u.id IN " +
       "(SELECT o.user.id FROM Order o WHERE o.status = :status)")
List<User> findUsersWithActiveOrders(@Param("status") OrderStatus status);
```

### Review Points

- [ ] Are there EAGER fetches?
- [ ] Are associated entities accessed in loops?
- [ ] Is JOIN FETCH or EntityGraph used?

---

## Entity Design

### equals/hashCode

```java
// ❌ Lombok @Data on Entity
@Entity
@Data  // equals/hashCode includes all fields, may trigger lazy loading
public class User {
    @Id
    private Long id;
    
    private String name;
    
    @OneToMany
    private List<Order> orders;  // equals/hashCode will trigger loading
}

// ✅ Use only @Getter @Setter
@Entity
@Getter
@Setter
public class User {
    @Id
    private Long id;
    
    // ✅ ID-based equals/hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id != null && id.equals(((User) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();  // Fixed value, avoids ID change issues
    }
}
```

### Field Naming

```java
// ❌ Reserved word conflicts
@Entity
public class Order {
    private String order;   // Conflicts with table name
    private String group;   // Database reserved word
    private String user;    // Database reserved word
}

// ✅ Use explicit naming
@Entity
public class Order {
    private String orderNo;
    private String groupName;
    private String userName;
}

// Or use @Column to escape
@Column(name = "`group`")
private String group;
```

### Review Points

- [ ] Does Entity avoid using @Data?
- [ ] Is equals/hashCode based on ID?
- [ ] Do field names avoid reserved words?

---

## Transaction Management

### Transaction Boundaries

```java
// ❌ Transaction scope too large (includes external calls)
@Transactional
public void processOrder(OrderRequest request) {
    Order order = saveOrder(request);
    paymentService.charge(order);  // External API call
    emailService.sendConfirmation(order);  // Send email
}

// ✅ Split transactions, external calls outside
public void processOrder(OrderRequest request) {
    Order order = saveOrderTransactional(request);
    try {
        paymentService.charge(order);
    } catch (PaymentException e) {
        compensateOrder(order);
        throw e;
    }
    emailService.sendConfirmation(order);
}

@Transactional
public Order saveOrderTransactional(OrderRequest request) {
    return orderRepository.save(Order.from(request));
}
```

### Read-Only Transactions

```java
// ✅ Mark read operations as readOnly
@Transactional(readOnly = true)
public Optional<User> findById(Long id) {
    return userRepository.findById(id);
}

// Benefits:
// 1. Database can optimize (no dirty data checking)
// 2. Avoids Session flush overhead
// 3. Some databases route to read replicas
```

### Review Points

- [ ] Do read operations use `readOnly = true`?
- [ ] Do transactions avoid including external calls?
- [ ] Is transaction management at Service layer (not Controller)?

---

## Query Optimization

### Index Usage

```java
// ✅ Add indexes to query fields
@Entity
@Table(indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_order_status_created", columnList = "status, createdAt")
})
public class User {
    @Column(unique = true)
    private String email;
}

// ✅ Composite index order: high selectivity first
// status has low selectivity, createdAt has high selectivity
// Query: WHERE status = ? ORDER BY createdAt DESC
// Index: (status, createdAt)
```

### Pagination Queries

```java
// ❌ Large offset pagination (poor performance)
Pageable pageable = PageRequest.of(10000, 20);  // OFFSET 200000

// ✅ Keyset pagination
@Query("SELECT o FROM Order o WHERE o.id > :lastId ORDER BY o.id LIMIT :limit")
List<Order> findAfter(@Param("lastId") Long lastId, @Param("limit") int limit);

// ✅ Use cursor instead of offset
public record CursorPage<T>(List<T> data, String nextCursor) {}
```

### Projection

```java
// ❌ Query entire Entity just to get a few fields
List<User> users = userRepository.findAll();
List<String> names = users.stream().map(User::getName).toList();

// ✅ Use Projection to query only needed fields
public interface UserName {
    String getName();
    String getEmail();
}

@Query("SELECT u.name as name, u.email as email FROM User u WHERE u.active = true")
List<UserName> findActiveUserNames();

// ✅ Or use constructor expression
@Query("SELECT new com.example.UserName(u.name, u.email) FROM User u")
List<UserName> findActiveUserNames();
```

### Review Points

- [ ] Are query fields indexed?
- [ ] Is large data pagination optimized?
- [ ] Is Projection used to reduce data transfer?

---

## Batch Operations

### Batch Insert

```java
// ❌ Loop single insert
for (Item item : items) {
    itemRepository.save(item);  // N INSERTs
}

// ✅ Batch insert
itemRepository.saveAll(items);  // 1 batch INSERT

// Configure batch size
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
```

### Batch Update

```java
// ❌ Loop update
for (User user : users) {
    user.setActive(true);
    userRepository.save(user);
}

// ✅ Batch update JPQL
@Modifying
@Query("UPDATE User u SET u.active = true WHERE u.id IN :ids")
int activateUsers(@Param("ids") List<Long> ids);

// ✅ Or use QueryDSL
queryFactory.update(user)
    .set(user.active, true)
    .where(user.id.in(ids))
    .execute();
```

### Review Points

- [ ] Are batch operations used instead of loops?
- [ ] Is batch_size configured?
- [ ] Are large batch operations processed in batches?

---

## Soft Delete

```java
// ✅ Soft delete implementation
@Entity
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class User {
    @Id
    private Long id;
    
    private boolean deleted = false;
    
    @Column(name = "deleted_at")
    private Instant deletedAt;
}

// Auto-filter deleted records
List<User> users = userRepository.findAll();  // Auto-adds WHERE deleted = false

// Hard delete requires native query
@Query(value = "DELETE FROM users WHERE id = :id", nativeQuery = true)
void hardDelete(@Param("id") Long id);
```

### Review Points

- [ ] Is soft delete needed?
- [ ] Does soft delete field include deletion timestamp?
- [ ] Is global filter configured correctly?
