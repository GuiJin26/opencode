# Concurrency & Virtual Threads Review Guide

Java concurrency programming best practices, covering virtual threads, thread safety, lock optimization, etc.

## Virtual Threads (Java 21+)

### Basic Usage

```java
// ❌ Traditional thread pool handling large I/O blocking tasks
ExecutorService executor = Executors.newFixedThreadPool(100);
for (int i = 0; i < 10000; i++) {
    executor.submit(() -> {
        // I/O operations (HTTP requests, database queries)
        Thread.sleep(1000);  // Blocks OS thread
    });
}

// ✅ Virtual threads for I/O-intensive tasks
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10000; i++) {
        executor.submit(() -> {
            // During I/O operations, virtual thread is suspended, OS thread released
            Thread.sleep(1000);  // Doesn't block OS thread
        });
    }
}

// Spring Boot 3.2+ configuration
// application.yml
spring:
  threads:
    virtual:
      enabled: true
```

### Use Cases

| Scenario | Recommendation |
|----------|----------------|
| I/O-intensive (HTTP requests, database) | ✅ Virtual threads |
| CPU-intensive (computation, encryption) | ❌ Platform thread pool |
| Blocking queue operations | ✅ Virtual threads |
| High synchronized lock contention | ⚠️ Needs evaluation |

### Caveats

```java
// ❌ Using synchronized in virtual threads (pins carrier thread)
synchronized (lock) {
    blockingIO();  // When blocked, pins OS thread, can't switch
}

// ✅ Use ReentrantLock instead
lock.lock();
try {
    blockingIO();  // Can switch normally
} finally {
    lock.unlock();
}

// ❌ ThreadLocal may occupy large memory in virtual threads
// Many virtual threads × ThreadLocal copy per thread

// ✅ Use ScopedValue (Java 21 preview) or reduce ThreadLocal usage
```

### Review Points

- [ ] Are I/O-intensive tasks considering virtual threads?
- [ ] Is synchronized avoided in virtual threads?
- [ ] Could ThreadLocal cause memory issues?

---

## Thread-Safe Collections

### Selection Guide

| Scenario | Recommended Collection |
|----------|------------------------|
| High concurrent read | `ConcurrentHashMap` |
| High concurrent read/write | `ConcurrentHashMap` |
| Modify during iteration | `CopyOnWriteArrayList` |
| Queue | `ConcurrentLinkedQueue` / `ArrayBlockingQueue` |
| Low contention | `Collections.synchronizedXxx` |

### Common Mistakes

```java
// ❌ HashMap in multi-threaded environment
private static final Map<String, String> cache = new HashMap<>();
// May cause infinite loop or data loss

// ✅ ConcurrentHashMap
private static final Map<String, String> cache = new ConcurrentHashMap<>();

// ❌ SimpleDateFormat is not thread-safe
private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
// Multi-threaded calls to format() will error

// ✅ DateTimeFormatter (thread-safe)
private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

// ✅ Or ThreadLocal
private static final ThreadLocal<SimpleDateFormat> sdf = 
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
```

### Compound Operations

```java
// ❌ Compound operations are not atomic
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
if (!map.containsKey("key")) {
    map.put("key", 1);  // Between check and put, other threads may modify
}

// ✅ Use atomic methods
map.putIfAbsent("key", 1);

// ✅ Atomic update
map.compute("key", (k, v) -> v == null ? 1 : v + 1);

// ✅ Atomic merge
map.merge("key", 1, Integer::sum);
```

### Review Points

- [ ] Are shared variables using thread-safe collections?
- [ ] Is SimpleDateFormat replaced with DateTimeFormatter?
- [ ] Are compound operations atomic?

---

## Lock Optimization

### Lock Granularity

```java
// ❌ Lock granularity too large
public synchronized void process() {
    validateInput();    // Doesn't need lock
    writeToDatabase();  // Needs lock
    sendNotification(); // Doesn't need lock
}

// ✅ Narrow lock scope
public void process() {
    validateInput();
    synchronized (this) {
        writeToDatabase();
    }
    sendNotification();
}

// ❌ Performing I/O inside lock
synchronized (lock) {
    database.query();   // Blocks other threads
    httpClient.send();  // Holds lock for long time
}

// ✅ I/O outside lock
Data data;
synchronized (lock) {
    data = prepareQuery();
}
database.query(data);  // Execute outside lock
```

### Read-Write Locks

```java
// ❌ Using mutex lock for read-heavy scenarios
private final Object lock = new Object();
public Data read() {
    synchronized (lock) { ... }  // Reads also queue
}

// ✅ Read-write lock
private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

public Data read() {
    rwLock.readLock().lock();
    try {
        return readData();
    } finally {
        rwLock.readLock().unlock();
    }
}

public void write(Data data) {
    rwLock.writeLock().lock();
    try {
        writeData(data);
    } finally {
        rwLock.writeLock().unlock();
    }
}

// ✅ Java 21+ use StampedLock (more efficient)
private final StampedLock sl = new StampedLock();

public Data read() {
    long stamp = sl.tryOptimisticRead();  // Optimistic read
    Data data = readData();
    if (!sl.validate(stamp)) {
        stamp = sl.readLock();  // Upgrade to pessimistic read
        try {
            data = readData();
        } finally {
            sl.unlockRead(stamp);
        }
    }
    return data;
}
```

### Review Points

- [ ] Is lock granularity minimized?
- [ ] Is I/O avoided inside locks?
- [ ] Are read-write locks used for read-heavy scenarios?

---

## Atomic Classes

### Replacing Synchronization

```java
// ❌ Synchronized counter
private int count;
public synchronized void increment() {
    count++;
}

// ✅ AtomicInteger
private final AtomicInteger count = new AtomicInteger();
public void increment() {
    count.incrementAndGet();
}

// ✅ LongAdder (more efficient for high concurrent accumulation)
private final LongAdder adder = new LongAdder();
public void increment() {
    adder.increment();
}
public long sum() {
    return adder.sum();
}

// ✅ Atomic reference
private final AtomicReference<Node> head = new AtomicReference<>();
public void update(Node newHead) {
    head.updateAndGet(current -> {
        newHead.next = current;
        return newHead;
    });
}
```

### CAS Operations

```java
// ✅ compareAndSet pattern
AtomicInteger value = new AtomicInteger(0);

// Retry until success
int oldValue, newValue;
do {
    oldValue = value.get();
    newValue = oldValue * 2;
} while (!value.compareAndSet(oldValue, newValue));

// ✅ Or use updateAndGet
value.updateAndGet(v -> v * 2);
```

### Review Points

- [ ] Are atomic classes used for simple counting?
- [ ] Is LongAdder considered for high concurrent accumulation?
- [ ] Is CAS retry handled correctly?

---

## CompletableFuture

### Async Orchestration

```java
// ❌ Sequential calls to multiple independent services
User user = userService.getUser(id);           // 200ms
List<Order> orders = orderService.getOrders(id); // 300ms
Credit credit = creditService.getCredit(id);   // 100ms
// Total time: 600ms

// ✅ Parallel calls
CompletableFuture<User> userFuture = 
    CompletableFuture.supplyAsync(() -> userService.getUser(id));
CompletableFuture<List<Order>> ordersFuture = 
    CompletableFuture.supplyAsync(() -> orderService.getOrders(id));
CompletableFuture<Credit> creditFuture = 
    CompletableFuture.supplyAsync(() -> creditService.getCredit(id));

CompletableFuture.allOf(userFuture, ordersFuture, creditFuture).join();
// Total time: 300ms (takes the longest)

// ✅ Chained calls
userService.getUserAsync(id)
    .thenCompose(user -> orderService.getOrdersAsync(user.getId()))
    .thenAccept(orders -> process(orders));

// ✅ Exception handling
CompletableFuture.supplyAsync(() -> riskyOperation())
    .exceptionally(ex -> {
        log.error("Operation failed", ex);
        return defaultValue;
    });
```

### Thread Pool Selection

```java
// ❌ Using default ForkJoinPool (shared, may block)
CompletableFuture.supplyAsync(() -> blockingIO());

// ✅ Specify custom thread pool
ExecutorService ioExecutor = Executors.newFixedThreadPool(20);
CompletableFuture.supplyAsync(() -> blockingIO(), ioExecutor);

// ✅ Or use virtual threads
ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
CompletableFuture.supplyAsync(() -> blockingIO(), virtualExecutor);
```

### Review Points

- [ ] Are independent operations executed in parallel?
- [ ] Are exceptions handled correctly?
- [ ] Is a thread pool specified for blocking operations?

---

## Structured Concurrency (Java 21+)

### Basic Usage

```java
// ❌ Unstructured: tasks can outlive parent scope
ExecutorService executor = Executors.newCachedThreadPool();
Future<User> userFuture = executor.submit(() -> userService.getUser(id));
Future<List<Order>> ordersFuture = executor.submit(() -> orderService.getOrders(id));
// If getUser throws, getOrders still runs - no automatic cleanup

// ✅ Structured concurrency: all tasks scoped to parent
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    StructuredTaskScope.Subtask<User> userTask = 
        scope.fork(() -> userService.getUser(id));
    StructuredTaskScope.Subtask<List<Order>> ordersTask = 
        scope.fork(() -> orderService.getOrders(id));
    
    scope.join();           // Wait for all tasks
    scope.throwIfFailed();  // Propagate exceptions
    
    User user = userTask.get();
    List<Order> orders = ordersTask.get();
}
```

### Shutdown Policies

```java
// ✅ ShutdownOnFailure: cancel remaining tasks on first failure
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var task1 = scope.fork(() -> riskyOperation1());
    var task2 = scope.fork(() -> riskyOperation2());
    scope.join().throwIfFailed();
    // If task1 fails, task2 is automatically cancelled
}

// ✅ ShutdownOnSuccess: cancel remaining on first success
try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
    scope.fork(() -> fetchFromServer1());
    scope.fork(() -> fetchFromServer2());
    scope.fork(() -> fetchFromServer3());
    scope.join();
    return scope.result();  // Returns first successful result
}

// ✅ Custom policy: collect all results
try (var scope = new StructuredTaskScope<User>()) {
    for (String source : sources) {
        scope.fork(() -> fetchUser(source));
    }
    scope.join();
    return scope.completedSubtasks()
        .map(Subtask::get)
        .toList();
}
```

### Integration with Virtual Threads

```java
// ✅ Structured concurrency + virtual threads = efficient I/O
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    // Each fork runs on its own virtual thread
    var userTask = scope.fork(() -> userService.getUser(id));
    var ordersTask = scope.fork(() -> orderService.getOrders(id));
    var creditTask = scope.fork(() -> creditService.getCredit(id));
    
    scope.join().throwIfFailed();
    
    return new UserDashboard(
        userTask.get(),
        ordersTask.get(),
        creditTask.get()
    );
}
```

### Nesting Scopes

```java
// ✅ Nested structured scopes
public Dashboard getDashboard(Long userId) throws Exception {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        var userTask = scope.fork(() -> userService.getUser(userId));
        
        var ordersTask = scope.fork(() -> {
            // Nested scope for parallel order processing
            try (var innerScope = new StructuredTaskScope.ShutdownOnFailure()) {
                var active = innerScope.fork(() -> orderRepo.findActive(userId));
                var history = innerScope.fork(() -> orderRepo.findHistory(userId));
                innerScope.join().throwIfFailed();
                return new Orders(active.get(), history.get());
            }
        });
        
        scope.join().throwIfFailed();
        return new Dashboard(userTask.get(), ordersTask.get());
    }
}
```

### Comparison with CompletableFuture

| Aspect | CompletableFuture | Structured Concurrency |
|--------|------------------|------------------------|
| Lifetime management | Manual | Automatic (scoped) |
| Exception handling | exceptionally() | throwIfFailed() |
| Cancellation | Manual propagation | Automatic |
| Debugging | Hard (async chains) | Easy (call stack) |
| Java version | 8+ | 21+ (preview → stable) |
| Best for | Fire-and-forget | Request-response patterns |

### Review Points

- [ ] Are concurrent tasks properly scoped?
- [ ] Is ShutdownOnFailure used when all tasks must succeed?
- [ ] Is ShutdownOnSuccess used for "first response wins" pattern?
- [ ] Are resources properly cleaned up on failure?
