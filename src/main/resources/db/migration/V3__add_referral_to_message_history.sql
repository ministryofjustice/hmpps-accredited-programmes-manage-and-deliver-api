ALTER TABLE message_history
ADD COLUMN referral_id UUID,
ADD CONSTRAINT fk_message_history_referral FOREIGN KEY (referral_id) REFERENCES referral(id);

CREATE INDEX IF NOT EXISTS idx_message_history_referral_id ON message_history(referral_id);