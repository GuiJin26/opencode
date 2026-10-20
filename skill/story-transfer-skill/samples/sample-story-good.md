# Order Cancellation

**Story ID:** ORD-483
**Author:** Business Team
**Priority:** High

## Story

As a customer, I want to cancel my order before it ships so that I don't receive items I no longer need and get my money back without calling support.

## Description

### Eligibility

- Orders can be cancelled only while the order status is "Placed" or "Processing". Orders in status "Shipped" or "Delivered" cannot be cancelled.
- Cancellation is available only to the signed-in customer who placed the order.

### Flow

1. Customer opens **My Orders** and clicks **Cancel order** on an eligible order.
2. System shows a confirmation dialog: "Cancel this order? This cannot be undone." with buttons **Yes, cancel order** and **Keep order**.
3. On confirmation, the system sets the order status to "Cancelled" and triggers a refund of the full order total (including shipping) to the original payment method.
4. Refunds appear on the customer's original payment method within 5 business days.
5. Customer sees a confirmation page showing order number, cancellation time, refund amount, and estimated refund date. A confirmation email is sent to the customer's email address on file.

### Rules

- Cancelled orders remain visible in **My Orders** with status "Cancelled"; the cancelled items return to available stock.
- If the customer clicks **Cancel order** twice in quick succession (e.g., double-click), exactly one cancellation and one refund must occur — no duplicate refunds.
- If the order ships while the confirmation dialog is open, clicking **Yes, cancel order** shows the message "This order can no longer be cancelled — it has shipped", and no status change or refund occurs.
- If the refund step fails after cancellation, the order stays "Cancelled", the customer sees "Order cancelled. Refund pending — you will be notified within 24 hours", and an alert is raised to the support queue.

### Out of scope

- Partial (per-item) cancellation — separate story ORD-501.
- Returns after delivery — existing RMA process, unchanged.

## Acceptance Criteria

1. Given an order in status "Processing", when the customer who placed it clicks **Cancel order** and confirms, then the order status changes to "Cancelled" and a refund of the full order total is initiated within 1 minute.
2. Given an order in status "Shipped", when the customer views it in **My Orders**, then **Cancel order** is not shown for that order.
3. Given an order has been cancelled, when the customer views **My Orders**, then the order is listed with status "Cancelled", the cancellation timestamp, and the refund amount.
4. Given a refund has been initiated, when 5 business days have elapsed, then the refund is visible on the customer's original payment method.
5. Given a customer clicks **Cancel order** twice within 3 seconds, when both requests are processed, then exactly one refund is created.
6. Given the order ships while the confirmation dialog is open, when the customer clicks **Yes, cancel order**, then the message "This order can no longer be cancelled — it has shipped" is displayed and no status change or refund occurs.

## References

- Refunds are processed by the existing Payment Service refund API (async confirmation; see PAY-REF-12). Payment Service unavailability is handled by the "refund pending" rule above.
- Expected volume: up to 500 cancellations per day.
- Cancellation records (order ID, timestamp, actor, refund amount, refund status) are stored for 24 months per current order-data retention policy.
