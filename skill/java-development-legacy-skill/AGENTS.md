# AGENTS.md

## Project Type

OpenCode skill definition for Java 8 / Spring Boot 2 development. Not an executable code project - no build, test, or lint commands exist.

## Structure

```
SKILL.md              # Skill definition with YAML frontmatter (entry point)
README.md             # User-facing documentation
assets/checklist.md   # Pre-commit development checklist
templates/*.java      # Code templates
```

## Key Conventions

- Skill name in frontmatter: `java-development-legacy`
- References shared files from `java-code-review-skill/reference/`
- Code templates are in Java syntax (`.java` extension for IDE recognition)
- Installed via: `cp -r . ~/.config/opencode/skills/java-development-legacy`

## When Editing

- SKILL.md frontmatter defines metadata (name, description, allowed-tools)
- Templates should be copy-paste ready with minimal modification
- Keep templates consistent with code review guidelines
- Focus on Java 8 specific patterns (Optional, Stream, CompletableFuture, java.time)

## Related Skills

- `java-code-review-legacy` - For code review of legacy Java projects
- `java-development` - For Java 17/21 / Spring Boot 3 development
- Shared references located in `java-code-review-skill/reference/`

## Usage Notes

This skill is designed for development sub-agents working on legacy projects. Key points:
1. Provides code templates that follow Java 8 best practices
2. References the same shared guides as code review skill
3. Focuses on "how-to" rather than "what to check"
4. Includes Java 8 specific patterns (Optional, Stream, CompletableFuture)
