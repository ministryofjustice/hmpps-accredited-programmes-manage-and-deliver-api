ALTER TABLE referral
    -- TODO Change these to sensible defaults
    ADD COLUMN sentence_end_date DATE NULL,
    ADD COLUMN sex               TEXT NULL,
    ADD COLUMN date_of_birth     DATE NULL;

COMMENT ON COLUMN referral.sentence_end_date IS 'The end date of the person on probation''s sentence';
COMMENT ON COLUMN referral.sex IS 'The sex of the person on probation';
COMMENT ON COLUMN referral.date_of_birth IS 'The date of birth of the person on probation';

DROP MATERIALIZED VIEW referral_caselist_item_view;

CREATE MATERIALIZED VIEW referral_caselist_item_view AS
WITH latest_status AS (SELECT referral_id,
                              referral_status_description_id,
                              ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                       FROM referral_status_history),
     latest_ldc_status AS (SELECT referral_id,
                                  has_ldc,
                                  ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                           from referral_ldc_history)

SELECT r.id,
       r.crn,
       r.person_name,
       r.cohort,
       r.sex,
       r.date_of_birth,
       r.sentence_end_date,
       rsd.description_text                                   as status,
       -- Default to false if there are no entries in the referral_ldc_history_table
       COALESCE(lds.has_ldc, false)                           as has_ldc,
       -- Default values if there are no entries in the referral_reporting_location table for this referral yet
       COALESCE(rrl.pdu_name, 'UNKNOWN_PDU_NAME')             as pdu_name,
       COALESCE(rrl.reporting_team, 'UNKNOWN_REPORTING_TEAM') as reporting_team

FROM referral r
         JOIN latest_status ls ON r.id = ls.referral_id AND ls.rn = 1
         JOIN referral_status_description rsd ON ls.referral_status_description_id = rsd.id
         LEFT JOIN latest_ldc_status lds ON r.id = lds.referral_id and lds.rn = 1
         LEFT JOIN referral_reporting_location rrl on r.id = rrl.referral_id;


-- Need unique index to be able to refresh view.
CREATE UNIQUE INDEX idx_referral_id ON referral_caselist_item_view (id);
CREATE INDEX idx_referral_crn ON referral_caselist_item_view (crn);
CREATE INDEX idx_referral_person_name ON referral_caselist_item_view (person_name);
CREATE INDEX idx_referral_sex ON referral_caselist_item_view (sex);
CREATE INDEX idx_referral_date_of_birth ON referral_caselist_item_view (date_of_birth);
CREATE INDEX idx_referral_sentence_end_date ON referral_caselist_item_view (sentence_end_date);
CREATE INDEX idx_referral_status ON referral_caselist_item_view (status);
CREATE INDEX idx_referral_cohort ON referral_caselist_item_view (cohort);
CREATE INDEX idx_referral_has_ldc ON referral_caselist_item_view (has_ldc);
