CREATE TABLE auction
(
    id                  UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    piece_id            UUID        NOT NULL UNIQUE REFERENCES piece (id),
    gallery_id          UUID        NOT NULL REFERENCES gallery (id),
    base_price_sats     BIGINT      NOT NULL DEFAULT 0 CHECK ( base_price_sats >= 0 and base_price_sats <= 100000000),
    current_price_sats  BIGINT      NOT NULL DEFAULT 0 CHECK ( base_price_sats >= 0),
    submission_fee_sats BIGINT      NOT NULL CHECK ( base_price_sats >= 0 and base_price_sats <= 100000000),
    status              VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK ( status IN ('OPEN', 'CLOSED') ),
    version             INT         NOT NULL DEFAULT 0,
    closed_at           TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_auction_piece_id ON auction (piece_id);
CREATE INDEX idx_auction_gallery_id ON auction (gallery_id);
CREATE INDEX idx_auction_status ON auction (status);