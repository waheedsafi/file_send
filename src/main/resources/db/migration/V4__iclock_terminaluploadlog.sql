CREATE TABLE IF NOT EXISTS iclock_terminaluploadlog (
    id BIGSERIAL PRIMARY KEY,
    terminal_id BIGINT NOT NULL,
    event VARCHAR(80) NOT NULL,
    content VARCHAR(80) NOT NULL,
    upload_count INTEGER NOT NULL DEFAULT 1,
    error_count INTEGER NOT NULL DEFAULT 0,
    upload_time TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_iclock_terminaluploadlog_terminal
        FOREIGN KEY (terminal_id) REFERENCES iclock_terminal(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_iclock_terminaluploadlog_terminal_id
    ON iclock_terminaluploadlog (terminal_id);

CREATE INDEX IF NOT EXISTS idx_iclock_terminaluploadlog_upload_time
    ON iclock_terminaluploadlog (upload_time);
