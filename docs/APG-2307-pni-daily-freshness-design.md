# APG-2307 — OASys PNI Daily Freshness: Design Document

> **Purpose:** Explain the design decision for how and when we fetch OASys assessment data  
> **Audience:** Technical and non-technical stakeholders  
> **Date:** 26 May 2026  
> **Branch:** `APG-2307/pni-fix-500-error`

---

## 1. What Was Happening (The Problem)

Every time a staff member opened a referral's personal details page, the system made a live call to OASys to check their assessment data. This happened **every single time** — even if the page was opened 10 times in a day for the same person.

**The result:**
- ~200,000 calls per week to OASys, most returning "no data found" (404)
- ~8,000 calls per week failing because OASys was temporarily unavailable (503)
- Those 503 failures crashed the page with a 500 error — staff couldn't view the referral at all

---

## 2. What We Changed (The Solution)

### Simple Rule: "Once a day is enough"

```
┌─────────────────────────────────────────────────────────┐
│           Staff opens referral details page              │
└─────────────────────────┬───────────────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │ Has OASys data been   │
              │ fetched TODAY already? │
              └───────────┬───────────┘
                          │
               ┌──────────┴──────────┐
               │                     │
           YES ▼                 NO  ▼
    ┌──────────────────┐   ┌──────────────────────┐
    │ Use stored data  │   │ Call OASys for fresh  │
    │ (skip OASys call)│   │ assessment data       │
    │                  │   │                       │
    │ Page loads fast  │   │ Store the result      │
    └──────────────────┘   └───────────┬───────────┘
                                       │
                            ┌──────────┴──────────┐
                            │                     │
                      SUCCESS ▼             FAILURE ▼
               ┌──────────────────┐   ┌──────────────────┐
               │ Update cohort &  │   │ Use safe defaults │
               │ LDC from OASys   │   │ (General Offence, │
               │                  │   │  No LDC)          │
               │ Page shows fresh │   │                   │
               │ data             │   │ Page still loads! │
               └──────────────────┘   │ (no 500 error)   │
                                      └──────────────────┘
```

### What about manual overrides?

```
              ┌───────────────────────────┐
              │ Has a clinician manually   │
              │ set BOTH cohort AND LDC?   │
              └───────────┬───────────────┘
                          │
               ┌──────────┴──────────┐
               │                     │
           YES ▼                 NO  ▼
    ┌──────────────────┐   ┌──────────────────┐
    │ ALWAYS skip      │   │ Follow the       │
    │ OASys call       │   │ "once a day"     │
    │                  │   │ rule above        │
    │ Clinician's      │   │                  │
    │ decision is      │   │                  │
    │ respected        │   │                  │
    └──────────────────┘   └──────────────────┘
```

---

## 3. Why "Once a Day"?

| Question | Answer |
|----------|--------|
| How often do OASys assessments change? | Rarely — typically days/weeks between assessments |
| How often do staff view the same referral? | Multiple times per day (checking progress, notes, etc.) |
| What's the risk of stale data? | Very low — if an assessment changes today, it's picked up tomorrow at latest |
| What's the cost of calling every time? | 200,000 wasted calls/week, 8,000 failures, page crashes |

**The trade-off:** Accept data that is at most 24 hours old, in exchange for eliminating page errors and reducing OASys load by ~95%.

---

## 4. What Data Is Affected?

| Data field | Source | Refresh frequency | Why |
|-----------|--------|-------------------|-----|
| **Cohort** (Sexual Offence / General Offence) | OASys PNI | Once per day ⭐ NEW | Determines programme pathway |
| **LDC** (Learning Disabilities & Challenges) | OASys PNI | Once per day ⭐ NEW | Determines if adapted programme needed |
| **Person name** | nDelius | Every page view (live) | Names can change (marriage, legal) |
| **Date of birth** | nDelius | Every page view (live) | Corrections |
| **Sentence end date** | nDelius | Every page view (live) | Can change on appeal |
| **PDU / Team** | nDelius | Every page view (live) | Transfers between teams |

**Key point:** Only OASys data (cohort + LDC) uses daily freshness. All nDelius data remains live on every page view because nDelius is fast, reliable, and the data can change more frequently.

---

## 5. What Happens When OASys Is Down?

**Before this fix:** Page crashed with a 500 error. Staff couldn't view the referral.

**After this fix:**

| Scenario | What happens | What staff sees |
|----------|-------------|-----------------|
| OASys returns 503 (down) | System uses safe defaults | Page loads with "General Offence" cohort and "No LDC" |
| OASys returns 404 (no assessment) | System stores "no data found" | Page loads normally — no assessment exists for this person |
| OASys returns success | System updates cohort + LDC | Page shows fresh assessment data |

In all cases: **the page loads**. No more 500 errors.

---

## 6. When Does Fresh OASys Data Appear?

| Scenario | When data refreshes |
|----------|-------------------|
| First time anyone opens this referral | Immediately (calls OASys) |
| Same referral opened again same day | Uses stored data (fast, no call) |
| Same referral opened next day | Calls OASys again (daily refresh) |
| Clinician manually overrides cohort/LDC | Never calls OASys again (their decision is final) |
| OASys was down earlier today | Tomorrow's first view will retry |

---

## 7. Impact Summary

| Metric | Before | After |
|--------|--------|-------|
| OASys calls per week | ~200,000 | ~28,000 (once per referral per day) |
| Page crashes from OASys 503 | ~8,200/week | **0** |
| Page load time (repeat views) | Includes OASys wait | Instant (DB only) |
| Data staleness (worst case) | Always live | Up to 24 hours |

---

## 8. Technical Summary (for developers)

**Guard logic in `ReferralService.refreshPersonalDetailsForReferral()`:**

```kotlin
val latestLdc = referralLdcHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralId)
val enrichedToday = latestLdc != null && latestLdc.createdBy == "SYSTEM" && latestLdc.createdAt?.toLocalDate() == LocalDate.now()
val bothOverridden = cohortService.hasOverriddenCohort(referralId) && ldcService.hasOverriddenLdcStatus(referralId)
val shouldFetchPni = !enrichedToday && !bothOverridden
```

**Why LDC history as the signal:**
- The data-importer does NOT write LDC entries (only cohort)
- LDC is only created when OASys PNI has been called (either at referral creation or page view)
- Checking `createdAt` date gives us same-day freshness without a new DB column

**Error handling:**
- OASys 503/timeout → caught, logs warning, uses defaults (`hasLdc=false`, `cohort=GENERAL_OFFENCE`)
- OASys 404 → `pniResponse=null` → same defaults, LDC entry still written (prevents re-calling today)
- nDelius failures → not caught (nDelius is healthy at 0% failure rate; if it fails, it's a real problem)

---

*Document created: 26 May 2026*  
*Related: [APG-2307 Full Analysis](./APG-2307-ndelius-oasys-timeout-full-analysis.md)*

