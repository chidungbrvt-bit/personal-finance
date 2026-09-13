CREATE UNIQUE INDEX uk_user_info_email
    ON user_info (email);

CREATE INDEX idx_account_user_active
    ON account_info (user_id, active);

CREATE INDEX idx_category_user_active
    ON category_info (user_id, active);

CREATE INDEX idx_transaction_user_date
    ON financial_transaction_info (user_id, transaction_date);

CREATE INDEX idx_transaction_user_account_date
    ON financial_transaction_info (user_id, account_id, transaction_date);

CREATE INDEX idx_transaction_user_category_date
    ON financial_transaction_info (user_id, category_id, transaction_date);

CREATE INDEX idx_transaction_user_type_date
    ON financial_transaction_info (user_id, type, transaction_date);

CREATE INDEX idx_budget_user_period
    ON budget_info (user_id, year, month);
