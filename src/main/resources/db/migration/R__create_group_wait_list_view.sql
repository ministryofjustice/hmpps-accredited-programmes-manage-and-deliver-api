DROP MATERIALIZED VIEW IF EXISTS group_waitlist_item_view;

CREATE MATERIALIZED VIEW group_waitlist_item_view AS
WITH latest_status AS (SELECT referral_id,
                              referral_status_description_id,
                              ROW_NUMBER()
                              OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                       FROM referral_status_history),
     latest_ldc_status AS (SELECT referral_id,
                                  has_ldc,
                                  ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                           from referral_ldc_history),
     active_group_membership AS (SELECT referral_id,
                                        programme_group_id,
                                        ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                                 from programme_group_membership
                                 WHERE deleted_at IS null)

SELECT r.id                                                   as referral_id,
       r.crn,
       r.person_name,
       r.sentence_end_date,
       r.sourced_from,
       r.cohort,
       -- Default to false if there are no entries in the referral_ldc_history_table
       COALESCE(lds.has_ldc, false)                           as has_ldc,
       r.date_of_birth,
       r.sex,
       rsd.description_text                                   as status,
       -- Default values if there are no entries in the referral_reporting_location table for this referral yet
       COALESCE(rrl.pdu_name, 'UNKNOWN_PDU_NAME')             as pdu_name,
       COALESCE(rrl.reporting_team, 'UNKNOWN_REPORTING_TEAM') as reporting_team,
       pgm.programme_group_id                                 as active_programme_group_id

FROM referral r
         JOIN latest_status ls ON r.id = ls.referral_id AND ls.rn = 1
         JOIN referral_status_description rsd ON ls.referral_status_description_id = rsd.id
         LEFT JOIN latest_ldc_status lds ON r.id = lds.referral_id and lds.rn = 1
         LEFT JOIN referral_reporting_location rrl on r.id = rrl.referral_id
         LEFT JOIN active_group_membership pgm on r.id = pgm.referral_id;

-- Need unique index to be able to refresh view.
CREATE UNIQUE INDEX IF NOT EXISTS idx_group_wait_list_id ON group_waitlist_item_view (referral_id);
CREATE INDEX IF NOT EXISTS idx_group_wait_list_crn ON group_waitlist_item_view (crn);
CREATE INDEX IF NOT EXISTS idx_group_wait_list_person_name ON group_waitlist_item_view (person_name);
CREATE INDEX IF NOT EXISTS idx_group_wait_list_sentence_end_date ON group_waitlist_item_view (sentence_end_date);
CREATE INDEX IF NOT EXISTS idx_group_wait_list_sourced_from ON group_waitlist_item_view (sourced_from);
CREATE INDEX IF NOT EXISTS idx_group_wait_list_cohort ON group_waitlist_item_view (cohort);
CREATE INDEX IF NOT EXISTS idx_group_wait_list_has_ldc ON group_waitlist_item_view (has_ldc);
CREATE INDEX IF NOT EXISTS idx_group_wait_list_date_of_birth ON group_waitlist_item_view (date_of_birth);
CREATE INDEX IF NOT EXISTS idx_group_wait_list_sex ON group_waitlist_item_view (sex);
CREATE INDEX IF NOT EXISTS idx_group_wait_list_status ON group_waitlist_item_view (status);
CREATE INDEX IF NOT EXISTS idx_group_wait_list_pdu_name ON group_waitlist_item_view (pdu_name);
CREATE INDEX IF NOT EXISTS idx_group_wait_list_reporting_team ON group_waitlist_item_view (reporting_team);
CREATE INDEX IF NOT EXISTS idx_group_wait_list_latest_group ON group_waitlist_item_view (active_programme_group_id);


CREATE OR REPLACE FUNCTION refresh_group_wait_list_item_view()
    RETURNS TRIGGER AS
$$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY group_waitlist_item_view;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_refresh_referral_status_group_wait_list
    AFTER INSERT OR UPDATE OR DELETE
    ON referral
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_group_wait_list_item_view();

CREATE OR REPLACE TRIGGER trigger_refresh_referral_status_history_group_wait_list
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_status_history
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_group_wait_list_item_view();

CREATE OR REPLACE TRIGGER trigger_refresh_referral_ldc_history_group_wait_list
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_ldc_history
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_group_wait_list_item_view();

CREATE OR REPLACE TRIGGER trigger_refresh_referral_reporting_location_group_wait_list
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_reporting_location
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_group_wait_list_item_view();

CREATE OR REPLACE TRIGGER trigger_refresh_referral_status_description_group_wait_list
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_status_description
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_group_wait_list_item_view();

CREATE OR REPLACE TRIGGER trigger_refresh_programme_group_membership_group_wait_list
    AFTER INSERT OR UPDATE OR DELETE
    ON programme_group_membership
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_group_wait_list_item_view();



