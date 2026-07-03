DROP MATERIALIZED VIEW IF EXISTS reporting_group_size;

CREATE MATERIALIZED VIEW reporting_group_size AS
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

CREATE UNIQUE INDEX IF NOT EXISTS idx_reporting_group_size_id ON reporting_group_size (id);
CREATE INDEX IF NOT EXISTS idx_reporting_group_size_created_at ON reporting_group_size (created_at);
CREATE INDEX IF NOT EXISTS idx_reporting_group_size_earliest_possible_start_date ON reporting_group_size (earliest_possible_start_date);

CREATE OR REPLACE FUNCTION refresh_reporting_group_size_view()
    RETURNS TRIGGER AS
$$
BEGIN
    BEGIN
        REFRESH MATERIALIZED VIEW CONCURRENTLY reporting_group_size;
    EXCEPTION WHEN SQLSTATE '40P01' THEN
        -- Deadlock detected - log and continue
        -- The view will be refreshed by the next successful refresh operation
        RAISE WARNING 'Materialized view refresh deadlock detected: %', SQLERRM;
        NULL;
    END;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_refresh_programme_group_reporting_group_size
    AFTER INSERT OR UPDATE OR DELETE
    ON programme_group
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_reporting_group_size_view();

CREATE OR REPLACE TRIGGER trigger_refresh_programme_group_membership_reporting_group_size
    AFTER INSERT OR UPDATE OR DELETE
    ON programme_group_membership
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_reporting_group_size_view();

CREATE OR REPLACE TRIGGER trigger_refresh_facilitator_reporting_group_size
    AFTER INSERT OR UPDATE OR DELETE
    ON facilitator
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_reporting_group_size_view();
