# Spring Boot API Summary Skill

## Purpose

Scan a Spring Boot project and generate a simple summary table of all REST endpoints with their downstream dependencies.

---

## Input

Provide one of the following:
1. **Repository URL** - Git repository link to clone and analyze
2. **Project Path** - Local directory path of the Spring Boot project

---

## Output

A markdown table with 4 columns:

| Endpoint | Method | Description | Downstream |
|----------|--------|-------------|------------|
| `/api/v1/orders` | POST | Creates a new order | `http://inventory-service:8080/api/v1/inventory/reserve`, `http://payment-service:8080/api/v1/payments`, Kafka(order-events) |

---

## Analysis Steps

### Step 1: Scan Project Structure

```
Find:
├── **/*Controller.java       → REST endpoints
├── **/*Service.java          → Business logic (for downstream tracing)
├── **/*Client.java           → External service calls
├── **/*Repository.java       → Database operations
├── **/*Feign*.java           → Feign client definitions
└── src/main/resources/*.yml  → Service configuration (URLs, endpoints)
```

### Step 2: Read Configuration Files

Scan `src/main/resources/` for:
- `application.yml` / `application.yaml`
- `application-{profile}.yml` (dev, test, prod, etc.)
- `application.properties`

**Extract:**

```yaml
# Example: application.yml
inventory-service:
  url: http://inventory-service:8080
  endpoints:
    reserve: /api/v1/inventory/reserve

payment-service:
  base-url: http://payment-service:8080

# Or Feign client config
feign:
  client:
    config:
      inventory-client:
        url: http://inventory-service:8080
```

**Map service names to full URLs:**
```
inventory-service → http://inventory-service:8080/api/v1/inventory/reserve
payment-service   → http://payment-service:8080/api/v1/payments
kafka             → Kafka(order-events)
database          → MySQL(orders, order_items)
```

### Step 3: Extract Endpoints

For each `@RestController`:

1. Identify `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`
2. Build full path by combining class-level and method-level annotations
3. Determine HTTP method
4. Write brief description based on method name and logic

### Step 4: Trace Downstream (Full Endpoint)

For each endpoint handler, identify and **resolve full URLs**:

| Downstream Type | How to Detect | Output Format |
|-----------------|---------------|---------------|
| **Database** | Repository calls, JPA operations | `MySQL(table_name)` or `PostgreSQL(table_name)` |
| **Service** | FeignClient, RestTemplate, WebClient | `http://service-host:port/api/path` |
| **Message Queue** | KafkaTemplate, RabbitMQ, JMS | `Kafka(topic_name)` or `RabbitMQ(queue_name)` |
| **External API** | HTTP client calls to external services | `https://external-api.com/endpoint` |

**Resolution Logic:**

```
1. Find FeignClient name → lookup in config
2. Extract base URL from application.yml
3. Combine with endpoint path from @RequestMapping
4. Output full URL: http://host:port/api/path
```

---

## Output Format

### Example Output

```markdown
# API Summary: order-service

## Endpoints

| Endpoint | Method | Description | Downstream |
|----------|--------|-------------|------------|
| `/api/v1/orders` | POST | Create a new order | `http://inventory-service:8080/api/v1/inventory/reserve`, `http://payment-service:8080/api/v1/payments`, Kafka(order-events) |
| `/api/v1/orders/{id}` | GET | Get order by ID | MySQL(orders) |
| `/api/v1/orders/{id}` | PUT | Update order status | MySQL(orders), Kafka(order-events) |
| `/api/v1/orders/{id}` | DELETE | Cancel an order | `http://inventory-service:8080/api/v1/inventory/release`, MySQL(orders) |
| `/api/v1/orders/user/{userId}` | GET | List orders by user | MySQL(orders, order_items) |

## Configuration Sources

Downstream URLs resolved from:
- `application.yml` (default)
- `application-dev.yml` (dev profile)
- `application-prod.yml` (prod profile)

## Dependencies Summary

| Type | Service | URL/Resource |
|------|---------|--------------|
| Database | MySQL | orders, order_items |
| Service | inventory-service | http://inventory-service:8080 |
| Service | payment-service | http://payment-service:8080 |
| MessageQueue | Kafka | order-events, order-audit |
```

---

## Usage Examples

### Example 1: Local Project
```
/springboot-api-summary /path/to/order-service
```

### Example 2: Git Repository
```
/springboot-api-summary https://github.com/example/order-service
```

---

## Tips for Description

Keep descriptions brief and consistent:
- Use verb + noun format: "Create order", "Get user", "Update status"
- Start with capital letter
- No period at the end
- Keep under 50 characters when possible

| Good | Bad |
|------|-----|
| Create a new order | This endpoint is responsible for creating a new order in the system |
| Get order by ID | Retrieves order information by its unique identifier |
| Cancel an order | Cancels the order and releases inventory |

---

## Notes

- Dynamic path variables shown as `{variable}` (e.g., `/orders/{id}`)
- **Full downstream URLs** resolved from configuration files
- Multiple downstream shown as comma-separated list
- If no downstream found, show `-`
- If URL not found in config, show service name with `⚠️` (e.g., `inventory-service ⚠️ config not found`)
- Focus on **readability** over completeness

## Configuration Pattern Examples

The skill handles common configuration patterns:

### Pattern 1: Direct URL
```yaml
inventory-service:
  url: http://inventory-service:8080/api/v1/inventory/reserve
```

### Pattern 2: Base URL + Endpoint
```yaml
inventory-service:
  base-url: http://inventory-service:8080
  endpoints:
    reserve: /api/v1/inventory/reserve
```

### Pattern 3: Feign Client Config
```yaml
feign:
  client:
    inventory-client:
      url: http://inventory-service:8080
```

### Pattern 4: Spring Cloud Config
```yaml
spring:
  cloud:
    discovery:
      client:
        simple:
          instances:
            inventory-service:
              - uri: http://inventory-service:8080
```
