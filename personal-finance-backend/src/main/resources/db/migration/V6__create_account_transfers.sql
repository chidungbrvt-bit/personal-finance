CREATE TABLE account_transfer_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    amount DECIMAL(19, 2) NOT NULL,
    transfer_date DATE NOT NULL,
    description VARCHAR(255) NULL,
    source_account_id BIGINT NOT NULL,
    target_account_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_transfer_source_account FOREIGN KEY (source_account_id) REFERENCES account_info (id),
    CONSTRAINT fk_transfer_target_account FOREIGN KEY (target_account_id) REFERENCES account_info (id),
    CONSTRAINT fk_transfer_user FOREIGN KEY (user_id) REFERENCES user_info (id),
    CONSTRAINT chk_transfer_different_accounts CHECK (source_account_id <> target_account_id),
    CONSTRAINT chk_transfer_positive_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_transfer_user_date
    ON account_transfer_info (user_id, transfer_date);
CREATE INDEX idx_transfer_source_account
    ON account_transfer_info (source_account_id);
CREATE INDEX idx_transfer_target_account
    ON account_transfer_info (target_account_id);
