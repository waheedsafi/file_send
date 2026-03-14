CREATE TABLE IF NOT EXISTS iclock_biodata (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    bio_tmp TEXT NOT NULL,
    bio_no INTEGER DEFAULT 0,
    bio_index INTEGER DEFAULT 0,
    bio_type INTEGER NOT NULL,
    major_ver VARCHAR(10) NOT NULL,
    minor_ver VARCHAR(10),
    bio_format INTEGER DEFAULT 0,
    valid INTEGER DEFAULT 1,
    duress INTEGER DEFAULT 0,
    update_time TIMESTAMPTZ,
    sn VARCHAR(50)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_iclock_biodata_tpl
    ON iclock_biodata(employee_id, bio_no, bio_index, bio_type, bio_format, major_ver);

CREATE INDEX IF NOT EXISTS idx_iclock_biodata_employee ON iclock_biodata(employee_id);
CREATE INDEX IF NOT EXISTS idx_iclock_biodata_type ON iclock_biodata(bio_type);
