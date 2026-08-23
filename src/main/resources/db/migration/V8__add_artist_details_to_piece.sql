ALTER TABLE piece
    ADD COLUMN artist_name VARCHAR(255) NOT NULL DEFAULT 'Unknown Artist',
    ADD COLUMN artist_profile TEXT,
    ADD COLUMN medium VARCHAR(100),
    ADD COLUMN dimensions VARCHAR(100);

CREATE INDEX idx_piece_artist_name ON piece (artist_name);