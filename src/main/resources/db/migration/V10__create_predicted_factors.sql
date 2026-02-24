-- Migration for table predicted_factors
CREATE TABLE predicted_factors (
    pred_id CHAR(36) NOT NULL,
    factor_id INT NOT NULL,
    PRIMARY KEY (pred_id, factor_id),
    FOREIGN KEY (pred_id) REFERENCES predictions(pred_id),
    FOREIGN KEY (factor_id) REFERENCES factors(factor_id)
);