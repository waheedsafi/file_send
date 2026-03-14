CREATE TABLE IF NOT EXISTS iclock_terminallog (
    id BIGSERIAL PRIMARY KEY,
    terminal_id BIGINT NOT NULL,
    terminal_tz SMALLINT NULL,
    admin VARCHAR(50) NULL,
    action_name SMALLINT NULL,
    action_time TIMESTAMPTZ NULL,
    object VARCHAR(50) NULL,
    param1 INTEGER NULL,
    param2 INTEGER NULL,
    param3 INTEGER NULL,
    upload_time TIMESTAMPTZ NULL,
    CONSTRAINT fk_iclock_terminallog_terminal
        FOREIGN KEY (terminal_id) REFERENCES iclock_terminal(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_iclock_terminallog_terminal_id
    ON iclock_terminallog (terminal_id);

CREATE INDEX IF NOT EXISTS idx_iclock_terminallog_action_time
    ON iclock_terminallog (action_time);

CREATE INDEX IF NOT EXISTS idx_iclock_terminallog_upload_time
    ON iclock_terminallog (upload_time);

CREATE TABLE IF NOT EXISTS iclock_errorcommandlog (
    id BIGSERIAL PRIMARY KEY,
    terminal_id BIGINT NOT NULL,
    error_code VARCHAR(16) NULL,
    error_msg VARCHAR(50) NULL,
    data_origin TEXT NULL,
    cmd VARCHAR(50) NULL,
    additional TEXT NULL,
    upload_time TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_iclock_errorcommandlog_terminal
        FOREIGN KEY (terminal_id) REFERENCES iclock_terminal(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_iclock_errorcommandlog_terminal_id
    ON iclock_errorcommandlog (terminal_id);

CREATE INDEX IF NOT EXISTS idx_iclock_errorcommandlog_upload_time
    ON iclock_errorcommandlog (upload_time);

CREATE TABLE IF NOT EXISTS iclock_terminalparameter (
    id BIGSERIAL PRIMARY KEY,
    terminal_id BIGINT NOT NULL,
    param_type VARCHAR(10) NULL,
    param_name VARCHAR(30) NOT NULL,
    param_value VARCHAR(100) NOT NULL,
    CONSTRAINT fk_iclock_terminalparameter_terminal
        FOREIGN KEY (terminal_id) REFERENCES iclock_terminal(id) ON DELETE CASCADE,
    CONSTRAINT uq_iclock_terminalparameter_terminal_param
        UNIQUE (terminal_id, param_name)
);

CREATE INDEX IF NOT EXISTS idx_iclock_terminalparameter_terminal_id
    ON iclock_terminalparameter (terminal_id);

CREATE TABLE IF NOT EXISTS iclock_transactionproofcmd (
    id BIGSERIAL PRIMARY KEY,
    terminal_id BIGINT NOT NULL,
    action_time TIMESTAMPTZ NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    terminal_count INTEGER NULL,
    server_count INTEGER NULL,
    flag SMALLINT NULL DEFAULT 0,
    reserved_init INTEGER NULL DEFAULT 0,
    reserved_float DOUBLE PRECISION NULL DEFAULT 0,
    reserved_char VARCHAR(30) NULL DEFAULT '0',
    CONSTRAINT fk_iclock_transactionproofcmd_terminal
        FOREIGN KEY (terminal_id) REFERENCES iclock_terminal(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_iclock_transactionproofcmd_terminal_id
    ON iclock_transactionproofcmd (terminal_id);

CREATE INDEX IF NOT EXISTS idx_iclock_transactionproofcmd_action_time
    ON iclock_transactionproofcmd (action_time);
