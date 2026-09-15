# SAR: "Recorded by" fields now surname-only (APG-2580)

**Date:** 15 September 2026
**Ticket:** APG-2580
**PR branch:** `APG-2580/sar-recorded-by-surname-only`
**Design doc:** [`../APG-2580-sar-recorded-by-surname-only.md`](../APG-2580-sar-recorded-by-surname-only.md)

## Summary

Two string fields in the SAR JSON now emit a **surname only** value instead of
the full name previously shipped. Compound surnames such as `van der Berg` are
preserved via a small particle whitelist (see design doc §4.1).

## Affected fields

| JSON path (relative to a referral) | Before | After |
|---|---|---|
| `programmeGroupMemberships[].attendances[].noteHistory[].recordedBy` | `"Joe Bloggs"` | `"Bloggs"` |
| `programmeGroupMemberships[].attendances[].recordedByFacilitator.personName` | `"Joe Bloggs"` | `"Bloggs"` |

For compound surnames:

| Full name | Emitted value |
|---|---|
| `John van der Berg` | `van der Berg` |
| `Maria de la Cruz`  | `de la Cruz` |
| `Ludwig von Beethoven` | `von Beethoven` |
| `Anne-Marie O'Brien` | `O'Brien` |
| `Dr Sarah Hughes` | `Hughes` |

Null / empty / whitespace-only inputs return `null` on `recordedBy`. The
`recordedByFacilitator.personName` field falls back to the stored full name if
the helper cannot derive a surname, so the non-nullable JSON field is always
populated.

## Unchanged (deliberate carve-out)

| JSON path | Reason |
|---|---|
| `programmeGroupMemberships[].attendances[].session.sessionFacilitators[].facilitator.personName` | This is the **Facilitators roster** section (list of who runs each session). Reviewer did not flag it; a roster is more informative with full names. A future PR may extend the surname transformation here if the SAR team asks — the helper and projection pattern are already in place. |

## Consumer impact

- HMPPS SAR collator template renders whatever JSON we emit — no code change needed on their side.
- No DB migration.
- No OpenAPI breaking change (`recordedBy` and `personName` remain `String?` / `String` respectively; only the value shape changes).

## References

- Design & validation: [`../APG-2580-sar-recorded-by-surname-only.md`](../APG-2580-sar-recorded-by-surname-only.md) (rev-4)
- Deferred rename follow-up: [`../APG-2580-sar-facilitator-personName-to-surname-rename-design.md`](../APG-2580-sar-facilitator-personName-to-surname-rename-design.md)
- Round-2 QAT review notes: [`../2026-07-14-SAR-custody-QAT-round2-review-notes.md`](../2026-07-14-SAR-custody-QAT-round2-review-notes.md)

