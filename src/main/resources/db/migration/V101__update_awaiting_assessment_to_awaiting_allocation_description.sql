-- Update description for transitioning from Awaiting Assessment to Awaiting Allocation

UPDATE referral_status_transition
    SET description = 'The person has been assessed as suitable and can be allocated to a group.'
    WHERE id = '550e8400-e29b-41d4-a716-446655440020';