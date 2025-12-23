-- Test Schema for H2 Database

CREATE SCHEMA IF NOT EXISTS tedtalks;

SET SCHEMA tedtalks;

-- TED_TALKS TABLE (simplified for H2 compatibility)
CREATE TABLE IF NOT EXISTS ted_talks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    talk_year INT NOT NULL,
    talk_month INT NOT NULL,
    views BIGINT DEFAULT 0,
    likes BIGINT DEFAULT 0,
    link VARCHAR(500),
    influence_score DOUBLE DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(title, author)
);

-- SPEAKERS TABLE
CREATE TABLE IF NOT EXISTS speakers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_talks_year ON ted_talks(talk_year);
CREATE INDEX IF NOT EXISTS idx_talks_author ON ted_talks(author);
CREATE INDEX IF NOT EXISTS idx_talks_influence ON ted_talks(influence_score);

