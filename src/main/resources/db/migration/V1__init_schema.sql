-- PostgreSQL Database Schema for TED Talks Analyzer
-- Simplified schema with only essential objects

-- 1. SPEAKERS TABLE (Normalized)
CREATE TABLE IF NOT EXISTS speakers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    bio TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. TED_TALKS TABLE
CREATE TABLE IF NOT EXISTS ted_talks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(200) NOT NULL,
    date DATE NOT NULL,
    talk_year INTEGER NOT NULL,
    talk_month INTEGER NOT NULL,
    views BIGINT NOT NULL CHECK (views >= 0),
    likes BIGINT NOT NULL CHECK (likes >= 0),
    link TEXT NOT NULL,
    influence_score DOUBLE PRECISION GENERATED ALWAYS AS
        (views * 0.7 + likes * 0.3) STORED,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_title_author UNIQUE (title, author)
);

-- 3. Performance indexes
CREATE INDEX IF NOT EXISTS idx_ted_talks_author ON ted_talks(author);
CREATE INDEX IF NOT EXISTS idx_ted_talks_year ON ted_talks(talk_year);
CREATE INDEX IF NOT EXISTS idx_ted_talks_year_month ON ted_talks(talk_year, talk_month);
CREATE INDEX IF NOT EXISTS idx_ted_talks_influence ON ted_talks(influence_score DESC);
CREATE INDEX IF NOT EXISTS idx_ted_talks_year_influence ON ted_talks(talk_year, influence_score DESC);
CREATE INDEX IF NOT EXISTS idx_ted_talks_date ON ted_talks(date);
CREATE INDEX IF NOT EXISTS idx_speakers_name ON speakers(name);

-- 4. Function for automatic timestamp updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 5. Triggers for updated_at columns
CREATE TRIGGER update_speakers_updated_at
    BEFORE UPDATE ON speakers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ted_talks_updated_at
    BEFORE UPDATE ON ted_talks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 6. Comments for documentation
COMMENT ON TABLE ted_talks IS 'TED Talks with denormalized speaker name for JDBC compatibility';
COMMENT ON TABLE speakers IS 'Speaker master data';
COMMENT ON COLUMN ted_talks.influence_score IS 'Calculated as: views * 0.7 + likes * 0.3';

