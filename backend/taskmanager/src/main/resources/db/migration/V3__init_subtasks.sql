CREATE TABLE subtasks (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title      VARCHAR(255) NOT NULL,
    done       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    task_id    UUID         NOT NULL REFERENCES tasks(id) ON DELETE CASCADE
);