CREATE TABLE IF NOT EXISTS staffs (
    id                    BIGSERIAL    PRIMARY KEY,
    name                  VARCHAR(100) NOT NULL,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    role                  VARCHAR(20)  NOT NULL,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    force_password_change BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_staffs_role CHECK (role IN ('ADMIN', 'STAFF'))
);

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    name_kana   VARCHAR(100),
    birth_date  DATE,
    notes       TEXT,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS offices (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    postal_code VARCHAR(8),
    address     VARCHAR(200),
    building    VARCHAR(200),
    phone       VARCHAR(20),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_offices (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL,
    office_id   BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, office_id),
    CONSTRAINT fk_user_offices_user   FOREIGN KEY (user_id)   REFERENCES users(id),
    CONSTRAINT fk_user_offices_office FOREIGN KEY (office_id) REFERENCES offices(id)
);

CREATE TABLE IF NOT EXISTS mail_send_batches (
    id         BIGSERIAL PRIMARY KEY,
    sent_by    BIGINT    NOT NULL,
    sent_at    TIMESTAMP NOT NULL,
    notes      TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mail_send_batches_staff FOREIGN KEY (sent_by) REFERENCES staffs(id)
);

CREATE TABLE IF NOT EXISTS mail_sends (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    office_id   BIGINT      NOT NULL,
    send_type   VARCHAR(20) NOT NULL,
    send_month  DATE        NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    batch_id    BIGINT,
    created_by  BIGINT      NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_mail_sends_send_type CHECK (send_type IN ('PLAN', 'MONITORING')),
    CONSTRAINT chk_mail_sends_status    CHECK (status    IN ('PENDING', 'SENT', 'DONE')),
    CONSTRAINT fk_mail_sends_user   FOREIGN KEY (user_id)    REFERENCES users(id),
    CONSTRAINT fk_mail_sends_office FOREIGN KEY (office_id)  REFERENCES offices(id),
    CONSTRAINT fk_mail_sends_batch  FOREIGN KEY (batch_id)   REFERENCES mail_send_batches(id),
    CONSTRAINT fk_mail_sends_staff  FOREIGN KEY (created_by) REFERENCES staffs(id)
);
