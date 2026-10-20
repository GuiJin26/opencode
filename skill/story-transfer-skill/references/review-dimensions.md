# Review Dimensions Checklist

The complete checklist Review Mode evaluates every story against. **Every dimension that applies to a perspective MUST appear in the review output** with either findings or an explicit "No issue found" (NFR-9) — never silently skipped.

Example IDs in the tables below are illustrative only; a real report assigns unique sequential IDs per perspective (D-1…, Q-1…).

## How to Apply This Checklist

1. **Context first.** Before flagging anything "missing," check the user-supplied context (existing system behavior, related stories, constraints, glossary). Never flag as missing anything the context already answers.
2. **Evidence always.** Every finding quotes the exact story text (use `"` quotes) or explicitly states **"absent from story."** No evidence → no finding.
3. **Severity per the guide.** Classify each finding Critical / Major / Minor using [`severity-guide.md`](severity-guide.md).
4. **No fabrication.** Only report issues you can point to in the story (or the absence of something a reasonable developer/QA would need). Do not invent problems.
5. **Validate, don't only criticize.** If a dimension passes, say so in one line — this tells the reader the checklist was actually applied.

## Perspective Mapping

| # | Dimension | Dev perspective (US-R1) | QA perspective (US-R2) |
|---|-----------|:---:|:---:|
| 7.1 | Clarity & Unambiguity | ✓ | ✓ |
| 7.2 | Completeness | ✓ | ✓ |
| 7.3 | Acceptance Criteria Quality | — | ✓ |
| 7.4 | Testability | — | ✓ |
| 7.5 | Edge Cases & Boundary Conditions | ✓ | ✓ |
| 7.6 | Error Handling & Negative Paths | ✓ | ✓ |
| 7.7 | Non-Functional Requirements | ✓ | — |
| 7.8 | Data Considerations | ✓ | — |
| 7.9 | Integration Considerations | ✓ | — |
| 7.10 | Consistency & Scope | ✓ | ✓ |

- **Developer Perspective Review** reports dimensions 7.1, 7.2, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10 (8 dimensions). Note: US-R1's "Business Rules" check is dimension 7.2's item "Business rules are stated, not implied."
- **QA Perspective Review** reports dimensions 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.10 (7 dimensions). Note: US-R2's "Test data needs" is addressed in the transfer output (qa-test-spec §3 Test Data Considerations) and via 7.4's "imagine writing a test case" check.
- A shared dimension (✓/✓) is judged from that perspective's concerns; the same underlying gap may legitimately appear in both perspectives (the Executive Summary highlights these as shared gaps).

Finding format: **ID (`D-n` dev / `Q-n` QA) · dimension · quoted evidence or "absent from story" · issue description · severity.**

---

## 7.1 Clarity & Unambiguity *(Dev + QA)*

**Checklist**
- [ ] No vague/subjective terms ("fast," "user-friendly," "appropriately," "etc.")
- [ ] No ambiguous pronouns or undefined terms
- [ ] Every statement has one interpretation only

**How to apply.** Scan every sentence and ask: *could two reasonable people implement or test this differently?* Hunt specifically for:

- Vague/subjective qualifiers: *fast, quickly, smoothly, user-friendly, intuitive, appropriately, properly, correctly, efficiently, some, several, as needed, etc., and so on, user-friendly manner, modern, flexible.*
- Ambiguous pronouns: *it, this, that, they* with no clear antecedent ("It should then update the record" — update what, with what?).
- Undefined domain terms a new pod member wouldn't know; undefined acronyms.
- Quantities without values ("a lot of records," "a short time").
- Lists that end with "etc." — the reader cannot know the full set.

**Example findings**
| ID | Severity | Evidence | Issue |
|----|----------|----------|-------|
| D-1 | Major | "The system should be fast and user-friendly" | "Fast" and "user-friendly" have no measurable definition; a developer cannot know what to build toward and QA cannot verify it. |
| Q-2 | Major | "handle invalid input appropriately" | "Appropriately" is undefined — which inputs, rejected how, with what message? Not testable. |
| D-3 | Minor | "…reports, analytics, etc." | "Etc." leaves the scope of the list unknown. |

**No-issue bar.** Statements use concrete values, named roles, and defined terms; each sentence has exactly one interpretation.

## 7.2 Completeness *(Dev + QA)*

**Checklist**
- [ ] User roles/personas involved are identified
- [ ] The full workflow is described (entry point → outcome), not just a fragment
- [ ] Business rules are stated, not implied
- [ ] In-scope vs. out-of-scope is clear

**How to apply.** Reconstruct the workflow mentally from entry point to final outcome (including what the user sees and what happens to their data at the end). Then check:

- Who are the actors? ("The user" is rarely enough — customer, admin, guest, back-office staff?)
- Does the description cover the whole flow, or jump from trigger to a mid-point? (e.g., "the order gets cancelled and the money comes back" — by what mechanism, in what timeframe, with what confirmation?)
- Are the rules that decide behavior stated? (eligibility conditions, limits, priorities, sequencing). Implied rules force developers to guess — this is the single most common cause of mis-implementation.
- Is it clear what is NOT included? Absence of an out-of-scope statement usually means the boundary is unknown.

**Example findings**
| ID | Severity | Evidence | Issue |
|----|----------|----------|-------|
| D-2 | Critical | "The customer … cancels the order" — absent from story | Eligibility rules are never stated (which order statuses can be cancelled?). A developer cannot build the core behavior without guessing; different guesses = different products. |
| D-4 | Major | "The user" | The role is not identified — customer? admin? any signed-in user? Permission behavior cannot be designed or tested. |
| Q-1 | Major | Workflow ends at "the order gets cancelled" | The end-state is a fragment: no confirmation screen, no notification, no final user-visible outcome — QA cannot verify the flow completed. |

**No-issue bar.** Roles named, full workflow described start-to-finish, rules explicit, scope boundaries stated.

## 7.3 Acceptance Criteria Quality *(QA)*

**Checklist**
- [ ] Acceptance criteria exist
- [ ] Each is specific, measurable, and independently verifiable
- [ ] Criteria cover the main flows, not only the happy path
- [ ] Expected results are observable and defined

**How to apply.**

- **No acceptance criteria at all → automatically Critical** (US-R2): QA has no verified definition of done, and the story's outcome is unverifiable.
- For each existing criterion, ask: *what exact input/trigger, what exact observable output, pass/fail decided how?* If you cannot write a pass/fail judgment from the criterion alone, it is untestable — flag with the quote and state **what measurable condition is missing**.
- Check coverage: criteria that only describe the happy path leave negative and boundary flows unverifiable (cross-check 7.5/7.6).
- If criteria exist and are testable — **confirm each as testable** in an "Acceptance criteria validation" table (validate, don't only criticize).

**Example findings**
| ID | Severity | Evidence | Issue |
|----|----------|----------|-------|
| Q-1 | Critical | Absent from story | No acceptance criteria at all. QA cannot derive test cases; the definition of done is undefined. |
| Q-3 | Major | "The page loads quickly" | Untestable: "quickly" has no measurable condition (missing: e.g., "within 2 seconds on a standard connection"). |
| Q-4 | Major | Single criterion: "Customer can cancel the order" | Criteria cover only the happy path; no criterion for ineligible orders, failures, or confirmation of outcome. |

**No-issue bar.** Criteria exist, each is specific/measurable/independently verifiable, main flows (not just happy path) are covered, expected results are observable.

## 7.4 Testability *(QA)*

**Checklist**
- [ ] Every statement of behavior can be verified by a test
- [ ] Expected outcomes are defined for each trigger/action
- [ ] Success/failure states are distinguishable

**How to apply.** Walk each behavioral statement and imagine writing a test case for it. A statement is testable only if you can name: the precondition, the trigger, and the observable expected result. Flag when:

- The expected outcome is not defined for a described action ("the customer submits the form" — then what?).
- Success and failure are indistinguishable (no defined difference in what the user sees or what data changes).
- Behavior depends on undefined conditions (time, order of events, system state).

Distinct from 7.3: 7.3 judges the *acceptance criteria as written*; 7.4 judges *all behavioral statements* in the story.

**Example findings**
| ID | Severity | Evidence | Issue |
|----|----------|----------|-------|
| Q-2 | Major | "validation works correctly" | Cannot be verified: no definition of valid vs. invalid input, or of the observable behavior on each. |
| Q-5 | Major | "the money comes back to them" | Expected outcome undefined: how much, to where, within what time, and how does the customer observe it? Success state cannot be confirmed in a test. |

**No-issue bar.** Every behavior has a defined trigger and observable outcome; success/failure are distinguishable for each.

## 7.5 Edge Cases & Boundary Conditions *(Dev + QA)*

**Checklist**
- [ ] Boundary values considered (min/max, empty, zero, first/last)
- [ ] Concurrent/duplicate submissions considered (where applicable)
- [ ] Unusual but valid usage considered
- [ ] Empty states / no-data states considered

**How to apply.** Systematically probe the story's data and actions:

- **Boundaries:** min/max values, empty list, zero quantity, first/last record, longest allowed input.
- **Concurrency:** double-click, re-submit, two users acting on the same record. If the flow involves money, inventory, or state changes, an unaddressed duplicate-submission case is Critical.
- **Empty/no-data:** what does the user see with nothing to show?
- **Unusual-but-valid:** expired session mid-flow, acting on an item that changed state a moment ago.

Flag each omission with a **concrete example of the missing scenario** (US-R1) — name the exact situation and why it is foreseeable for this feature.

**Example findings**
| ID | Severity | Evidence | Issue |
|----|----------|----------|-------|
| D-5 | Critical | "clicks Cancel on the order" — absent from story | Duplicate submission not considered: double-clicking "Cancel" could trigger two refunds. Missing scenario: "customer clicks Cancel twice within seconds — exactly one cancellation/refund must occur." |
| Q-3 | Major | Absent from story | Empty state not considered: what does "My Orders" show when the customer has no orders? |
| D-6 | Major | Absent from story | State-change race not considered: order ships while the cancellation dialog is open — which action wins? |

**No-issue bar.** Foreseeable boundaries, concurrency, empty states, and unusual-but-valid usage are each either addressed or explicitly out of scope.

## 7.6 Error Handling & Negative Paths *(Dev + QA)*

**Checklist**
- [ ] Invalid input behavior defined
- [ ] Failure behavior defined (what does the user see? what happens to their data?)
- [ ] Unauthorized/permission-denied behavior defined (where applicable)

**How to apply.** For each user input and each external action in the story, ask what happens when it goes wrong:

- **Invalid input:** wrong format, out-of-range, missing mandatory fields — rejected how, with what message, what is preserved?
- **Failed operations:** downstream system down, timeout, partial failure — what does the user see, and is their data left consistent? (e.g., refund fails after cancellation succeeded)
- **Unauthorized access:** user who lacks permission, or acting on another user's data — blocked how, with what response?

If the story has no permission model at all, note that unauthorized behavior is undefined and raise it as an open question rather than assuming none is needed.

**Example findings**
| ID | Severity | Evidence | Issue |
|----|----------|----------|-------|
| D-7 | Major | Absent from story | Invalid input behavior undefined: nothing about what happens if cancellation is attempted on an ineligible order (already shipped) — block? message? refund anyway? |
| D-8 | Major | Absent from story | Failure behavior undefined: if the refund step fails, is the order still cancelled? What does the user see, and is their money/account left in a consistent state? |
| Q-4 | Major | Absent from story | Unauthorized path untestable: no defined behavior for a signed-in customer attempting to cancel another customer's order. |

**No-issue bar.** Each input failure, operation failure, and permission denial has a defined user-visible behavior and data outcome (or is explicitly not applicable).

## 7.7 Non-Functional Requirements *(Dev)*

**Checklist**
- [ ] Performance expectations stated where relevant (response time, volume)
- [ ] Security/privacy considerations stated where relevant (PII, access control, audit)
- [ ] Compatibility constraints stated where relevant (browsers, devices, versions)
- [ ] Accessibility requirements stated where relevant

**How to apply.** Judge relevance to THIS story — do not demand NFRs for a trivial internal change (that would be noise, and Minor at most). Consider:

- **Performance:** is there volume in play (bulk data, many concurrent users, large files)? If yes and unstated → Major; if not relevant → no issue.
- **Security/privacy:** does the story touch personal data, money, or permissions? If yes and unstated → Major.
- **Compatibility:** is there a UI or a documented device/browser variance? Unstated → Minor (Major for UI-heavy stories).
- **Accessibility:** relevant for user-facing UIs; unstated → Minor unless the audience includes accessibility needs.

**Example findings**
| ID | Severity | Evidence | Issue |
|----|----------|----------|-------|
| D-9 | Major | "the money comes back to them" | Financial data with no security/privacy consideration: refund flows typically involve payment credentials/PII; no statement of access control or audit logging. |
| D-10 | Minor | Absent from story | No performance expectation for the cancellation action (acceptable if low-volume, but a one-line target avoids disputes). |

**No-issue bar.** NFRs relevant to this story are stated; irrelevant NFR categories are consciously not needed.

## 7.8 Data Considerations *(Dev)*

**Checklist**
- [ ] Data created/read/updated/deleted is identified
- [ ] Data validation rules stated
- [ ] Data volume/growth considered
- [ ] Data migration or historical data handling addressed (where applicable)

**How to apply.** List every data item the story touches and check each is described:

- Which entities/fields are created or changed (e.g., an order status field — new value "Cancelled"? a cancellation record with timestamp, actor, reason?).
- Validation rules for each user-provided field (required? format? length? range?).
- Volume/growth: how many records/day, retention, archiving — infer reasonableness, mark assumptions.
- Migration/historical: do existing records need the new behavior, or only new ones? (Only "where applicable" — flag as Minor when clearly not applicable.)

**Example findings**
| ID | Severity | Evidence | Issue |
|----|----------|----------|-------|
| D-11 | Major | "the cancellation should be tracked somewhere" | Data model undefined: "somewhere" gives no entity, fields, or retention. A cancellation record (who, when, what was refunded) cannot be designed. |
| D-12 | Major | Absent from story | Validation rules unstated: no field-level rules for any input in the flow. |
| D-13 | Minor | Absent from story | Historical data handling unaddressed: should orders placed before release also be cancellable? |

**No-issue bar.** Entities/fields, validation rules, volume behavior, and (where applicable) migration/historical handling are identified.

## 7.9 Integration Considerations *(Dev)*

**Checklist**
- [ ] Upstream/downstream systems identified
- [ ] Interface/API impact described
- [ ] Failure of external dependencies considered
- [ ] Impact on existing functionality identified (regression risk)

**How to apply.** Trace the story's flow across system boundaries. Any mention of money, email/SMS, third-party features, existing shared data, or "the system will notify/send/sync" implies an integration:

- Name the implied upstream/downstream systems; flag if unnamed.
- If a core outcome depends on an external system and that system is unspecified or its failure behavior undefined, that is at least Major — Critical when the main flow cannot work without it.
- Regression: does the change alter anything existing users see or depend on?

**Example findings**
| ID | Severity | Evidence | Issue |
|----|----------|----------|-------|
| D-14 | Critical | "the money comes back to them" | A refund requires a payment-system integration that the story never identifies. The core outcome cannot be implemented without knowing which system, which interface, and what happens if it is slow or down. |
| D-15 | Major | Absent from story | Regression risk not assessed: cancelling returns items to stock — does this touch existing inventory logic other features depend on? |

**No-issue bar.** Upstream/downstream systems, interfaces, dependency-failure behavior, and regression impact are identified — or consciously not applicable.

## 7.10 Consistency & Scope *(Dev + QA)*

**Checklist**
- [ ] No internal contradictions within the story
- [ ] Story is sized appropriately (not an epic in disguise)
- [ ] Dependencies on other stories/systems are declared

**How to apply.**

- **Contradictions:** read the story twice looking for statements that cannot both be true (two different timeframes, two different eligibility rules, a rule in the notes that conflicts with the description). A contradiction on a core behavior is Critical — it blocks deterministic implementation AND testing.
- **Sizing:** could this be built and tested in one iteration? Multiple unrelated behaviors bundled together (e.g., "cancellation + new reporting page + admin configuration") is an epic in disguise → Major.
- **Dependencies:** does the story reference or rely on other stories, teams, or system changes? Undeclared → Major.

**Example findings**
| ID | Severity | Evidence | Issue |
|----|----------|----------|-------|
| D-16 | Critical | "refund within 3 days" (notes) vs. "refunds are processed within 7 days" (description) | Internal contradiction: both statements cannot be true — a developer cannot implement and QA cannot verify the refund timeframe until the story states which one applies. |
| D-17 | Major | "For reporting, the cancellation should be tracked somewhere" | Scope creep: reporting is a separate concern bundled in with no detail — split into its own story or declare the dependency. |
| Q-6 | Major | Absent from story | Dependencies undeclared: refund processing appears to depend on payment-system behavior outside this story. |

**No-issue bar.** No contradictions, right-sized, dependencies declared (or consciously none).

---

## Reporting Reminders

- Every dimension above appears in the report for each applicable perspective — with a findings table OR "**No issue found.**" plus one line on why it passes.
- Findings are ordered within each dimension by severity (Critical → Major → Minor).
- Quote evidence exactly; use "Absent from story" when the issue is an omission.
- Severity definitions, decision heuristics, and the quality-rating rule: [`severity-guide.md`](severity-guide.md).
- Output structure and finding tables: [`output-templates.md`](output-templates.md).
