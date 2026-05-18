CREATE TABLE referral_cohort_history
(
    id          UUID PRIMARY KEY UNIQUE             NOT NULL,
    referral_id UUID                                NOT NULL references referral (id),
    cohort      TEXT      DEFAULT 'GENERAL_OFFENCE' NOT NULL,
    created_by  TEXT      DEFAULT current_user      NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE referral_cohort_history IS 'Table containing the history of any cohort updates by a user';
COMMENT ON COLUMN referral_cohort_history.id IS 'Unique id for the cohort history';
COMMENT ON COLUMN referral_cohort_history.referral_id IS 'The Id associated with the referral';
COMMENT ON COLUMN referral_cohort_history.created_by IS 'The username of the user which updated the cohort';
COMMENT ON COLUMN referral_cohort_history.created_at IS 'Timestamp of when the cohort was updated';

-- Drop dependent views so we can drop cohort from referral table
DROP MATERIALIZED VIEW referral_caselist_item_view;
DROP MATERIALIZED VIEW group_waitlist_item_view;

ALTER TABLE referral
    DROP COLUMN cohort CASCADE;