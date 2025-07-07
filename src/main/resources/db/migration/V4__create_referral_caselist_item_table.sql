CREATE TABLE referral_caselist_item (
    referral_id UUID NOT NULL references referral(id),
    last_updated_at TIMESTAMP NOT NULL,
    crn TEXT NOT NULL,
    person_name TEXT NOT NULL,
    probation_office TEXT NOT NULL,
    sentence_end_date TEXT NOT NULL,
    pss_end_date TEXT NULL,
    cohort TEXT NOT NULL,
    referral_status TEXT NOT NULL
);

CREATE UNIQUE INDEX idx_referral_id ON referral_caselist_item(referral_id);
CREATE INDEX idx_crn ON referral_caselist_item(crn);
CREATE INDEX idx_person_name ON referral_caselist_item(person_name);
CREATE INDEX idx_probation_office ON referral_caselist_item(probation_office);
CREATE INDEX idx_sentence_end_date ON referral_caselist_item(sentence_end_date);
CREATE INDEX idx_pss_end_date ON referral_caselist_item(pss_end_date);
CREATE INDEX idx_cohort ON referral_caselist_item(cohort);
CREATE INDEX idx_referral_status ON referral_caselist_item(referral_status);

COMMENT ON TABLE referral_caselist_item IS 'This table holds a data required to display the case list view so we do not need to fetch on every load.';
COMMENT ON COLUMN referral_caselist_item.referral_id IS 'The unique ID of a referral.';
COMMENT ON COLUMN referral_caselist_item.last_updated_at IS 'Timestamp of when this row was last updated.';
COMMENT ON COLUMN referral_caselist_item.crn IS 'The CRN of the individual being referred.';
COMMENT ON COLUMN referral_caselist_item.person_name IS 'The Name of the individual being referred.';
COMMENT ON COLUMN referral_caselist_item.probation_office IS 'The Probation office the referral is asscociated with.';
COMMENT ON COLUMN referral_caselist_item.sentence_end_date IS 'The end date of the individuals sentence';
COMMENT ON COLUMN referral_caselist_item.pss_end_date IS 'The Post Sentence Supervision end date (if applicable) for the referral.';
COMMENT ON COLUMN referral_caselist_item.cohort IS 'The cohort that the referral is part of.';
COMMENT ON COLUMN referral_caselist_item.referral_status IS 'The current status of the referral.';

