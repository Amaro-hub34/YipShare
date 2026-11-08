-- SQLite translation of the Drizzle (PostgreSQL) schema.
-- Must run on Android API 24 (SQLite 3.9): no STRICT tables, no JSON functions, no UPSERT.
-- uuid -> TEXT (the app generates it), enum -> TEXT + CHECK, timestamp -> TEXT ISO-8601,
-- boolean -> INTEGER 0/1, jsonb -> TEXT. Foreign keys need PRAGMA foreign_keys = ON.
-- reviews and reports name their author; the reviewed or reported user is the other person of the loan.
-- loans: the first delivery code moves accepted -> on_loan (sets start_time), the second one -> finished.
-- articles: loan_durations lists the offered options (indexes of the app's duration list, e.g. '0,3');
-- deleted_at marks a logical delete, because loans keep pointing to the article.
-- condominium roles live in user_roles; a permission_overrides row with {"allowed": false} on request_loans
-- takes away a neighbor's right to borrow in that condominium (e.g. unpaid maintenance).
-- a pending loan is a request to the lender, who accepts it (accepted) or turns it down (rejected).

CREATE TABLE roles (
    role_id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE permissions (
    permission_id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    constraint_schema TEXT NOT NULL
);

CREATE TABLE categories (
    category_id TEXT PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE users (
    user_id TEXT PRIMARY KEY,
    username TEXT UNIQUE,
    email TEXT UNIQUE,
    password_hash TEXT,
    paternal_surname TEXT,
    maternal_surname TEXT,
    name TEXT,
    phone_number TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    computed_rating INTEGER NOT NULL,
    state TEXT NOT NULL CHECK (state IN ('active', 'suspended', 'deleted')),
    CONSTRAINT chk_active_user_data CHECK (
        state = 'deleted'
        OR (username IS NOT NULL AND password_hash IS NOT NULL AND name IS NOT NULL)
    )
);

CREATE TABLE condominiums (
    condominium_id TEXT PRIMARY KEY,
    short_code TEXT NOT NULL UNIQUE CHECK (length(short_code) = 6),
    user_id TEXT NOT NULL REFERENCES users (user_id),
    name TEXT NOT NULL,
    street TEXT NOT NULL,
    neighborhood TEXT NOT NULL,
    postal_code TEXT NOT NULL CHECK (length(postal_code) = 5),
    municipality TEXT NOT NULL,
    city TEXT NOT NULL,
    digital_state TEXT NOT NULL CHECK (digital_state IN ('approved', 'pending', 'suspended', 'deleted')),
    verification_data TEXT,
    approved_at TEXT,
    approved_by TEXT REFERENCES users (user_id),
    CONSTRAINT chk_approval_metadata CHECK (
        digital_state <> 'approved'
        OR (approved_at IS NOT NULL AND approved_by IS NOT NULL)
    )
);

CREATE TABLE temporary_condominium_verification_data (
    condominium_id TEXT PRIMARY KEY REFERENCES condominiums (condominium_id),
    ecrpc_document TEXT NOT NULL,
    aana_document TEXT NOT NULL,
    owner_ine_document TEXT NOT NULL
);

CREATE TABLE condominium_units (
    condominium_unit_id TEXT PRIMARY KEY,
    condominium_id TEXT NOT NULL REFERENCES condominiums (condominium_id),
    name TEXT NOT NULL
);

CREATE TABLE articles (
    article_id TEXT PRIMARY KEY,
    condominium_id TEXT NOT NULL REFERENCES condominiums (condominium_id),
    lending_user TEXT NOT NULL REFERENCES users (user_id),
    name TEXT,
    description TEXT NOT NULL,
    state TEXT NOT NULL CHECK (state IN ('excellent', 'good', 'medium', 'bad')),
    availability TEXT NOT NULL CHECK (availability IN ('available', 'not_available', 'pending')),
    maximum_lend_time INTEGER NOT NULL,
    image TEXT,
    loan_durations TEXT NOT NULL DEFAULT '0',
    deleted_at TEXT,
    CONSTRAINT chk_positive_lend_time CHECK (maximum_lend_time > 0)
);

CREATE TABLE loans (
    loan_id TEXT PRIMARY KEY,
    lender_id TEXT NOT NULL REFERENCES users (user_id),
    user_id TEXT NOT NULL REFERENCES users (user_id),
    article_id TEXT NOT NULL REFERENCES articles (article_id),
    request_text TEXT NOT NULL,
    state TEXT NOT NULL CHECK (state IN ('pending', 'accepted', 'rejected', 'on_loan', 'finished')),
    start_time TEXT,
    return_time TEXT NOT NULL,
    CONSTRAINT chk_started_loan CHECK (state NOT IN ('on_loan', 'finished') OR start_time IS NOT NULL)
);

CREATE TABLE reviews (
    review_id TEXT PRIMARY KEY,
    loan_id TEXT NOT NULL REFERENCES loans (loan_id),
    author_id TEXT NOT NULL REFERENCES users (user_id),
    review_text TEXT NOT NULL,
    rating INTEGER NOT NULL,
    CONSTRAINT chk_review_rating_range CHECK (rating BETWEEN 1 AND 50),
    CONSTRAINT uq_review_loan_author UNIQUE (loan_id, author_id)
);

CREATE TABLE reports (
    report_id TEXT PRIMARY KEY,
    loan_id TEXT NOT NULL REFERENCES loans (loan_id),
    author_id TEXT NOT NULL REFERENCES users (user_id),
    report_text TEXT NOT NULL,
    resolved_at TEXT,
    resolved_by TEXT REFERENCES users (user_id),
    CONSTRAINT chk_report_resolution CHECK (
        (resolved_at IS NULL AND resolved_by IS NULL)
        OR (resolved_at IS NOT NULL AND resolved_by IS NOT NULL)
    )
);

CREATE TABLE report_evidences (
    report_evidence_id TEXT PRIMARY KEY,
    report_id TEXT NOT NULL REFERENCES reports (report_id),
    image TEXT
);

CREATE TABLE notifications (
    notification_id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users (user_id),
    header TEXT NOT NULL,
    body TEXT NOT NULL,
    image TEXT,
    action TEXT NOT NULL
);

CREATE TABLE user_roles (
    uuid TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users (user_id),
    condominium_id TEXT REFERENCES condominiums (condominium_id),
    role_id TEXT NOT NULL REFERENCES roles (role_id),
    is_global INTEGER NOT NULL CHECK (is_global IN (0, 1)),
    CONSTRAINT chk_is_global_role_assignment CHECK (
        (is_global = 1 AND condominium_id IS NULL)
        OR (is_global = 0 AND condominium_id IS NOT NULL)
    )
);

-- SQLite treats NULLs as distinct in UNIQUE; IFNULL emulates Postgres NULLS NOT DISTINCT.
CREATE UNIQUE INDEX uq_user_condominium_role_user_roles
    ON user_roles (user_id, IFNULL(condominium_id, ''), role_id);

CREATE TABLE role_permissions (
    role_id TEXT NOT NULL REFERENCES roles (role_id),
    permission_id TEXT NOT NULL REFERENCES permissions (permission_id),
    constraints TEXT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE permission_overrides (
    uuid TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users (user_id),
    condominium_id TEXT REFERENCES condominiums (condominium_id),
    permission_id TEXT NOT NULL REFERENCES permissions (permission_id),
    constraints TEXT NOT NULL
);

CREATE UNIQUE INDEX uq_user_condominium_permission_permission_overrides
    ON permission_overrides (user_id, IFNULL(condominium_id, ''), permission_id);

CREATE TABLE condominium_members (
    user_id TEXT NOT NULL REFERENCES users (user_id),
    condominium_id TEXT NOT NULL REFERENCES condominiums (condominium_id),
    condominium_unit_id TEXT NOT NULL REFERENCES condominium_units (condominium_unit_id),
    PRIMARY KEY (user_id, condominium_id, condominium_unit_id)
);

CREATE TABLE pending_condominium_members (
    user_id TEXT NOT NULL REFERENCES users (user_id),
    condominium_id TEXT NOT NULL REFERENCES condominiums (condominium_id),
    unit TEXT NOT NULL CHECK (length(unit) <= 255),
    suggested_unit_id TEXT NOT NULL REFERENCES condominium_units (condominium_unit_id),
    PRIMARY KEY (user_id, condominium_id)
);

CREATE TABLE article_categories (
    article_id TEXT NOT NULL REFERENCES articles (article_id),
    category_id TEXT NOT NULL REFERENCES categories (category_id),
    PRIMARY KEY (article_id, category_id)
);

-- Fixed catalog rows the app relies on.
INSERT INTO roles (role_id, name) VALUES ('role_condominium_admin', 'condominium_admin');

INSERT INTO permissions (permission_id, name, constraint_schema)
VALUES ('permission_request_loans', 'request_loans', '{"allowed": "boolean"}');

-- Same order as the app's category list, so category_N is the N-th entry.
INSERT INTO categories (category_id, name) VALUES
    ('category_0', 'Herramientas'),
    ('category_1', 'Electrodomésticos'),
    ('category_2', 'Jardinería'),
    ('category_3', 'Cocina'),
    ('category_4', 'Limpieza'),
    ('category_5', 'Electrónica'),
    ('category_6', 'Automotriz'),
    ('category_7', 'Otros');
