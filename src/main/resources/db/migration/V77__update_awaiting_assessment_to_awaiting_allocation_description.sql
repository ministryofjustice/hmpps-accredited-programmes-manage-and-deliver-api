-- Update description for transitioning from Awaiting Assessment to Awaiting Allocation

UPDATE referral_status_transition
    SET description = 'The person cannot start the programme now but may be able to join another group soon.'
    WHERE id = '550e8400-e29b-41d4-a716-446655440020';
