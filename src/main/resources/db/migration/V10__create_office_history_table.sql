CREATE TABLE IF NOT EXISTS office_history
(
    id              UUID PRIMARY KEY UNIQUE NOT NULL,
    referral_id     UUID NOT NULL REFERENCES referral(id),
    office_name     TEXT  NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by_user TEXT NOT NULL,
    deleted_at      TEXT
);
