# Story Review Report

| | |
|---|---|
| **Story** | Order Cancellation (ORD-482) |
| **Review date** | 2026-10-18 |
| **Mode** | Review + Transfer |
| **Story source** | samples/sample-story-poor.md |
| **Context used** | none |
| **Previous report** | none |
| **Output handling** | created new file |

## 1. Executive Summary

- **Quality rating:** Not ready
- **Issues found:** 8 Critical · 25 Major · 5 Minor
  - Developer perspective: 4 Critical · 14 Major · 3 Minor
  - QA perspective: 4 Critical · 11 Major · 2 Minor
- **Gaps raised by both perspectives:**
  - D-5 / Q-4 — which orders may be cancelled is never stated (eligibility rules absent)
  - D-7 / Q-10 — double-clicking Cancel could trigger two refunds; no duplicate-submission rule
  - D-11 / Q-14 — behavior when the refund fails after cancellation is undefined
  - D-20 / Q-16 — "tracked somewhere" bundles an unspecified reporting concern into the story
  - D-21 / Q-17 — dependencies on systems outside the story (payment, reporting) are undeclared
- **Top gaps to discuss with the BA:**
  1. The story has no acceptance criteria at all — there is no agreed definition of "done" for developers or testers (Q-6).
  2. It never says which orders can be cancelled — e.g., may a delivered order be "cancelled"? Different answers produce different products (D-5 / Q-4).
  3. The money paths are unprotected: double-clicking Cancel, or a refund that fails halfway, have no defined behavior — both can cost the customer money (D-7 / D-11 / Q-10 / Q-14).
  4. "The money comes back to them" relies on a payment system the story never names (D-18).
  5. The customer's journey is a fragment — no confirmation step, no visible end result, no refund timeframe (D-6 / Q-5).

The story describes a valuable feature but is not yet ready for walkthrough sign-off; the transfer artifacts generated alongside this report carry a "story not yet ready" warning until the Critical gaps are resolved with the BA.

## 2. Developer Perspective Review

### 2.1 Clarity & Unambiguity — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-1 | Major | "The system should be fast and user-friendly" | "Fast" and "user-friendly" have no measurable definition — a developer cannot know what to build toward, and the statement cannot be verified. |
| D-2 | Major | "It should also handle invalid input appropriately." | "Appropriately" is undefined — which inputs count as invalid, rejected how, with what message? The required behavior cannot be implemented deterministically. |
| D-3 | Major | "Please make sure validation works correctly." | "Correctly" is undefined — no statement of which fields are validated against what rules, or what happens on failure. |
| D-4 | Minor | "The cancellation should work smoothly for all orders." | "Smoothly" is subjective with no observable definition; it has one natural reading (the flow completes without errors) but states nothing checkable. |

### 2.2 Completeness — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-5 | Critical | "clicks Cancel on the order" — eligibility rules absent from story | Which orders may be cancelled is never stated (order status? time window? payment state?). A developer cannot build the core behavior without guessing — different guesses are different products. |
| D-6 | Major | "The order gets cancelled and the money comes back to them." | The workflow is a fragment: no confirmation step before the cancellation takes effect, no defined end state (confirmation page? notification?), and no timeframe for the money to return. |

### 2.3 Edge Cases & Boundary Conditions — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-7 | Critical | "clicks Cancel on the order" — duplicate submission absent from story | Double-clicking Cancel (or re-submitting) could trigger two cancellations and two refunds. Missing scenario: "customer clicks Cancel twice within seconds — exactly one cancellation and one refund must occur." |
| D-8 | Major | Absent from story | State-change race not considered: the order ships (or otherwise changes state) while the cancellation is being processed — which action wins, and what does the customer see? |
| D-9 | Major | Absent from story | Repeat cancellation not considered: clicking Cancel on an order that is already cancelled — is the action still offered, rejected with a message, or does it trigger another refund? |

### 2.4 Error Handling & Negative Paths — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-10 | Major | Absent from story | Behavior for cancelling an ineligible order (e.g., already shipped) is undefined — blocked? error message? refunded anyway? |
| D-11 | Critical | Absent from story | Failure behavior on the money path is undefined: if the refund step fails after the order is cancelled, the customer has a cancelled order and no money back — the story is silent on what the customer sees and how they recover. |
| D-12 | Major | Absent from story | Unauthorized access is undefined: may a signed-in customer cancel another customer's order (e.g., via a crafted request)? Blocked how, with what response? |

### 2.5 Non-Functional Requirements — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-13 | Major | "the money comes back to them" | The flow moves money and touches payment data, yet the story states no security/privacy consideration (who may perform or see cancellations; whether an audit trail is required). |
| D-14 | Minor | Absent from story | No accessibility or compatibility requirements for the user-facing cancellation flow; likely low-risk, but a one-line statement avoids later disputes. |

### 2.6 Data Considerations — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-15 | Major | "the cancellation should be tracked somewhere" | "Somewhere" defines no entity and no fields: a cancellation record (who, when, which order, how much refunded) cannot be designed, and retention is unstated. |
| D-16 | Major | Absent from story | Validation rules are unstated: the flow's inputs (if any — e.g., a cancellation reason) have no required/format/length rules. |
| D-17 | Minor | Absent from story | Historical data handling is unaddressed: should orders placed before this feature ships also be cancellable? (Obvious safe default — only eligible-status orders — but worth confirming.) |

### 2.7 Integration Considerations — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-18 | Critical | "the money comes back to them" | A refund requires a payment-system integration the story never identifies. The core outcome cannot be implemented without knowing which system, which interface, and what happens if it is slow or unavailable. |
| D-19 | Major | Absent from story | Regression risk is not assessed: does cancelling touch existing order, stock, or reporting logic that other features depend on? |

### 2.8 Consistency & Scope — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-20 | Major | "For reporting, the cancellation should be tracked somewhere" | Scope creep: reporting is a separate concern bundled in with no detail — split it into its own story or declare the dependency. |
| D-21 | Major | Absent from story | Dependencies are undeclared: the refund outcome and the reporting need both rely on systems/capabilities outside this story (payment system, reporting platform). |

No internal contradictions were found — the Critical findings above are omissions, not conflicting statements.

## 3. QA Perspective Review

### 3.1 Clarity & Unambiguity — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| Q-1 | Major | "The system should be fast and user-friendly" | No measurable definition — QA cannot know what to measure or what pass/fail means. |
| Q-2 | Major | "It should also handle invalid input appropriately." | "Appropriately" is undefined — which inputs, rejected how, with what observable result? Not testable. |
| Q-3 | Minor | "The cancellation should work smoothly for all orders." | "Smoothly" is subjective; no observable outcome is defined against which to verify. |

### 3.2 Completeness — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| Q-4 | Critical | "clicks Cancel on the order" — eligibility absent from story | Without eligibility rules, tests cannot be designed for the core behavior — the story reads as if every order is cancellable ("all orders" in Notes), which is almost certainly not intended (e.g., delivered orders). |
| Q-5 | Major | "The order gets cancelled and the money comes back to them." | The end state is a fragment: no confirmation screen, notification, or refund timeframe is defined — QA cannot verify the flow completed or that the customer observed success. |

### 3.3 Acceptance Criteria Quality — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| Q-6 | Critical | Absent from story | No acceptance criteria at all. QA has no verified definition of done; test cases cannot be derived from the story. (No validation table is included — there are no criteria to validate.) |

### 3.4 Testability — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| Q-7 | Major | "Please make sure validation works correctly." | Cannot be verified: no definition of valid vs. invalid input, or of the observable behavior for each. |
| Q-8 | Major | "the money comes back to them" | Expected outcome undefined: how much, to where, within what time, and how does the customer observe it? Success cannot be confirmed in a test. |
| Q-9 | Major | "the cancellation should be tracked somewhere" | "Somewhere" names no observable output — there is nothing concrete to check for the reporting requirement. |

### 3.5 Edge Cases & Boundary Conditions — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| Q-10 | Critical | Absent from story | Duplicate submission unaddressed: double-clicking Cancel could create two refunds — no scenario or expected result exists ("click twice within seconds → exactly one refund"). |
| Q-11 | Major | Absent from story | Mid-flow state change unaddressed: the order ships while the customer is cancelling — no expected result is defined, so no test can assert which action wins. |
| Q-12 | Minor | Absent from story | Empty state not mentioned (what a customer with no orders sees in My Orders). Likely existing behavior; stating "unchanged" would close it. |

### 3.6 Error Handling & Negative Paths — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| Q-13 | Major | Absent from story | Negative path untestable: cancelling an ineligible order (already shipped) has no defined expected result — blocked? message? refunded anyway? |
| Q-14 | Critical | Absent from story | Refund-failure path untestable: if the refund fails after cancellation, the expected result (customer message, money state, recovery) is undefined — a test cannot assert the outcome. |
| Q-15 | Major | Absent from story | Unauthorized path untestable: a signed-in customer attempting to cancel another customer's order has no defined expected response. |

### 3.7 Consistency & Scope — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| Q-16 | Major | "For reporting, the cancellation should be tracked somewhere" | Test scope unclear: reporting is bundled in without detail — must QA verify reporting, and against what? Split it out or declare the dependency. |
| Q-17 | Major | Absent from story | Dependencies undeclared: refund processing and reporting depend on systems outside this story — dependent testing cannot be planned. |

No internal contradictions were found.

## 4. Suggestions for the BA

### S-1 — Spell out which orders can be cancelled (Critical)

- **What's missing or unclear:** The story says the customer "clicks Cancel on the order" but never says which orders are allowed to be cancelled — the note "should work smoothly for all orders" suggests every order, but it is hard to believe a delivered order can be "cancelled."
- **Why it matters:** Developers will each assume a different rule (only before shipping? before payment? everything?), and each assumption produces a different product. Testers cannot prepare test orders because they don't know which ones should work.
- **What to add or clarify:** A short list of the order situations where Cancel is available and where it is not.
  - Example phrasing: "Orders can be cancelled while their status is 'Placed' or 'Processing'. Orders already shipped or delivered cannot be cancelled."
- *Addresses: D-5, Q-4*

### S-2 — Add acceptance criteria (Critical)

- **What's missing or unclear:** The story has no acceptance criteria — the short, checkable list of results that everyone agrees means "done."
- **Why it matters:** Without them, developers and testers each decide for themselves what "correct" looks like, and the walkthrough meeting will spend its time debating basics instead of details.
- **What to add or clarify:** Three to six bullet points, each describing a situation, an action, and the visible result.
  - Example phrasing: "Given an order in status 'Processing', when the customer cancels it, then the order shows as 'Cancelled' in My Orders and the refund is started."
- *Addresses: Q-6*

### S-3 — Protect the money paths: double-clicks and failed refunds (Critical)

- **What's missing or unclear:** Nothing says what should happen if the customer clicks Cancel twice quickly, or if the refund cannot be completed after the order is cancelled.
- **Why it matters:** Both situations can cost the customer money — a double click could refund twice (then someone must recover the extra money), and a failed refund leaves the customer with a cancelled order and no money returned, with no way to know what happens next.
- **What to add or clarify:** One sentence for each situation describing the intended result.
  - Example phrasing: "If the customer clicks Cancel twice, only one cancellation and one refund happen." and "If the refund fails, the order stays cancelled, the customer sees 'Refund pending — we will notify you within 24 hours', and the support team is alerted."
- *Addresses: D-7, D-11, Q-10, Q-14*

### S-4 — Name the system that sends the money back (Critical)

- **What's missing or unclear:** "The money comes back to them" doesn't say which system issues the refund (for example, the payment provider that charged the card) or what happens if that system is slow or unavailable.
- **Why it matters:** The refund is the heart of this story; without knowing which system handles it, developers cannot connect to it and cannot say what happens when it fails — the story's main promise cannot be built.
- **What to add or clarify:** The name of the payment/refund system (and any reference document), plus one line on expected timing.
  - Example phrasing: "Refunds are processed by the existing Payment Service refund function and appear on the customer's original payment method within 5 business days."
- *Addresses: D-18*

### S-5 — Complete the customer's journey (Major)

- **What's missing or unclear:** The story jumps from "clicks Cancel" to "the order gets cancelled and the money comes back" — there is no confirmation step, no description of what the customer sees when it's done, and no timeframe for the refund.
- **Why it matters:** Without the full journey, developers will invent the middle (or skip the confirmation), and testers cannot verify the flow finished correctly because nothing describes the finish.
- **What to add or clarify:** The steps from opening My Orders to the final visible result, including any confirmation prompt and how long the refund takes.
  - Example phrasing: "The customer clicks Cancel, confirms in a dialog, sees a confirmation page with the refund amount, and receives a confirmation email; the refund appears within 5 business days."
- *Addresses: D-6, Q-5, Q-8*

### S-6 — Replace vague words with measurable ones (Major)

- **What's missing or unclear:** Words like "fast," "user-friendly," "appropriately," "correctly," and "smoothly" appear where concrete expectations are needed.
- **Why it matters:** Each of these can be read differently by the business, developers, and testers — "fast" could mean one second or ten — so the built result may not match what you meant, and no one can prove whether it passed.
- **What to add or clarify:** Replace each vague word with a number or a concrete behavior.
  - Example phrasing: "The cancellation completes within 3 seconds" instead of "the system should be fast"; "invalid entries are rejected with the message '…'" instead of "handle invalid input appropriately."
- *Addresses: D-1, D-2, D-3, D-4, Q-1, Q-2, Q-3, Q-7*

### S-7 — Say what happens when an order can't be cancelled (Major)

- **What's missing or unclear:** The story doesn't describe the customer's experience when cancellation isn't possible (for example, the order has already shipped).
- **Why it matters:** Developers must choose between hiding the button, disabling it, or showing an error — each looks different to the customer, and testers need to know which one to expect.
- **What to add or clarify:** One sentence for the "not allowed" case.
  - Example phrasing: "If the order has shipped, the Cancel option is not shown and the customer sees 'This order can no longer be cancelled.'"
- *Addresses: D-10, Q-13*

### S-8 — Say who is allowed to cancel (Major)

- **What's missing or unclear:** The story says "the customer" cancels, but doesn't state that only the customer who placed the order may cancel it.
- **Why it matters:** Without this, one person might be able to cancel another person's order — a privacy and money-safety problem — and testers won't know whether to test such an attempt, or how to judge it.
- **What to add or clarify:** A line restricting the action to the order's owner.
  - Example phrasing: "Cancellation is available only to the signed-in customer who placed the order."
- *Addresses: D-12, Q-15*

### S-9 — Cover the "in the meantime" moments (Major)

- **What's missing or unclear:** Nothing says what happens if the order changes while the customer is cancelling — e.g., the order ships just as they click Cancel — or if they try to cancel an order that was already cancelled.
- **Why it matters:** These situations will happen in real life; if the story is silent, each developer handles them differently and testers can't say which behavior is right.
- **What to add or clarify:** One line per situation naming the intended outcome.
  - Example phrasing: "If the order ships while the customer is cancelling, the cancellation is refused and the customer sees 'This order has already shipped.'" and "Cancelling an already-cancelled order has no effect and no second refund occurs."
- *Addresses: D-8, D-9, Q-11*

### S-10 — Make the reporting ask concrete — or split it out (Major)

- **What's missing or unclear:** "For reporting, the cancellation should be tracked somewhere" doesn't say what information is kept, where, or for how long — and it isn't clear this story should include reporting at all.
- **Why it matters:** "Somewhere" cannot be built or checked; and bundling a reporting need into the cancellation story makes both harder to deliver and test in one go.
- **What to add or clarify:** Either list the details to keep for each cancellation, or move reporting to its own story and reference it here.
  - Example phrasing: "Each cancellation records: order number, date/time, who cancelled, and refund amount — kept for 24 months." (or "Reporting on cancellations is handled in story ORD-XXX.")
- *Addresses: D-15, D-20, Q-9, Q-16*

### S-11 — List what the customer provides, and the rules for it (Major)

- **What's missing or unclear:** The story doesn't say whether the customer must provide anything when cancelling (e.g., a reason) or what makes an entry valid.
- **Why it matters:** If there is an input, its rules (required? maximum length?) decide what developers build and what testers check; unstated rules mean both sides guess.
- **What to add or clarify:** Name each input and its rule, or state there is none.
  - Example phrasing: "No information is required to cancel — one click plus confirmation." or "An optional reason may be entered, up to 200 characters."
- *Addresses: D-16*

### S-12 — Point out what this change touches and depends on (Major)

- **What's missing or unclear:** The story doesn't mention which existing parts of the system are affected (e.g., stock levels when items return) or which other teams/systems/stories it relies on (e.g., the payment system, reporting).
- **Why it matters:** Unlisted connections are where late surprises come from — other features may behave differently after this change, and dependent work may not be ready when this story is.
- **What to add or clarify:** A short "depends on / affects" list.
  - Example phrasing: "Depends on: Payment Service refund capability. Affects: stock availability for returned items; existing order history page."
- *Addresses: D-19, D-21, Q-17*

### S-13 — Say what oversight the money flow needs (Major)

- **What's missing or unclear:** Because this feature moves customers' money, the story says nothing about who can see or perform cancellations, or whether a record is needed for audits (a checkable history of who did what).
- **Why it matters:** Money-moving features normally need at least a basic record and access restriction; deciding this after build is expensive and may breach company policy.
- **What to add or clarify:** One or two lines on access and record-keeping.
  - Example phrasing: "Only the ordering customer can cancel; each cancellation and refund is recorded with who, when, and how much, visible to support staff."
- *Addresses: D-13*

### S-14 — Confirm whether older orders can be cancelled too (Minor)

- **What's missing or unclear:** It isn't stated whether orders placed before this feature goes live can also be cancelled.
- **Why it matters:** It's a small point, but it decides the size of the first release's effect and gives testers the right set of orders to prepare.
- **What to add or clarify:** One line.
  - Example phrasing: "Orders placed before the release date can also be cancelled if they are in an eligible status."
- *Addresses: D-17*

### S-15 — Note accessibility needs for the cancellation screen (Minor)

- **What's missing or unclear:** No accessibility requirements (e.g., usable with screen readers or keyboard only) are mentioned for the cancellation dialog/page.
- **Why it matters:** A one-line requirement avoids rework later if the flow must meet accessibility standards.
- **What to add or clarify:** One line.
  - Example phrasing: "The cancellation dialog meets our standard accessibility requirements (WCAG 2.1 AA)."
- *Addresses: D-14*

### S-16 — Confirm the empty view stays as-is (Minor)

- **What's missing or unclear:** The story doesn't mention what a customer with no orders sees in My Orders.
- **Why it matters:** Probably today's behavior is fine — saying so explicitly closes the question and stops testers from raising it during test design.
- **What to add or clarify:** One line.
  - Example phrasing: "My Orders behavior for customers with no orders is unchanged."
- *Addresses: Q-12*
