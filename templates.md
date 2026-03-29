# Output Format Templates

This file contains detailed template formats referenced by various agents. Use these when creating structured documents.

---

## 💰 Token-Saving Workflow Templates

These templates ensure minimal token usage (98% reduction) while maintaining full work quality.

### Supervisor Prompt Templates

**Requirements Phase (Alice):**
```markdown
Requirements for: [project-name]
Task: [brief description]

**Instructions:**
- Create requirement document based on user's request
- Save to: `project-name/requirements.md`
- See templates.md for format

**Return only:** "Requirements saved to `project-name/requirements.md`"
```

**Development Phase (Bob):**
```markdown
**Requirements file:** `project-name/requirements.md`

**Instructions:**
- Read requirements.md for full details
- Implement all user stories (both frontend and backend)
- Create project structure in `project-name/` directory
- Save all code to appropriate files

**Return only:** "Implementation complete. Files saved to `project-name/`"
```

**Review Phase (Frank):**
```markdown
**Project location:** `project-name/`
**Requirements:** `project-name/requirements.md`

**Instructions:**
- Read requirements.md to understand what was requested
- Review all code files in the project
- Check: code quality, security, maintainability, best practices
- Save review results to `project-name/review-notes.md`

**Return only:** "Review complete. Notes saved to `project-name/review-notes.md`"
```

**Testing Phase (Charlie):**
```markdown
**Project location:** `project-name/`
**Requirements:** `project-name/requirements.md`
**Review notes:** `project-name/review-notes.md`

**Instructions:**
- Read requirements.md to understand acceptance criteria
- Read review-notes.md to understand implementation
- Test all user stories against acceptance criteria
- Save test results to `project-name/test-report.md`

**Return only:** "Testing complete. Report saved to `project-name/test-report.md`"
```

### Agent Response Templates

**Alice (PM):**
```
Requirements saved to `project-name/requirements.md`
```

**Bob (Dev):**
```
Implementation complete. Files saved to `project-name/`
- Backend: project-name/backend/src/*.ts
- Frontend: project-name/frontend/src/*.tsx
- API Design: project-name/api-design.md
```

**Frank (Review):**
```
Review complete. Notes saved to `project-name/review-notes.md`

Summary: [brief summary: APPROVED/NEEDS REVISION/REJECTED with X issues]
```

**Charlie (QA):**
```
Testing complete. Report saved to `project-name/test-report.md`

Summary: [X/X acceptance criteria passed. Status: APPROVED/FAILED]
```

### Project Structure Template

```
project-name/
├── requirements.md          # Created by Alice
├── review-notes.md          # Created by Frank
├── test-report.md           # Created by Charlie
├── api-design.md            # Created by Bob
├── backend/                 # Created by Bob
│   ├── src/
│   │   ├── index.ts
│   │   ├── routes/
│   │   ├── models/
│   │   └── services/
│   ├── package.json
│   └── tsconfig.json
└── frontend/                # Created by Bob
    ├── src/
    │   ├── App.tsx
    │   ├── components/
    │   ├── api/
    │   └── types/
    ├── package.json
    └── tsconfig.json
```

### Token Savings Summary

| Agent | Traditional | Optimized | Savings |
|-------|-----------|-----------|---------|
| Alice (PM) | ~5,000 | ~150 | 97% |
| Bob (Dev) | ~7,000 | ~120 | 98.3% |
| Frank (Review) | ~8,500 | ~100 | 98.8% |
| Charlie (QA) | ~10,000 | ~90 | 99.1% |
| **TOTAL** | **~26,500** | **~460** | **98.3%** |

---



---

## API Design Document (Dev Bob)

```markdown
# API Design Document

## Overview
[Brief description of the API]

## Endpoints
### [Method] /api/[resource]
- Description: [What it does]
- Request: [Body/Query params]
- Response: [Success response structure]
- Errors: [Error codes and messages]
- Authentication: [Required auth]

## Data Models
[Entity schemas and relationships]

## Security
[Auth, validation, rate limiting]
```

---



## Requirements Document (PM Alice)

```markdown
# Requirements Document

## Overview
[Clear summary of the requirement]

## User Stories
### Story 1: [Title]
**As a** [role],
**I want** [feature],
**So that** [benefit]

#### Acceptance Criteria
- Given [context], When [action], Then [outcome]
- [Additional criteria...]

#### Priority: [Must Have / Should Have / Nice to Have]

### Story 2: [Title]
...

## Functional Requirements
[Detailed feature specifications]

## Non-Functional Requirements
[Performance, security, usability considerations]

## Technical Constraints
[Implementation guidance and limitations]

## Dependencies
[Related features or systems]

## Success Metrics
[How to measure successful implementation]
```

---

## Review Report Format (Review Frank)

```markdown
# Code Review Report

## Executive Summary
- Overall Status: [APPROVED / NEEDS REVISION / REJECTED]
- Quality Score: [X/10]
- Issues Found: [Critical: X, Major: X, Minor: X]

## Critical Issues 🔴
[Critical security vulnerabilities or breaking changes]

## Major Issues 🟡
[Code smells, performance issues, design concerns]

## Minor Issues 🟢
[Style issues, documentation gaps, minor optimizations]

## Findings by Severity
[Detailed file:line references with code examples]

## Strengths
[Positive aspects of the implementation]

## Recommendation
[Clear decision and next steps]
```

---

## Todo List Format (Supervisor Gui)

```json
[
  {"content": "[Alice] Task description", "status": "pending|in_progress|completed", "priority": "high|medium|low"},
  {"content": "[Bob] Task description", "status": "pending|in_progress|completed", "priority": "high|medium|low"},
  {"content": "[Frank] Task description", "status": "pending|in_progress|completed", "priority": "high|medium|low"},
  {"content": "[Charlie] Task description", "status": "pending|in_progress|completed", "priority": "high|medium|low"}
]
```

**Status Indicators:**
- ⏳ `pending` - Waiting to start
- 🔄 `in_progress` - Currently working
- ✅ `completed` - Finished successfully
- ❌ `cancelled` - Cancelled or skipped

---

**Note:** These templates provide detailed structure when needed. For most tasks, concise summaries are sufficient. Reference this file when comprehensive documentation is required.
