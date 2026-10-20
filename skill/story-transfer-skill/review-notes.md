# Code Review — story-transfer-skill (opencode skill deliverable)

**Reviewer:** Frank (Code Reviewer)
**Date:** 2026-10-18
**Reviewed:** SKILL.md, references/review-dimensions.md, references/severity-guide.md, references/output-templates.md, samples/sample-story-poor.md, samples/sample-story-good.md vs. requirements.md (14 stories, Section 6 I/O, Section 7 dimensions, Section 8 NFRs)

---

## Summary

**Verdict: APPROVE WITH CHANGES**

The skill is an exceptionally faithful implementation of the requirements. All 14 user stories are covered (including Nice-to-Have US-W7 as an optional extension); all Section 6.2 required output sections are present in the templates; the 10 review dimensions and the 8-dev/7-QA perspective mapping are consistent across SKILL.md and both reference files; and every key NFR rule (no fabrication, full checklist, deterministic failure, BA tone, traceability, minimal chat) is encoded explicitly and redundantly where it matters. Samples are well designed for their purpose.

**1 Major issue** found (a self-contradicting template convention in output-templates.md that could cause the US-W2 "story not yet ready" warning to be dropped from output) plus **9 Minor issues**. None block QA testing — the Major item is a one-line template fix — but the Major item should be fixed before the skill is used in a real walkthrough.

---

## Findings

| ID | Severity | File | Description | Recommendation |
|----|----------|------|-------------|----------------|
| F-1 | **Major** | references/output-templates.md | **Blockquote convention conflict.** The preamble (line 3) declares: "blockquoted `>` lines are instructions for the generating AI and must NOT appear in the final files." However, the US-W2 Critical-gaps warning blocks in Template 2 (lines 178–186) and Template 3 (lines 285–287) are themselves blockquotes that ARE intended as verbatim output content. A literal executor following the global rule would strip the "STORY NOT YET READY" warning from dev-spec.md / qa-test-spec.md — directly breaking US-W2 — or, conversely, might keep instruction blockquotes in final files. The intent is recoverable from the adjacent HTML comment ("include ONLY for…"), but the convention is objectively contradictory. | Either (a) reformat the warning as non-blockquote content (e.g., `**⚠ STORY NOT YET READY — OPEN CRITICAL GAPS**` heading + plain list), or (b) scope the preamble rule: "blockquoted lines are instructions and must not appear in final files — **except** the readiness warning blocks in Templates 2/3, which are verbatim content." |
| F-2 | Minor | SKILL.md (Workflow intro, line 62) | Typo/grammar: "Steps 4 runs for `review` and `review+transfer`; step 5 runs for…" — "Steps 4" should be "Step 4". Also, it is implicit (not stated) that Steps 1–3 and 6–7 always run. | Fix to "Step 4 runs for…; Step 5 runs for…; Steps 1–3, 6, and 7 always run." |
| F-3 | Minor | references/output-templates.md (Template 1 header) | Header hardcodes `\| **Mode** \| Review \|` while US-W1, SKILL.md Step 1, and output-templates' own Consistency Requirement #5 say every output header must label the mode that produced it. In a full-pipeline run it is ambiguous whether review-report.md should say "Review" or "Review + Transfer". | Make it a placeholder: `\| **Mode** \| {Review \| Review + Transfer} \|`. |
| F-4 | Minor | references/review-dimensions.md (example tables) | Example finding IDs collide across dimensions: Q-1, Q-2, Q-3, Q-4 are each reused for *different* findings in different dimensions (e.g., Q-1 = "workflow fragment" in 7.2 but Q-1 = "no acceptance criteria" in 7.3), while D-n examples happen to be unique. An executing AI may treat example IDs as canonical or reuse IDs incorrectly in a real report. | Add one line near the top: "Example IDs are illustrative only; a real report assigns unique sequential IDs per perspective (D-1…, Q-1…)." Optionally renumber Q examples to be unique. |
| F-5 | Minor | references/review-dimensions.md (7.10, D-16) | The D-16 example is labeled "Internal contradiction" but the two quoted phrases ("cancels the order" vs. "works smoothly for all orders") do not conflict — both are unbounded. The "contradiction" relies on an unstated domain assumption (shipped orders can't be cancelled) and actually duplicates D-2's missing-eligibility gap. It teaches the wrong pattern for the 7.10 contradiction check and models double-counting one gap under two dimensions. | Replace with a true contradiction example, e.g., the one already in severity-guide.md: "Notes say refund within 3 days; description says within 7 — both cannot be true." |
| F-6 | Minor | references/severity-guide.md + references/output-templates.md | The Quality Rating Rule is duplicated verbatim in two files (currently consistent, but two sources of truth will drift when one is edited). | Keep the rule in severity-guide.md only and have output-templates.md reference it (matching SKILL.md Step 4d, which already points to the severity guide). |
| F-7 | Minor | references/review-dimensions.md | US-R2's paraphrased dimension list includes "Test data needs," which has no explicit home in the QA review — it surfaces only in the QA Test Spec transfer output (§3). The skill handled the analogous "Business Rules → 7.2" mapping with an explicit note (line 28) but left this one silent, so it looks like an acceptance criterion was dropped. | Add a mapping note (like the Business Rules note): "US-R2's 'Test data needs' is addressed in transfer output (qa-test-spec §3 Test Data Considerations) and via 7.4's 'imagine writing a test case' check." |
| F-8 | Minor | SKILL.md (Step 3, File Map) | Reference/sample paths (`references/…`, `samples/…`) are relative, and nothing states they resolve against the skill's own directory. A fresh AI session could look for them in the current workspace CWD and fail. | Add one line in Step 3 or the File Map: "All file paths below are relative to this skill's directory." |
| F-9 | Minor | SKILL.md (Batch Review, step 1) | "Confirm the intent" is vague — confirm with whom, and confirm what exactly (that a batch run is review-only)? | Reword: "If the request is ambiguous (e.g., a directory that may contain one story), confirm with the user that they want a batch review; batches run review only by default." |
| F-10 | Minor | references/output-templates.md (Template 1 §3.3) | "AC #" in the Acceptance criteria validation table refers to the *story's original* criterion numbering, while `AC-n` IDs in qa-test-spec.md refer to *generated* criteria — same prefix, two namespaces. Minor confusion risk for the executing AI. | Rename the column header to "Story AC #" or add a parenthetical "(story's own numbering)". |

---

## Coverage Matrix

| Story | Priority | Covered | Where / Notes |
|-------|----------|---------|---------------|
| US-R1 Dev-perspective review | Must | ✅ Yes | SKILL.md Step 4a; review-dimensions 7.1–7.10 Dev mapping (8 dims); Template 1 §2 with all 8 dimension subsections. Quote-evidence, edge-case-with-example, and "No issue found" rules all present. |
| US-R2 QA-perspective review | Must | ✅ Yes | Step 4b; 7 QA dims; Template 1 §3. No-ACs → Critical; untestable → quote + missing measurable condition; happy-path-only → missing negative/boundary listed; testable criteria confirmed via §3.3 validation table. Minor: "Test data needs" mapping undocumented (F-7). |
| US-R3 BA-facing suggestions | Must | ✅ Yes | Step 4c; Template 1 §4 (what → why → what to add + example phrasing); no-orphan rule + "Addresses: {finding IDs}" per suggestion; severity-ordered; tone rules in Rule 4 and severity-guide. |
| US-R4 Executive summary | Should | ✅ Yes | Step 4d; Template 1 §1: rating, counts by severity and per perspective, shared gaps, top 3–5, <2-min read. |
| US-T1 Dev-oriented transfer | Must | ✅ Yes | Step 5b; Template 2: Objective, FRs, Edge Cases, Error Handling, Data, Integration, Open Questions & Assumptions (complete-list rule); no-silent-ambiguity (Rule 6); Source column = story or assumption only. |
| US-T2 QA-oriented transfer | Must | ✅ Yes | Step 5c; Template 3: GWT ACs (drafts marked), scenario table with all 5 required columns + category + FR/AC mapping, test data, open questions; untestable criteria flagged never dropped. |
| US-T3 Combined run consistency | Must | ✅ Yes | Step 5e; shared FR IDs; identical-warning and no-contradiction rules in output-templates "Consistency Requirements Before Writing Files". |
| US-W1 Mode selection | Must | ✅ Yes | Three modes; default `review+transfer` (matches A-6/OQ-2); mode labeled in headers and chat reply. Minor header hardcoding (F-3). |
| US-W2 Review gates Transfer | Should | ✅ Yes | Step 5d; warning block + "based on incomplete story input — pending BA clarification" markers + no-Critical appendix in both templates. Transfer never refuses (matches OQ-1 current design). Minor: warning block convention conflict (F-1). |
| US-W3 Flexible input | Must | ✅ Yes | File path / pasted text; Step 2 validation with error format and NO output files; Rule 7 "Context is truth" + "Context first" in review-dimensions. |
| US-W4 Re-review / Gap Closure Check | Should | ✅ Yes | Step 4e; Template 1 §5: Resolved / Partially resolved / Not addressed with evidence, newly introduced issues; fresh full review preserved. |
| US-W5 File-based outputs | Must | ✅ Yes | Step 6: three files, deterministic overwrite policy, "Output handling" header field, never overwrite inputs; Step 7 minimal reply. |
| US-W6 Traceability | Should | ✅ Yes | Rule 5; markers table (`[assumption: OQ-n]`, "Absent from story"); OQ completeness rules in Template 2 §7. |
| US-W7 Batch review | Nice to Have | ✅ Yes | SKILL.md Batch Review section: per-story validation, `review-report-{slug}.md`, `backlog-readiness.md` summary table with rating + severity counts. |

**NFR encoding check (key rules per review instructions):**

| NFR | Encoded | Where |
|-----|---------|-------|
| NFR-2 Minimal chat reply | ✅ | Rule 8 + Step 7 (file paths + 3–5 bullets, example given) |
| NFR-4 BA-friendly tone | ✅ | Rule 4 + severity-guide tone rule (banned/preferred phrasing, plain-language term example) |
| NFR-7 No fabrication | ✅ | Rule 1 + Source column ("no third option") + OQ completeness |
| NFR-8 Deterministic failure | ✅ | Rule 3 + Step 2 reject conditions + error message template |
| NFR-9 Full checklist always reported | ✅ | Rule 2 + "No issue found" bar per dimension + Template 1 §2/§3 "ALL 8/ALL 7" instructions |
| NFR-10 Traceability markers | ✅ | Rule 5 + markers table + "Addresses:" back-references |

NFR-1, 3, 5, 6, 11, 12 also verified as covered. NFR-9's literal "all 10 dimensions" is correctly interpreted as 8 Dev + 7 QA (union = 10) and the interpretation is explicitly documented in review-dimensions.md — not silent.

**Requirements-doc quirks the skill handled correctly** (for the record, not defects of the skill): US-R1/US-R2 cite "Section 8" for dimensions, but dimensions actually live in Section 7 — the skill correctly uses 7.1–7.10 numbering; "Business Rules" from US-R1 is folded into 7.2 with an explicit mapping note.

---

## Positive Observations

1. **Outstanding traceability discipline.** Nearly every rule, step, and template element cites its source story/NFR ID (e.g., "Rule 3 — Deterministic failure (NFR-8)"). This makes the skill auditable against the requirements — exactly what US-W6 asks the skill to do for stories, applied to the skill itself.
2. **"Non-Negotiable Rules" with explicit precedence** ("These rules override everything else. If any instruction conflicts with them, the rules win.") is the right pattern for LLM-executed skills — it protects the key NFRs against drift in long contexts.
3. **Consistency across files is excellent.** Dimension counts (10 total, 8 Dev, 7 QA), severity definitions, file names, ID scheme, mode default, and quality-rating rule agree across SKILL.md, both reference files, and the templates. Section 6.2's required sections are all present in the templates (mostly as supersets).
4. **The severity guide is deterministic by design** — first-match-wins heuristics, perspective-independence rule, relevance anti-inflation rule, and a mechanical rating mapping. This is what makes repeatable reviews across runs plausible.
5. **Samples are genuinely fit for purpose.** The poor sample deliberately exercises nearly every checklist item (vague terms "fast/appropriately/smoothly," no ACs, happy-path only, unbounded eligibility, unidentified refund integration, "tracked somewhere," duplicate-submission risk, scope creep via reporting) while still passing Step 2 input validation — so it tests review quality, not input rejection. The good sample demonstrates eligibility rules, confirmation flow, double-click and race handling, refund-failure behavior, out-of-scope declarations, 6 GWT ACs covering positive/negative/boundary, named integration (PAY-REF-12), volume, and data retention — a realistic "Ready / Ready with minor gaps" story.
6. **Example findings in review-dimensions.md were clearly written against the poor sample**, creating a coherent teaching chain (sample → dimension → example finding → severity guide example). This significantly helps a fresh session calibrate.
7. **Sensible resolution of requirements open questions** (OQ-1: transfer runs with warnings, never refuses; OQ-2: default review+transfer; OQ-3: three separate files; OQ-4: process and emit Open Questions rather than interactively asking) — each matches the requirements' stated "current design" rather than inventing new behavior.

---

## Approval Decision

**APPROVE WITH CHANGES** — recommend Charlie (QA) proceed with testing; request Bob address F-1 (Major) before the skill is used on a real story, and fold the Minor items (F-2 … F-10) into the same pass. None of the Minor items block testing.
