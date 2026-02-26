-- Migration for table users
CREATE TABLE IF NOT EXISTS users (
    user_id CHAR(36) PRIMARY KEY,
    gender_id INT NOT NULL,
    occupation_id INT NOT NULL,
    work_rmt_id INT NOT NULL,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    username VARCHAR(60) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    dob DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_gender FOREIGN KEY (gender_id) REFERENCES genders(gender_id),
    CONSTRAINT fk_user_occupation FOREIGN KEY (occupation_id) REFERENCES occupations(occupation_id),
    CONSTRAINT fk_user_work_remote FOREIGN KEY (work_rmt_id) REFERENCES work_remotes(work_rmt_id)
);