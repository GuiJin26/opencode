# Java Code Review Skill (Legacy)

A code review skill for Java 8 and Spring Boot 2.x legacy projects.

## Features

- **Java 8 Focus**: Lambda, Stream API, Optional, CompletableFuture, Date/Time API
- **Spring Boot 2**: Configuration, dependency injection, exception handling
- **Concurrency**: Thread-safe collections, atomic classes, locks
- **Shared Best Practices**: Architecture, security, JPA, testing, performance

## Installation

```bash
cp -r . ~/.config/opencode/skills/java-code-review-legacy
```

## Usage

Invoke the skill when reviewing Java 8 / Spring Boot 2 code:

```
Use java-code-review-legacy skill to review this PR
```

## Structure

```
java-code-review-legacy-skill/
├── SKILL.md                    # Skill definition (entry point)
├── README.md                   # This file
├── reference/
│   ├── java-8-features.md      # Java 8 specific features
│   ├── spring-boot-2.md        # Spring Boot 2 best practices
│   └── concurrency.md          # Java 8 concurrency patterns
├── assets/
│   └── checklist.md            # Quick review checklist
└── templates/
    └── review-template.md      # PR review output format
```

## Shared References

This skill shares common references with `java-code-review` skill:
- Architecture design review
- Code quality anti-patterns
- JPA & database optimization
- Security review checklist
- Common bug patterns
- Testing standards
- Performance optimization

## Related Skills

- `java-code-review` - For Java 17/21 and Spring Boot 3.x projects
