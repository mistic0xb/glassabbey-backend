CREATE TABLE gallery
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    creator_id   UUID         NOT NULL REFERENCES creator (id),
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    end_at       TIMESTAMPTZ,
    published_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_gallery_creator_id ON gallery (creator_id);
CREATE INDEX idx_gallery_status ON gallery (status);