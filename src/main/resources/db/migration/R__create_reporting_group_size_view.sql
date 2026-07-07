DROP VIEW IF EXISTS reporting_group_size;

CREATE VIEW reporting_group_size AS
WITH active_group_memberships AS (
    SELECT programme_group_id,
           COUNT(*)::INTEGER AS group_size
    FROM programme_group_membership
    WHERE deleted_at IS NULL
    GROUP BY programme_group_id
)
SELECT pg.id,
       pg.code,
       pg.created_at,
       pg.sex,
       pg.cohort,
       pg.is_ldc,
       pg.earliest_possible_start_date,
       pg.region_name,
       pg.probation_delivery_unit_code    AS pdu_code,
       pg.probation_delivery_unit_name    AS pdu_name,
       pg.delivery_location_code          AS location_code,
       pg.delivery_location_name          AS location_name,
       COALESCE(agm.group_size, 0)        AS group_size,
       facilitator.ndelius_person_code    AS facilitator_staff_code
FROM programme_group pg
         LEFT JOIN active_group_memberships agm ON pg.id = agm.programme_group_id
         LEFT JOIN facilitator ON pg.treatment_manager_id = facilitator.id
WHERE pg.deleted_at IS NULL;
