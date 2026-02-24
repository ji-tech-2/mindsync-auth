-- Migration for table occupations
CREATE TABLE occupations (
    occupation_id INT PRIMARY KEY AUTO_INCREMENT,
    occupation_name VARCHAR(100) NOT NULL UNIQUE
);