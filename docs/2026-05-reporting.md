# Reporting Architecture Outline (May 2026)

> First draft of the document generated ahead of private beta.  Last reviewed by TJWC 2026-05-18

## Purpose

This document explains the intial strategy for delivering point-in-time (i.e. non event-driven) reporting for Manage and Deliver.

It is written for:

- Developers extending reporting features.
- LLM agents needing implementation context and intent.

## Scope and Purpose

The current structure is intentionally MVP-oriented, because the requirements for reporting were presented as non-negotiable, but were captured relatively close to the delivery of private beta.  

At time of writing, reporting requirements are still emerging or are un-tested.  Interventions Manager (IM) only allows exports of the tables it presents in its UI.  Current consumers of reporting data are therefore likely limited by what IM can offer, as well as its model of the process, which may no longer be accurate. 

Therefore the primary goals of early delivery were:

- Deliver reporting data quickly, i.e. build the features
- Deliver the data safely, i.e. using existing access-control mechanisms.
- Learn who uses reporting data, how often, and for which operational decisions.
- Validate report content and cadence before investing in richer UI/visualisation tooling.

We have intentionally de-scoped:

- A "self service" model of accessing, querying, and formatting data
- Building full in-product dashboards/charts and complex visual interfaces.

## Logistical implementation

Reporting takes the form of comma separated value (.csv) text files, accessed by the user through URLs.  These URLS will be accessed through the UI itself (i.e. users won't be interacting with the API directly), but the UI will act as a simple façade onto the endpoints. 

It is initially planned that reporting data will be accessed by internal members of the M&D team, through generation of URLs (e.g. through a manual script), and data will then be passed (e.g. via email) to our end users.

This pattern export allows early usage feedback with low delivery cost.  

## Technical implementation 

Under the hood, we are using materialised views and CSV export provide predictable, auditable outputs.  These are boring technology choices, which allow quick feature development and iteration.

We are using the standard Spring Boot components (for this service):

- `ReportingController` receives requests, validates them, and fetches the data
- `ReportingService` gathers and formats the data into e.g. CSV 
- Materialised views are SQL primiatives which gather data from other tables and present them in one place, they are performant, native primitives to postgres
- Repostitories (e.g. `ReportingGroupSizeRepository`) communciate wiht a SQL materialised view to fetch the data 

## Security

Reporting endpoints will require a dedicated reporting role (`ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_REPORTING`).  At time of writing this role has yet to be created or assigned to any users, and therefore...

The reporting endpoints are disabled by defaults, and enabled via the `reporting.endpoint` environment varaible.  This has been set to `true` in local, dev, and test environments

## Consumption Model (Near-Term)

Planned delivery method is intentionally lightweight:

- A simple `.js` script (or minimal `.html` page with inline `script`) generates links for recent reporting windows.
- Expected windows are typically the last 1 to 4 weeks.
- Users click links to retrieve CSV files directly.

## Design Principles For Follow-On Work

- Keep report endpoints explicit and versionable.
- Keep controllers thin and orchestration-focused.
- Keep CSV/data-shaping logic in services.
- Isolate each report type to its own repository and materialised view.
- Optimise for traceability and testability over UI sophistication.
- Use real usage feedback to prioritise any future visualisation layer.

