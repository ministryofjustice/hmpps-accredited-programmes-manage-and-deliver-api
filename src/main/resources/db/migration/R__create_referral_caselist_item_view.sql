DROP MATERIALIZED VIEW IF EXISTS referral_caselist_item_view;

CREATE MATERIALIZED VIEW referral_caselist_item_view AS
WITH latest_status AS (SELECT referral_id,
                              referral_status_description_id,
                              ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                       FROM referral_status_history),
     latest_ldc_status AS (SELECT referral_id,
                                  has_ldc,
                                  ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                           from referral_ldc_history),
     latest_cohort AS (SELECT referral_id,
                              cohort,
                              ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                       from referral_cohort_history)

SELECT r.id,
       r.crn,
       r.person_name,
       -- Default to GENERAL_OFFENCE if there are no entries in the referral_cohort_history_table
       COALESCE(lc.cohort, 'GENERAL_OFFENCE')                 as cohort,
       r.sentence_end_date,
       r.sourced_from                                         as sentence_end_date_source,
       rsd.description_text                                   as status,
       -- Default to false if there are no entries in the referral_ldc_history_table
       COALESCE(lds.has_ldc, false)                           as has_ldc,
       -- Default values if there are no entries in the referral_reporting_location table for this referral yet
       COALESCE(rrl.pdu_name, 'UNKNOWN_PDU_NAME')             as pdu_name,
       COALESCE(rrl.reporting_team, 'UNKNOWN_REPORTING_TEAM') as reporting_team,
       COALESCE(rrl.region_name, 'UNKNOWN_REGION_NAME')       as region_name

FROM referral r
         JOIN latest_status ls ON r.id = ls.referral_id AND ls.rn = 1
         JOIN referral_status_description rsd ON ls.referral_status_description_id = rsd.id
         LEFT JOIN latest_ldc_status lds ON r.id = lds.referral_id and lds.rn = 1
         LEFT JOIN latest_cohort lc ON r.id = lc.referral_id and lc.rn = 1
         LEFT JOIN referral_reporting_location rrl on r.id = rrl.referral_id;


-- Need unique index to be able to refresh view.
CREATE UNIQUE INDEX IF NOT EXISTS idx_referral_id ON referral_caselist_item_view (id);
CREATE INDEX IF NOT EXISTS idx_referral_crn ON referral_caselist_item_view (crn);
CREATE INDEX IF NOT EXISTS idx_referral_person_name ON referral_caselist_item_view (person_name);
CREATE INDEX IF NOT EXISTS idx_referral_sentence_end_date ON referral_caselist_item_view (sentence_end_date);
CREATE INDEX IF NOT EXISTS idx_referral_status ON referral_caselist_item_view (status);
CREATE INDEX IF NOT EXISTS idx_referral_cohort ON referral_caselist_item_view (cohort);
CREATE INDEX IF NOT EXISTS idx_referral_has_ldc ON referral_caselist_item_view (has_ldc);
CREATE INDEX IF NOT EXISTS idx_referral_region_name ON referral_caselist_item_view (region_name);

CREATE OR REPLACE FUNCTION refresh_caselist_item_view()
    RETURNS TRIGGER AS
$$
BEGIN
    -- This is set to false when doing batch imports from IM so we don't try to refresh multiple thousand times at once
    IF current_setting('app.skip_mv_refresh', true) = 'true' THEN
        RETURN NEW;
    END IF;

    REFRESH MATERIALIZED VIEW CONCURRENTLY referral_caselist_item_view;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_refresh_referral_status
    AFTER INSERT OR UPDATE OR DELETE
    ON referral
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_caselist_item_view();

CREATE OR REPLACE TRIGGER trigger_refresh_referral_status_history
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_status_history
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_caselist_item_view();

CREATE OR REPLACE TRIGGER trigger_refresh_referral_ldc_history
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_ldc_history
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_caselist_item_view();

CREATE OR REPLACE TRIGGER trigger_refresh_referral_reporting_location
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_reporting_location
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_caselist_item_view();

CREATE OR REPLACE TRIGGER trigger_refresh_referral_status_description
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_status_description
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_caselist_item_view();

CREATE OR REPLACE TRIGGER trigger_refresh_referral_cohort_mapping
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_cohort_history
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_caselist_item_view();