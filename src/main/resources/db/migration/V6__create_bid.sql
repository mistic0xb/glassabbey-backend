CREATE TABLE bid
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    auction_id         UUID         NOT NULL REFERENCES auction (id),
    nwc_conn_id        UUID         NOT NULL REFERENCES nwc_conn (id),
    session_id         VARCHAR(255),
    idempotency_key    UUID         NOT NULL UNIQUE,
    bidder_name        VARCHAR(255) NOT NULL,
    bid_increment_sats BIGINT       NOT NULL CHECK (bid_increment_sats > 0),
    willing_amt_sats   BIGINT       NOT NULL CHECK (bid_increment_sats > 0),
    payment_hash       VARCHAR(255) UNIQUE,
    payment_request    TEXT,
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    confirmed_at       TIMESTAMPTZ,
    expires_at         TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_bid_auction_id ON bid (auction_id);
CREATE INDEX idx_bid_payment_hash ON bid (payment_hash);
CREATE INDEX idx_bid_status ON bid (status);
CREATE UNIQUE INDEX idx_one_pending_bid_per_bidder
    ON bid (auction_id, bidder_name) WHERE status = 'PENDING';