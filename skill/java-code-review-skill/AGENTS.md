# AGENTS.md

## Project Type

OpenCode skill definition for Java 17/21 / Spring Boot 3 modern code review. Not an executable code project - no build, test, or lint commands exist.

## Structure

```
SKILL.md              # Skill definition with YAML frontmatter (entry point)
README.md             # User-facing documentation
reference/*.md        # All reference guides (10 files)
├── java-17-21-features.md    # Version-specific: Java modern features
├── spring-boot-3.md          # Version-specific: Spring Boot 3
├── concurrency-modern.md     # Version-specific: Virtual threads
├── architecture.md           # Shared
├── code-quality.md           # Shared
├── jpa-database.md           # Shared
├── security.md               # Shared
├── common-bugs.md            # Shared
├── testing.md                # Shared
└── performance.md            # Shared
assets/checklist.md   # Quick review checklist
templates/review-template.md  # Standardized PR review output format
```

## Key Conventions

- Skill name in frontmatter: `java-code-review`
- Version-specific files: `java-17-21-features.md`, `spring-boot-3.md`, `concurrency-modern.md`
- Shared files: `architecture.md`, `code-quality.md`, etc. (used by both skills)
- Installed via: `cp -r . ~/.config/opencode/skills/java-code-review`

## When Editing

- SKILL.md frontmatter defines metadata (name, description, allowed-tools)
- Reference docs are standalone markdown files linked from SKILL.md
- Shared docs are used by both `java-code-review` and `java-code-review-legacy` skills
- Maintain consistency between SKILL.md overview and reference/*.md details

## Related Skills

- `java-code-review-legacy` - For Java 8 / Spring Boot 2.x projects (legacy)
- Legacy skill references shared files via relative paths `../java-code-review-skill/reference/`
