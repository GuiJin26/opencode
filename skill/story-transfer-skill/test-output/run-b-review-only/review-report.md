# Story Review Report

| | |
|---|---|
| **Story** | Order Cancellation (ORD-483) |
| **Review date** | 2026-10-18 |
| **Mode** | Review |
| **Story source** | samples/sample-story-good.md |
| **Context used** | none |
| **Previous report** | none |
| **Output handling** | created new file |

## 1. Executive Summary

- **Quality rating:** Ready
- **Issues found:** 0 Critical · 0 Major · 7 Minor
  - Developer perspective: 0 Critical · 0 Major · 5 Minor
  - QA perspective: 0 Critical · 0 Major · 2 Minor
- **Gaps raised by both perspectives:**
  - D-2 / Q-2 — the exact response for an unauthorized cancellation attempt (another customer's order) is not explicitly described; the access rule itself is clear.
- **Top gaps to discuss with the BA:**
  1. Two described behaviors have no matching acceptance criterion: the refund-failure path ("Refund pending — you will be notified within 24 hours") and the confirmation email (Q-1).
  2. What exactly does the support team see when the refund-failure alert is raised (D-1)?
  3. What response does an unauthorized cancellation attempt receive (D-2 / Q-2)?
  4. Are orders placed before this feature ships also cancellable (D-4)?
  5. Minor polish: accessibility needs for the dialog, confirmation-email failure behavior (D-3, D-5).

The story is ready for walkthrough; the minor items above can be confirmed during the meeting without blocking development or test design.

## 2. Developer Perspective Review

### 2.1 Clarity & Unambiguity — No issue found

Language is concrete throughout: named statuses ("Placed", "Processing", "Shipped", "Delivered"), quoted dialog text, and measurable timeframes ("within 5 business days", "within 1 minute", "within 24 hours", "twice within 3 seconds"). Every statement has a single interpretation.

### 2.2 Completeness — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-1 | Minor | "an alert is raised to the support queue" | The alert's content and destination are undescribed — what the support team sees and is expected to do is left open. The triggering condition and the customer-facing outcome are fully defined, so this is a polish gap only. |

### 2.3 Edge Cases & Boundary Conditions — No issue found

Double-click ("exactly one cancellation and one refund must occur"), mid-flow state change (order ships while the dialog is open), already-shipped orders, and refund failure are each addressed with explicit expected outcomes. Boundary values are quantified (5 business days, 3 seconds, full order total including shipping).

### 2.4 Error Handling & Negative Paths — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-2 | Minor | "Cancellation is available only to the signed-in customer who placed the order." | The access rule is clear, but the response to an unauthorized attempt (what the requester sees if they bypass the UI, e.g., a crafted request) is not explicitly stated. Natural reading — the Cancel action never appears for another customer's order — makes this low-risk. |

### 2.5 Non-Functional Requirements — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-3 | Minor | Absent from story | No accessibility or compatibility requirements stated for the user-facing confirmation dialog/page. Volume (500/day), retention (24 months), and access control are covered; only this polish item is missing. |

### 2.6 Data Considerations — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-4 | Minor | Absent from story | Historical data handling unaddressed: should orders placed before this feature ships also be cancellable? (Obvious safe default — any order in an eligible status — but one line would close it.) Entities/fields, retention, and volume are well specified ("Cancellation records (order ID, timestamp, actor, refund amount, refund status) … 24 months", "up to 500 cancellations per day"). |

### 2.7 Integration Considerations — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| D-5 | Minor | "A confirmation email is sent to the customer's email address on file." | The email is a downstream dependency whose failure behavior is not stated (if the email cannot be sent, is anything retried or logged?). Low risk: the in-app confirmation page is the primary observable outcome. The payment integration, by contrast, is fully specified (PAY-REF-12, "refund pending" rule for unavailability). |

### 2.8 Consistency & Scope — No issue found

No internal contradictions ("before it ships" in the narrative matches the Placed/Processing eligibility; "cannot be undone" is consistent with the no-uncancel path; the 5-business-day refund window matches AC-4). The story is right-sized for one iteration, and dependencies are declared (ORD-501 for partial cancellation, the existing RMA process, PAY-REF-12 for refunds).

## 3. QA Perspective Review

### 3.1 Clarity & Unambiguity — No issue found

All behavioral statements use concrete triggers and observable outcomes; "quick succession (e.g., double-click)" is made measurable by AC-5's "within 3 seconds".

### 3.2 Completeness — No issue found

Roles are identified (the signed-in customer who placed the order), the workflow runs entry point → outcome (My Orders → dialog → status change → refund → confirmation page + email), business rules are stated explicitly, and out-of-scope items are declared (partial cancellation, returns after delivery).

### 3.3 Acceptance Criteria Quality — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| Q-1 | Minor | Refund-failure rule and email have no matching criterion | Two described behaviors lack a corresponding acceptance criterion: the refund-failure path ("Order cancelled. Refund pending — you will be notified within 24 hours") and the confirmation email. The rules themselves are testable as written, so this is a coverage polish item, not a happy-path-only problem — negative and boundary flows ARE covered by AC-2, AC-5, and AC-6. |

**Acceptance criteria validation**

| Story AC # | Story criterion (quoted) | Testable? | Notes |
|------|--------------------------|-----------|-------|
| 1 | "Given an order in status 'Processing', when the customer who placed it clicks Cancel order and confirms, then the order status changes to 'Cancelled' and a refund of the full order total is initiated within 1 minute." | Yes | Concrete precondition, trigger, and two measurable outcomes with a 1-minute bound. |
| 2 | "Given an order in status 'Shipped', when the customer views it in My Orders, then Cancel order is not shown for that order." | Yes | Specific state and directly observable UI outcome. |
| 3 | "Given an order has been cancelled, when the customer views My Orders, then the order is listed with status 'Cancelled', the cancellation timestamp, and the refund amount." | Yes | Observable list content, fully specified. |
| 4 | "Given a refund has been initiated, when 5 business days have elapsed, then the refund is visible on the customer's original payment method." | Yes | Measurable elapsed-time condition and externally observable result. |
| 5 | "Given a customer clicks Cancel order twice within 3 seconds, when both requests are processed, then exactly one refund is created." | Yes | Precise trigger window and countable outcome. |
| 6 | "Given the order ships while the confirmation dialog is open, when the customer clicks Yes, cancel order, then the message 'This order can no longer be cancelled — it has shipped' is displayed and no status change or refund occurs." | Yes | Concrete mid-flow state, exact expected message, and negative assertions. |

### 3.4 Testability — No issue found

Every behavioral statement names a trigger and an observable outcome ("an alert is raised to the support queue" is observable via the queue; "the cancelled items return to available stock" is observable via stock levels); success and failure states are distinguishable throughout.

### 3.5 Edge Cases & Boundary Conditions — No issue found

Duplicate submission (AC-5), mid-flow state change (AC-6), ineligible state display (AC-2), and refund failure (Rules §4) each carry explicit expected outcomes.

### 3.6 Error Handling & Negative Paths — Issues found

| ID | Severity | Story evidence | Issue |
|----|----------|----------------|-------|
| Q-2 | Minor | "Cancellation is available only to the signed-in customer who placed the order." | The expected response for a cross-customer cancellation attempt (e.g., a direct request against another customer's order) is not stated. Natural reading — other customers never see the Cancel action — means this mainly matters for direct API-level access; a one-line expected response would close it. |

### 3.7 Consistency & Scope — No issue found

No contradictions found; the story is right-sized; dependencies are declared (ORD-501, RMA process, PAY-REF-12).

## 4. Suggestions for the BA

### S-1 — Add criteria for the two uncovered behaviors (Minor)

- **What's missing or unclear:** The refund-failure path and the confirmation email are described in the rules but have no matching acceptance criterion, unlike the six flows that do.
- **Why it matters:** Testers will design cases for them anyway; a stated criterion guarantees the expected result you intend, rather than the tester's reading of the rules.
- **What to add or clarify:** One criterion per behavior, in the same style as the existing six.
  - Example phrasing: "Given the refund step fails after cancellation, when the customer views the order, then the message 'Order cancelled. Refund pending — you will be notified within 24 hours' is shown and the order status remains 'Cancelled'."
- *Addresses: Q-1*

### S-2 — Describe what the support team receives (Minor)

- **What's missing or unclear:** "An alert is raised to the support queue" doesn't say what the alert contains or what the support team should do next.
- **Why it matters:** The support team needs enough detail (order number, customer, refund amount) to act without asking the customer to repeat the story.
- **What to add or clarify:** One line on alert content and expected follow-up.
  - Example phrasing: "The alert includes order number, customer contact, and refund amount; support follows up within 24 hours."
- *Addresses: D-1*

### S-3 — State the response to an unauthorized attempt (Minor)

- **What's missing or unclear:** The story says only the ordering customer may cancel, but not what response someone gets if they try to cancel another person's order directly.
- **Why it matters:** A one-line expected response lets testers cover the case without guessing what "blocked" should look like.
- **What to add or clarify:** One line.
  - Example phrasing: "Requests to cancel another customer's order are rejected with 'You do not have permission to cancel this order.'"
- *Addresses: D-2, Q-2*

### S-4 — Confirm whether older orders are cancellable (Minor)

- **What's missing or unclear:** It isn't stated whether orders placed before this feature goes live can also be cancelled.
- **Why it matters:** It decides the first release's scope and tells testers which historical orders to prepare.
- **What to add or clarify:** One line.
  - Example phrasing: "Orders placed before the release date can also be cancelled if they are in an eligible status."
- *Addresses: D-4*

### S-5 — Note accessibility needs for the dialog (Minor)

- **What's missing or unclear:** No accessibility requirements are mentioned for the confirmation dialog and confirmation page.
- **Why it matters:** A one-line requirement avoids rework if the flow must meet accessibility standards later.
- **What to add or clarify:** One line.
  - Example phrasing: "The cancellation dialog and confirmation page meet our standard accessibility requirements (WCAG 2.1 AA)."
- *Addresses: D-3*

### S-6 — Say what happens if the confirmation email cannot be sent (Minor)

- **What's missing or unclear:** The confirmation email's failure behavior is not described.
- **Why it matters:** If the email fails, the customer still has the confirmation page — saying so explicitly tells developers and testers that no retry/alert is expected (or defines one if it is).
- **What to add or clarify:** One line.
  - Example phrasing: "If the confirmation email cannot be sent, the cancellation still completes and the failure is logged; no customer action is needed."
- *Addresses: D-5*
