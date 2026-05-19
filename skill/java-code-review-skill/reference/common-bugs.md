# Java Common Bug List

High-frequency bug patterns in Java development and their fixes.

## NullPointerException (NPE)

### Common Scenarios

```java
// ❌ Not checking null
String name = user.getName();
return name.length();  // user might be null

// ✅ Use Optional
return Optional.ofNullable(user)
    .map(User::getName)
    .map(String::length)
    .orElse(0);

// ✅ Use Objects.requireNonNull
public void process(User user) {
    User safeUser = Objects.requireNonNull(user, "User cannot be null");
    // ...
}

// ✅ Use @NonNull annotation (compile-time check)
public void process(@NonNull User user) { }
```

### String Comparison

```java
// ❌ Literal after may cause NPE
if (name.equals("admin")) { }  // NPE when name is null

// ✅ Literal first
if ("admin".equals(name)) { }

// ✅ Use Objects.equals
if (Objects.equals(name, "admin")) { }
```

### Map Operations

```java
// ❌ Direct get may be null
String value = map.get(key);
if (value.isEmpty()) { }  // NPE when value is null

// ✅ Check null or use getOrDefault
String value = map.getOrDefault(key, "");

// ✅ Use computeIfAbsent
String value = map.computeIfAbsent(key, k -> defaultValue);
```

---

## Concurrency Issues

### SimpleDateFormat Thread Safety

```java
// ❌ Static SimpleDateFormat (not thread-safe)
private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

// ✅ Use DateTimeFormatter (thread-safe)
private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

// ✅ Or ThreadLocal
private static final ThreadLocal<SimpleDateFormat> sdf = 
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
```

### Double-Checked Locking

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

// ✅ Better: use inner class lazy loading
public class Singleton {
    private Singleton() {}
    
    private static class Holder {
        static final Singleton INSTANCE = new Singleton();
    }
    
    public static Singleton getInstance() {
        return Holder.INSTANCE;
    }
}
```

### HashMap Infinite Loop

```java
// ❌ HashMap may loop infinitely during resize in multi-threaded environment
Map<String, String> map = new HashMap<>();

// ✅ ConcurrentHashMap
Map<String, String> map = new ConcurrentHashMap<>();

// ✅ Collections.synchronizedMap (lower performance)
Map<String, String> map = Collections.synchronizedMap(new HashMap<>());
```

---

## Resource Leaks

### Unclosed Streams

```java
// ❌ Stream not closed
FileInputStream fis = new FileInputStream(file);
byte[] data = fis.readAllBytes();
// fis not closed!

// ✅ try-with-resources
try (FileInputStream fis = new FileInputStream(file)) {
    byte[] data = fis.readAllBytes();
}

// ✅ Multiple resources
try (
    FileInputStream fis = new FileInputStream(input);
    FileOutputStream fos = new FileOutputStream(output)
) {
    // ...
}
```

### Unclosed Connections

```java
// ❌ Database connection not closed
Connection conn = dataSource.getConnection();
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery("SELECT ...");
// Not closed, connection leak!

// ✅ try-with-resources
try (
    Connection conn = dataSource.getConnection();
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT ...")
) {
    // ...
}
```

---

## Exception Handling

### Swallowing Exceptions

```java
// ❌ Empty catch block
try {
    doSomething();
} catch (Exception e) {
    // Nothing done, problem hidden
}

// ❌ Only printing stack trace
try {
    doSomething();
} catch (Exception e) {
    e.printStackTrace();  // Should not use in production
}

// ✅ Proper logging
try {
    doSomething();
} catch (Exception e) {
    log.error("Failed to do something", e);
    throw new BusinessException("Operation failed", e);
}
```

### Wrong Exception Type

```java
// ❌ Catching too broadly
try {
    // code
} catch (Exception e) {  // Catches all exceptions including RuntimeException
    throw new BusinessException(e);
}

// ✅ Catch specific exceptions
try {
    // code
} catch (IOException e) {
    throw new BusinessException("IO error", e);
} catch (SQLException e) {
    throw new BusinessException("Database error", e);
}
```

### finally Return Value

```java
// ❌ return in finally overrides exception
public int getValue() {
    try {
        throw new RuntimeException();
    } finally {
        return 0;  // Exception swallowed, returns 0
    }
}

// ✅ finally should not have return
public int getValue() {
    try {
        return calculate();
    } finally {
        cleanup();  // Only cleanup
    }
}
```

---

## Collection Operations

### Modifying Collection During Iteration

```java
// ❌ Modifying collection while iterating
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
for (String s : list) {
    if (s.equals("b")) {
        list.remove(s);  // ConcurrentModificationException
    }
}

// ✅ Use iterator
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("b")) {
        it.remove();
    }
}

// ✅ Use removeIf
list.removeIf(s -> s.equals("b"));

// ✅ Collect then batch remove
Set<String> toRemove = list.stream()
    .filter(s -> shouldRemove(s))
    .collect(toSet());
list.removeAll(toRemove);
```

### Array to List Pitfall

```java
// ❌ Arrays.asList returns fixed-size List
List<String> list = Arrays.asList("a", "b", "c");
list.add("d");  // UnsupportedOperationException

// ✅ Create new ArrayList
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));

// ✅ Java 9+ List.of (immutable)
List<String> list = List.of("a", "b", "c");

// ✅ Java 9+ mutable ArrayList
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
```

### Sublist View

```java
// ❌ Sublist is a view, original list modification affects it
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
List<String> subList = list.subList(0, 2);
list.add("e");  // subList operations will throw exception

// ✅ Create new list
List<String> subList = new ArrayList<>(list.subList(0, 2));
```

---

## Numeric Issues

### Integer Overflow

```java
// ❌ Integer overflow
int a = Integer.MAX_VALUE;
int b = a + 1;  // Overflows to negative

// ✅ Use long or check
if (a > Integer.MAX_VALUE - b) {
    throw new ArithmeticException("Integer overflow");
}

// ✅ Math.addExact (throws exception on overflow)
int sum = Math.addExact(a, b);

// ✅ Use long
long sum = (long) a + b;
```

### Floating Point Comparison

```java
// ❌ Direct floating point comparison
double a = 0.1 + 0.2;
if (a == 0.3) { }  // false!

// ✅ Use precision comparison
if (Math.abs(a - 0.3) < 1e-9) { }

// ✅ Use BigDecimal
BigDecimal a = new BigDecimal("0.1").add(new BigDecimal("0.2"));
if (a.compareTo(new BigDecimal("0.3")) == 0) { }  // true
```

### BigDecimal Construction

```java
// ❌ double constructor has precision issues
BigDecimal bd = new BigDecimal(0.1);  // 0.10000000000000000555...

// ✅ String constructor
BigDecimal bd = new BigDecimal("0.1");  // Exact

// ✅ Or use valueOf
BigDecimal bd = BigDecimal.valueOf(0.1);  // Internally uses string
```

---

## String Operations

### String Concatenation

```java
// ❌ String concatenation in loop
String result = "";
for (String s : list) {
    result += s;  // Creates new object each time, O(n²)
}

// ✅ StringBuilder
StringBuilder sb = new StringBuilder();
for (String s : list) {
    sb.append(s);
}
String result = sb.toString();

// ✅ String.join
String result = String.join("", list);
```

### String Comparison

```java
// ❌ Using == to compare strings
if (str == "hello") { }  // Compares references, unreliable

// ✅ Use equals
if ("hello".equals(str)) { }

// ✅ Case-insensitive
if ("hello".equalsIgnoreCase(str)) { }
```

---

## Lombok Pitfalls

### @Data on Entity

```java
// ❌ @Data on JPA Entity
@Entity
@Data  // equals/hashCode/toString will trigger lazy loading
public class User {
    @OneToMany
    private List<Order> orders;
}

// ✅ Use only @Getter @Setter
@Entity
@Getter
@Setter
public class User {
    @Override
    public boolean equals(Object o) {
        // ID-based implementation
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

### @Builder Default Values

```java
// ❌ @Builder with default values
@Builder
public class Order {
    private String id;
    private String note = "N/A";  // @Builder won't use default value!
}
// Order.builder().build().getNote() == null

// ✅ Use @Builder.Default
@Builder
public class Order {
    private String id;
    @Builder.Default
    private String note = "N/A";
}
```
