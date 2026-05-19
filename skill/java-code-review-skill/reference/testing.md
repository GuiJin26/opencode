# Java Testing Standards

Unit testing and integration testing best practices.

## Test Layers

| Test Type | Framework | Speed | Coverage |
|-----------|-----------|-------|----------|
| Unit Test | JUnit 5 + Mockito | Milliseconds | Single class/method |
| Integration Test | Spring Boot Test | Seconds | Multiple components |
| End-to-End Test | Testcontainers | Minutes | Complete flow |

## Unit Testing

### Basic Structure

```java
// ✅ GIVEN-WHEN-THEN structure
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUser_WhenUserExists() {
        // GIVEN
        User expected = new User(1L, "Alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(expected));
        
        // WHEN
        User actual = userService.findById(1L);
        
        // THEN
        assertThat(actual).isEqualTo(expected);
    }
    
    @Test
    void shouldThrowException_WhenUserNotFound() {
        // GIVEN
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // WHEN & THEN
        assertThatThrownBy(() -> userService.findById(1L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("User not found");
    }
}
```

### Mock Guidelines

```java
// ❌ Over-mocking
@Mock private UserRepository userRepo;
@Mock private OrderRepository orderRepo;
@Mock private EmailService emailService;
@Mock private CacheService cacheService;
@Mock private Logger logger;  // Don't mock logs!

// ✅ Only mock external dependencies
@Mock private UserRepository userRepo;
@Mock private EmailService emailService;
@InjectMocks private UserService userService;

// ✅ Use @Spy for partial mocking
@Spy
private List<String> list = new ArrayList<>();

@Test
void test() {
    list.add("real");  // Calls real method
    when(list.size()).thenReturn(100);  // Mock return value
}
```

### Parameterized Tests

```java
// ✅ Parameterized test
@ParameterizedTest
@CsvSource({
    "alice@example.com, true",
    "invalid-email, false",
    "'', false"
})
void shouldValidateEmail(String email, boolean expected) {
    boolean actual = validator.isValidEmail(email);
    assertThat(actual).isEqualTo(expected);
}

@ParameterizedTest
@EnumSource(value = OrderStatus.class, names = {"PENDING", "PROCESSING"})
void shouldProcessActiveOrders(OrderStatus status) {
    // ...
}
```

---

## Integration Testing

### Spring Boot Testing

```java
// ✅ Narrowed Context (faster)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;

    @Test
    void shouldReturnUser() throws Exception {
        when(userService.findById(1L)).thenReturn(new UserResponse(1L, "Alice"));
        
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Alice"));
    }
}

// ✅ Test Repository
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindActiveUsers() {
        userRepository.save(new User("Alice", true));
        userRepository.save(new User("Bob", false));
        
        List<User> active = userRepository.findByActiveTrue();
        
        assertThat(active).hasSize(1)
            .first().extracting("name").isEqualTo("Alice");
    }
}
```

### Testcontainers

```java
// ✅ Using Testcontainers
@Testcontainers
@SpringBootTest
class OrderIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    void shouldCreateOrder() {
        Order order = orderRepository.save(new Order("ORDER-001"));
        assertThat(order.getId()).isNotNull();
    }
}
```

---

## Test Naming

```java
// ❌ Vague naming
@Test
void test1() { }

@Test
void testUser() { }

// ✅ Business scenario naming
@Test
void shouldCreateOrder_WhenItemsAreValid() { }

@Test
void shouldThrowException_WhenPaymentFails() { }

@Test
void shouldNotAllowDuplicateEmail_WhenEmailExists() { }

// ✅ Use DisplayName
@Test
@DisplayName("Should not allow order creation when cart is empty")
void shouldNotAllowOrderCreation_WhenCartIsEmpty() { }
```

---

## Test Coverage

### What to Test

```java
// ✅ Test edge cases
@Test
void shouldHandleEmptyList() { }

@Test
void shouldHandleNullInput() { }

@Test
void shouldHandleMaxValue() { }

@Test
void shouldHandleNegativeValue() { }

// ✅ Test exception paths
@Test
void shouldRollbackTransaction_WhenExceptionOccurs() { }

// ✅ Test concurrency
@Test
void shouldHandleConcurrentAccess() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(100);
    
    IntStream.range(0, 100).forEach(i -> {
        executor.submit(() -> {
            service.increment();
            latch.countDown();
        });
    });
    
    latch.await(5, TimeUnit.SECONDS);
    assertThat(service.getCount()).isEqualTo(100);
}
```

### What Not to Test

- Framework code (Spring, JPA auto-generated)
- Simple Getters/Setters
- Third-party library functionality
- Configuration classes

---

## Assertion Best Practices

```java
// ❌ JUnit native assertions
assertEquals(expected, actual);
assertTrue(list.isEmpty());
assertNotNull(user);

// ✅ AssertJ fluent assertions
assertThat(actual).isEqualTo(expected);
assertThat(list).isEmpty();
assertThat(user).isNotNull();

// ✅ Collection assertions
assertThat(users)
    .hasSize(3)
    .extracting("name")
    .containsExactlyInAnyOrder("Alice", "Bob", "Charlie");

// ✅ Exception assertions
assertThatThrownBy(() -> service.process(null))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("Input cannot be null");

// ✅ Async assertions
await().atMost(5, TimeUnit.SECONDS)
    .until(() -> service.getStatus() == Status.COMPLETED);
```

---

## Test Isolation

```java
// ❌ Shared state between tests
class BadTest {
    private static List<String> shared = new ArrayList<>();
    
    @Test
    void test1() {
        shared.add("a");
        assertThat(shared).hasSize(1);
    }
    
    @Test
    void test2() {
        // shared might contain "a", depends on execution order
        shared.add("b");
    }
}

// ✅ Each test independent
class GoodTest {
    private List<String> list;
    
    @BeforeEach
    void setUp() {
        list = new ArrayList<>();  // New instance for each test
    }
    
    @Test
    void test1() {
        list.add("a");
        assertThat(list).hasSize(1);
    }
    
    @Test
    void test2() {
        list.add("b");
        assertThat(list).hasSize(1);
    }
}
```

---

## Performance Testing

```java
// ✅ Use JMH for benchmarking
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SortingBenchmark {

    private List<Integer> data;

    @Setup
    public void setup() {
        data = IntStream.range(0, 10000)
            .boxed()
            .collect(Collectors.toList());
        Collections.shuffle(data);
    }

    @Benchmark
    public void quickSort() {
        Collections.sort(new ArrayList<>(data));
    }

    @Benchmark
    public void streamSorted() {
        data.stream().sorted().collect(Collectors.toList());
    }
}
```
