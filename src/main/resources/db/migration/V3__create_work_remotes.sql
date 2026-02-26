-- Migration for table work_remotes
CREATE TABLE IF NOT EXISTS work_remotes (
    work_rmt_id SERIAL PRIMARY KEY,
    work_rmt_name VARCHAR(100) NOT NULL UNIQUE
);