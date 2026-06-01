-- Drop the triggers on referral table to prevent them firing during UPDATE
-- The repeatable migrations will recreate them after recreating the views
DROP TRIGGER IF EXISTS trigger_refresh_referral_status ON referral;
DROP TRIGGER IF EXISTS trigger_refresh_referral_status_group_wait_list ON referral;

ALTER TABLE referral
    ADD COLUMN created_by TEXT DEFAULT 'Accredited Programmes automated update';

UPDATE referral SET created_by = 'Accredited Programmes automated update' WHERE created_by IS NULL;
