-- Remove transition from 'Awaiting Allocation' to 'Scheduled'

DELETE FROM referral_status_transition WHERE id = 'f6ba774b-e2db-4114-8bc5-9cf82f5cde36';