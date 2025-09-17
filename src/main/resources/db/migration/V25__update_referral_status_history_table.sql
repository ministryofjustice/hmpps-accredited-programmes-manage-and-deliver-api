ALTER TABLE referral_status_history 
ADD COLUMN referral_status_description_id UUID REFERENCES referral_status_description (id);

CREATE INDEX idx_referral_status_history_description_id ON referral_status_history (referral_status_description_id);

COMMENT ON COLUMN referral_status_history.referral_status_description_id IS 'References the status description for this history entry';