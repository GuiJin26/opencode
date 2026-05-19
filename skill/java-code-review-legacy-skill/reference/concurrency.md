# Concurrency Review Guide (Java 8)

Java 8 concurrency programming best practices, covering thread safety, lock optimization, and CompletableFuture patterns.

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

// ✅ DateTimeFormatter (thread-safe) - Java 8+
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

// ✅ Use StampedLock for better read performance (Java 8+)
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

// ✅ LongAdder (Java 8+) - more efficient for high concurrent accumulation
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

## CompletableFuture (Java 8)

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
CompletableFuture.supplyAsync(() -> userService.getUser(id))
    .thenCompose(user -> orderService.getOrdersAsync(user.getId()))
    .thenAccept(orders -> process(orders));

// ✅ Exception handling
CompletableFuture.supplyAsync(() -> riskyOperation())
    .exceptionally(ex -> {
        log.error("Operation failed", ex);
        return defaultValue;
    });

// ✅ Handle both success and failure
CompletableFuture.supplyAsync(() -> riskyOperation())
    .handle((result, ex) -> {
        if (ex != null) {
            log.error("Failed", ex);
            return fallbackValue;
        }
        return result;
    });
```

### Thread Pool Selection

```java
// ❌ Using default ForkJoinPool (shared, may block)
CompletableFuture.supplyAsync(() -> blockingIO());

// ✅ Specify custom thread pool
ExecutorService ioExecutor = Executors.newFixedThreadPool(20);
CompletableFuture.supplyAsync(() -> blockingIO(), ioExecutor);

// ✅ Always shut down executor when done
ioExecutor.shutdown();
```

### Combining Futures

```java
// ✅ thenCombine - combine two independent futures
CompletableFuture<Price> priceFuture = getPriceAsync();
CompletableFuture<Tax> taxFuture = getTaxAsync();
CompletableFuture<Total> totalFuture = priceFuture.thenCombine(taxFuture, 
    (price, tax) -> new Total(price, tax));

// ✅ thenCompose - chain dependent futures
CompletableFuture<User> userFuture = getUserAsync(id);
CompletableFuture<List<Order>> ordersFuture = userFuture.thenCompose(
    user -> getOrdersAsync(user.getId()));

// ✅ allOf - wait for all
CompletableFuture<Void> all = CompletableFuture.allOf(future1, future2, future3);

// ✅ anyOf - first to complete
CompletableFuture<Object> first = CompletableFuture.anyOf(future1, future2, future3);
```

### Java 8 Limitations

```java
// ⚠️ No orTimeout / completeOnTimeout (added in Java 9)
// Workaround for timeout:
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
CompletableFuture<User> future = CompletableFuture.supplyAsync(() -> getUser(id));
scheduler.schedule(() -> future.cancel(true), 5, TimeUnit.SECONDS);

// ⚠️ No exceptionallyAsync (added in Java 12)
// Use exceptionally() instead - runs in same thread as completion
future.exceptionally(ex -> fallback());

// ⚠️ No defaultExecutor customization at instance level
// Use explicit executor in supplyAsync
```

### Review Points

- [ ] Are independent operations executed in parallel?
- [ ] Are exceptions handled correctly?
- [ ] Is a thread pool specified for blocking operations?
- [ ] Is the executor properly shut down?

---

## Parallel Streams

### When to Use

```java
// ✅ CPU-intensive operations on large datasets
long count = largeList.parallelStream()
    .filter(this::cpuIntensivePredicate)
    .count();

// ❌ I/O operations (blocks common pool)
list.parallelStream()
    .forEach(item -> httpClient.call(item));  // Bad!

// ❌ Small datasets (overhead > benefit)
smallList.parallelStream()  // Overhead of splitting > processing
    .map(this::simpleTransform)
    .collect(toList());

// ❌ Operations requiring ordering (unless using ordered collectors)
list.parallelStream()
    .forEachOrdered(item -> process(item));  // Serialized!
```

### Thread Safety

```java
// ❌ Non-thread-safe collection
List<Result> results = new ArrayList<>();
list.parallelStream()
    .forEach(item -> results.add(process(item)));  // Race condition!

// ✅ Use collect (thread-safe)
List<Result> results = list.parallelStream()
    .map(this::process)
    .collect(Collectors.toList());

// ✅ Use thread-safe collection
ConcurrentLinkedQueue<Result> results = new ConcurrentLinkedQueue<>();
list.parallelStream()
    .forEach(item -> results.add(process(item)));
```

### Custom Thread Pool

```java
// ❌ Uses common ForkJoinPool
list.parallelStream().forEach(this::process);

// ✅ Use custom pool for I/O-bound parallel streams
ForkJoinPool customPool = new ForkJoinPool(20);
customPool.submit(() -> 
    list.parallelStream().forEach(this::process)
).get();
```

### Review Points

- [ ] Is parallelStream used only for CPU-intensive large datasets?
- [ ] Are thread-safe collections used for side effects?
- [ ] Is ordering preserved when needed?

---

## Double-Checked Locking

### Correct Implementation

```java
// ❌ Incorrect double-checked locking
private static Instance instance;
public static Instance getInstance() {
    if (instance == null) {
        synchronized (Instance.class) {
            if (instance == null) {
                instance = new Instance();  // May return partially initialized object
            }
        }
    }
    return instance;
}

// ✅ volatile ensures visibility
private static volatile Instance instance;

// ✅ Better: use inner class lazy loading (Initialization-on-demand holder idiom)
public class Singleton {
    private Singleton() {}
    
    private static class Holder {
        static final Singleton INSTANCE = new Singleton();
    }
    
    public static Singleton getInstance() {
        return Holder.INSTANCE;
    }
}

// ✅ Or use enum singleton (thread-safe by default)
public enum Singleton {
    INSTANCE;
    
    public void doSomething() { }
}
```

---

## Thread Lifecycle Management

### Proper Executor Shutdown

```java
// ❌ Executor not shut down
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(() -> doWork());
// Application won't exit - non-daemon threads alive

// ✅ Graceful shutdown
ExecutorService executor = Executors.newFixedThreadPool(10);
// Submit tasks...
executor.shutdown();  // No new tasks
try {
    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow();  // Force shutdown
    }
} catch (InterruptedException e) {
    executor.shutdownNow();
    Thread.currentThread().interrupt();
}

// ✅ Use try-with-resources (Java 19+) or helper method
ExecutorService executor = Executors.newFixedThreadPool(10);
try {
    executor.submit(() -> doWork());
} finally {
    executor.shutdown();
    executor.awaitTermination(60, TimeUnit.SECONDS);
}
```

---

## Common Concurrency Bugs

### HashMap Infinite Loop

```java
// ❌ HashMap may loop infinitely during resize in multi-threaded environment
Map<String, String> map = new HashMap<>();
// Multiple threads doing put() can corrupt internal structure

// ✅ ConcurrentHashMap
Map<String, String> map = new ConcurrentHashMap<>();
```

### Deadlock Prevention

```java
// ❌ Inconsistent lock ordering causes deadlock
public void transfer(Account from, Account to, BigDecimal amount) {
    synchronized (from) {
        synchronized (to) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
// Thread 1: transfer(A, B, x) - locks A then B
// Thread 2: transfer(B, A, y) - locks B then A
// Deadlock!

// ✅ Consistent lock ordering
public void transfer(Account from, Account to, BigDecimal amount) {
    Account first = from.getId() < to.getId() ? from : to;
    Account second = from.getId() < to.getId() ? to : from;
    
    synchronized (first) {
        synchronized (second) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}

// ✅ Or use tryLock with timeout
if (from.getLock().tryLock(1, TimeUnit.SECONDS)) {
    try {
        if (to.getLock().tryLock(1, TimeUnit.SECONDS)) {
            try {
                // Transfer
            } finally {
                to.getLock().unlock();
            }
        }
    } finally {
        from.getLock().unlock();
    }
}
```

---

## Migration to Modern Java

When reviewing Java 8 concurrency code, identify opportunities for future migration:

| Java 8 Pattern | Modern Equivalent | Version |
|----------------|-------------------|---------|
| `CompletableFuture.supplyAsync()` | Virtual threads (Java 21) | Better throughput for I/O |
| Thread pools for I/O | `Executors.newVirtualThreadPerTaskExecutor()` | Java 21 |
| Manual timeout handling | `orTimeout()`, `completeOnTimeout()` | Java 9+ |
| `future.exceptionally()` | `future.exceptionallyAsync()` | Java 12+ |
| ForkJoinPool for parallel streams | Virtual threads | Java 21 |

Mark these as `💡 [suggestion]` with migration notes rather than blocking issues.
