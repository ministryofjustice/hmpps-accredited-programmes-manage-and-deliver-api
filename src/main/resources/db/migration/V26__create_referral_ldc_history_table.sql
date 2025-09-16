CREATE TABLE referral_ldc_history
(
    id          UUID PRIMARY KEY UNIQUE             NOT NULL,
    referral_id UUID                                NOT NULL references referral (id),
    has_ldc     BOOLEAN   DEFAULT FALSE             NOT NULL,
    created_by  TEXT      DEFAULT current_user      NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE referral_ldc_history IS 'Contains a history of the changes to the LDC (Learning Difficulties and Challenges) for a referral.';
COMMENT ON COLUMN referral_ldc_history.id IS 'The unique identifier for a referral_ldc_history entry.';
COMMENT ON COLUMN referral_ldc_history.referral_id IS 'Foreign key relationship to the referral table.';
COMMENT ON COLUMN referral_ldc_history.has_ldc IS 'If a referral has Learning Difficulties or Challenges.';
COMMENT ON COLUMN referral_ldc_history.created_by IS 'Username of the person/system that changed the LDC status of the referral';
COMMENT ON COLUMN referral_ldc_history.created_at IS 'Timestamp of when the LDC status was changed for a referral.';

CREATE INDEX idx_referral_ldc_history_referral_id ON referral_ldc_history (referral_id);
CREATE INDEX idx_referral_ldc_history_created_by ON referral_ldc_history (created_by);