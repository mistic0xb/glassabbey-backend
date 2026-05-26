CREATE TABLE creator
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    pubkey     VARCHAR(64) NOT NULL UNIQUE,
    name       VARCHAR(255),
    picture    TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);