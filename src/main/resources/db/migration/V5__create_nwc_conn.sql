CREATE TABLE nwc_conn
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    creator_id    UUID         NOT NULL REFERENCES creator (id),
    wallet_name   VARCHAR(255) NOT NULL,
    encrypted_nwc TEXT         NOT NULL,
    is_primary    BOOLEAN      NOT NULL DEFAULT false,
    is_active     BOOLEAN      NOT NULL DEFAULT true,
    last_used_at  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_nwc_conn_creator_id ON nwc_conn (creator_id);