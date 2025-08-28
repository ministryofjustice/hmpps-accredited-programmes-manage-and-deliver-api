# Geographic Reference Data Implementation

This work was done by wilson <thomaswilsonxyz>, for [APG-1213](https://dsdmoj.atlassian.net/browse/APG-1213), using reference data [provided by the BAs](https://justiceuk-my.sharepoint.com/:x:/g/personal/lauren_darby1_justice_gov_uk/EW62LeWVZE5NnXnLLSrNpswBmmNe9rNHkzFBR02RMjXxpw?e=EWZkRE) as the basis for work.

## Data Model Hierarchy

The geographic reference data follows a three-tier hierarchy:

1. **Region** - Large geographic areas (e.g., "North East", "South West")
2. **Probation Delivery Unit (PDU)** - Geographic areas within a region (e.g., "Bristol PDU", "East Kent")
3. **Office** - Individual probation offices within a PDU (e.g., "Derby: Derwent Centre")

## Data Model Relationships

- A **Region** can contain many **PDUs** (one-to-many)
- A **PDU** belongs to exactly one **Region** (many-to-one)
- A **PDU** can contain many **Offices** (one-to-many)
- An **Office** belongs to exactly one **PDU** (many-to-one)
- An **Office** indirectly belongs to exactly one **Region** through its PDU

## Soft Delete Implementation

All tables implement soft deletion using the `deleted_at` timestamp field:
- **Active records**: `deleted_at IS NULL`
- **Deleted records**: `deleted_at IS NOT NULL`

Repository methods are implemented to exclude soft-deleted records by default, with explicit methods for accessing all records when needed.

## Data Loading Strategy

Data is loaded via Flyway migration scripts:

1. **V14__create_geographic_reference_tables.sql** - Creates table structure
2. **V15__load_region_data.sql** - Loads region reference data
3. **V16__load_pdu_data.sql** - ... PDUs  
4. **V17__load_office_data.sql** - ... Offices

## Key Design Decisions

1. Used SQL loading migration directly.  Previous experience loading in and managing .csv files has taught us this is just too complex.
2. The tables use business identifiers as primary keys, rather than generated UUIDs.  This helps reduce the amount of indirection between what was present in the BAs' CSV files and what's in the database. 
3. Soft delete (i.e. `deleted_at`) as opposed to deleting rows allows for a more flexible, de-buggable future. 
4. Data loading errors are logged but don't prevent application startup.  We've seen several errors like this in the future prevent start-up of the application, and create crash loops.  So I have designed around this. 


