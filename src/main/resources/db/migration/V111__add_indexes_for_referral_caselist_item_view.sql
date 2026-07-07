CREATE INDEX IF NOT EXISTS idx_referral_status_history_referral_id_created_at ON referral_status_history (referral_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_referral_ldc_history_referral_id_created_at ON referral_ldc_history (referral_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_referral_cohort_history_referral_id_created_at ON referral_cohort_history (referral_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_programme_group_membership_referral_id_created_at ON programme_group_membership (referral_id, created_at DESC) WHERE deleted_at IS NULL;
