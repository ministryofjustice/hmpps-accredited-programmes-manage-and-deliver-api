CREATE TABLE message_history (
    id UUID NOT NULL PRIMARY KEY,
    event_type TEXT NOT NULL,
    detail_url TEXT NOT NULL,
    description TEXT,
    occurred_at TIMESTAMP NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
