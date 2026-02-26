-- Migration for table occupations
CREATE TABLE IF NOT EXISTS occupations (
    occupation_id SERIAL PRIMARY KEY,
    occupation_name VARCHAR(100) NOT NULL UNIQUE
);