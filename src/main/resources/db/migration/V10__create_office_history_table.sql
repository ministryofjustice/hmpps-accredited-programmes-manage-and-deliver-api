CREATE TABLE office_history
(
    id              UUID PRIMARY KEY UNIQUE       NOT NULL,
    referral_id UUID NOT NULL REFERENCES referral(id),
    office_name     contact_email   text  NOT NULL,
    created_at      timestamp with time zone not null,
    created_by_user text not null,
    deleted_at      text,
);
