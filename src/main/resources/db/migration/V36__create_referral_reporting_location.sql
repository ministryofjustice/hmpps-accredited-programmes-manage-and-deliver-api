CREATE TABLE referral_reporting_location
(
    id             UUID PRIMARY KEY NOT NULL,
    referral_id    UUID UNIQUE      NOT NULL references referral (id),
    pdu_name       TEXT             NOT NULL DEFAULT 'UNKNOWN_PDU_NAME',
    reporting_team TEXT             NOT NULL DEFAULT 'UNKNOWN_REPORTING_TEAM'
);

COMMENT ON TABLE referral_reporting_location IS 'Contains the reporting location data associated with a referral.';
COMMENT ON COLUMN referral_reporting_location.referral_id IS 'Foreign key relationship to the referral table.';
COMMENT ON COLUMN referral_reporting_location.pdu_name IS 'The name of the PDU associated with the referral.';
COMMENT ON COLUMN referral_reporting_location.reporting_team IS 'The name of the reporting team associated with the referral.';

CREATE INDEX idx_referral_reporting_location_pdu_name ON referral_reporting_location (pdu_name);
CREATE INDEX idx_referral_reporting_location_reporting_team ON referral_reporting_location (reporting_team);


-- Recreate view and add PDU and Reporting team
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
CREATE INDEX idx_referral_status ON referral_caselist_item_view (status);
CREATE INDEX idx_referral_cohort ON referral_caselist_item_view (cohort);
CREATE INDEX idx_referral_has_ldc ON referral_caselist_item_view (has_ldc);

CREATE TRIGGER trigger_refresh_referral_reporting_location
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_reporting_location
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_caselist_item_view();