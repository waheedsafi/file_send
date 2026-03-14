CREATE TABLE IF NOT EXISTS app_async_job (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payload TEXT NULL,
    result TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    started_at TIMESTAMPTZ NULL,
    finished_at TIMESTAMPTZ NULL,
    error_message TEXT NULL
);

CREATE INDEX IF NOT EXISTS idx_app_async_job_status ON app_async_job (status);
CREATE INDEX IF NOT EXISTS idx_app_async_job_action ON app_async_job (action);
CREATE INDEX IF NOT EXISTS idx_app_async_job_created_at ON app_async_job (created_at);
