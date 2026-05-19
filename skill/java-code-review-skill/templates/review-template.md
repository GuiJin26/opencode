# PR Review Template

Standard output format for Java/Spring Boot code reviews.

---

## Summary

Brief overview of the PR and its purpose.

**Verdict:** ✅ Approve / 💬 Comment / 🔄 Request Changes

---

## 🔴 Blocking Issues

Issues that must be fixed before merge.

### [Issue Title]

**File:** `path/to/File.java:123`

**Problem:** Description of the issue.

```java
// Current code
problematic code here
```

**Suggestion:** How to fix it.

```java
// Recommended code
fixed code here
```

---

## 🟡 Important Issues

Issues that should be fixed but don't block merge.

### [Issue Title]

**File:** `path/to/File.java:456`

**Problem:** Description.

**Suggestion:** Fix recommendation.

---

## 🟢 Minor Issues (Nit)

Small improvements, non-blocking.

| File | Line | Comment |
|------|------|---------|
| `File.java` | 10 | Consider extracting to constant |
| `Service.java` | 25 | Typo in variable name |

---

## 💡 Suggestions

Optional improvements.

---

## 🎉 Praise

Highlight good practices and well-written code.

| File | Comment |
|------|---------|
| `UserService.java` | Clean separation of concerns |
| `OrderRepository.java` | Good use of JPQL projection |

---

## Review Checklist Summary

| Category | Status |
|----------|--------|
| Architecture | ✅ / ⚠️ / ❌ |
| Java Features | ✅ / ⚠️ / ❌ |
| Spring Boot | ✅ / ⚠️ / ❌ |
| Database | ✅ / ⚠️ / ❌ |
| Security | ✅ / ⚠️ / ❌ |
| Performance | ✅ / ⚠️ / ❌ |
| Testing | ✅ / ⚠️ / ❌ |

---

## Details by Category

### Architecture

- [ ] Single responsibility followed
- [ ] No circular dependencies
- [ ] Proper layer separation

### Spring Boot

- [ ] Constructor injection used
- [ ] Transactions at correct layer
- [ ] Global exception handling

### Database

- [ ] No N+1 queries
- [ ] Proper indexing
- [ ] Batch operations optimized

### Security

- [ ] Input validation present
- [ ] No SQL injection risks
- [ ] Sensitive data protected

### Performance

- [ ] Pagination for list endpoints
- [ ] No O(n²) algorithms
- [ ] Caching considered

### Testing

- [ ] Core logic covered
- [ ] Edge cases tested
- [ ] Exception paths covered
