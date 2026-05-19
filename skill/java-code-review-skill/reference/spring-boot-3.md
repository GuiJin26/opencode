# Spring Boot Best Practices Review Guide

Spring Boot 3.x code review points, covering dependency injection, configuration management, exception handling, etc.

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
public record PaymentProperties(
    String apiKey,
    int timeout,
    String url
) {}

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentProperties props;
    
    public void process() {
        String key = props.apiKey();  // IDE hints, type-safe
    }
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

### Global Exception Handler

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

// ✅ Global exception handler (Spring Boot 3 ProblemDetail)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException e) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, 
            e.getMessage()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, 
            e.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception e) {
        log.error("Unexpected error", e);
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        );
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
public record OrderRequest(
    @NotBlank(message = "Customer ID is required")
    String customerId,
    
    @NotEmpty(message = "Items cannot be empty")
    @Size(max = 100, message = "Max 100 items per order")
    List<@Valid ItemRequest> items,
    
    @Pattern(regexp = "^[A-Z]{3}$")
    String currency
) {}

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

## Spring Boot Actuator

### Endpoint Security

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
      show-details: never  // Don't expose health details

// ✅ Secure sensitive endpoints
management:
  endpoints:
    web:
      base-path: /actuator
  server:
    port: 8081  // Separate management port
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

// ✅ Health group for composite checks
management:
  endpoint:
    health:
      group:
        readiness:
          include: db,redis
          show-details: never
```

### Review Points

- [ ] Are actuator endpoints properly secured?
- [ ] Is health endpoint not exposing sensitive details?
- [ ] Is management port separated from application port?

---

## Observability (Micrometer)

### Metrics Configuration

```java
// ✅ Enable metrics with Micrometer
@Bean
MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
    return registry -> registry.config()
        .commonTags("application", "my-app")
        .commonTags("region", System.getenv("REGION"));
}

// ✅ Custom metrics
@Service
public class OrderService {
    private final Counter orderCounter;
    private final Timer orderTimer;
    
    public OrderService(MeterRegistry registry) {
        this.orderCounter = Counter.builder("orders.created")
            .description("Number of orders created")
            .tag("type", "online")
            .register(registry);
        this.orderTimer = Timer.builder("orders.processing.time")
            .register(registry);
    }
    
    public Order createOrder(OrderRequest request) {
        return orderTimer.record(() -> {
            orderCounter.increment();
            return doCreateOrder(request);
        });
    }
}
```

### Distributed Tracing

```yaml
# ✅ Enable tracing with Spring Boot 3
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0  // Sample 100% (reduce in production)
  zipkin:
    tracing:
      endpoint: http://zipkin:9411/api/v2/spans

# ✅ Add Baggage for context propagation
management:
  tracing:
    propagation:
      type: b3
    baggage:
      remote-fields:
        - user-id
        - tenant-id
```

### Logging with Trace Context

```java
// ✅ Structured logging with trace ID
logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"

// ✅ MDC for custom context
import org.slf4j.MDC;

public class AuditService {
    public void log(String userId, String action) {
        MDC.put("userId", userId);
        try {
            log.info("Action: {}", action);
        } finally {
            MDC.remove("userId");
        }
    }
}
```

### Review Points

- [ ] Are custom metrics defined for key business operations?
- [ ] Is distributed tracing configured?
- [ ] Is trace context included in logs?
- [ ] Is sampling rate appropriate for production?

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
    path: /actuator/health/liveness
    port: 8080
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
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

- [ ] Are liveness and readiness probes configured?
- [ ] Is graceful shutdown enabled?
- [ ] Is the application stateless for horizontal scaling?
