---
name: story-transfer-skill
version: "1.0.0"
tags: [user-story, story-review, requirements, dev-spec, qa-spec, acceptance-criteria, agile, walkthrough]
description: |
  Review BA-written user stories from both Developer and QA perspectives, then transfer
  the story into a Developer Specification (implementation guidance) and a QA Test
  Specification (Given/When/Then acceptance criteria + test scenario outline).
  Produces shareable Markdown files: review-report.md, dev-spec.md, qa-test-spec.md.
  Use when asked to: review a user story, analyze or assess a user story, check story
  quality or readiness, transfer or convert a story into dev/QA specs, generate
  acceptance criteria or test scenarios from a story, prepare a story for a walkthrough,
  improve a user story, or check whether previous story gaps were resolved.
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# Story Transfer Skill

Review BA-written user stories from **Developer** and **QA** perspectives, then **transfer** them into role-specific specifications — so gaps are found at story walkthrough time, not during development or testing.

## What This Skill Does

| Mode | What runs | Output files |
|------|-----------|--------------|
| `review` | Dev-perspective review + QA-perspective review + BA suggestions + executive summary | `review-report.md` |
| `transfer` | Story → Developer Specification + QA Test Specification | `dev-spec.md`, `qa-test-spec.md` |
| `review+transfer` (**default**) | Full pipeline: review first, then gated transfer | all three files |

Outputs are written to the **current workspace/project directory** as Markdown files. The chat reply stays minimal (file paths + short summary); full content lives in the files.

## Inputs

| Input | Required | How to recognize it |
|-------|----------|---------------------|
| **User story** | Yes | A file path, or story text pasted directly in the request. Title, narrative ("As a… I want… so that…"), description, acceptance criteria if any. |
| **Mode** | No | `review`, `transfer`, or `review+transfer` stated in the request. If absent → default to `review+transfer` and state the mode used in every output header. |
| **Context** | No | Anything the user supplies about existing system behavior, related stories, constraints, glossary, known functionality. |
| **Previous review report** | No | A prior `review-report.md` (path or pasted). Enables the **Gap Closure Check**. |

v1 constraints: one story per run (see Batch Review below for the optional multi-story extension); English input and output only.

## Non-Negotiable Rules

These rules override everything else. If any instruction conflicts with them, the rules win.

1. **No fabrication (NFR-7).** Never invent requirements, behaviors, or findings. Transferred content must come from the story text or be explicitly marked `[assumption: OQ-n]` and listed in Open Questions & Assumptions. Review findings must quote story evidence or state "absent from story."
2. **Full checklist always reported (NFR-9).** Every review dimension that applies to a perspective appears in the review output with either findings or an explicit "No issue found." Never silently skip a dimension.
3. **Deterministic failure (NFR-8).** Invalid or insufficient input produces a clear input error and NO output files. Never produce a fabricated review of a story that isn't there.
4. **BA-facing tone (NFR-4).** The "Suggestions for the BA" section uses plain business language, is constructive and non-judgmental (never "you failed to…"; prefer "consider adding…"), and explains any unavoidable technical term in parentheses. Dev-spec and QA-spec may use precise technical terminology (NFR-5).
5. **Traceability (NFR-10).** Every finding quotes evidence or notes its absence; every transferred requirement/test is traceable to the story or marked as an assumption.
6. **No silent ambiguity resolution (US-T1).** When the story is ambiguous, the transfer states the working assumption inline, marks it, and lists it under Open Questions & Assumptions.
7. **Context is truth (US-W3).** Before flagging something "missing," check the user-supplied context. Never flag as missing anything the context already answers.
8. **Minimal chat response (NFR-2).** Reply with file paths + 3–5 bullets only. Never dump full document content into chat.

## Workflow

Follow these steps in order. Step 4 runs for `review` and `review+transfer`; Step 5 runs for `transfer` and `review+transfer`; Steps 1–3, 6, and 7 always run.

### Step 1 — Determine mode and collect inputs

- Identify from the request: **mode**, **story** (path or pasted text), **context** (optional), **previous review report** (optional).
- Mode not stated → run `review+transfer` (the default) and label every output header with the mode used.
- No story provided at all → ask the user for the story and stop. Do not proceed, do not invent.

### Step 2 — Validate the story input

Read the story (from the file path, or use the pasted text). A story is **valid** only if it contains **all** of:

1. Readable, non-empty content.
2. A title or a narrative ("As a… I want… so that…").
3. At least **one full sentence of description** beyond the title.

Reject (report a clear input error, stop, produce NO output files) when any of the following holds:

- The file does not exist or cannot be read.
- The content is only a title, or fragments/bullets without one complete sentence.
- The story is not in English (v1 is English-only).
- The content is clearly not a user story (e.g., source code, a log file).

Use this error format in the chat reply:

```
Input error — cannot process this story.

What was received: {description, e.g., "a title only: 'Order Cancellation'"}
Why it was rejected: {e.g., "fewer than one full sentence of description — there is nothing to review"}
What to provide: a user story with at least a title and one full sentence of desired behavior
(e.g., "As a customer, I want to … so that …"), ideally with acceptance criteria.

No output files were produced.
```

If the user re-supplies a valid story in the same conversation, continue the workflow with it.

Note: a valid but unusual story (e.g., an infrastructure task) is still processed; add a one-line note that the skill is optimized for feature/function stories.

### Step 3 — Load reference files

All file paths in this document (e.g., `references/…`, `samples/…`) are relative to this skill's own directory — not the current workspace.

- Before reviewing: read [`references/review-dimensions.md`](references/review-dimensions.md) (the 10-dimension checklist, perspective mapping, how to apply each dimension) and [`references/severity-guide.md`](references/severity-guide.md) (Critical/Major/Minor definitions and the quality-rating rule).
- Before writing outputs: read [`references/output-templates.md`](references/output-templates.md) (exact templates and the shared ID scheme). Follow the templates exactly (NFR-1).

### Step 4 — Review the story

Produce the review content per `references/review-dimensions.md`. Summary of what to build:

**4a. Developer Perspective Review.** Evaluate the 8 dev-side dimensions: Clarity & Unambiguity; Completeness (incl. business rules stated, not implied); Edge Cases & Boundary Conditions; Error Handling & Negative Paths; Non-Functional Requirements; Data Considerations; Integration Considerations; Consistency & Scope. Flag ambiguous statements with the exact quote and why a developer cannot implement it deterministically; flag missing foreseeable edge cases with a concrete example of the missing scenario; flag implied but unspecified data/integration impacts. Every dimension appears with findings or "No issue found."

**4b. QA Perspective Review.** Evaluate the 7 QA-side dimensions: Clarity & Unambiguity; Completeness; Acceptance Criteria Quality; Testability; Edge Cases & Boundary Conditions; Error Handling & Negative Paths; Consistency & Scope. Rules: no acceptance criteria at all → **Critical**; each untestable criterion → flag with the quote and the missing measurable condition; happy-path-only story → list the missing negative and boundary scenarios; existing criteria that ARE testable → confirm them as testable (validate, don't only criticize).

Finding format everywhere: **ID (`D-n`/`Q-n`) · dimension · quoted story evidence (or "absent from story") · issue description · severity (Critical / Major / Minor)** per the severity guide.

**4c. Suggestions for the BA.** Convert EVERY finding into at least one suggestion — no orphan findings without BA-facing guidance. Pattern per suggestion: **what's missing/unclear → why it matters (what could go wrong in dev/test) → what to add or clarify, with a concrete example phrasing**. Plain business language, constructive tone, ordered Critical → Major → Minor.

**4d. Executive Summary.** Quality rating (Ready / Ready with minor gaps / Not ready — rule in the severity guide), issue counts by severity and by perspective, gaps raised by both perspectives, top 3–5 gaps to discuss with the BA. Short enough to read in under 2 minutes.

**4e. Gap Closure Check** (only when a previous review report was provided). For each finding in the previous report, judge against the updated story: **Resolved / Partially resolved / Not addressed**, each backed by a quote from the updated story or an explicit "absent from story" note. Also report any **newly introduced** issues. The review itself is a fresh, full review of the updated story — the closure check is an additional section, not a replacement.

### Step 5 — Transfer the story

Produce both specifications from the SAME story baseline (plus context). Summary of what to build (full structure in `references/output-templates.md`):

**5a. Extract functional requirements.** Discrete, numbered, testable statements (`FR-1`, `FR-2`, …). This ID set is **shared** between `dev-spec.md` and `qa-test-spec.md` — dev and QA reference the same requirement IDs.

**5b. Build the Developer Specification** (`dev-spec.md`): Objective (restated, unambiguous) · Functional Requirements · Edge Cases & Boundary Conditions (expected behavior stated or "needs BA confirmation") · Error Handling Expectations (invalid input, failed operations, unauthorized access) · Data Considerations (entities/fields, validation, volume, migration, privacy) · Integration Considerations (upstream/downstream, API impacts, contract changes) · Open Questions & Assumptions (the COMPLETE list of every inferred item). Ambiguities are never silently resolved — state the working assumption and list it.

**5c. Build the QA Test Specification** (`qa-test-spec.md`): Acceptance Criteria in Given/When/Then form (one per user-visible behavior, concrete preconditions, triggers, measurable outcomes) · Test Scenario Outline table (Scenario | Category Positive/Negative/Boundary | Precondition | Steps/Trigger | Expected Result, each scenario mapped to FR/AC IDs) · Test Data Considerations · Open Questions. Rules: if the story had no acceptance criteria, derive draft GWT criteria each marked **draft — requires BA confirmation**; if the story had criteria, refine each into GWT form — criteria that cannot be made testable are flagged, never dropped.

**5d. Apply review gating (full pipeline only, US-W2).**
- Critical findings exist → both specs carry a prominent **"Story not yet ready — open Critical gaps"** warning at the top listing the Critical findings, and affected requirement/test sections are marked **"based on incomplete story input — pending BA clarification"** rather than omitted. The transfer still runs; it never refuses.
- No Critical findings (only Major/Minor) → transfer proceeds normally; summarize Major/Minor findings in an appendix of each output.
- Transfer-only mode (no review ran) → header states "Not reviewed — run Review mode or the full pipeline for a quality assessment."

**5e. Consistency check (US-T3).** Before writing files, cross-check both specs for contradictory statements about the same behavior. If a contradiction is found, resolve it back to the story text (or mark as assumption) — the two documents must never disagree. The FR set is single-sourced and identical in both files.

### Step 6 — Write output files

- Write to the current workspace directory (or a user-specified output directory).
- File names: `review-report.md` (review), `dev-spec.md` + `qa-test-spec.md` (transfer), all three (full pipeline). One story per run.
- **Overwrite policy (deterministic, US-W5):** overwrite existing output files from a previous run by default. Never overwrite the story input file or context files. A previous review report may be overwritten by the new `review-report.md` only after its findings have been fully incorporated into the Gap Closure Check.
- Every output header includes: story title/ID, date, mode used, story source, output handling (created/overwrote).
- Follow `references/output-templates.md` exactly.

### Step 7 — Reply minimally

Chat reply = file paths + 3–5 bullet summary. Example:

```
Done. Review + Transfer completed for "Order Cancellation" (mode: review+transfer, default).
- review-report.md — Not ready · 2 Critical · 5 Major · 3 Minor (overwrote previous file)
- dev-spec.md — generated with "story not yet ready" warning (2 open Critical gaps)
- qa-test-spec.md — 6 acceptance criteria, all marked draft — requires BA confirmation
Top gap to raise with the BA: no acceptance criteria at all.
```

## Batch Review (optional extension, US-W7)

Only when the user explicitly provides **multiple** stories (a directory of story files or one document containing several stories):

1. If the request is ambiguous (e.g., a directory that may contain one story), confirm with the user that they want a batch review; batches run **review only** by default.
2. Validate each story individually (Step 2); skip invalid ones with a per-story error note — never fabricate.
3. Run Step 4 for each story independently.
4. Save per-story reports as `review-report-{slug}.md` (slug = story ID or short hyphenated title).
5. Additionally save `backlog-readiness.md` with a summary table: | Story | Rating | Critical | Major | Minor | Report file |.
6. Full-pipeline transfer remains one story per run.

## Review Dimensions at a Glance

Full checklists, application guidance, and example findings: [`references/review-dimensions.md`](references/review-dimensions.md).

| # | Dimension | Dev | QA |
|---|-----------|:---:|:--:|
| 7.1 | Clarity & Unambiguity | ✓ | ✓ |
| 7.2 | Completeness (incl. business rules) | ✓ | ✓ |
| 7.3 | Acceptance Criteria Quality | — | ✓ |
| 7.4 | Testability | — | ✓ |
| 7.5 | Edge Cases & Boundary Conditions | ✓ | ✓ |
| 7.6 | Error Handling & Negative Paths | ✓ | ✓ |
| 7.7 | Non-Functional Requirements | ✓ | — |
| 7.8 | Data Considerations | ✓ | — |
| 7.9 | Integration Considerations | ✓ | — |
| 7.10 | Consistency & Scope | ✓ | ✓ |

## What This Skill Does NOT Do

- Does not write the original story from scratch or replace the BA.
- Does not generate code, automated tests, or executable test scripts.
- Does not integrate with Jira/Azure DevOps in v1 (input is pasted text or files).
- Does not make delivery decisions — the pod lead and business team decide.

## File Map

| File | Purpose |
|------|---------|
| [`references/review-dimensions.md`](references/review-dimensions.md) | The 10-dimension checklist, perspective mapping, how to apply each dimension, example findings |
| [`references/severity-guide.md`](references/severity-guide.md) | Critical/Major/Minor definitions, examples, quality-rating rule |
| [`references/output-templates.md`](references/output-templates.md) | Exact templates for all three output files + shared ID scheme |
| [`samples/sample-story-poor.md`](samples/sample-story-poor.md) | Realistically weak BA story for testing |
| [`samples/sample-story-good.md`](samples/sample-story-good.md) | Well-written story for contrast testing |

## Quick Examples

- `Review this story: /path/to/story.md` → review mode, `review-report.md`
- `Transfer the following story (transfer mode): <pasted text>` → `dev-spec.md` + `qa-test-spec.md`
- `Analyze story.md. Context: refunds go through Payment Service. Previous review: review-report.md` → full pipeline (default) with Gap Closure Check
