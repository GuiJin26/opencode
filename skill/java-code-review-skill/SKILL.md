---
name: java-code-review
version: "2.0.0"
tags: [java, spring-boot, code-review, performance, security, java-17, java-21, spring-boot-3]
description: |
  Java/Spring Boot code review skill for modern Java (17/21) and Spring Boot 3.x.
  Covers modern Java features (Records, Virtual Threads, Pattern Matching), Spring Boot 3 best practices,
  JPA optimization, security review, etc.
  For legacy Java 8 / Spring Boot 2 projects, use the java-code-review-legacy skill instead.
allowed-tools:
  - Read
  - Grep
  - Glob
  - Bash
  - WebFetch
---

# Java Code Review Skill (Modern)

A code review skill designed for **Java 17/21** and **Spring Boot 3.x** projects.

> **Note:** For Java 8 / Spring Boot 2 legacy projects, use the `java-code-review-legacy` skill.

## Use Cases

- Review Java/Spring Boot Pull Requests
- Code quality assessment and improvement suggestions
- Architecture design and pattern review
- Performance issue diagnosis (N+1 queries, memory leaks)
- Security vulnerability detection
- Technical debt identification

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

### Modern Features (Java 17/21+)

- [ ] Records replacing POJOs/DTOs
- [ ] Switch expressions replacing traditional switch
- [ ] Text blocks for multiline strings
- [ ] Sealed Classes to restrict inheritance
- [ ] Pattern Matching to simplify type checking
- [ ] Virtual Threads for I/O-intensive tasks

### Spring Boot 3 Best Practices

- [ ] Constructor injection replacing @Autowired
- [ ] @ConfigurationProperties for type-safe configuration
- [ ] ProblemDetail for error responses (RFC 7807)
- [ ] Single responsibility Controllers, business logic in Service
- [ ] Observability with Micrometer

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

### Modern Java & Spring Boot 3.x (Version-Specific)

| Topic | Document |
|-------|----------|
| **Java 17/21 Features** | [reference/java-17-21-features.md](reference/java-17-21-features.md) |
| **Spring Boot 3 Best Practices** | [reference/spring-boot-3.md](reference/spring-boot-3.md) |
| **Concurrency & Virtual Threads** | [reference/concurrency-modern.md](reference/concurrency-modern.md) |

### General Best Practices (Shared)

| Topic | Document |
|-------|----------|
| **Architecture Design Review** | [reference/architecture.md](reference/architecture.md) |
| **Code Quality Anti-patterns** | [reference/code-quality.md](reference/code-quality.md) |
| **JPA & Database** | [reference/jpa-database.md](reference/jpa-database.md) |
| **Security Review** | [reference/security.md](reference/security.md) |
| **Common Bug List** | [reference/common-bugs.md](reference/common-bugs.md) |
| **Testing Standards** | [reference/testing.md](reference/testing.md) |
| **Performance Review** | [reference/performance.md](reference/performance.md) |

## Quick Checklist

See [assets/checklist.md](assets/checklist.md)

## Review Template

See [templates/review-template.md](templates/review-template.md) for standardized PR review output format.
