# Java Security Review Guide

Java/Spring Boot application security review checklist, covering injection attacks, authentication/authorization, sensitive data handling, etc.

## SQL Injection

### Parameterized Queries

```java
// ❌ String concatenation (SQL injection risk)
String sql = "SELECT * FROM users WHERE name = '" + name + "'";
jdbcTemplate.query(sql, ...);

// ❌ JPA native query concatenation
@Query(value = "SELECT * FROM users WHERE name = :name", nativeQuery = true)
User findByName(@Param("name") String name);  // If name = "'; DROP TABLE users;--"

// ✅ JPA parameter binding
@Query("SELECT u FROM User u WHERE u.name = :name")
User findByName(@Param("name") String name);

// ✅ JdbcTemplate parameterized
jdbcTemplate.query(
    "SELECT * FROM users WHERE name = ?",
    (rs, rowNum) -> mapUser(rs),
    name
);

// ✅ Criteria API
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<User> cq = cb.createQuery(User.class);
Root<User> user = cq.from(User.class);
cq.where(cb.equal(user.get("name"), name));
```

### Safe Dynamic Queries

```java
// ✅ Use QueryDSL or JPA Criteria
public List<User> search(String name, String email) {
    JPAQuery<User> query = new JPAQuery<>(em);
    QUser u = QUser.user;
    
    if (name != null) {
        query.where(u.name.eq(name));  // Safe
    }
    if (email != null) {
        query.where(u.email.eq(email));  // Safe
    }
    return query.fetch();
}

// ❌ Concatenating dynamic SQL
StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
if (name != null) {
    sql.append(" AND name = '").append(name).append("'");  // Dangerous!
}
```

### Review Points

- [ ] Are parameterized queries used?
- [ ] Is Criteria/QueryDSL used for dynamic SQL?
- [ ] Are native query parameters bound correctly?

---

## XSS Prevention

### Output Encoding

```java
// ❌ Directly outputting user input
@GetMapping("/profile")
public String profile(@RequestParam String name, Model model) {
    model.addAttribute("name", name);  // Not encoded
    return "profile";  // Thymeleaf auto-escapes, but other templates may not
}

// ✅ Use OWASP Encoder
import org.owasp.encoder.Encode;

model.addAttribute("name", Encode.forHtml(name));

// ✅ Spring Boot default HTML escaping (Thymeleaf)
<div th:text="${name}"></div>  <!-- Auto-escaped -->

// ❌ Disabling escaping
<div th:utext="${userInput}"></div>  <!-- Dangerous! -->
```

### JSON Output

```java
// ✅ Jackson default HTML escaping
@GetMapping("/search")
public User search(@RequestParam String keyword) {
    return userService.search(keyword);  // JSON output escapes special chars
}

// ❌ Manually building JSON
String json = "{\"name\": \"" + name + "\"}";  // Not escaped

// ✅ Use ObjectNode
ObjectNode node = objectMapper.createObjectNode();
node.put("name", name);  // Auto-escaped
```

### Review Points

- [ ] Is user input properly encoded?
- [ ] Is template escaping avoided being disabled?
- [ ] Is JSON output using libraries instead of manual concatenation?

---

## Authentication & Authorization

### Spring Security Configuration

```java
// ✅ Least privilege configuration
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .permitAll()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
    return http.build();
}

// ❌ Overly permissive configuration
.anyRequest().permitAll()  // Dangerous! All endpoints public
```

### Method-Level Security

```java
// ✅ Method-level security
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/users/{id}")
public void deleteUser(@PathVariable Long id) { ... }

@PreAuthorize("#id == authentication.principal.id")
@GetMapping("/users/{id}/profile")
public Profile getProfile(@PathVariable Long id) { ... }

@PostAuthorize("returnObject.owner == authentication.principal.id")
@GetMapping("/documents/{id}")
public Document getDocument(@PathVariable Long id) { ... }
```

### Password Storage

```java
// ❌ Plaintext password storage
user.setPassword(password);  // Dangerous!

// ❌ Using MD5/SHA1
user.setPassword(DigestUtils.md5Hex(password));  // Cracked

// ✅ BCrypt (Spring Security default)
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// ✅ Usage
user.setPassword(passwordEncoder.encode(password));
```

### Review Points

- [ ] Are sensitive endpoints access-controlled?
- [ ] Are passwords using strong encryption algorithms?
- [ ] Is method-level security used?

---

## Sensitive Data Protection

### Log Masking

```java
// ❌ Logging sensitive information
log.info("User login: email={}, password={}", email, password);
log.info("Credit card: {}", creditCardNumber);
log.debug("SSN: {}", ssn);

// ✅ Masking
log.info("User login: email={}", maskEmail(email));
log.info("Credit card: {}", maskCreditCard(creditCardNumber));

// ✅ Use @ToString.Exclude
@Entity
@Getter
@ToString
public class User {
    private String name;
    
    @ToString.Exclude
    private String password;
    
    @ToString.Exclude
    private String creditCard;
}
```

### Response Masking

```java
// ❌ Returning sensitive fields
public record UserResponse(
    Long id,
    String name,
    String email,
    String password,      // Should not return
    String creditCard     // Should not return
) {}

// ✅ Separate DTO
public record UserResponse(
    Long id,
    String name,
    String email
) {}

// ✅ Use Jackson views
public class PublicView { }
public class AdminView extends PublicView { }

public record User(
    @JsonView(PublicView.class) String name,
    @JsonView(AdminView.class) String email
) {}
```

### Configuration Security

```java
// ❌ Plaintext password in config file
spring:
  datasource:
    password: my-secret-password  // Dangerous!

// ✅ Environment variables
spring:
  datasource:
    password: ${DB_PASSWORD}

// ✅ Use Spring Cloud Config encryption
spring:
  datasource:
    password: "{cipher}encrypted_value"

// ✅ Use Vault
@Value("${db.password}")
private String dbPassword;
```

### Review Points

- [ ] Do logs avoid recording sensitive information?
- [ ] Do API responses contain sensitive fields?
- [ ] Are passwords stored in plaintext in config files?

---

## CSRF Protection

```java
// ✅ Spring Security enables CSRF by default
// Form submissions need CSRF token

// ❌ Disabling CSRF (only for stateless APIs)
.csrf(csrf -> csrf.disable())  // Use with caution

// ✅ Custom CSRF storage
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
)

// ✅ SPA application integration
// Frontend reads CSRF token from cookie, puts in request header
<meta name="_csrf" content="${_csrf.token}"/>
```

---

## Dependency Security

### Vulnerability Scanning

```xml
<!-- Use OWASP Dependency Check -->
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>8.2.1</version>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>

<!-- Use Spring Boot validation -->
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>build-info</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Command Checks

```bash
# Maven dependency check
mvn dependency:tree
mvn dependency-check:check

# Gradle dependency check
gradle dependencies
gradle dependencyCheckAnalyze
```

### Review Points

- [ ] Are dependency vulnerabilities scanned regularly?
- [ ] Is the latest stable version used?
- [ ] Are unused dependencies removed?

---

## Input Validation

### Bean Validation

```java
// ✅ Use Bean Validation
public record CreateUserRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 100)
    String name,
    
    @NotBlank
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
    String password,
    
    @Min(0) @Max(150)
    int age,
    
    @Past
    LocalDate birthDate
) {}

// ✅ Custom validator
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
public @interface Phone {
    String message() default "Invalid phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### Business Validation

```java
// ✅ Business rule validation
@Service
public class UserService {
    
    public void createUser(CreateUserRequest request) {
        // Framework validation passed, do business validation
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already exists");
        }
        // ...
    }
}
```

### Review Points

- [ ] Is Bean Validation used?
- [ ] Are field lengths, formats, ranges validated?
- [ ] Is business rule validation complete?
