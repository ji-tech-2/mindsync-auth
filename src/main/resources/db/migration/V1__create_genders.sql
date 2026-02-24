-- Migration for table genders
CREATE TABLE genders (
    gender_id INT PRIMARY KEY AUTO_INCREMENT,
    gender_name VARCHAR(50) NOT NULL UNIQUE
);

-- Seed data
INSERT INTO genders (gender_name) VALUES ('Female'), ('Male'), ('Non-binary/Other');