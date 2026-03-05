update session_facilitator
set facilitator_type = 'REGULAR_FACILITATOR'
where facilitator_type = 'LEAD_FACILITATOR';

update programme_group_facilitator
set facilitator_type = 'REGULAR_FACILITATOR'
where facilitator_type = 'LEAD_FACILITATOR';
