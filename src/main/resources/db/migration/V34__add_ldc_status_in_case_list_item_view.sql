DROP MATERIALIZED VIEW referral_caselist_item_view;

CREATE MATERIALIZED VIEW referral_caselist_item_view AS
WITH latest_status AS (SELECT referral_id,
                              referral_status_description_id,
                              ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY start_date DESC) as rn
                       FROM referral_status_history),
     latest_ldc_status AS (SELECT referral_id,
                                  has_ldc,
                                  ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                           from referral_ldc_history)

SELECT r.id,
       r.crn,
       r.person_name,
       r.cohort,
       rsd.description_text         as status,
       -- Default to false if there are no entries in the referral_ldc_history_table
       COALESCE(lds.has_ldc, false) as has_ldc

FROM referral r
         JOIN latest_status ls ON r.id = ls.referral_id AND ls.rn = 1
         JOIN referral_status_description rsd ON ls.referral_status_description_id = rsd.id
         LEFT JOIN latest_ldc_status lds ON r.id = lds.referral_id and lds.rn = 1;


-- Need unique index to be able to refresh view.
CREATE UNIQUE INDEX idx_referral_id ON referral_caselist_item_view (id);
CREATE INDEX idx_referral_crn ON referral_caselist_item_view (crn);
CREATE INDEX idx_referral_person_name ON referral_caselist_item_view (person_name);
CREATE INDEX idx_referral_status ON referral_caselist_item_view (status);

CREATE TRIGGER trigger_refresh_referral_ldc_history
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_ldc_history
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_caselist_item_view();