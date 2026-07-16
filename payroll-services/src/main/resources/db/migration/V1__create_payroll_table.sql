CREATE TABLE payroll (
                         id              BIGSERIAL PRIMARY KEY,
                         employee_id     BIGINT NOT NULL,          -- reference only, no FK across services
                         basic_salary    NUMERIC(12,2) NOT NULL,
                         allowance       NUMERIC(12,2) NOT NULL DEFAULT 0,
                         bonus           NUMERIC(12,2) NOT NULL DEFAULT 0,
                         tax             NUMERIC(12,2) NOT NULL DEFAULT 0,
                         net_salary      NUMERIC(12,2) NOT NULL,
                         pay_period      VARCHAR(7)  NOT NULL,     -- e.g. '2026-07'
                         created_at      TIMESTAMP NOT NULL DEFAULT now(),
                         updated_at      TIMESTAMP NOT NULL DEFAULT now(),
                         UNIQUE (employee_id, pay_period)
);

CREATE INDEX idx_payroll_employee_id ON payroll(employee_id);