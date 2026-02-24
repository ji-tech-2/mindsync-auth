-- Migration for table occupations
CREATE TABLE occupations (
    occupation_id INT PRIMARY KEY AUTO_INCREMENT,
    occupation_name VARCHAR(100) NOT NULL UNIQUE
);

-- Seed data
INSERT INTO occupations (occupation_name) VALUES ('Employed'), ('Student'), ('Self-employed'), ('Retired'), ('Unemployed');