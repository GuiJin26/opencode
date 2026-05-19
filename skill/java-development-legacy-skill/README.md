# Java Development Skill (Legacy)

A development skill for **Java 8** and **Spring Boot 2.x** projects.

## Overview

This skill provides development patterns, best practices, and code templates for Java 8 / Spring Boot 2 development. It references the same shared guides as the code review skill but focuses on applying them during development.

## Features

- **Java 8 Patterns** - Optional, Stream API, CompletableFuture, Date/Time API
- **Spring Boot 2 Patterns** - Custom ErrorResponse, configuration, transaction management
- **Code Templates** - Ready-to-use templates for Entity, Service, Controller, Repository, and Tests
- **Best Practices** - Patterns and anti-patterns for common scenarios

## Installation

```bash
cp -r . ~/.config/opencode/skills/java-development-legacy
```

## Usage

Use this skill when:
- Implementing features for Java 8 projects
- Working with Spring Boot 2.x applications
- Maintaining legacy codebases
- Creating boilerplate code for legacy projects

## Related Skills

| Skill | Purpose |
|-------|---------|
| `java-development` | Development for Java 17/21/Spring Boot 3 |
| `java-code-review` | Code review for modern Java |
| `java-code-review-legacy` | Code review for Java 8/Spring Boot 2 |

## Structure

```
SKILL.md              # Skill definition (entry point)
assets/checklist.md   # Pre-commit development checklist
templates/*.java      # Code templates
```

## Java 8 vs Modern Java

| Feature | Java 8 | Java 17/21 |
|---------|--------|------------|
| DTOs | `@Data` class | `record` |
| Switch | Traditional | Switch expression |
| Multi-line strings | Concatenation | Text blocks |
| Type checking | instanceof + cast | Pattern matching |
| Async I/O | Thread pools | Virtual threads |
| Stream to List | `.collect(Collectors.toList())` | `.toList()` |

## Quick Start

1. Read the [Development Checklist](assets/checklist.md)
2. Use [Code Templates](templates/) for boilerplate
3. Follow patterns in [SKILL.md](SKILL.md)
4. Reference shared guides for detailed best practices
