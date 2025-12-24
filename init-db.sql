-- Database initialization script for Docker
-- This runs automatically when the PostgreSQL container starts for the first time

-- Create schema
CREATE SCHEMA IF NOT EXISTS tedtalks;

-- Grant privileges to the user
GRANT ALL PRIVILEGES ON SCHEMA tedtalks TO tedtalks_user;
GRANT ALL ON ALL TABLES IN SCHEMA tedtalks TO tedtalks_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA tedtalks TO tedtalks_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA tedtalks GRANT ALL ON TABLES TO tedtalks_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA tedtalks GRANT ALL ON SEQUENCES TO tedtalks_user;

-- Set search path
ALTER DATABASE tedtalks_db SET search_path TO tedtalks, public;

