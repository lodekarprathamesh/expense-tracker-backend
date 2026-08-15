-- ============================================================
-- Expense Tracker - Initial schema
-- ============================================================

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE accounts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    bank_name       VARCHAR(100) NOT NULL,
    nickname        VARCHAR(100),
    account_suffix  VARCHAR(10),
    account_type    VARCHAR(30) NOT NULL DEFAULT 'BANK',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);

CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(60) NOT NULL,
    icon            VARCHAR(50),
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    UNIQUE (user_id, name)
);

CREATE TABLE transactions (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id          BIGINT REFERENCES accounts(id) ON DELETE SET NULL,
    category_id         BIGINT REFERENCES categories(id) ON DELETE SET NULL,

    amount              NUMERIC(14, 2) NOT NULL,
    transaction_type    VARCHAR(10) NOT NULL,
    merchant            VARCHAR(255),
    note                VARCHAR(500),

    source              VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    source_reference    VARCHAR(255),
    raw_capture_text    TEXT,

    transaction_time    TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_source CHECK (source IN ('MANUAL', 'UPI_AUTO', 'NOTIFICATION_AUTO'))
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_user_time ON transactions(user_id, transaction_time DESC);
CREATE INDEX idx_transactions_category ON transactions(category_id);

CREATE UNIQUE INDEX uq_transactions_dedupe
    ON transactions(user_id, source, source_reference)
    WHERE source_reference IS NOT NULL;

INSERT INTO categories (name, icon, is_default) VALUES
    ('Food & Dining', 'utensils', TRUE),
    ('Groceries', 'shopping-cart', TRUE),
    ('Transport', 'car', TRUE),
    ('Shopping', 'bag', TRUE),
    ('Bills & Utilities', 'receipt', TRUE),
    ('Entertainment', 'film', TRUE),
    ('Health', 'heart-pulse', TRUE),
    ('Rent', 'home', TRUE),
    ('Transfer', 'arrow-left-right', TRUE),
    ('Salary/Income', 'wallet', TRUE),
    ('Other', 'more-horizontal', TRUE);
