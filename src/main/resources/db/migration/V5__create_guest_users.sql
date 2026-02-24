-- Migration for table guest_users
CREATE TABLE guest_users (
    guest_id CHAR(36) PRIMARY KEY,
    gender_id INT NOT NULL,
    occupation_id INT NOT NULL,
    work_rmt_id INT NOT NULL,
    session_token VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (gender_id) REFERENCES genders(gender_id),
    FOREIGN KEY (occupation_id) REFERENCES occupations(occupation_id),
    FOREIGN KEY (work_rmt_id) REFERENCES work_remotes(work_rmt_id)
);