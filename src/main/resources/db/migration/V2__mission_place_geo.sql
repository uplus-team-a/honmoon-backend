-- Enable pgvector extension if not exists
CREATE EXTENSION IF NOT EXISTS vector;

-- Add latitude/longitude columns to mission_place
ALTER TABLE IF EXISTS mission_place
    ADD COLUMN IF NOT EXISTS latitude double precision,
    ADD COLUMN IF NOT EXISTS longitude double precision;

-- Optional: maintain a 2D vector column for approximate nearest neighbor (lat,lng)
ALTER TABLE IF EXISTS mission_place
    ADD COLUMN IF NOT EXISTS location_vec vector(2);

-- Backfill existing rows: set location_vec from latitude/longitude when available
UPDATE mission_place
SET location_vec = ARRAY[COALESCE(latitude, 0)::float4, COALESCE(longitude, 0)::float4]::vector
WHERE location_vec IS NULL;

-- Create index for fast KNN search on vector
CREATE INDEX IF NOT EXISTS idx_mission_place_location_vec_l2
    ON mission_place USING ivfflat (location_vec vector_l2_ops)
    WITH (lists = 100);

-- Also create a btree index on (latitude, longitude) for fallback
CREATE INDEX IF NOT EXISTS idx_mission_place_lat_lng
    ON mission_place (latitude, longitude); 