
ALTER TABLE users
    ADD COLUMN enabled                BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN failed_login_attempts  INT       NOT NULL DEFAULT 0,
    ADD COLUMN locked_until           TIMESTAMP,
    ADD COLUMN token_version          INT       NOT NULL DEFAULT 0;