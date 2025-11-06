CREATE TABLE referral_motivation_background_and_non_associations
(
    id                      UUID NOT NULL PRIMARY KEY,
    referral_id             UUID NOT NULL,
    maintains_innocence     BOOLEAN NOT NULL,
    motivations             TEXT NOT NULL,
    non_associations        TEXT NOT NULL,
    other_considerations    TEXT NOT NULL,
    created_by              TEXT NOT NULL,
    created_at              TIMESTAMP NOT NULL,
    last_updated_by         TEXT,
    last_updated_at         TIMESTAMP,
    CONSTRAINT fk_referral FOREIGN KEY (referral_id) REFERENCES referral (id),
    CONSTRAINT unique_referral_motivation_background_and_non_associations_referral_id UNIQUE (referral_id)
);

COMMENT ON TABLE referral_motivation_background_and_non_associations IS 'This table holds a data on motivations, background, and non-associations for a referral.';
COMMENT ON COLUMN referral_motivation_background_and_non_associations.id IS 'The unique ID of the row.';
COMMENT ON COLUMN referral_motivation_background_and_non_associations.referral_id IS 'The unique ID of a referral.';
COMMENT ON COLUMN referral_motivation_background_and_non_associations.maintains_innocence IS 'Boolean value indicating whether the referral maintains innocence.';
COMMENT ON COLUMN referral_motivation_background_and_non_associations.motivations IS 'Information on the motivation to participate in an accredited programme.';
COMMENT ON COLUMN referral_motivation_background_and_non_associations.non_associations IS 'Information on any non-associations relevant to the referral.';
COMMENT ON COLUMN referral_motivation_background_and_non_associations.referral_id IS 'Information on any other considerations relevant to the referral.';
COMMENT ON COLUMN referral_motivation_background_and_non_associations.created_by IS 'Name of the user who created this row.';
COMMENT ON COLUMN referral_motivation_background_and_non_associations.created_at IS 'Timestamp of when this row was created.';
COMMENT ON COLUMN referral_motivation_background_and_non_associations.last_updated_by IS 'Name of the user who last updated this row.';
COMMENT ON COLUMN referral_motivation_background_and_non_associations.last_updated_at IS 'Timestamp of when this row was last updated.';
