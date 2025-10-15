CREATE TABLE IF NOT EXISTS referral (
    id UUID NOT NULL PRIMARY KEY,
    person_name TEXT,
    crn TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS referral_status_history (
    id UUID NOT NULL PRIMARY KEY,
    status TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT,
    start_date TIMESTAMP,
    end_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS referral_status_history_mapping (
    id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    referral_id UUID NOT NULL REFERENCES referral(id),
    referral_status_history_id UUID NOT NULL REFERENCES referral_status_history(id),
    CONSTRAINT unique_referral_status_history_mapping UNIQUE (referral_id, referral_status_history_id)
);

CREATE INDEX IF NOT EXISTS idx_referral_status_history_mapping_referral_id ON referral_status_history_mapping(referral_id);
CREATE INDEX IF NOT EXISTS idx_referral_status_history_mapping_history_id ON referral_status_history_mapping(referral_status_history_id);