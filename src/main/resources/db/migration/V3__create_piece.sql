CREATE TABLE piece
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    gallery_id      UUID         NOT NULL REFERENCES gallery (id),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    img_url         TEXT,
    base_price_sats BIGINT       NOT NULL DEFAULT 0 CHECK ( base_price_sats >= 0 and base_price_sats <= 100000000),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_piece_gallery_id ON piece (gallery_id);