# Requirements Document: Story Transfer Skill

**Project Name:** story-transfer-skill
**Document Version:** 1.0 (Draft for review)
**Date:** 2026-10-18
**Author:** Alice (Product Manager)
**Status:** Pending confirmation from Pod Team Lead

---

## 1. Project Overview & Background

The IT pod team receives requirements from a business team. When a new function or feature needs to be delivered, Business Analysts (BAs) from the business team write user stories in agile language and walk them through with the IT pod so that everyone understands the details.

In practice, however, story quality is inconsistent:

- Stories are written by **different BAs with different backgrounds and writing capabilities**.
- BAs have **limited IT knowledge**, which makes it hard for them to anticipate technical concerns (data, integration, error handling, boundaries).
- As a result, stories are often **too simple and not clear enough** for developers and QA to fully understand, leading to **mis-implementation or wrong testing**.

This project delivers an **opencode skill** (a SKILL.md-based skill, with supporting files if needed) that closes this gap by providing two core capabilities:

1. **Story Review Mode** — Review a BA-written user story from BOTH the Developer's and the QA's perspectives; identify issues and generate actionable improvement suggestions addressed **to the BA**, so the BA realizes the gaps and corrects/supplements the story.
2. **Story Transfer Mode** — Transform the story content into (a) **developer-oriented language** (implementation guidance, technical considerations) and (b) **QA-oriented language** (testable acceptance criteria, test scenario outlines), so developers implement correctly and QA designs test cases easily.

---

## 2. Problem Statement

| # | Problem | Consequence |
|---|---------|-------------|
| P1 | Stories written by BAs lack technical depth (no edge cases, no error handling, no data/integration details) | Developers guess during implementation; rework and mis-implementation |
| P2 | Acceptance criteria are missing, vague, or untestable | QA cannot derive test cases; wrong or incomplete testing |
| P3 | Story quality varies by BA writing capability | Unpredictable backlog quality; walkthrough meetings spend excessive time on clarifying basics |
| P4 | Gaps are discovered late (during development or testing) instead of at story walkthrough time | Rework, delays, defects escaping to production |
| P5 | Developers and QA each interpret the same story independently with no shared, role-specific artifact | Misalignment between implementation and testing |

**Root cause:** BAs are domain experts but not IT experts. There is no systematic, repeatable mechanism to (a) detect gaps in BA-written stories early and (b) translate business intent into role-specific, unambiguous guidance for dev and QA.

---

## 3. Goals & Non-Goals

### 3.1 Goals

- **G1:** Provide a repeatable, systematic **review** of BA-written user stories from both Developer and QA perspectives.
- **G2:** Produce **actionable, BA-friendly improvement suggestions** so BAs can self-correct and supplement their stories.
- **G3:** **Transform** reviewed story content into developer-oriented implementation guidance (technical considerations, edge cases, error handling, boundaries, API/data impacts).
- **G4:** **Transform** reviewed story content into QA-oriented deliverables (Given/When/Then acceptance criteria, positive/negative/boundary test scenario outlines).
- **G5:** Deliver all of the above as an **opencode skill** usable by pod team members with a single invocation, producing structured, shareable Markdown outputs.
- **G6:** Reduce time spent in story walkthrough meetings on basic clarifications; shift gap discovery **before** implementation starts.

### 3.2 Non-Goals

- **NG1:** The skill does **not** replace the BA or write the original user story from scratch.
- **NG2:** The skill does **not** generate code, automated tests, or executable test scripts.
- **NG3:** The skill does **not** integrate directly with Jira/Azure DevOps or other ticketing tools in v1 (input is pasted text or files; integration may be a future enhancement).
- **NG4:** The skill does **not** make final delivery decisions; the pod lead and business team remain the decision makers.
- **NG5:** The skill does **not** review non-functional stories (e.g., pure infrastructure tasks) unless a user story is provided; it is optimized for feature/function stories.

---

## 4. Target Users & Their Needs

| User | Role Description | Key Needs |
|------|------------------|-----------|
| **BA (Business Analyst)** | Writes user stories; domain expert; limited IT knowledge | Receive clear, non-judgmental, business-friendly feedback explaining WHAT is missing and WHY it matters, so they can improve the story themselves |
| **Developer** | Implements the story | Receive unambiguous implementation guidance: business rules, edge cases, error handling, boundary conditions, API/data impacts, and open questions — before coding starts |
| **QA Engineer** | Tests the implementation | Receive testable acceptance criteria (Given/When/Then) and a structured test scenario outline (positive/negative/boundary) that maps directly to test case design |
| **Pod Lead (IT Team Lead)** | Coordinates the pod; runs story walkthroughs with the business team | Quickly assess story quality, get a consolidated gap summary for the walkthrough meeting, and distribute role-specific artifacts to dev and QA |

---

## 5. User Stories & Acceptance Criteria

Priorities: **Must Have (M)**, **Should Have (S)**, **Nice to Have (N)**.

---

### 5.1 Story Review Mode

#### US-R1: Review story from Developer perspective — **Must Have**

> **As a** Developer,
> **I want** the skill to review a BA-written user story from the Developer's perspective,
> **so that** implementation gaps (ambiguity, missing business rules, missing edge cases, missing error handling, missing data/integration details, unbounded scope) are identified before development starts.

**Acceptance Criteria:**
- Given a BA-written user story is provided as input, when Review Mode runs, then the skill produces a **Developer Perspective Review** section that checks the story against the Developer-side review dimensions (see Section 8: Clarity, Completeness, Business Rules, Edge Cases & Boundaries, Error Handling, Data, Integration, NFR, Scope).
- Given the story contains an ambiguous or vague statement (e.g., "the system should be fast" or "handle invalid input appropriately"), when the review runs, then the ambiguity is flagged with the exact quoted text and an explanation of why a developer cannot implement it deterministically.
- Given the story omits a foreseeable edge case (e.g., empty list, max input length, concurrent updates, permission denial), when the review runs, then the omission is flagged with a concrete example of the missing scenario.
- Given the story implies data creation, modification, migration, or an upstream/downstream system interaction that is not specified, when the review runs, then the missing data/integration detail is flagged.
- Each finding includes: **dimension, quoted story evidence, issue description, and severity** (Critical / Major / Minor).
- Given the story is complete and clear on a dimension, then that dimension is marked "OK" (or noted as "no issue found") rather than omitted, so the reviewer knows it was checked.

#### US-R2: Review story from QA perspective — **Must Have**

> **As a** QA Engineer,
> **I want** the skill to review a BA-written user story from the QA's perspective,
> **so that** untestable statements, missing acceptance criteria, and missing negative/boundary scenarios are identified before test design starts.

**Acceptance Criteria:**
- Given a BA-written user story is provided as input, when Review Mode runs, then the skill produces a **QA Perspective Review** section that checks the story against the QA-side review dimensions (see Section 8: Acceptance Criteria quality, Testability, Edge Cases/Boundary coverage, Negative paths, Observability of expected results, Test data needs).
- Given the story has no acceptance criteria at all, when the review runs, then this is flagged as a **Critical** issue.
- Given an acceptance criterion is untestable (subjective, immeasurable, or depends on undefined behavior — e.g., "the page loads quickly" or "validation works correctly"), when the review runs, then it is flagged with the quoted text and an explanation of what measurable condition is missing.
- Given the story only describes the happy path, when the review runs, then missing negative and boundary test scenarios are listed (e.g., invalid input, empty state, maximum values, unauthorized access).
- Each finding includes: **dimension, quoted story evidence, issue description, and severity** (Critical / Major / Minor).
- Given acceptance criteria exist and are testable, then each is validated and confirmed as testable in the output.

#### US-R3: BA-facing improvement suggestions — **Must Have**

> **As a** BA,
> **I want** to receive improvement suggestions written in plain business language and addressed to me,
> **so that** I understand exactly what is missing in my story, why it matters, and how to fix it — without needing IT knowledge.

**Acceptance Criteria:**
- Given Review Mode has produced findings, then the skill generates a **"Suggestions for the BA"** section where every finding is converted into an actionable suggestion.
- Each suggestion follows the pattern: **what is missing/unclear → why it matters (what could go wrong in dev/test if unfixed) → what to add or clarify (with a concrete example phrasing where possible)**.
- Suggestions use **plain business language**: no code, no technical jargon (no terms like "API contract," "race condition," "N+1" without a plain-language explanation in parentheses).
- Suggestions are written in a **constructive, respectful tone** — they must never blame or judge the BA's capability (e.g., avoid "you failed to…"; prefer "consider adding…").
- Given a finding exists in the Developer or QA review, then there must be at least one corresponding BA-facing suggestion — no orphan findings without BA-facing guidance.
- Given multiple findings exist, then suggestions are ordered by severity (Critical first), so the BA knows what to address first.

#### US-R4: Consolidated review summary for Pod Lead — **Should Have**

> **As a** Pod Lead,
> **I want** a consolidated review summary with an overall quality assessment and prioritized gap list,
> **so that** I can run the story walkthrough meeting efficiently and focus the discussion on the most important gaps.

**Acceptance Criteria:**
- Given Review Mode runs, then the output starts with an **Executive Summary** containing: overall story quality rating (e.g., Ready / Ready with minor gaps / Not ready), issue counts by severity, and the top 3–5 gaps to discuss with the BA.
- Given both perspectives are reviewed, then the summary clearly separates Developer-perspective gaps from QA-perspective gaps, and highlights gaps that appear in both.
- The summary is short enough to be read in under 2 minutes.

---

### 5.2 Story Transfer Mode

#### US-T1: Transfer to Developer-oriented language — **Must Have**

> **As a** Developer,
> **I want** the story content transformed into developer-oriented implementation guidance,
> **so that** I can implement the story correctly without guessing at business rules, edge cases, error handling, or data/integration behavior.

**Acceptance Criteria:**
- Given a user story is provided as input, when Transfer Mode runs, then the skill produces a **Developer Specification** document containing at minimum:
  - **Restated objective**: a concise, unambiguous statement of what is being built and why.
  - **Functional requirements**: business rules extracted and stated as discrete, numbered, testable statements.
  - **Edge cases & boundary conditions**: a list of edge/boundary scenarios derived from the story, each with expected behavior stated or explicitly marked as "needs BA confirmation."
  - **Error handling expectations**: expected system behavior for invalid input, failed operations, and unauthorized access, stated or marked as "needs BA confirmation."
  - **Data considerations**: entities/fields created or changed, validation rules, data volume considerations, and data migration/privacy concerns where inferable — flagged as assumptions where not stated in the story.
  - **Integration considerations**: upstream/downstream systems, API impacts, and contract changes where inferable — flagged as assumptions where not stated.
  - **Open questions & assumptions**: every inferred item must be listed here so the developer knows what was NOT in the original story.
- Given the original story is ambiguous on a point, then the transfer must NOT silently resolve the ambiguity; it must state the assumption made and list it under Open Questions & Assumptions.
- Every requirement statement in the Developer Specification must be traceable to the original story (content that came from the story) or explicitly marked as an assumption/inference.

#### US-T2: Transfer to QA-oriented language — **Must Have**

> **As a** QA Engineer,
> **I want** the story content transformed into testable acceptance criteria (Given/When/Then) and a test scenario outline,
> **so that** I can design test cases quickly and completely.

**Acceptance Criteria:**
- Given a user story is provided as input, when Transfer Mode runs, then the skill produces a **QA Test Specification** document containing at minimum:
  - **Acceptance criteria in Given/When/Then format**: one criterion per user-visible behavior, each with concrete preconditions, triggers, and measurable expected outcomes.
  - **Test scenario outline**: a table or structured list of scenarios categorized as **Positive** (happy paths), **Negative** (invalid input, error paths, unauthorized access), and **Boundary** (limits, thresholds, empty/zero/max states).
  - **Test data considerations**: notable data setups the scenarios require, where inferable.
  - **Open questions**: any behavior that cannot be turned into a deterministic expected outcome, listed for BA confirmation.
- Given the original story lacks acceptance criteria, then the skill derives draft Given/When/Then criteria from the story description, clearly marked as **draft — requires BA confirmation**.
- Given the original story has acceptance criteria, then each is refined into Given/When/Then form; criteria that cannot be made testable without more information are flagged rather than dropped.
- Each test scenario must include: scenario name, category (positive/negative/boundary), precondition, steps/trigger, and expected result.

#### US-T3: Combined transfer run — **Must Have**

> **As a** Pod Lead,
> **I want** both the Developer Specification and the QA Test Specification generated from a single run,
> **so that** I can distribute role-specific artifacts to the whole pod at once, guaranteed to be derived from the same story baseline.

**Acceptance Criteria:**
- Given Transfer Mode runs on one story, then both specifications are produced and are mutually consistent (no contradictory statements between the dev and QA views of the same behavior).
- Both specifications share the same numbered set of functional requirements so dev and QA can reference the same requirement IDs during walkthrough and defect discussions.

---

### 5.3 Workflow & Orchestration Stories

#### US-W1: Mode selection via invocation — **Must Have**

> **As a** pod team member,
> **I want** to invoke the skill with a clearly selected mode (Review / Transfer / Review + Transfer),
> **so that** I get exactly the output I need for the current stage of the workflow.

**Acceptance Criteria:**
- Given the user invokes the skill with a story, then the skill supports three modes: **Review only**, **Transfer only**, and **Review + Transfer** (full pipeline).
- Given no mode is explicitly specified, then the skill asks the user which mode to run (or applies a documented default of **Review + Transfer**), and states the mode used in the output header.
- The output document clearly labels which mode produced it.

#### US-W2: Review gates Transfer (full pipeline) — **Should Have**

> **As a** Pod Lead,
> **I want** the full pipeline (Review + Transfer) to carry unresolved review findings into the transfer outputs,
> **so that** the team never treats a transfer artifact as final when the underlying story still has Critical gaps.

**Acceptance Criteria:**
- Given the full pipeline runs and Critical review findings exist, then the Developer Specification and QA Test Specification each carry a prominent **"Story not yet ready — open Critical gaps"** warning at the top, listing the Critical findings.
- Given the full pipeline runs and Critical findings exist, then affected requirement/test sections are marked with "based on incomplete story input — pending BA clarification" rather than being omitted.
- Given the full pipeline runs and no Critical findings exist (only Major/Minor), then transfer proceeds normally and Major/Minor findings are summarized in an appendix of each output for awareness.

#### US-W3: Flexible story input — **Must Have**

> **As a** pod team member,
> **I want** to provide the user story as a file path or as pasted text, and optionally add context (existing system behavior, related stories, known constraints),
> **so that** the skill works with how stories are actually shared in our workflow today.

**Acceptance Criteria:**
- Given the user provides a story as a file path, then the skill reads and parses the story from that file.
- Given the user pastes story text directly, then the skill processes the pasted text.
- Given the story text is malformed or too short to be a story (e.g., only a title, or fewer than one full sentence of description), then the skill reports a clear input error and does not produce a fabricated review.
- Given the user supplies optional context, then the review and transfer must take that context into account and must not flag as "missing" things the context already answers.

#### US-W4: Re-review cycle support — **Should Have**

> **As a** BA,
> **I want** to re-run Review Mode on my updated story and see whether previous gaps are resolved,
> **so that** I can confirm my corrections are sufficient before the walkthrough.

**Acceptance Criteria:**
- Given a previously reviewed story is updated and re-submitted for review, then the skill produces a fresh review of the updated story.
- Given the user provides the previous review report alongside the updated story, then the new review includes a **Gap Closure Check** indicating, for each previous finding, whether it is Resolved / Partially resolved / Not addressed.
- The fresh review may also report **newly introduced** issues caused by the changes.

#### US-W5: File-based, shareable outputs — **Must Have**

> **As a** pod team member,
> **I want** the skill's outputs saved as well-named Markdown files in the project/workspace directory,
> **so that** I can attach them to the ticket, share them in the walkthrough meeting, and reference them later.

**Acceptance Criteria:**
- Given any mode runs, then outputs are saved as Markdown file(s) with clear, predictable names (e.g., `review-report.md`, `dev-spec.md`, `qa-test-spec.md` — exact naming per Section 7).
- Given the full pipeline runs, then a single consolidated output file (or a clearly linked set of files) is produced so nothing is scattered.
- Given output files already exist from a previous run, then the skill either overwrites them with the new result or creates versioned files — behavior must be deterministic and stated in the output.
- The skill's chat response stays **minimal** (file paths + short summary), not the full document content.

#### US-W6: Traceability of findings and outputs — **Should Have**

> **As a** Pod Lead,
> **I want** every review finding, suggestion, requirement, and test scenario to be traceable to its origin (original story text or marked assumption),
> **so that** the business team can validate that we didn't invent requirements, and the pod can audit what came from where.

**Acceptance Criteria:**
- Given a review finding exists, then it quotes or references the specific story text (or explicitly notes "absent from story").
- Given a transferred requirement or test scenario was inferred rather than stated in the story, then it carries an explicit marker (e.g., `[assumption]`) and appears in the Open Questions & Assumptions list.

#### US-W7: Batch review of multiple stories — **Nice to Have**

> **As a** Pod Lead,
> **I want** to run Review Mode over a set of stories (e.g., a whole epic or sprint backlog),
> **so that** I can assess backlog readiness at a glance before sprint planning.

**Acceptance Criteria:**
- Given multiple stories are provided (e.g., a directory of files or a multi-story document), then the skill produces a per-story review plus a backlog-level readiness summary table (story, quality rating, Critical/Major/Minor counts).

---

## 6. Input / Output Specifications

### 6.1 Input

| Item | Required? | Description |
|------|-----------|-------------|
| **User story** | Yes | The BA-written story: title, description/narrative ("As a… I want… so that…"), and acceptance criteria if any. Provided as a file path or pasted text. May include business rules, mockups references, or notes. |
| **Mode** | No (default: Review + Transfer) | One of: `review`, `transfer`, `review+transfer`. |
| **Context (optional)** | No | Known system behavior, related stories, constraints, glossary, existing functionality — anything that helps the skill avoid false "missing" findings. |
| **Previous review report (optional)** | No | Enables the Gap Closure Check (US-W4). |

### 6.2 Output

#### 6.2.1 Review Mode → `review-report.md`

1. **Header** — story title/ID, date, mode used.
2. **Executive Summary** (US-R4) — quality rating, issue counts by severity and perspective, top gaps to discuss.
3. **Developer Perspective Review** (US-R1) — findings grouped by review dimension; each finding: dimension, quoted evidence, issue description, severity.
4. **QA Perspective Review** (US-R2) — same structure, QA-side dimensions.
5. **Suggestions for the BA** (US-R3) — ordered by severity; each suggestion: what's missing → why it matters → what to add (with example phrasing).
6. **Gap Closure Check** (US-W4, when a previous report is provided) — Resolved / Partially resolved / Not addressed.

#### 6.2.2 Transfer Mode → `dev-spec.md` (Developer Specification)

1. **Header** — story title/ID, date, mode, readiness warning if Critical gaps were found (US-W2).
2. **Objective** — restated, unambiguous.
3. **Functional Requirements** — numbered, discrete, testable statements (shared requirement IDs with QA spec — US-T3).
4. **Edge Cases & Boundary Conditions** — scenario + expected behavior or "needs BA confirmation."
5. **Error Handling Expectations** — invalid input, failures, unauthorized access.
6. **Data Considerations** — entities/fields, validation, volume, migration, privacy; marked assumptions.
7. **Integration Considerations** — upstream/downstream, API impacts; marked assumptions.
8. **Open Questions & Assumptions** — complete list of all inferred items.

#### 6.2.3 Transfer Mode → `qa-test-spec.md` (QA Test Specification)

1. **Header** — story title/ID, date, mode, readiness warning if Critical gaps were found.
2. **Acceptance Criteria (Given/When/Then)** — one per user-visible behavior; drafts marked "requires BA confirmation" when derived without original criteria.
3. **Test Scenario Outline** — table with columns: Scenario | Category (Positive/Negative/Boundary) | Precondition | Steps/Trigger | Expected Result.
4. **Test Data Considerations** — notable data setups.
5. **Open Questions** — behaviors that cannot be made deterministic without BA input.

#### 6.2.4 Full pipeline (Review + Transfer)

- Produces `review-report.md` + `dev-spec.md` + `qa-test-spec.md` (or one consolidated `story-analysis.md` containing all three sections — **open question OQ-3**).
- Chat response is minimal: file paths + 3–5 bullet summary.

---

## 7. Review Dimensions Checklist

The Review Mode MUST evaluate every story against each dimension below. Dimensions are marked with the primary audience most affected.

### 7.1 Clarity & Unambiguity *(Dev + QA)*
- [ ] No vague/subjective terms ("fast," "user-friendly," "appropriately," "etc.")
- [ ] No ambiguous pronouns or undefined terms
- [ ] Every statement has one interpretation only

### 7.2 Completeness *(Dev + QA)*
- [ ] User roles/personas involved are identified
- [ ] The full workflow is described (entry point → outcome), not just a fragment
- [ ] Business rules are stated, not implied
- [ ] In-scope vs. out-of-scope is clear

### 7.3 Acceptance Criteria Quality *(QA)*
- [ ] Acceptance criteria exist
- [ ] Each is specific, measurable, and independently verifiable
- [ ] Criteria cover the main flows, not only the happy path
- [ ] Expected results are observable and defined

### 7.4 Testability *(QA)*
- [ ] Every statement of behavior can be verified by a test
- [ ] Expected outcomes are defined for each trigger/action
- [ ] Success/failure states are distinguishable

### 7.5 Edge Cases & Boundary Conditions *(Dev + QA)*
- [ ] Boundary values considered (min/max, empty, zero, first/last)
- [ ] Concurrent/duplicate submissions considered (where applicable)
- [ ] Unusual but valid usage considered
- [ ] Empty states / no-data states considered

### 7.6 Error Handling & Negative Paths *(Dev + QA)*
- [ ] Invalid input behavior defined
- [ ] Failure behavior defined (what does the user see? what happens to their data?)
- [ ] Unauthorized/permission-denied behavior defined (where applicable)

### 7.7 Non-Functional Requirements *(Dev)*
- [ ] Performance expectations stated where relevant (response time, volume)
- [ ] Security/privacy considerations stated where relevant (PII, access control, audit)
- [ ] Compatibility constraints stated where relevant (browsers, devices, versions)
- [ ] Accessibility requirements stated where relevant

### 7.8 Data Considerations *(Dev)*
- [ ] Data created/read/updated/deleted is identified
- [ ] Data validation rules stated
- [ ] Data volume/growth considered
- [ ] Data migration or historical data handling addressed (where applicable)

### 7.9 Integration Considerations *(Dev)*
- [ ] Upstream/downstream systems identified
- [ ] Interface/API impact described
- [ ] Failure of external dependencies considered
- [ ] Impact on existing functionality identified (regression risk)

### 7.10 Consistency & Scope *(Dev + QA)*
- [ ] No internal contradictions within the story
- [ ] Story is sized appropriately (not an epic in disguise)
- [ ] Dependencies on other stories/systems are declared

**Rule:** Every dimension must appear in the review output with either findings or an explicit "no issue found," so users can trust that the checklist was fully applied (see US-R1/US-R2 acceptance criteria).

---

## 8. Non-Functional Requirements

### 8.1 Output Usability
- **NFR-1 (Structured output):** All outputs use consistent Markdown structure with headings, tables, and severity tags, per Section 6.2 — stable enough that team members know where to look across stories.
- **NFR-2 (Minimal chat response):** The skill's chat reply contains only file paths and a short summary; full content lives in files.
- **NFR-3 (Scannability):** Exec summary readable in < 2 minutes; findings and suggestions ordered by severity.

### 8.2 Language & Tone
- **NFR-4 (BA-facing tone):** All BA-facing suggestions must be in plain business language, constructive, and non-judgmental. Technical terms, when unavoidable, must include a plain-language explanation.
- **NFR-5 (Dev/QA-facing precision):** Dev and QA specifications may use precise technical terminology appropriate to those roles.
- **NFR-6 (Language):** V1 processes and outputs English. (Multilingual support — open question OQ-7.)

### 8.3 Reliability & Consistency
- **NFR-7 (No fabrication):** The skill must never invent requirements; inferred content must be marked as assumption and listed in Open Questions.
- **NFR-8 (Deterministic failure):** Invalid or insufficient input produces a clear error message, never a fabricated review.
- **NFR-9 (Full checklist applied):** All 10 review dimensions are always evaluated and reported, including "no issue found" results.

### 8.4 Traceability
- **NFR-10:** Every finding quotes story evidence or notes its absence; every transferred item is marked as story-sourced or assumption (US-W6).

### 8.5 Skill Delivery
- **NFR-11:** Delivered as an opencode skill: a SKILL.md with clear trigger description and usage instructions, plus supporting files (e.g., dimension checklist reference, output templates) as needed.
- **NFR-12:** Single-invocation usage: a pod member can run any mode without reading long documentation; SKILL.md itself documents invocation and modes.

---

## 9. Assumptions & Open Questions

### 9.1 Assumptions (please confirm)

- **A-1:** Input stories are in **English**.
- **A-2:** The skill processes **one story per run** in v1 (batch mode US-W7 is Nice to Have).
- **A-3:** The skill runs inside the **opencode** environment as a SKILL.md-based skill; no external services or APIs are required in v1.
- **A-4:** Output files are written to the **current workspace/project directory**, and the user (not the skill) is responsible for attaching them to the ticket/tool.
- **A-5:** Severity scale is **Critical / Major / Minor**, defined as: Critical = blocks correct implementation or testing; Major = likely causes rework or missed test coverage; Minor = clarity/polish improvement.
- **A-6:** The full-pipeline default (when no mode specified) is **Review + Transfer**, per US-W1.
- **A-7:** Review is based **only on the story text plus any user-provided context**; the skill has no access to the actual codebase or system documentation in v1.
- **A-8:** The skill is **domain-generic** (works for any business domain), not customized to a specific product line in v1.
- **A-9:** Given/When/Then (Gherkin-style) is the accepted format for acceptance criteria in QA outputs.

### 9.2 Open Questions (to be resolved before/during implementation)

- **OQ-1:** Should Transfer Mode run on a story with known Critical gaps, or should it refuse and require a re-reviewed story? (Current design: it runs but carries prominent warnings — see US-W2. Confirm.)
- **OQ-2:** Should the default mode be `review+transfer` or `review` only? (Assumed: `review+transfer`. Confirm.)
- **OQ-3:** Full pipeline output: **one consolidated file** (`story-analysis.md`) or **three separate files** (`review-report.md`, `dev-spec.md`, `qa-test-spec.md`)? (Assumed: three separate files for easy role-based distribution; confirm team preference.)
- **OQ-4:** Should the skill ask clarifying questions interactively when input is ambiguous, or always process and output Open Questions? (Assumed: process and output Open Questions, to keep runs short.)
- **OQ-5:** Does the team use a **story template** (e.g., a standard Jira story format) the skill should recognize and parse structurally?
- **OQ-6:** Is there an agreed severity definition in the team today, or should this skill's Critical/Major/Minor scale become the pod standard?
- **OQ-7:** Are non-English stories (e.g., Chinese) in scope now or in a later version?
- **OQ-8:** Should review findings be aggregated into team-level metrics over time (e.g., most common gap types per BA/domain) — potentially a v2 feature?
- **OQ-9:** Should the skill support comparing a story against **related existing stories** in the same run (requires the user to supply them as context)?

---

## 10. Priorities Summary

| ID | Story | Priority |
|----|-------|----------|
| US-R1 | Review story from Developer perspective | Must Have |
| US-R2 | Review story from QA perspective | Must Have |
| US-R3 | BA-facing improvement suggestions | Must Have |
| US-R4 | Consolidated review summary for Pod Lead | Should Have |
| US-T1 | Transfer to Developer-oriented language | Must Have |
| US-T2 | Transfer to QA-oriented language | Must Have |
| US-T3 | Combined transfer run (dev + QA consistency) | Must Have |
| US-W1 | Mode selection via invocation | Must Have |
| US-W2 | Review gates Transfer (full pipeline) | Should Have |
| US-W3 | Flexible story input (file/paste/context) | Must Have |
| US-W4 | Re-review cycle with Gap Closure Check | Should Have |
| US-W5 | File-based, shareable outputs | Must Have |
| US-W6 | Traceability of findings and outputs | Should Have |
| US-W7 | Batch review of multiple stories | Nice to Have |
