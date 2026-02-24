-- Migration for table predictions
CREATE TABLE predictions (
    pred_id CHAR(36) PRIMARY KEY,
    user_id CHAR(36),
    guest_id CHAR(36),
    pred_date TIMESTAMP NOT NULL,
    screen_time FLOAT NOT NULL,
    work_screen FLOAT NOT NULL,
    leisure_screen FLOAT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (guest_id) REFERENCES guest_users(guest_id)
);