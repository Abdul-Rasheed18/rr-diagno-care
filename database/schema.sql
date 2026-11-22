-- =====================================================================
--  RR Diagno Care - Online Test Booking & Lab Management Portal
--  schema.sql : database, tables and the sp_get_daily_revenue procedure
--  Run with:  mysql -u root -p < database/schema.sql
-- =====================================================================

DROP DATABASE IF EXISTS rr_diagno_care;
CREATE DATABASE rr_diagno_care CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rr_diagno_care;

-- ---------------------------------------------------------------------
-- Users (patients, lab technicians, admins)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(120) NOT NULL,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(120) NOT NULL UNIQUE,
    phone         VARCHAR(15)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          ENUM('ROLE_ADMIN','ROLE_STAFF','ROLE_PATIENT') NOT NULL DEFAULT 'ROLE_PATIENT',
    gender        ENUM('MALE','FEMALE','OTHER') NULL,
    date_of_birth DATE NULL,
    address       VARCHAR(255) NULL,
    enabled       TINYINT(1) NOT NULL DEFAULT 1,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Individual diagnostic tests
-- ---------------------------------------------------------------------
CREATE TABLE diagnostic_tests (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    code              VARCHAR(20)  NOT NULL UNIQUE,
    name              VARCHAR(150) NOT NULL,
    category          ENUM('Blood','Thyroid','Diabetes','Liver & Kidney') NOT NULL,
    price             DECIMAL(10,2) NOT NULL,
    sample_type       VARCHAR(50)  NOT NULL DEFAULT 'Blood',
    preparation_notes VARCHAR(255) NULL,
    unit              VARCHAR(30)  NULL,
    normal_min        DECIMAL(12,2) NULL,
    normal_max        DECIMAL(12,2) NULL,
    turnaround_hours  INT NOT NULL DEFAULT 24,
    description       VARCHAR(500) NULL,
    active            TINYINT(1) NOT NULL DEFAULT 1,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_test_price CHECK (price >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Combo health packages and their bundled tests
-- ---------------------------------------------------------------------
CREATE TABLE health_packages (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL UNIQUE,
    description VARCHAR(500) NULL,
    price       DECIMAL(10,2) NOT NULL,
    preparation_notes VARCHAR(255) NULL,
    featured    TINYINT(1) NOT NULL DEFAULT 0,
    active      TINYINT(1) NOT NULL DEFAULT 1,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_pkg_price CHECK (price >= 0)
) ENGINE=InnoDB;

CREATE TABLE package_tests (
    package_id BIGINT NOT NULL,
    test_id    BIGINT NOT NULL,
    PRIMARY KEY (package_id, test_id),
    CONSTRAINT fk_pt_package FOREIGN KEY (package_id) REFERENCES health_packages(id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_test    FOREIGN KEY (test_id)    REFERENCES diagnostic_tests(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Appointment time slots with capacity
-- ---------------------------------------------------------------------
CREATE TABLE time_slots (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    slot_label VARCHAR(40) NOT NULL UNIQUE,
    sort_order INT NOT NULL,
    capacity   INT NOT NULL DEFAULT 5,
    active     TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Bookings (no online payment: offline payment_mode + payment_status)
-- ---------------------------------------------------------------------
CREATE TABLE bookings (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_ref        VARCHAR(30) NOT NULL UNIQUE,
    patient_id         BIGINT NOT NULL,
    appointment_date   DATE NOT NULL,
    time_slot          VARCHAR(40) NOT NULL,
    visit_mode         ENUM('WALK_IN','HOME_COLLECTION') NOT NULL,
    collection_address VARCHAR(255) NULL,
    contact_phone      VARCHAR(15) NOT NULL,
    payment_mode       ENUM('PAY_AT_CENTER','PAY_ON_COLLECTION') NOT NULL,
    payment_status     ENUM('PENDING','PAID') NOT NULL DEFAULT 'PENDING',
    status             ENUM('BOOKED','SAMPLE_COLLECTED','PROCESSING','COMPLETED','CANCELLED') NOT NULL DEFAULT 'BOOKED',
    total_amount       DECIMAL(10,2) NOT NULL,
    report_remarks     VARCHAR(500) NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at       TIMESTAMP NULL,
    CONSTRAINT fk_booking_patient FOREIGN KEY (patient_id) REFERENCES users(id),
    INDEX idx_booking_date_slot (appointment_date, time_slot),
    INDEX idx_booking_status (status)
) ENGINE=InnoDB;

-- Purchased line items (a test or a package) - inserted with batchUpdate
CREATE TABLE booking_items (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    item_type  ENUM('TEST','PACKAGE') NOT NULL,
    item_id    BIGINT NOT NULL,
    item_name  VARCHAR(150) NOT NULL,
    price      DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_item_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- One result row per individual test (packages are expanded) - inserted with batchUpdate
CREATE TABLE test_results (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id    BIGINT NOT NULL,
    test_id       BIGINT NOT NULL,
    result_value  VARCHAR(50) NULL,
    numeric_value DECIMAL(12,2) NULL,
    remarks       VARCHAR(255) NULL,
    entered_by    VARCHAR(50) NULL,
    entered_at    TIMESTAMP NULL,
    UNIQUE KEY uq_result (booking_id, test_id),
    CONSTRAINT fk_result_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_result_test    FOREIGN KEY (test_id)    REFERENCES diagnostic_tests(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Audit trail written by the Spring AOP LabAuditAspect
-- ---------------------------------------------------------------------
CREATE TABLE audit_logs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL,
    action       VARCHAR(60)  NOT NULL,
    entity_type  VARCHAR(40)  NOT NULL,
    entity_id    BIGINT NULL,
    details      VARCHAR(500) NULL,
    outcome      ENUM('SUCCESS','FAILURE') NOT NULL DEFAULT 'SUCCESS',
    execution_ms BIGINT NOT NULL DEFAULT 0,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Stored procedure: daily bookings & revenue for a given appointment date
--   Revenue counts every non-cancelled booking scheduled for that date;
--   collected = PAID, pending = PENDING.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_get_daily_revenue;

DELIMITER $$
CREATE PROCEDURE sp_get_daily_revenue(
    IN  p_date              DATE,
    OUT p_total_bookings    INT,
    OUT p_cancelled         INT,
    OUT p_total_revenue     DECIMAL(12,2),
    OUT p_collected_revenue DECIMAL(12,2),
    OUT p_pending_revenue   DECIMAL(12,2)
)
BEGIN
    SELECT COUNT(*),
           COALESCE(SUM(total_amount), 0),
           COALESCE(SUM(CASE WHEN payment_status = 'PAID'    THEN total_amount ELSE 0 END), 0),
           COALESCE(SUM(CASE WHEN payment_status = 'PENDING' THEN total_amount ELSE 0 END), 0)
      INTO p_total_bookings, p_total_revenue, p_collected_revenue, p_pending_revenue
      FROM bookings
     WHERE appointment_date = p_date
       AND status <> 'CANCELLED';

    SELECT COUNT(*)
      INTO p_cancelled
      FROM bookings
     WHERE appointment_date = p_date
       AND status = 'CANCELLED';
END$$
DELIMITER ;
