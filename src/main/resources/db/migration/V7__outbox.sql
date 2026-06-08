CREATE TABLE outbox
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    event_type   VARCHAR(50) NOT NULL,
    payload      JSONB       NOT NULL,
    processed    BOOLEAN     NOT NULL DEFAULT false,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unprocessed ON outbox (created_at) WHERE processed = false;