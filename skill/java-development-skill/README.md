# Java Development Skill

A development skill for **Java 17/21** and **Spring Boot 3.x** projects.

## Overview

This skill provides development patterns, best practices, and code templates for Java/Spring Boot development. It references the same shared guides as the code review skill but focuses on applying them during development.

## Features

- **Development Workflow** - Step-by-step guide from design to implementation
- **Code Templates** - Ready-to-use templates for Entity, Service, Controller, Repository, and Tests
- **Best Practices** - Patterns and anti-patterns for common scenarios
- **Quick Reference** - Naming conventions, annotations, and common patterns

## Installation

```bash
cp -r . ~/.config/opencode/skills/java-development
```

## Usage

Use this skill when:
- Implementing new features
- Writing new code
- Following best practices during development
- Creating boilerplate code

## Related Skills

| Skill | Purpose |
|-------|---------|
| `java-code-review` | Code review for modern Java |
| `java-code-review-legacy` | Code review for Java 8/Spring Boot 2 |
| `java-development-legacy` | Development for Java 8/Spring Boot 2 |

## Structure

```
SKILL.md              # Skill definition (entry point)
assets/checklist.md   # Pre-commit development checklist
templates/*.java      # Code templates (Entity, Service, Controller, Repository, Test)
```

## Templates

| Template | Description |
|----------|-------------|
| `entity-template.java` | JPA Entity with proper equals/hashCode |
| `service-template.java` | Service layer with transaction management |
| `controller-template.java` | REST API controller |
| `repository-template.java` | JPA Repository interface |
| `test-template.java` | Unit test with Mockito |

## Quick Start

1. Read the [Development Checklist](assets/checklist.md)
2. Use [Code Templates](templates/) for boilerplate
3. Follow patterns in [SKILL.md](SKILL.md)
4. Reference shared guides for detailed best practices
