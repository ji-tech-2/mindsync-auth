-- Migration for table work_remotes
CREATE TABLE work_remotes (
    work_rmt_id INT PRIMARY KEY AUTO_INCREMENT,
    work_rmt_name VARCHAR(100) NOT NULL UNIQUE
);