# Bug Investigation: Regional Filtering — Group Allocations & Waitlist

**Ticket:** APG (bug) — Group Allocations and Waitlist displaying referrals from outside the logged-in user's region
**Environment:** Pre-prod
**Status:** Root cause traced to API / data importer — UI is clear
**Date:** 2026-05-20

---

## Summary

An East Midlands programme team member can see referrals from other regions (e.g. Greater Manchester) on the Group Allocations and Waitlist pages. This is a data access control issue.

---

## UI Investigation — Findings

### Data flow (UI → API)

```
groupOverviewController.ts
  └─ showGroupOverviewWaitlist() / showGroupOverviewAllocated()
       └─ accreditedProgrammesManageAndDeliverService.getGroupWaitlistMembers()
       └─ accreditedProgrammesManageAndDeliverService.getGroupAllocatedMembers()
            └─ RestClient.get()
                 └─ GET /bff/group/{groupId}/WAITLIST
                 └─ GET /bff/group/{groupId}/ALLOCATED
```

**Relevant files:**
- `server/groupOverview/groupOverviewController.ts` — lines 78–129 (waitlist), 22–76 (allocations)
- `server/services/accreditedProgrammesManageAndDeliverService.ts` — lines 176–203
- `server/data/restClient.ts` — line 66 (token type)

### Key finding: System client token, not user token

The UI authenticates API calls using a **system client token**:

```typescript
// accreditedProgrammesManageAndDeliverService.ts:99-107
const hmppsAuthClient = this.hmppsAuthClientBuilder()
const systemToken = await hmppsAuthClient.getSystemClientToken(username)
```

The `username` is passed as context, but the system token does **not** carry the user's regional claims from hmpps-auth/Delius. Region resolution must happen server-side in the API by looking up the user's region from their username.

### UI verdict: No code changes required

The UI passes **no regional filter** to the API — this is correct. Regional access control is the API's responsibility. The UI renders exactly what the API returns.

There is **no client-side regional filtering code** anywhere in the UI for these endpoints, and there should not be.

---

## API Investigation — Where to look

### Endpoint in scope

```
GET /bff/group/{groupId}/WAITLIST
GET /bff/group/{groupId}/ALLOCATED
```

Query params forwarded from UI (pagination + filter):
```typescript
{ page, size, ...filterParams }
```

No `regionCode` or user-region header is sent by the UI — the API must derive the user's region from the authenticated principal.

### Likely causes

#### Cause 1 — API does not enforce regional access control on these endpoints

The BFF group allocation endpoints may return all referrals for the group without checking whether the calling user belongs to that group's region.

**Check:**
- Does the handler for `GET /bff/group/{groupId}/ALLOCATED|WAITLIST` resolve the current user's region?
- Is the query scoped to only return referrals where `regionCode` matches the user's region?
- Is there a missing `@PreAuthorize` / security filter / service-layer region check?

#### Cause 2 — Group itself is not region-scoped

A group belonging to East Midlands may have referrals from other regions attached to it (possibly due to incorrect data import), meaning even a correctly secured endpoint would return cross-region data if the underlying group membership is wrong.

**Check:**
- Is there a region constraint on group membership (i.e. can a referral from Greater Manchester be a member of an East Midlands group)?
- If so, is that constraint enforced at allocation time (`POST /group/{groupId}/allocate/{referralId}`)?

---

## Data Importer Investigation — If the bug travels here

See companion file: [`bug-investigation-regional-filtering-data-importer.md`](./bug-investigation-regional-filtering-data-importer.md)

Potential data importer cause: referrals imported without correct `regionCode`, or group membership created without region validation, meaning the data at rest is miscategorised before the API even queries it.

---

## Acceptance Criteria (from ticket)

> Given a logged-in user belongs to the East Midlands region
> When they access Group Allocations and Waitlist
> Then only East Midlands referrals are displayed.

> Given referrals exist for other regions
> When a regional user accesses the waitlist
> Then referrals outside their assigned region are excluded.

> Given the waitlist is displayed
> When referral details are shown
> Then PDU and Reporting Team values only relate to the logged-in user's region.

---

## Reproduction

1. Log into pre-prod as an East Midlands programme team member
2. Navigate to Groups → Allocations and Waitlist
3. Observe Greater Manchester referrals and PDUs visible in the waitlist tab

---

## Notes

- Pre-prod data configuration may also be a contributing factor (dirty/miscategorised seed data)
- This issue does not exist in the UI codebase — no UI fix is expected
- Once root cause confirmed in API, check whether data importer needs a fix to reprocess/retag existing referral-group memberships

