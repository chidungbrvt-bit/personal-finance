CREATE TABLE user_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE account_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    type ENUM('CASH', 'BANK', 'E_WALLET') NOT NULL,
    initial_balance DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    active BIT(1) NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES user_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE category_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    description VARCHAR(255) NULL,
    active BIT(1) NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_category_user_name_type UNIQUE (user_id, name, type),
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES user_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE financial_transaction_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    description VARCHAR(255) NULL,
    account_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_transaction_account FOREIGN KEY (account_id) REFERENCES account_info (id),
    CONSTRAINT fk_transaction_category FOREIGN KEY (category_id) REFERENCES category_info (id),
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES user_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE budget_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    year INT NOT NULL,
    month INT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_budget_user_category_period UNIQUE (user_id, category_id, year, month),
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES category_info (id),
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES user_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
