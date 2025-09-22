DELETE FROM referral_status_history;

INSERT INTO referral_status_history (id, created_at, created_by, start_date, referral_status_description_id, referral_id)
SELECT
    gen_random_uuid(),
    NOW(),
    'SYSTEM',
    NOW(),
    (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
    r.id
FROM referral r;
