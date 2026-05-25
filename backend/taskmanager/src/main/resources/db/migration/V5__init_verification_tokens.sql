CREATE TABLE verification_tokens (
                                     id         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
                                     token      UUID      NOT NULL UNIQUE,
                                     user_id    UUID      NOT NULL REFERENCES users(id),
                                     expires_at TIMESTAMP NOT NULL,
                                     used       BOOLEAN   NOT NULL DEFAULT FALSE,
                                     created_at TIMESTAMP NOT NULL DEFAULT NOW()
);