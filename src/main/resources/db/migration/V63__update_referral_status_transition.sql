-- Remove transition from 'Scheduled' to 'On Programme'

DELETE FROM referral_status_transition WHERE id = '071a3a52-02f1-4c71-a6ca-77594a2793ea';

-- Remove transition from 'On Programme' to 'Programme Complete'

DELETE FROM referral_status_transition WHERE id = 'f3c0b0ec-3b6d-4daa-b863-e447490a36ad';