CREATE TABLE tasks (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    priority    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    done        BOOLEAN      NOT NULL DEFAULT FALSE,
    deadline    TIMESTAMP,
    embedding   TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE
);