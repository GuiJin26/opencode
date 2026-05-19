# Spring Boot 2 Best Practices Review Guide

Spring Boot 2.x code review points, covering dependency injection, configuration management, exception handling, and legacy-specific considerations.

## Dependency Injection

### Constructor Injection vs Field Injection

```java
// ❌ Field injection @Autowired
// Drawbacks: hard to test, hides excessive dependencies, not immutable
@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private CacheService cacheService;
    // Too many dependencies, should split
}

// ✅ Constructor injection
// Benefits: explicit dependencies, easy to mock, fields can be final
@Service
public class UserService {
    private final UserRepository userRepo;
    private final EmailService emailService;

    public UserService(UserRepository userRepo, EmailService emailService) {
        this.userRepo = userRepo;
        this.emailService = emailService;
    }
}

// ✅ Simplified with Lombok
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final EmailService emailService;
}
```

### Circular Dependency Detection

```java
// ❌ Circular dependency: ServiceA -> ServiceB -> ServiceA
@Service
public class ServiceA {
    private final ServiceB serviceB;
    public ServiceA(ServiceB serviceB) { this.serviceB = serviceB; }
}

@Service
public class ServiceB {
    private final ServiceA serviceA;
    public ServiceB(ServiceA serviceA) { this.serviceA = serviceA; }
}

// ✅ Refactor: extract common logic to a third Service
@Service
public class SharedService {
    // Common logic
}
```

### Review Points

- [ ] Is constructor injection used?
- [ ] Does a class have more than 5 dependencies? (Consider splitting)
- [ ] Are there circular dependencies?

---

## Configuration Management

### Type-Safe Configuration

```java
// ❌ @Value scattered in code
@Service
public class PaymentService {
    @Value("${app.payment.api-key}")
    private String apiKey;
    
    @Value("${app.payment.timeout}")
    private int timeout;
    
    @Value("${app.payment.url}")
    private String url;
}

// ✅ @ConfigurationProperties type-safe
@ConfigurationProperties(prefix = "app.payment")
@Component
@Getter @Setter
public class PaymentProperties {
    private String apiKey;
    private int timeout;
    private String url;
}

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentProperties props;
    
    public void process() {
        String key = props.getApiKey();  // IDE hints, type-safe
    }
}
```

### Enable Configuration Properties

```java
// ✅ Register configuration properties
@Configuration
@EnableConfigurationProperties(PaymentProperties.class)
public class PaymentConfig {
}

// Or annotate directly on the properties class
@ConfigurationProperties(prefix = "app.payment")
@Component
@Getter @Setter
public class PaymentProperties {
    // ...
}
```

### Environment Isolation

```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
# application-dev.yml
app:
  payment:
    api-key: dev-key
    timeout: 30000

---
# application-prod.yml
app:
  payment:
    api-key: ${PAYMENT_API_KEY}  # Read from environment variable
    timeout: 10000
```

### Sensitive Data Handling

```java
// ❌ Hardcoded sensitive information
@Service
public class PaymentService {
    private String apiKey = "sk_live_12345";  // Dangerous!
}

// ❌ Plaintext storage in config file
app:
  payment:
    api-key: sk_live_12345  // Dangerous!

// ✅ Environment variables or secret management
app:
  payment:
    api-key: ${PAYMENT_API_KEY}
```

### Review Points

- [ ] Is @ConfigurationProperties used?
- [ ] Is sensitive data injected via environment variables?
- [ ] Are different environment configurations isolated?

---

## Exception Handling

### Global Exception Handler (Spring Boot 2 Style)

```java
// ❌ Try-catch everywhere swallowing exceptions
try {
    userService.create(user);
} catch (Exception e) {
    e.printStackTrace();  // Should not use in production
    return null;  // Swallows exception, upper layer unaware
}

// ✅ Custom business exception
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode code) {
        super(code.getMessage());
        this.errorCode = code;
    }
}

// ✅ Global exception handler (Spring Boot 2)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorResponse response = new ErrorResponse(
            "BUSINESS_ERROR",
            e.getMessage()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException e) {
        ErrorResponse response = new ErrorResponse(
            "NOT_FOUND",
            e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.toList());
        
        ErrorResponse response = new ErrorResponse(
            "VALIDATION_ERROR",
            String.join(", ", errors)
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        log.error("Unexpected error", e);
        ErrorResponse response = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

// ✅ Custom error response DTO
@Data
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
    private Instant timestamp = Instant.now();
    
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

### Exception Classification

| Exception Type | HTTP Status | Scenario |
|----------------|-------------|----------|
| `EntityNotFoundException` | 404 | Resource not found |
| `IllegalArgumentException` | 400 | Parameter validation failed |
| `BusinessException` | 400/409 | Business rule violation |
| `AccessDeniedException` | 403 | Insufficient permissions |
| `Exception` | 500 | Unknown error |

### Review Points

- [ ] Is a global exception handler used?
- [ ] Does exception information expose sensitive data?
- [ ] Are business exceptions distinguished from system exceptions?

---

## Controller Design

### Responsibility Division

```java
// ❌ Controller contains business logic
@RestController
public class OrderController {
    
    @PostMapping("/orders")
    public Order createOrder(@RequestBody OrderRequest request) {
        // Validation logic
        if (request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Items cannot be empty");
        }
        
        // Calculation logic
        BigDecimal total = request.getItems().stream()
            .map(Item::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Persistence
        Order order = new Order();
        order.setItems(request.getItems());
        order.setTotal(total);
        return orderRepository.save(order);
    }
}

// ✅ Controller only handles HTTP layer
@RestController
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping("/orders")
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }
}

// ✅ Service handles business logic
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final PriceCalculator priceCalculator;
    
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        BigDecimal total = priceCalculator.calculateTotal(request.getItems());
        Order order = Order.from(request, total);
        return OrderResponse.from(orderRepository.save(order));
    }
}
```

### Parameter Validation

```java
// ✅ Use Bean Validation
@Data
public class OrderRequest {
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotEmpty(message = "Items cannot be empty")
    @Size(max = 100, message = "Max 100 items per order")
    private List<@Valid ItemRequest> items;
    
    @Pattern(regexp = "^[A-Z]{3}$")
    private String currency;
}

// Controller auto-validation
@PostMapping("/orders")
public OrderResponse createOrder(
    @Valid @RequestBody OrderRequest request  // Auto-validation
) {
    return orderService.createOrder(request);
}
```

### Review Points

- [ ] Does Controller only handle HTTP layer?
- [ ] Is Bean Validation used?
- [ ] Are DTOs used instead of Entities for return types?

---

## Service Design

### Transaction Management

```java
// ❌ Opening transaction in Controller (connection held too long)
@RestController
public class OrderController {
    @Transactional  // Wrong location
    @PostMapping("/orders")
    public Order createOrder(...) { }
}

// ❌ @Transactional on private method (AOP doesn't work)
@Service
public class OrderService {
    @Transactional
    private void saveInternal() { }  // Doesn't work!
}

// ✅ Add transaction to Service public methods
@Service
public class OrderService {
    
    @Transactional(readOnly = true)  // Read optimization
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }
    
    @Transactional  // Write operation
    public Order createOrder(OrderRequest request) {
        return orderRepository.save(Order.from(request));
    }
}
```

### Transaction Propagation

```java
// Common propagation behaviors
@Transactional(propagation = Propagation.REQUIRED)   // Default, join existing transaction
@Transactional(propagation = Propagation.REQUIRES_NEW) // Suspend current, create new transaction
@Transactional(propagation = Propagation.NOT_SUPPORTED) // Suspend current, execute without transaction
```

### Review Points

- [ ] Are read operations marked with `readOnly = true`?
- [ ] Are transactions only at the Service layer?
- [ ] Are external API calls avoided within transactions?

---

## RESTful API Design

### URL Conventions

```java
// ❌ Non-standard URLs
@GetMapping("/getOrderById/{id}")
@GetMapping("/order/delete/{id}")
@GetMapping("/ordersAll")

// ✅ RESTful style
@GetMapping("/orders/{id}")       // Get
@PostMapping("/orders")           // Create
@PutMapping("/orders/{id}")       // Full update
@PatchMapping("/orders/{id}")     // Partial update
@DeleteMapping("/orders/{id}")    // Delete
@GetMapping("/orders")            // List (with pagination)
```

### Status Code Usage

| Operation | Success Status | Failure Status |
|-----------|----------------|----------------|
| Create | 201 Created | 400 Bad Request |
| Get | 200 OK | 404 Not Found |
| Update | 200 OK | 400/404 |
| Delete | 204 No Content | 404 |
| List | 200 OK | 400 |

### Pagination Convention

```java
// ✅ Pagination parameters
@GetMapping("/orders")
public Page<OrderResponse> getOrders(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "createdAt,desc") String sort
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    return orderService.getOrders(pageable);
}
```

### Review Points

- [ ] Do URLs follow RESTful conventions?
- [ ] Are HTTP status codes correct?
- [ ] Is there a maximum limit for pagination?

---

## Spring Boot Actuator (Spring Boot 2)

### Endpoint Configuration

```java
// ❌ Exposing all actuator endpoints publicly
management:
  endpoints:
    web:
      exposure:
        include: "*"

// ✅ Only expose necessary endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: never  # Don't expose health details

// ✅ Secure sensitive endpoints
management:
  endpoints:
    web:
      base-path: /actuator
  server:
    port: 8081  # Separate management port
```

### Health Indicators

```java
// ✅ Custom health indicator
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        if (checkExternalService()) {
            return Health.up()
                .withDetail("service", "available")
                .build();
        }
        return Health.down()
            .withDetail("service", "unavailable")
            .build();
    }
}
```

### Review Points

- [ ] Are actuator endpoints properly secured?
- [ ] Is health endpoint not exposing sensitive details?
- [ ] Is management port separated from application port?

---

## Security Configuration (Spring Security 5.x)

### Basic Security Setup

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/public/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Method-Level Security

```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
}

// Usage
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/users/{id}")
public void deleteUser(@PathVariable Long id) { ... }

@PreAuthorize("#id == authentication.principal.id")
@GetMapping("/users/{id}/profile")
public Profile getProfile(@PathVariable Long id) { ... }
```

### Review Points

- [ ] Are sensitive endpoints access-controlled?
- [ ] Are passwords using BCrypt?
- [ ] Is method-level security used?

---

## Testing (Spring Boot 2)

### JUnit 5 with Spring Boot 2

```java
// ✅ Use JUnit 5 (default in Spring Boot 2.2+)
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
}
```

### Integration Testing

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
```

### Testcontainers

```java
// ✅ Using Testcontainers
@Testcontainers
@SpringBootTest
class OrderIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
        .withDatabaseName("testdb");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

---

## Limitations & Migration Considerations

### Spring Boot 2 vs 3 Key Differences

| Feature | Spring Boot 2 | Spring Boot 3 |
|---------|---------------|---------------|
| Java Version | Java 8+ | Java 17+ |
| Jakarta EE | javax.* | jakarta.* |
| Error Response | Custom ErrorResponse | ProblemDetail (RFC 7807) |
| Security Config | WebSecurityConfigurerAdapter | SecurityFilterChain bean |
| Native Support | Limited | Full GraalVM support |
| Observability | Micrometer + custom | Micrometer Tracing built-in |
| Virtual Threads | Not supported | Supported (Java 21) |

### When Reviewing Legacy Code

Mark potential migration improvements as `💡 [suggestion]`:

```java
// 💡 [suggestion] Consider updating to Jakarta namespace when migrating to Spring Boot 3
import javax.persistence.Entity;  // Will become jakarta.persistence.Entity
import javax.validation.constraints.NotBlank;  // Will become jakarta.validation.constraints.NotBlank

// 💡 [suggestion] WebSecurityConfigurerAdapter is deprecated in Spring Security 5.7+
// Consider using SecurityFilterChain bean pattern

// 💡 [suggestion] Consider using ProblemDetail for error responses when migrating to Spring Boot 3
```

---

## Docker & Kubernetes Ready

### Health Probes Configuration

```yaml
# ✅ Kubernetes probes via Actuator
management:
  endpoint:
    health:
      probes:
        enabled: true
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true

# Kubernetes deployment
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
```

### Graceful Shutdown

```yaml
# ✅ Graceful shutdown configuration
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

### Review Points

- [ ] Are health probes configured?
- [ ] Is graceful shutdown enabled?
- [ ] Is the application stateless for horizontal scaling?
