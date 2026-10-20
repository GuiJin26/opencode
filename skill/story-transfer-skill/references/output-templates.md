# Output Templates

Exact Markdown templates for the skill's three output files. Follow them exactly (NFR-1) — team members must know where to look across stories. Placeholders are `{curly braces}`; blockquoted `>` lines are instructions for the generating AI and must NOT appear in the final files.

## Shared ID Scheme (US-T3, US-W6)

IDs are consistent across all three files. The FR set is **single-sourced and identical** in `dev-spec.md` and `qa-test-spec.md`; OQ IDs are shared between the two specs so dev and QA discuss the same open items.

| ID | Meaning | Lives in |
|----|---------|----------|
| `D-n` | Developer-perspective finding | review-report §2 |
| `Q-n` | QA-perspective finding | review-report §3 |
| `S-n` | BA-facing suggestion | review-report §4 |
| `FR-n` | Functional requirement (discrete, numbered, testable) | dev-spec §2; referenced by qa-test-spec |
| `EC-n` | Edge case / boundary condition | dev-spec §3 |
| `AC-n` | Acceptance criterion (Given/When/Then) | qa-test-spec §1 |
| `TS-n` | Test scenario | qa-test-spec §2 |
| `OQ-n` | Open question or assumption | dev-spec §7; subset echoed in qa-test-spec §4 |

**Markers (traceability, NFR-10):**

| Marker | Meaning | Rule |
|--------|---------|------|
| `[assumption: OQ-n]` | Content inferred, not stated in the story | Must ALSO appear in dev-spec §7 Open Questions & Assumptions |
| `draft — requires BA confirmation` | Acceptance criterion derived without original criteria | Applies to every derived AC when the story had none (US-T2) |
| `based on incomplete story input — pending BA clarification` | Section affected by a Critical finding (full pipeline, US-W2) | Content stays in the doc, marked — never omitted |
| `needs BA confirmation` | Expected behavior that could not be derived from story or context | Add a matching OQ entry |
| `Absent from story` | Issue is an omission, not present text | Used as story evidence for omission findings |

**Source column values** (dev-spec tables): `Story — "quote or section reference"` or `[assumption: OQ-n]`. No third option — everything is either story-sourced or an assumption (no fabrication, NFR-7).

## Quality Rating Rule

Single source of truth: the "Quality Rating Rule" in [`severity-guide.md`](severity-guide.md). Apply that deterministic mapping (finding counts → Ready / Ready with minor gaps / Not ready) to the "Quality rating" line in Template 1 §1.

---

## Template 1: `review-report.md`

```markdown
# Story Review Report

| | |
|---|---|
| **Story** | {story title / ID} |
| **Review date** | {YYYY-MM-DD} |
| **Mode** | {Review | Review + Transfer} |
| **Story source** | {file path or "pasted text"} |
| **Context used** | {none, or brief list of context items the user provided} |
| **Previous report** | {none, or file path — Gap Closure Check included} |
| **Output handling** | {created new file / overwrote previous run's file} |

## 1. Executive Summary

- **Quality rating:** {Ready | Ready with minor gaps | Not ready}
- **Issues found:** {C} Critical · {M} Major · {m} Minor
  - Developer perspective: {C} Critical · {M} Major · {m} Minor
  - QA perspective: {C} Critical · {M} Major · {m} Minor
- **Gaps raised by both perspectives:** {finding IDs + one line each, or "none"}
- **Top gaps to discuss with the BA:**
  1. {one line, plain language — most severe first}
  2. {…}
  3. {…}
  {3–5 items}

{One-sentence readiness note for the walkthrough meeting.}

## 2. Developer Perspective Review

> Report ALL 8 dimensions in this order. Each dimension gets EITHER a findings table
> OR "**No issue found.**" plus one line on why it passes. Never omit a dimension (NFR-9).
> Within a dimension, order findings Critical → Major → Minor.

### 2.1 Clarity & Unambiguity — {Issues found | No issue found}

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-1 | Major | "the system should be fast and user-friendly" | "Fast" and "user-friendly" have no measurable definition — a developer cannot know what to build toward. |

<!-- When clean, instead write:
**No issue found.** Story language is concrete; statements have a single interpretation.
-->

### 2.2 Completeness — {…}
### 2.3 Edge Cases & Boundary Conditions — {…}
### 2.4 Error Handling & Negative Paths — {…}
### 2.5 Non-Functional Requirements — {…}
### 2.6 Data Considerations — {…}
### 2.7 Integration Considerations — {…}
### 2.8 Consistency & Scope — {…}

## 3. QA Perspective Review

> Report ALL 7 dimensions in this order, same per-dimension rules as §2.

### 3.1 Clarity & Unambiguity — {…}
### 3.2 Completeness — {…}

### 3.3 Acceptance Criteria Quality — {…}

> If the story HAS acceptance criteria, include this validation table (confirm testable
> ones — don't only criticize, US-R2). If it has none, the Critical finding covers it.

**Acceptance criteria validation**

| Story AC # | Story criterion (quoted) | Testable? | Notes |
|------|--------------------------|-----------|-------|
| 1 | "…" | Yes | {one line confirming what makes it testable} |
| 2 | "…" | No | {what measurable condition is missing} |

### 3.4 Testability — {…}
### 3.5 Edge Cases & Boundary Conditions — {…}
### 3.6 Error Handling & Negative Paths — {…}
### 3.7 Consistency & Scope — {…}

## 4. Suggestions for the BA

> Plain business language. Constructive, non-judgmental. Ordered Critical → Major → Minor.
> EVERY finding from §2 and §3 must be covered by at least one suggestion — no orphans (US-R3).

### S-1 — {short action title} ({severity})

- **What's missing or unclear:** {plain language; quote the story where helpful}
- **Why it matters:** {what could go wrong in development or testing if left as-is}
- **What to add or clarify:** {concrete guidance}
  - Example phrasing: "{a sentence the BA can adapt directly}"
- *Addresses: {finding IDs, e.g., D-2, Q-1}*

## 5. Gap Closure Check

> Include this section ONLY when a previous review report was provided (US-W4).

### 5.1 Previous findings

| Previous finding | Severity | Status | Evidence in updated story |
|------------------|----------|--------|---------------------------|
| D-1: {short title} | Major | Resolved | "quote from the updated story that resolves it" |
| Q-2: {short title} | Critical | Partially resolved | {what was addressed + what still remains} |
| D-4: {short title} | Minor | Not addressed | Absent from story — no matching change found |

> Status values: **Resolved** / **Partially resolved** / **Not addressed**.
> Every status is backed by a quote from the updated story or an explicit absence note — never asserted without evidence.

### 5.2 Newly introduced issues

{Findings in the updated story that were not present in the previous review, with IDs and
severity — or "None — no new issues introduced by the updates."}
```

---

## Template 2: `dev-spec.md`

```markdown
# Developer Specification

| | |
|---|---|
| **Story** | {story title / ID} |
| **Date** | {YYYY-MM-DD} |
| **Mode** | {Transfer | Review + Transfer} |
| **Story source** | {file path or "pasted text"} |
| **Readiness** | {one of the readiness lines below} |
| **Output handling** | {created new file / overwrote previous run's file} |

> Readiness line — choose exactly one:
> - Full pipeline WITH Critical findings: `Not ready — {N} open Critical gaps (see warning below)`
> - Full pipeline, no Critical: `No Critical gaps; {M} Major and {m} Minor findings — see Appendix`
> - Transfer-only: `Not reviewed — run Review mode or the full pipeline for a quality assessment`

<!-- Critical-gaps warning block — include ONLY for full-pipeline runs with Critical
     findings (US-W2). List every Critical finding. -->

**⚠ STORY NOT YET READY — OPEN CRITICAL GAPS**

This specification was generated from a story with unresolved Critical findings.
Sections built on incomplete input are marked "based on incomplete story input —
pending BA clarification". Do not treat this specification as final until the BA
resolves the gaps below.

- [Critical] {D-2}: {one line}
- [Critical] {Q-1}: {one line}

## 1. Objective

{2–4 sentences: what is being built, for whom, and why — an unambiguous restatement of
the story's intent. If the objective itself is ambiguous, state the working
interpretation and mark it [assumption: OQ-n].}

## 2. Functional Requirements

> Discrete, numbered, testable statements — one requirement per row, no compound
> requirements. This ID set is SHARED with the QA Test Specification (US-T3).

| ID | Requirement | Source |
|----|-------------|--------|
| FR-1 | {single testable statement} | Story — "quote or section reference" |
| FR-2 | {single testable statement} | [assumption: OQ-1] |

## 3. Edge Cases & Boundary Conditions

> Expected behavior is stated where derivable; otherwise "needs BA confirmation" AND a
> matching OQ entry. In full-pipeline runs with Critical findings, rows built on the
> gapped areas carry the marker "based on incomplete story input — pending BA
> clarification" instead of being omitted.

| ID | Scenario | Expected behavior | Source |
|----|----------|-------------------|--------|
| EC-1 | {e.g., customer has no orders} | {observable behavior, or "needs BA confirmation → OQ-2"} | {Story / [assumption: OQ-n]} |
| EC-2 | {e.g., double-click on submit} | {…} | {…} |

## 4. Error Handling Expectations

| Condition | Expected system behavior | Source |
|-----------|--------------------------|--------|
| Invalid input: {case} | {user-visible behavior + data outcome, or "needs BA confirmation → OQ-n"} | {…} |
| Failed operation: {case} | {…} | {…} |
| Unauthorized access: {case} | {…, or "no permission model in story → OQ-n"} | {…} |

## 5. Data Considerations

| Item | Detail | Source |
|------|--------|--------|
| Entities/fields created or changed | {…} | {…} |
| Validation rules | {…} | {…} |
| Volume/growth | {…} | {…} |
| Migration / historical data | {…, or "not applicable"} | {…} |
| Privacy (PII, access control, audit) | {…} | {…} |

## 6. Integration Considerations

| Item | Detail | Source |
|------|--------|--------|
| Upstream systems | {…} | {…} |
| Downstream systems | {…} | {…} |
| API / contract impacts | {…} | {…} |
| External dependency failure behavior | {…, or "needs BA confirmation → OQ-n"} | {…} |
| Regression risk to existing functionality | {…} | {…} |

## 7. Open Questions & Assumptions

> The COMPLETE list of every inferred item (NFR-7, US-T1). Every [assumption: OQ-n]
> marker used anywhere in this document MUST appear here; every "needs BA confirmation"
> row above MUST have an entry here. OQ IDs are shared with the QA Test Specification.

| ID | Type | Item | Impact if unresolved |
|----|------|------|----------------------|
| OQ-1 | Assumption | {the assumption made and where it is used} | {what goes wrong if the assumption is wrong} |
| OQ-2 | Open question | {the question for the BA} | {…} |

## Appendix: Review Findings (Awareness)

> Include ONLY for full-pipeline runs with NO Critical findings (US-W2).
> One-line summary of each Major/Minor finding for awareness.

| Finding | Severity | One-line summary |
|---------|----------|------------------|
| D-3 | Major | {…} |
| Q-2 | Minor | {…} |
```

---

## Template 3: `qa-test-spec.md`

```markdown
# QA Test Specification

| | |
|---|---|
| **Story** | {story title / ID} |
| **Date** | {YYYY-MM-DD} |
| **Mode** | {Transfer | Review + Transfer} |
| **Story source** | {file path or "pasted text"} |
| **Readiness** | {same readiness-line rules as dev-spec.md} |
| **Output handling** | {created new file / overwrote previous run's file} |

<!-- Critical-gaps warning block — same rules as dev-spec.md, listing the same
     Critical findings (identical set, US-T3 shared baseline). -->

**⚠ STORY NOT YET READY — OPEN CRITICAL GAPS**

{same content and wording as the dev-spec warning block}

## 1. Acceptance Criteria (Given/When/Then)

> One criterion per user-visible behavior. Concrete preconditions, triggers, and
> MEASURABLE expected outcomes. Each AC maps to its FR ID(s).
> - Story HAD acceptance criteria → refine each into GWT form. Criteria that cannot be
>   made testable are kept and flagged — never dropped.
> - Story had NO acceptance criteria → derive draft criteria; mark EVERY one
>   "draft — requires BA confirmation" (US-T2).
> - In full-pipeline runs with Critical findings, ACs built on gapped areas carry the
>   marker "based on incomplete story input — pending BA clarification".

**AC-1** (maps to FR-1) — {optional short title}
- **Given** {concrete precondition}
- **When** {trigger / action}
- **Then** {measurable, observable expected outcome}
- Status: {Refined from story criterion #1 | draft — requires BA confirmation}
- Notes: {e.g., what measurable condition the original wording lacked; optional}

**AC-2** (maps to FR-2) — {…}

## 2. Test Scenario Outline

> Cover all three categories: Positive (happy paths), Negative (invalid input, error
> paths, unauthorized access), Boundary (limits, thresholds, empty/zero/max states).
> Every scenario maps to at least one FR/AC. Each row includes: scenario name,
> category, precondition, steps/trigger, expected result.

| ID | Scenario | Category | Precondition | Steps/Trigger | Expected Result | Maps to |
|----|----------|----------|--------------|---------------|-----------------|---------|
| TS-1 | {name} | Positive | {…} | {…} | {…} | FR-1, AC-1 |
| TS-2 | {name} | Negative | {…} | {…} | {…} | FR-2 |
| TS-3 | {name} | Boundary | {…} | {…} | {…} | FR-1, AC-3 |

## 3. Test Data Considerations

- {Notable data setups the scenarios require, e.g., "an account with an order in each
  status: Placed, Processing, Shipped, Delivered"}
- {Mark inferred setups with [assumption: OQ-n] where not stated in the story.}

## 4. Open Questions

> Behaviors that cannot be turned into a deterministic expected outcome without BA
> input. Use the SAME OQ IDs as the Developer Specification — one shared list, this
> section shows the test-blocking subset.

| ID | Question | Blocks scenario(s) |
|----|----------|--------------------|
| OQ-2 | {…} | TS-4, TS-7 |

## Appendix: Review Findings (Awareness)

> Same rules as dev-spec.md appendix: full-pipeline runs with NO Critical findings only.

| Finding | Severity | One-line summary |
|---------|----------|------------------|
| {…} | {…} | {…} |
```

---

## Consistency Requirements Before Writing Files (US-T3)

1. The FR set in `dev-spec.md` §2 and referenced in `qa-test-spec.md` is identical — same IDs, same meanings.
2. The Critical-gaps warning block lists the SAME Critical findings in both specs.
3. No statement in one spec may contradict the other spec's description of the same behavior. Cross-check before writing; if a contradiction is found, resolve it against the story text (or mark as assumption).
4. OQ IDs are shared: an item appearing in both specs keeps one ID.
5. Every output header labels the mode that produced it (US-W1), and every header states whether the file was created or overwritten (US-W5).
