-- Migration for table factor_advices
CREATE TABLE factor_advices (
    advice_id INT PRIMARY KEY AUTO_INCREMENT,
    factor_id INT,
    advice_text TEXT NOT NULL,
    FOREIGN KEY (factor_id) REFERENCES factors(factor_id)
);