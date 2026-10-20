# Severity Guide

Severity definitions, decision heuristics, and the story quality-rating rule used by the Story Transfer Skill.

## Definitions (A-5)

| Severity | Definition | Meaning |
|----------|------------|---------|
| **Critical** | Blocks correct implementation or testing | A developer cannot build the behavior (or two developers would build different things), or QA cannot verify the outcome, without guessing. Must be resolved before/at walkthrough. |
| **Major** | Likely causes rework or missed test coverage | Implementable in some form, but a foreseeable gap will probably surface later as rework, a defect, or an untested path. |
| **Minor** | Clarity/polish improvement | No functional risk by itself; fixing it improves readability and reduces clarification time. |

## Decision Heuristics

Ask in order — first match wins:

1. **Is the main flow ambiguous or undefined?** (core business rule missing, internal contradiction, no acceptance criteria at all, main outcome depends on an unspecified integration) → **Critical**.
2. **Would missing this cause money loss, data corruption, security exposure, or duplicate side effects?** (e.g., double refund, inconsistent state after failure) → **Critical**.
3. **Would a reasonable developer or QA have to guess, and the guess is plausibly wrong?** (vague term in a real rule, undefined error path, missing boundary) → **Major**.
4. **Is it purely wording, terminology, or a nice-to-have clarification with one obvious safe interpretation?** → **Minor**.

Additional rules:

- **Judge each perspective independently.** The same underlying gap may be Critical for QA (e.g., no acceptance criteria → cannot test) and only Major for Dev (implementation is inferable). Report both, with each perspective's own severity; the Executive Summary lists them as shared gaps.
- **Relevance matters.** Do not inflate severity for dimensions irrelevant to the story (e.g., demanding performance targets on a low-volume internal screen). If irrelevant, either "No issue found" or Minor.
- **When genuinely torn between two levels**, pick the higher one and say why in the issue description.

## Examples by Severity

### Critical

- No acceptance criteria at all (QA — cannot verify the story is done).
- Core eligibility/business rule missing: "cancel the order" with no statement of which orders can be cancelled (devs build different products).
- Internal contradiction: one section says refund in 3 days, another says 7 — both cannot be true.
- Main outcome depends on an unspecified external system: refund with no payment-system identified.
- Duplicate-submission risk with financial side effects unaddressed (double-click → double refund).
- Primary failure path undefined AND failure leaves user data inconsistent (cancel succeeds, refund fails — story silent).

### Major

- Vague term inside a real rule: "the system should be fast," "handle invalid input appropriately."
- Roles not identified ("the user") — permission behavior cannot be designed/tested.
- Untestable acceptance criterion: "the page loads quickly."
- Happy-path-only coverage: no negative/boundary scenarios.
- Error behavior undefined for invalid input or unauthorized access.
- Data model implied but unspecified ("tracked somewhere").
- Validation rules unstated for user-provided fields.
- External dependency failure behavior undefined.
- Undeclared regression risk on shared existing functionality.
- Story is an epic in disguise (multiple unrelated behaviors bundled).
- Dependencies on other stories undeclared.

### Minor

- "Etc." at the end of a list — scope of the list unclear.
- Inconsistent terminology for the same concept across the story.
- Accessibility/compatibility not mentioned on a low-risk internal UI.
- Historical-data question with an obvious safe default (only new records get the behavior).
- Grammar/typo-level issues that cause momentary confusion but have one natural reading.

## Quality Rating Rule (Executive Summary, US-R4)

Deterministic mapping from finding counts to the overall rating:

| Condition | Rating |
|-----------|--------|
| ≥ 1 Critical finding (either perspective) | **Not ready** |
| 0 Critical and ≥ 1 Major finding | **Ready with minor gaps** |
| 0 Critical and 0 Major (Minor findings allowed, or fully clean) | **Ready** |

Always state the rating together with the counts behind it (e.g., "Not ready — 2 Critical · 5 Major · 3 Minor").

## Tone Rule for BA-Facing Content (NFR-4)

Severity applies to the **story text, never the author**. When findings reach the BA ("Suggestions for the BA" section):

- Plain business language; explain unavoidable technical terms in parentheses — e.g., "race condition (two actions happening at the same moment, where the order of processing matters)".
- Constructive, non-judgmental phrasing: "consider adding…" / "it would help to clarify…" — never "you failed to…" / "this is wrong."
- Every suggestion ends with something actionable: what to add, ideally with an example phrasing the BA can adapt.
