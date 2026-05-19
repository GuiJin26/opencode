# AGENTS.md

## Project Type

OpenCode skill definition for Java 17/21 / Spring Boot 3 development. Not an executable code project - no build, test, or lint commands exist.

## Structure

```
SKILL.md              # Skill definition with YAML frontmatter (entry point)
README.md             # User-facing documentation
assets/checklist.md   # Pre-commit development checklist
templates/*.java      # Code templates (5 files)
```

## Key Conventions

- Skill name in frontmatter: `java-development`
- References shared files from `java-code-review-skill/reference/`
- Code templates are in Java syntax (`.java` extension for IDE recognition)
- Installed via: `cp -r . ~/.config/opencode/skills/java-development`

## When Editing

- SKILL.md frontmatter defines metadata (name, description, allowed-tools)
- Templates should be copy-paste ready with minimal modification
- Keep templates consistent with code review guidelines
- Add new templates when common patterns emerge

## Related Skills

- `java-code-review` - For code review of modern Java projects
- `java-development-legacy` - For Java 8 / Spring Boot 2 development
- Shared references located in `java-code-review-skill/reference/`

## Usage Notes

This skill is designed for development sub-agents. Key points:
1. Provides code templates that follow best practices
2. References the same shared guides as code review skill
3. Focuses on "how-to" rather than "what to check"
4. Includes naming conventions and common patterns
