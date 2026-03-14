CREATE TABLE IF NOT EXISTS personnel_company (
    id BIGSERIAL PRIMARY KEY,
    company_code VARCHAR(50) NOT NULL UNIQUE,
    company_name VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS personnel_department (
    id BIGSERIAL PRIMARY KEY,
    dept_code VARCHAR(50) NOT NULL,
    dept_name VARCHAR(100) NOT NULL,
    parent_dept_id BIGINT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    dept_manager_id BIGINT NULL,
    company_id BIGINT NOT NULL,
    CONSTRAINT uk_personnel_department_company_code UNIQUE (company_id, dept_code),
    CONSTRAINT fk_personnel_department_parent
        FOREIGN KEY (parent_dept_id) REFERENCES personnel_department(id) ON DELETE CASCADE,
    CONSTRAINT fk_personnel_department_company
        FOREIGN KEY (company_id) REFERENCES personnel_company(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS personnel_position (
    id BIGSERIAL PRIMARY KEY,
    position_code VARCHAR(50) NOT NULL,
    position_name VARCHAR(100) NOT NULL,
    parent_position_id BIGINT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    company_id BIGINT NOT NULL,
    CONSTRAINT uk_personnel_position_company_code UNIQUE (company_id, position_code),
    CONSTRAINT fk_personnel_position_parent
        FOREIGN KEY (parent_position_id) REFERENCES personnel_position(id) ON DELETE CASCADE,
    CONSTRAINT fk_personnel_position_company
        FOREIGN KEY (company_id) REFERENCES personnel_company(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS personnel_area (
    id BIGSERIAL PRIMARY KEY,
    area_code VARCHAR(30) NOT NULL,
    area_name VARCHAR(30) NOT NULL,
    parent_area_id BIGINT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    company_id BIGINT NOT NULL,
    employee_count INTEGER NOT NULL DEFAULT 0,
    device_count INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_personnel_area_company_code UNIQUE (company_id, area_code),
    CONSTRAINT fk_personnel_area_parent
        FOREIGN KEY (parent_area_id) REFERENCES personnel_area(id) ON DELETE CASCADE,
    CONSTRAINT fk_personnel_area_company
        FOREIGN KEY (company_id) REFERENCES personnel_company(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS personnel_employee (
    id BIGSERIAL PRIMARY KEY,
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    create_user VARCHAR(150) NULL,
    change_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    change_user VARCHAR(150) NULL,
    status SMALLINT NOT NULL DEFAULT 0,

    emp_code VARCHAR(20) NOT NULL,
    emp_code_digit BIGINT NULL,
    first_name VARCHAR(25) NULL,
    last_name VARCHAR(25) NULL,
    nickname VARCHAR(25) NULL,
    passport VARCHAR(30) NULL,
    driver_license_automobile VARCHAR(30) NULL,
    driver_license_motorcycle VARCHAR(30) NULL,
    department_id BIGINT NULL,
    position_id BIGINT NULL,
    superior_id BIGINT NULL,

    self_password VARCHAR(128) NULL,
    gender VARCHAR(1) NULL,
    birthday DATE NULL,
    address VARCHAR(200) NULL,
    postcode VARCHAR(10) NULL,
    office_tel VARCHAR(20) NULL,
    contact_tel VARCHAR(20) NULL,
    mobile VARCHAR(20) NULL,
    national VARCHAR(50) NULL,
    religion VARCHAR(20) NULL,
    title VARCHAR(20) NULL,
    ssn VARCHAR(20) NULL,
    update_time TIMESTAMPTZ NULL,
    hire_date DATE NULL,
    city VARCHAR(20) NULL,
    emp_type SMALLINT NULL,
    enable_payroll BOOLEAN NOT NULL DEFAULT TRUE,
    email VARCHAR(50) NULL,
    leave_group INTEGER NULL,

    device_password VARCHAR(20) NULL,
    dev_privilege INTEGER NULL DEFAULT 0,
    card_no VARCHAR(20) NULL,
    acc_group VARCHAR(5) NULL,
    acc_timezone VARCHAR(20) NULL,
    enroll_sn VARCHAR(20) NULL,
    verify_mode INTEGER NULL DEFAULT 0,

    app_status SMALLINT NULL DEFAULT 0,
    app_role SMALLINT NULL DEFAULT 1,

    company_id BIGINT NOT NULL,

    CONSTRAINT uk_personnel_employee_company_emp_code UNIQUE (company_id, emp_code),
    CONSTRAINT fk_personnel_employee_department
        FOREIGN KEY (department_id) REFERENCES personnel_department(id) ON DELETE CASCADE,
    CONSTRAINT fk_personnel_employee_position
        FOREIGN KEY (position_id) REFERENCES personnel_position(id) ON DELETE CASCADE,
    CONSTRAINT fk_personnel_employee_company
        FOREIGN KEY (company_id) REFERENCES personnel_company(id) ON DELETE CASCADE,
    CONSTRAINT fk_personnel_employee_superior
        FOREIGN KEY (superior_id) REFERENCES personnel_employee(id) ON DELETE SET NULL
);

ALTER TABLE personnel_department
    ADD CONSTRAINT fk_personnel_department_manager
    FOREIGN KEY (dept_manager_id) REFERENCES personnel_employee(id) ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS personnel_employee_area (
    employee_id BIGINT NOT NULL,
    area_id BIGINT NOT NULL,
    PRIMARY KEY (employee_id, area_id),
    CONSTRAINT fk_personnel_employee_area_employee
        FOREIGN KEY (employee_id) REFERENCES personnel_employee(id) ON DELETE CASCADE,
    CONSTRAINT fk_personnel_employee_area_area
        FOREIGN KEY (area_id) REFERENCES personnel_area(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS iclock_terminal (
    id BIGSERIAL PRIMARY KEY,
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    create_user VARCHAR(150) NULL,
    change_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    change_user VARCHAR(150) NULL,
    status SMALLINT NOT NULL DEFAULT 0,

    sn VARCHAR(50) NOT NULL UNIQUE,
    alias VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NULL,
    terminal_tz SMALLINT NOT NULL DEFAULT 0,
    heartbeat INTEGER NOT NULL DEFAULT 10,
    transfer_mode SMALLINT NOT NULL DEFAULT 1,
    transfer_interval INTEGER NOT NULL DEFAULT 1,
    transfer_time VARCHAR(100) NOT NULL DEFAULT '00:00;14:05',
    fw_ver VARCHAR(100) NULL,
    push_protocol VARCHAR(30) NULL,
    push_ver VARCHAR(30) NULL,
    language INTEGER NULL,
    terminal_name VARCHAR(30) NULL,
    platform VARCHAR(30) NULL,
    oem_vendor VARCHAR(50) NULL,
    user_count INTEGER NULL,
    transaction_count INTEGER NULL,
    fp_count INTEGER NULL,
    fp_alg_ver VARCHAR(10) NULL,
    face_count INTEGER NULL,
    face_alg_ver VARCHAR(10) NULL,
    fv_count INTEGER NULL,
    fv_alg_ver VARCHAR(10) NULL,
    palm_count INTEGER NULL,
    palm_alg_ver VARCHAR(10) NULL,
    lock_func SMALLINT NULL,
    log_stamp VARCHAR(30) NULL,
    op_log_stamp VARCHAR(30) NULL,
    capture_stamp VARCHAR(30) NULL,

    real_ip VARCHAR(45) NULL,
    state INTEGER NOT NULL DEFAULT 1,
    area_id BIGINT NULL,
    product_type SMALLINT NULL DEFAULT 9,
    is_attendance SMALLINT NULL DEFAULT 1,
    is_registration SMALLINT NULL DEFAULT 0,
    purpose SMALLINT NULL,
    controller_type SMALLINT NULL DEFAULT 0,
    authentication SMALLINT NULL DEFAULT 1,
    style VARCHAR(20) NULL,
    upload_flag VARCHAR(10) NULL DEFAULT '1111100000',
    is_tft BOOLEAN NOT NULL DEFAULT FALSE,
    last_activity TIMESTAMPTZ NULL,
    upload_time TIMESTAMPTZ NULL,
    push_time TIMESTAMPTZ NULL,
    is_access SMALLINT NULL DEFAULT 0,

    CONSTRAINT fk_iclock_terminal_area
        FOREIGN KEY (area_id) REFERENCES personnel_area(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS iclock_terminal_command (
    id BIGSERIAL PRIMARY KEY,
    terminal_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    commit_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    transfer_time TIMESTAMPTZ NULL,
    return_time TIMESTAMPTZ NULL,
    return_value INTEGER NULL,
    package INTEGER NULL,
    CONSTRAINT fk_iclock_terminal_command_terminal
        FOREIGN KEY (terminal_id) REFERENCES iclock_terminal(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS iclock_transaction (
    id BIGSERIAL PRIMARY KEY,
    company_code VARCHAR(50) NULL,
    emp_code VARCHAR(20) NOT NULL,
    emp_id BIGINT NULL,
    punch_time TIMESTAMPTZ NOT NULL,
    punch_state VARCHAR(5) NOT NULL,
    verify_type INTEGER NOT NULL DEFAULT 0,
    work_code VARCHAR(20) NULL,
    terminal_sn VARCHAR(50) NULL,
    terminal_alias VARCHAR(50) NULL,
    terminal_id BIGINT NULL,
    area_alias VARCHAR(30) NULL,
    longitude DOUBLE PRECISION NULL,
    latitude DOUBLE PRECISION NULL,
    gps_location TEXT NULL DEFAULT '',
    mobile VARCHAR(50) NULL,
    source SMALLINT NULL DEFAULT 1,
    purpose SMALLINT NULL DEFAULT 1,
    crc VARCHAR(100) NULL,
    is_attendance SMALLINT NULL DEFAULT 1,
    reserved VARCHAR(100) NULL,
    upload_time TIMESTAMPTZ NULL,
    sync_status SMALLINT NULL DEFAULT 0,
    sync_time TIMESTAMPTZ NULL,
    is_mask SMALLINT NULL DEFAULT 255,
    temperature NUMERIC(4,1) NULL DEFAULT 255,

    CONSTRAINT uk_iclock_transaction_company_emp_time
        UNIQUE (company_code, emp_code, punch_time),
    CONSTRAINT fk_iclock_transaction_employee
        FOREIGN KEY (emp_id) REFERENCES personnel_employee(id) ON DELETE SET NULL,
    CONSTRAINT fk_iclock_transaction_terminal
        FOREIGN KEY (terminal_id) REFERENCES iclock_terminal(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_personnel_employee_emp_code ON personnel_employee (emp_code);
CREATE INDEX IF NOT EXISTS idx_personnel_employee_company ON personnel_employee (company_id);
CREATE INDEX IF NOT EXISTS idx_personnel_employee_department ON personnel_employee (department_id);
CREATE INDEX IF NOT EXISTS idx_personnel_employee_position ON personnel_employee (position_id);
CREATE INDEX IF NOT EXISTS idx_personnel_employee_superior ON personnel_employee (superior_id);

CREATE INDEX IF NOT EXISTS idx_iclock_terminal_area ON iclock_terminal (area_id);
CREATE INDEX IF NOT EXISTS idx_iclock_terminal_last_activity ON iclock_terminal (last_activity);
CREATE INDEX IF NOT EXISTS idx_iclock_terminal_command_pending ON iclock_terminal_command (terminal_id, transfer_time, return_time, id);
CREATE INDEX IF NOT EXISTS idx_iclock_transaction_emp_code ON iclock_transaction (emp_code);
CREATE INDEX IF NOT EXISTS idx_iclock_transaction_terminal_sn ON iclock_transaction (terminal_sn);
CREATE INDEX IF NOT EXISTS idx_iclock_transaction_punch_time ON iclock_transaction (punch_time);

INSERT INTO personnel_company(id, company_code, company_name, is_default)
VALUES (1, '1', 'Default Company', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO personnel_department(id, dept_code, dept_name, parent_dept_id, is_default, dept_manager_id, company_id)
VALUES (1, '1', 'Default Department', NULL, TRUE, NULL, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO personnel_position(id, position_code, position_name, parent_position_id, is_default, company_id)
VALUES (1, '1', 'Default Position', NULL, TRUE, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO personnel_area(id, area_code, area_name, parent_area_id, is_default, company_id, employee_count, device_count)
VALUES (1, '1', 'Default Area', NULL, TRUE, 1, 0, 0)
ON CONFLICT (id) DO NOTHING;

SELECT setval('personnel_company_id_seq', GREATEST((SELECT MAX(id) FROM personnel_company), 1));
SELECT setval('personnel_department_id_seq', GREATEST((SELECT MAX(id) FROM personnel_department), 1));
SELECT setval('personnel_position_id_seq', GREATEST((SELECT MAX(id) FROM personnel_position), 1));
SELECT setval('personnel_area_id_seq', GREATEST((SELECT MAX(id) FROM personnel_area), 1));
