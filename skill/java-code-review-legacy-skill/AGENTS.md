# AGENTS.md

## Project Type

OpenCode skill definition for Java 8 / Spring Boot 2 legacy code review. Not an executable code project - no build, test, or lint commands exist.

## Structure

```
SKILL.md              # Skill definition with YAML frontmatter (entry point)
README.md             # User-facing documentation
reference/*.md        # Version-specific reference guides (3 files)
├── java-8-features.md        # Java 8: Lambda, Stream, Optional, CompletableFuture
├── spring-boot-2.md          # Spring Boot 2 specific practices
└── concurrency.md            # Java 8 concurrency (no virtual threads)
assets/checklist.md   # Quick review checklist
templates/review-template.md  # Standardized PR review output format
```

## Key Conventions

- Skill name in frontmatter: `java-code-review-legacy`
- References to shared files use relative paths to `../java-code-review-skill/reference/`
- Installed via: `cp -r . ~/.config/opencode/skills/java-code-review-legacy`

## When Editing

- SKILL.md frontmatter defines metadata (name, description, allowed-tools)
- Reference docs are standalone markdown files linked from SKILL.md
- Maintain consistency with the `java-code-review` (modern) skill for shared concepts
- Mark migration suggestions with `💡 [suggestion]` for future modernization

## Related Skills

- `java-code-review` - For Java 17/21 and Spring Boot 3.x projects (modern)
- Shared references located in `java-code-review-skill/reference/`
