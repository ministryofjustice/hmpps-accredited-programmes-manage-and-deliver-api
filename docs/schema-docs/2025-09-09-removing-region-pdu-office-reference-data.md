# Removing the Region, PDU, and Office Reference Data

> [!NOTE]
> Last updated by Wilson 2025-09-09.  Ryan F and Jack D were involved in the decision making and will have context.

## Summary

The `region`, `pdu`, and `office` table have been removed in the migration file `V20__delete_region_office_pdu_tables.sql`.

This data is now sourced from nDelius through our API integration, and therefore removing it is part of keeping our codebase and databases tidy.

## Details

The implementation of the "Delivery Location Preferences" flow initially relied on loading a set of reference data, i.e. a list of Regions, PDUs (Probation Delivery Units), and Offices from a set of static SQL tables which we (the Accredited Programmes (AcP) team) would be responsible for maintaining.

After comparing that data retrieved by the wider AcP Team against data provided by the Probation Integration Team, we discovered that the state of data in the real world would make this flow untenable. 

Below is a summary list of reasons why we decided instead to collect data from nDelius (via the Probation Integration Team's API) to display on the "Set/Update Delivery Location Preferences" flow.  But the **tl;dr - the region/pdu/office data set is managed in nDelius and is used across a huge number of domains, making for an unpredictable and a "pragmatic over perfect" data model - meaning that we can't rely on it purely to serve Accredited Programmes**

1. The relevant list of PDUs (and their Offices) is retrieved via the Licence Condition or the Requirement that initially caused the Referral to an Accredited Programme.  It is _not_ (as initially thought) retrieved thought he Person on Probation's Probation Practitioner.   The _only_ way we can access the Probation Practitioner associated with a Requirement/Licence is through nDelius.
2. Not every office in a "full" PDU will offer Accredited Programmes.  Therefore, the data is managed within nDelius by creating a duplicate PDU ot contain only the offices which offer Accredited Programmes.  While this isn't ideal from or perspective, it is the pragmatic choice made by the wider Probation system, and we should acknowledge and respect it as a realistic constraint.  By accessing the data through nDelius, we can assume that this de-duplication and filtering has benn done.
3. We initially thought that we could go from a Referral to a list of possible Delivery Locations through a Person on Probation's Probation Practitioner.  However, we have learned that we are better off sourcing the list through the `manager` of the Licence Condition or Requirement that initially triggers the creation of a Referral to an Accredited Programme in Community.  When we fetch this data, the Probation Integration Team are also able to return us a list of PDUs and Offices for that member of staff, making the sourcing of data simple and reliable.

