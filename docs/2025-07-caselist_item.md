# Architecture: Referral Caselist Item

This document details the software development team's thinking about solving a software architecture problem related to the Caselist view in Accredited Programmes in Community.  It is somewhere between a JIRA (which are largely made to track work, and be disposed) and a Confluence page (which can detail decisions, but often get out-dated quickly).

It introduces the problem, and provides a high-level technical overview for what a solution might look like, and the pitfalls we need to avoid.

## The Problem Context

In the Custody setting, we have seen the Referral entity become a God Object[^godobject].  Having one entity contain so many use-cases has made the code hard to modify over the long-run, and has performance implications.  We are keen to keep the design of our data entities small in the Community setting.  Hopefully this will allow our codebase to become and remain easier to change, understand, test, and use.

The AcP Community dev team are building out the "Caselist" view as one of our first journeys.  A Caselist is a list of Referrals.  Initially these will be Referrals related to a given Probation Delivery Unit (PDU), but one could also imagine a Caselist for an individual Probation Officer, or of Referrals in a specific status.  

The PDU Caselist screen contains a table of Referrals, which is sortable, searchable, filterable, and paginated.  The information presented in the table is from both local and external sources:

- Local data: this is information that the Manage & Deliver itself is concerned with, e.g. Availability or Probation Office.  Users, or possibly the system, will be able to read and modify this data entirely within the M&D web platform.
- External data: information stored outside our service's bounds.  Primarily this is data from NDelius (e.g. a Person's name and date of birth) or OASys (e.g. their risk).  This data might be readable by our users from the web service, and possibly even editable, but the data is ultimately stored outside of our system.

These functional requirements introduce non-trivial technical complexity that would make it easy to stumble into another God Object antipattern, as well as several other performance pitfalls.

## tl;dr Architecture Diagram 

![An architecture diagram showing components of the ReferralCaselistItem bounded context](./assets/ReferralCaselistItemBoundedContextArchitecture-2.drawio.png)

Each of the components of the above diagram will be explained in greater detail below.  

For context, I have provided the `.excalidraw` file from the initial architecture mobbing session on `./assets/ReferralCaselistIemBoundedContext.excalidraw` file.

## Decision: Use a separate Bounded Context for the Caselist pages

We have decided that the Caselist page should not show a list of `Referral`s, such as are stored in the `referrals` table in oru database, but rather we will create a purpose-build aggregated data entity: `ReferralCaseListItem`.

This will allow our software architecture to adhere to the Single Responsibility Principle[^spr], and build a performant, eventually consistent data structure to present.  In the parlance of Domain-Driven Design (DDD), the `ReferralCaselistItem` represents code allocated to a specific _Bounded Context_.  This approach recognises that having strongly centralised code can cause problems:

> In those younger days we were advised to build a unified model of the entire business, but DDD recognizes that we've learned that “total unification of the domain model for a large system will not be feasible or cost-effective”.  So instead DDD divides up a large system into Bounded Contexts, each of which can have a unified model - essentially a way of structuring MultipleCanonicalModels.[^boundedcontext]

That is, by recognising the difference between managing a `Referral` versus viewing the Caselist  pages, we are able to build the right tools for the problem at hand, e.g. status validation on the core `Referral` versus data filtering and sorting of the `ReferralCaselistItem`.  It means that components of our system can have fewer responsibilities, and can remain ignorant of other parts of the system.  These are well-established goals for codebase architecture.

For a more in-context example, Microsoft examine Bounded Contexts for an imagined drone delivery company [^msboundedcontext].

## Decision: Use a SQL view, not a managed table, to store the ReferralCaselistItem

Instead of storing the data for a `ReferralCaselistItem` in a managed table, we are going to access the data with a SQL View:

>  An SQL view is a virtual table derived from one or more existing database tables created based on a specific SQL query.  In simple terms, an SQL view is a stored query that creates a virtual table consisting of rows and columns when executed. Using SQL views, you can store SQL queries as templates and retrieve data from different tables as if coming from a single table.[^dbvis]

We initially made the oppose call (i.e. to use a managed table), but the idea was challenged.  Using a SQL View provides us with a greater level of simplicity to implement and build.  It brings us the benfit of _Strong Consistency_, i.e. the `ReferralCaselistItem` reflects immediate changes in the tables it references[^strongconsistency]  

It may introduce us with challenges when debugging data, and in a local development set-up.  At the moment, we suspect that the benefits outweigh the costs - and will allow us to deliver work quicker.

Developers may need to pay extra attention here when writing tests around this part of the codebase.  We have a history of over reliance on mocked/stubbed data.  We should avoid that. 

Additionally, we will need to have conversations around seeding of data into multiple tables in the Development environment, to allow e2e tests.

## Decision: Cache data from external sources, and periodically check and refresh it.

We need a strategy for keeping the `ReferralCaselistItem` up to date with externally managed data.  Unlike the change of internal data, we cannot guarantee clean, easy to work with events associated with a single change.

To do so, we will create a cached layer, which will be a set of managed postgres table (and not e.g. a Redis instance, at least for now) which contains basic information about when our system received Probation Data (e.g. NDelius, OASys) for a person, as identified by their CRN.

This cache should not contain any more information than it needs to in order to show the `ReferralCaselistItem`, and perform the basic actions required to find cached entries by a CRN.

There are three things that can cause this data to be initially fetched, shown in the Diagram below.  A user can view the Referral Details page, the Referral can be initially created by our system, or a scheduled job (i.e. a Cron Job) will periodically check for any cached data associated with non-closed Referral which hasn't been updated in a period of time, e.g. 3d.

```mermaid
flowchart TD
    CreateAReferral(A new Referral is Created)
    ViewDetails(A User loads the Referral Details page)
    CronJob(The Cron Job searches the Cache)
    FetchNDelius[Fetch fresh data from NDelius & OASys]
    StoreCache[Store the response in our cache]
    UpdateReferralCaseListItem[Update the ReferralCaselistItem]

    CreateAReferral --> FetchNDelius
    ViewDetails --> FetchNDelius
    CronJob -- The Referral is not closed && cache is stale--> FetchNDelius
    FetchNDelius --> StoreCache
    StoreCache --> UpdateReferralCaseListItem
```

Because of the need for Strong Consistency when loading the detail page (i.e. when a Referral Detail page is loaded, the data from NDelius and OASys is loaded immediately, and that fresh data is shown to the user), there's two ways that we handle the refresh of a cache:

1. Our system regularly checks for stale caches, e.g. through a Cron Job, and emits an event that indicates a batch of CRNs need to have their Probation Data refreshed.  Batching the data like this will allow for better performance of our system, by reducing the overheads of emitting and processing an event.  Another part of our system listens for those events requesting a refresh, and does so.
2. A human user views a page which triggers a forced and synchronous refresh of data, i.e. the page will not load until the fresh data has been retrieved.  We may as wel use this as a chance to refresh the cache.

Let's take a look at what happens when a Referral Details page is loaded: 

```mermaid
flowchart TD
    Start[View the Referral Details page]
    Start --> ReferralExists{Does the Referral Exist?}
    ReferralExists --> |No| NotFound[Return 404]
    ReferralExists --> |Yes| FetchNdelius[Fetch data from nDelius and OASys, by a CRN]
    FetchNdelius --> UpdateCache[Update the cached Probation Data against a CRN]
```
Although it might be possible to check and possibly refresh a cache based on events flowing downstream from NDelius and OASys, experience from other teams in MoJ, and a little of our own experience, has let us know how broad and frequent these events can be.  They can be as non-specific as e.g. "a database row changed", which would lead to us building and maintaining a system to listen to, and then filter out, the relevant changes.  This is exactly the pattern we have had to adopt in the Referral Creation workflow, where we are much more likely to receive irrelevant events (e.g. a requirement for a user to submit to a body fluid test for illegal substances) than relevant ones (i.e. a Referral to an Accredited Programme). 


[^dbvis]: https://www.dbvis.com/thetable/sql-views-a-comprehensive-guide/
[^godobject]: https://en.wikipedia.org/wiki/God_object
[^boundedcontext]: https://martinfowler.com/bliki/BoundedContext.html
[^msboundedcontext]: https://learn.microsoft.com/en-us/azure/architecture/microservices/model/domain-analysis#define-bounded-contexts
[^strongconsistency]: https://systemdesign.one/consistency-patterns/

