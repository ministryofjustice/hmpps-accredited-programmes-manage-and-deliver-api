-- Clear the referral status history
DELETE FROM referral_status_history;

-- For each existing referral create a new status history entry with a linked referral status description of "Awaiting Assessment"
INSERT INTO referral_status_history (id, referral_status_description_id, status, created_at, created_by, start_date, end_date)
SELECT
    r.id,
    '76b2f8d8-260c-4766-a716-de9325292609',
    null,
    NOW(),
    'SYSTEM',
    NOW(),
    null
FROM referral r;
