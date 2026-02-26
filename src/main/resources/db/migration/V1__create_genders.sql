-- Migration for table genders
CREATE TABLE IF NOT EXISTS genders (
    gender_id SERIAL PRIMARY KEY,
    gender_name VARCHAR(50) NOT NULL UNIQUE
);