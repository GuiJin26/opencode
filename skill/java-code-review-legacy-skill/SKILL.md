---
name: java-code-review-legacy
version: "1.0.0"
tags: [java, spring-boot, code-review, performance, security, java-8, spring-boot-2, legacy]
description: |
  Java/Spring Boot code review skill for legacy Java 8 and Spring Boot 2.x projects.
  Covers Java 8 features (Lambda, Stream, Optional, CompletableFuture), Spring Boot 2 best practices,
  JPA optimization, security review, etc.
  For modern Java 17/21 / Spring Boot 3 projects, use the java-code-review skill instead.
allowed-tools:
  - Read
  - Grep
  - Glob
  - Bash
  - WebFetch
---

# Java Code Review Skill (Legacy)

A code review skill designed for **Java 8** and **Spring Boot 2.x** legacy projects.

> **Note:** For Java 17/21 / Spring Boot 3 modern projects, use the `java-code-review` skill.

## Use Cases

- Review legacy Java 8 / Spring Boot 2 Pull Requests
- Code quality assessment and improvement suggestions
- Architecture design and pattern review
- Performance issue diagnosis (N+1 queries, memory leaks)
- Security vulnerability detection
- Technical debt identification
- Migration planning to modern Java/Spring Boot

## AI Security Rules

> **Prerequisite:** Follow `ai-security` skill rules when using AI tools.

Before sending code to AI for review:
- No passwords, API keys, tokens
- No real emails (use `user@example.com`)
- No production configs
- Redact secrets: `password="xxx"` → `password="${PASSWORD}"`

During review, flag these as 🔴 **blocking**:
- Hardcoded secrets in code
- Secrets in logs (`log.info("password: {}", pwd)`)
- Credentials in connection strings

See: [ai-security skill](../../../ai-security-skill/SKILL.md) for full rules.

## Review Principles

### 1. Constructive Feedback

```markdown
❌ "This code has a problem."
✅ "There may be a concurrency issue here. When multiple threads access simultaneously, a race condition can occur. Consider using ConcurrentHashMap or locking."

❌ "Why use this pattern?"
✅ "Have you considered the Strategy pattern? This would make the logic for different payment methods easier to extend and test."
```

### 2. Priority Markers

| Marker | Meaning | Action |
|--------|---------|--------|
| 🔴 `[blocking]` | Must fix | Blocks merge |
| 🟡 `[important]` | Should fix | Recommend fix before merge |
| 🟢 `[nit]` | Minor issue | Non-blocking |
| 💡 `[suggestion]` | Improvement suggestion | Optional |
| 📚 `[learning]` | Knowledge sharing | No change needed |
| 🎉 `[praise]` | Excellent code | Worth praising |

## Four-Phase Review Process

### Phase 1: Context Understanding (2-3 min)

1. Read PR description and linked Issues
2. Check PR size (>400 lines suggests splitting)
3. Verify CI/CD status (tests passing)
4. Understand business requirements background

### Phase 2: High-Level Design Review (5-10 min)

1. **Architecture Design** - Does the solution match the problem scale?
2. **Performance Impact** - Are there N+1, large loops, memory leak risks?
3. **File Organization** - Is the class/package structure reasonable?
4. **Test Strategy** - Are edge cases covered?

### Phase 3: Line-by-Line Review (10-20 min)

Check each file for:
- **Logical Correctness** - Edge cases, null checks, concurrency safety
- **Security** - SQL injection, XSS, sensitive data handling
- **Performance** - Query optimization, resource management
- **Maintainability** - Clear naming, single responsibility, appropriate comments

### Phase 4: Summary & Decision (2-3 min)

1. Summarize key issues
2. Highlight positives
3. State review conclusion: ✅ Approve / 💬 Comment / 🔄 Request Changes

## Core Review Dimensions

### Java 8 Features

- [ ] Lambda expressions used appropriately (not overly complex)
- [ ] Stream API for data pipelines (not for simple iterations)
- [ ] Optional only for return values
- [ ] CompletableFuture for async operations
- [ ] DateTimeFormatter instead of SimpleDateFormat
- [ ] java.time API instead of Date/Calendar

### Spring Boot 2 Best Practices

- [ ] Constructor injection replacing @Autowired
- [ ] @ConfigurationProperties for type-safe configuration
- [ ] Global exception handling with @RestControllerAdvice
- [ ] Single responsibility Controllers, business logic in Service
- [ ] Proper transaction management at Service layer

### Database & Performance (Shared)

- [ ] @Transactional(readOnly = true) for read operations
- [ ] N+1 queries (JOIN FETCH / @EntityGraph)
- [ ] Entity correctly implements equals/hashCode
- [ ] Indexes cover query conditions

### Concurrency Safety (Shared)

- [ ] ConcurrentHashMap vs HashMap for shared state
- [ ] DateTimeFormatter instead of SimpleDateFormat
- [ ] Reasonable lock granularity
- [ ] Thread-safe collections for concurrent access

## Detailed Guides

### Java 8 & Spring Boot 2.x (Version-Specific)

| Topic | Document |
|-------|----------|
| **Java 8 Features** | [reference/java-8-features.md](reference/java-8-features.md) |
| **Spring Boot 2 Best Practices** | [reference/spring-boot-2.md](reference/spring-boot-2.md) |
| **Concurrency (Java 8)** | [reference/concurrency.md](reference/concurrency.md) |

### General Best Practices (Shared)

Shared references are located in the `java-code-review` skill's `reference/` directory.

| Topic | Document |
|-------|----------|
| **Architecture Design Review** | [../java-code-review-skill/reference/architecture.md](../java-code-review-skill/reference/architecture.md) |
| **Code Quality Anti-patterns** | [../java-code-review-skill/reference/code-quality.md](../java-code-review-skill/reference/code-quality.md) |
| **JPA & Database** | [../java-code-review-skill/reference/jpa-database.md](../java-code-review-skill/reference/jpa-database.md) |
| **Security Review** | [../java-code-review-skill/reference/security.md](../java-code-review-skill/reference/security.md) |
| **Common Bug List** | [../java-code-review-skill/reference/common-bugs.md](../java-code-review-skill/reference/common-bugs.md) |
| **Testing Standards** | [../java-code-review-skill/reference/testing.md](../java-code-review-skill/reference/testing.md) |
| **Performance Review** | [../java-code-review-skill/reference/performance.md](../java-code-review-skill/reference/performance.md) |

## Quick Checklist

See [assets/checklist.md](assets/checklist.md)

## Review Template

See [templates/review-template.md](templates/review-template.md) for standardized PR review output format.

## Migration to Modern Java

When reviewing legacy code, identify migration opportunities and mark them as `💡 [suggestion]`:

| Java 8 Pattern | Modern Equivalent | Benefit |
|----------------|-------------------|---------|
| POJO/DTO with Lombok | `record` (Java 16+) | Immutable, concise |
| Traditional switch | Switch expression (Java 14+) | No fall-through, returns value |
| String concatenation for multi-line | Text block (Java 15+) | Readable |
| `instanceof` + cast | Pattern matching (Java 16+) | Eliminates cast |
| Thread pools for I/O | Virtual threads (Java 21) | Better throughput |
| `Collectors.toList()` | `.toList()` (Java 16+) | Shorter |
| `javax.*` packages | `jakarta.*` (Spring Boot 3) | Jakarta EE namespace |

### Migration Notes Format

```markdown
💡 [suggestion] Consider updating to Jakarta namespace when migrating to Spring Boot 3:
- `javax.persistence.Entity` → `jakarta.persistence.Entity`
- `javax.validation.constraints.NotBlank` → `jakarta.validation.constraints.NotBlank`
```
