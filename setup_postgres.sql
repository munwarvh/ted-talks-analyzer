-- PostgreSQL Database Setup Script for TED Talks Analyzer
-- Run this script as a PostgreSQL superuser (e.g., postgres or your admin user)
--
-- Execute with: psql -U <your_admin_user> -f setup_postgres.sql
-- Or from psql prompt: \i setup_postgres.sql

-- 1. Create the database
DROP DATABASE IF EXISTS tedtalks;
CREATE DATABASE tedtalks
    WITH
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

-- 2. Create the user/role
DROP USER IF EXISTS tedtalks_user;
CREATE USER tedtalks_user WITH PASSWORD 'tedtalks_password';

-- 3. Grant privileges on database
GRANT ALL PRIVILEGES ON DATABASE tedtalks TO tedtalks_user;
GRANT CONNECT ON DATABASE tedtalks TO tedtalks_user;

-- 4. Connect to the database and create dedicated schema
\c tedtalks

-- Create dedicated schema for the application
DROP SCHEMA IF EXISTS tedtalks CASCADE;
CREATE SCHEMA tedtalks AUTHORIZATION tedtalks_user;

-- Grant all privileges on the schema
GRANT ALL ON SCHEMA tedtalks TO tedtalks_user;
GRANT CREATE ON SCHEMA tedtalks TO tedtalks_user;

-- Set the search path for the user
ALTER USER tedtalks_user SET search_path TO tedtalks, public;

-- Grant privileges on all tables, sequences, and functions (existing and future)
ALTER DEFAULT PRIVILEGES FOR USER tedtalks_user IN SCHEMA tedtalks
    GRANT ALL ON TABLES TO tedtalks_user;

ALTER DEFAULT PRIVILEGES FOR USER tedtalks_user IN SCHEMA tedtalks
    GRANT ALL ON SEQUENCES TO tedtalks_user;

ALTER DEFAULT PRIVILEGES FOR USER tedtalks_user IN SCHEMA tedtalks
    GRANT ALL ON FUNCTIONS TO tedtalks_user;

-- Also grant minimal public schema access (for extensions if needed)
GRANT USAGE ON SCHEMA public TO tedtalks_user;

-- Display connection info
\echo '============================================'
\echo 'PostgreSQL Database Setup Complete!'
\echo '============================================'
\echo 'Database: tedtalks'
\echo 'Schema: tedtalks'
\echo 'User: tedtalks_user'
\echo 'Password: tedtalks_password'
\echo 'Connection URL: jdbc:postgresql://localhost:5432/tedtalks?currentSchema=tedtalks'
\echo '============================================'
\echo 'Search path for tedtalks_user: tedtalks, public'
\echo '============================================'

