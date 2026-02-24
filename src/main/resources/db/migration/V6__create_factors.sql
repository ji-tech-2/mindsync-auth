-- Migration for table factors
CREATE TABLE factors (
    factor_id INT PRIMARY KEY AUTO_INCREMENT,
    factor_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    importance FLOAT NOT NULL
);