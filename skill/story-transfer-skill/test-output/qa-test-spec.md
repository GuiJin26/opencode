# QA Test Specification

| | |
|---|---|
| **Story** | Order Cancellation (ORD-482) |
| **Date** | 2026-10-18 |
| **Mode** | Review + Transfer |
| **Story source** | samples/sample-story-poor.md |
| **Readiness** | Not ready — 8 open Critical gaps (see warning below) |
| **Output handling** | created new file |

**⚠ STORY NOT YET READY — OPEN CRITICAL GAPS**

This specification was generated from a story with unresolved Critical findings. Sections built on incomplete input are marked "based on incomplete story input — pending BA clarification". Do not treat this specification as final until the BA resolves the gaps below.

- [Critical] D-5: Eligibility rules absent — which orders may be cancelled is unknown.
- [Critical] D-7: Duplicate submission (double-click on Cancel) could cause two refunds — unaddressed.
- [Critical] D-11: Refund-failure behavior undefined (cancelled order, no money returned).
- [Critical] D-18: The refund depends on a payment system the story never identifies.
- [Critical] Q-4: Eligibility gap from the QA view — core behavior untestable.
- [Critical] Q-6: No acceptance criteria at all — no definition of done.
- [Critical] Q-10: Duplicate-submission scenario missing — double refund unverifiable.
- [Critical] Q-14: Refund-failure path untestable — expected outcome undefined.

## 1. Acceptance Criteria (Given/When/Then)

The original story contains no acceptance criteria; every criterion below is derived from the story description and is marked **draft — requires BA confirmation**. Criteria built on gapped areas carry the marker "based on incomplete story input — pending BA clarification".

**AC-1** (maps to FR-1, FR-2) — Cancel an eligible order
- **Given** the customer is signed in and views one of their not-yet-shipped orders in My Orders [assumption: OQ-1]
- **When** they click Cancel on the order
- **Then** the order's status changes to "Cancelled"
- Status: draft — requires BA confirmation
- Notes: eligibility is an assumption (Critical gap D-5/Q-4) — based on incomplete story input — pending BA clarification.

**AC-2** (maps to FR-3) — Refund issued on cancellation
- **Given** the customer has just cancelled an order
- **When** the cancellation completes
- **Then** a refund of the full order total is initiated to the customer's original payment method [assumption: OQ-2]
- Status: draft — requires BA confirmation
- Notes: refund mechanism unidentified (Critical gap D-18) — based on incomplete story input — pending BA clarification.

**AC-3** (maps to FR-4) — Cancellation tracked
- **Given** an order has been cancelled
- **When** the cancellation record is inspected for reporting
- **Then** a record of the cancellation exists [assumption: OQ-3]
- Status: draft — requires BA confirmation
- Notes: "tracked somewhere" defines no observable output (D-15/Q-9) — pending BA clarification.

**AC-4** (maps to FR-5) — Ineligible order
- **Given** an order that has already shipped
- **When** the customer views it in My Orders
- **Then** the Cancel action is not available (or the attempt is rejected) [assumption: OQ-1]
- Status: draft — requires BA confirmation
- Notes: based on incomplete story input — pending BA clarification (Critical gap D-5/Q-4).

**AC-5** (maps to FR-6) — No duplicate refund
- **Given** the customer clicks Cancel twice within seconds
- **When** both requests are processed
- **Then** exactly one cancellation and one refund exist for the order
- Status: draft — requires BA confirmation
- Notes: based on incomplete story input — pending BA clarification (Critical gap D-7/Q-10).

**AC-6** (maps to FR-7) — Invalid input
- **Given** invalid input is provided during cancellation (if any input is collected)
- **When** it is submitted
- **Then** the system rejects it with a user-visible message
- Status: draft — requires BA confirmation
- Notes: cannot be made measurable until validation rules are defined (OQ-5) — flagged, not dropped.

## 2. Test Scenario Outline

| ID | Scenario | Category | Precondition | Steps/Trigger | Expected Result | Maps to |
|----|----------|----------|--------------|---------------|-----------------|---------|
| TS-1 | Cancel an eligible order | Positive | Signed-in customer with a not-yet-shipped order [assumption: OQ-1] | Open My Orders → click Cancel on the order | Order status becomes "Cancelled" | FR-1, FR-2, AC-1 |
| TS-2 | Refund initiated after cancellation | Positive | An order has just been cancelled | Observe the refund outcome | Refund of the full order total initiated to original payment method [assumption: OQ-2] — pending BA clarification (D-18) | FR-3, AC-2 |
| TS-3 | Cancellation recorded for reporting | Positive | An order has been cancelled | Inspect the cancellation record | A cancellation record exists [assumption: OQ-3] | FR-4, AC-3 |
| TS-4 | Cancel attempt on a shipped order | Negative | Signed-in customer with a shipped order | Open My Orders → attempt to cancel | Cancel not offered / attempt rejected — based on incomplete story input — pending BA clarification (OQ-1) | FR-5, AC-4 |
| TS-5 | Cross-customer cancel attempt | Negative | Customer B signed in; order belongs to customer A | Customer B attempts to cancel customer A's order (e.g., direct request) | Blocked with a defined response — pending BA confirmation (OQ-10) | FR-1 |
| TS-6 | Refund fails after cancellation | Negative | Order cancelled; refund step fails | Observe customer-visible outcome and money state | Undefined — pending BA confirmation (OQ-9, OQ-14) | FR-3 |
| TS-7 | Invalid input during cancellation | Negative | Cancellation flow collects an input | Submit invalid input | Rejected with user-visible message — pending BA confirmation (OQ-5) | FR-7, AC-6 |
| TS-8 | Double-click Cancel | Boundary | Signed-in customer with an eligible order | Click Cancel twice within seconds | Exactly one cancellation and one refund — pending BA confirmation (OQ-4) | FR-6, AC-5 |
| TS-9 | Cancel an already-cancelled order | Boundary | Order already cancelled | Click Cancel (if offered) on the cancelled order | No second cancellation/refund — pending BA confirmation (OQ-7) | FR-6 |
| TS-10 | Order ships mid-cancellation | Boundary | Cancellation being processed; order ships concurrently | Complete the cancellation attempt | Defined winner (ship vs. cancel) — pending BA confirmation (OQ-6) | FR-2, FR-3 |
| TS-11 | Pre-release order cancellation | Boundary | Order placed before feature release, in eligible status | Attempt to cancel | Per historical-data decision — pending BA confirmation (OQ-12) | FR-5 |

## 3. Test Data Considerations

- An account with orders in various statuses (e.g., Placed, Processing, Shipped, Delivered, already Cancelled) [assumption: OQ-1 — the status set is assumed].
- Two customer accounts to test cross-customer access attempts (OQ-10).
- A payment method on the test account that can receive refunds and be made to fail, to exercise the refund-failure path (OQ-9, OQ-14).

## 4. Open Questions

Behaviors that cannot be turned into a deterministic expected outcome without BA input. Same OQ IDs as the Developer Specification — one shared list; this section shows the test-blocking subset.

| ID | Question | Blocks scenario(s) |
|----|----------|--------------------|
| OQ-1 | Which orders are eligible for cancellation? | TS-1, TS-4, TS-11, AC-1, AC-4 |
| OQ-4 | Exactly one refund on repeated Cancel clicks? | TS-8, AC-5 |
| OQ-5 | Which inputs are collected and what validation applies? | TS-7, AC-6 |
| OQ-9 | Expected outcome when the refund fails after cancellation? | TS-6 |
| OQ-10 | Expected response for cross-customer cancel attempts? | TS-5 |
| OQ-14 | Which payment system processes refunds; its failure behavior? | TS-2, TS-6, AC-2 |
