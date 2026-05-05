-- ============================================
-- Leave Management System - Database Schema
-- MySQL 8.x
-- ============================================

CREATE DATABASE IF NOT EXISTS leave_management_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE leave_management_db;

-- ============================================
-- Table: users
-- ============================================
CREATE TABLE IF NOT EXISTS users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100)  NOT NULL,
  email         VARCHAR(150)  NOT NULL UNIQUE,
  password      VARCHAR(255)  NOT NULL,
  contact_number VARCHAR(20)  NOT NULL UNIQUE,
  role          ENUM('EMPLOYEE', 'MANAGER') NOT NULL DEFAULT 'EMPLOYEE',
  department    VARCHAR(100)  NOT NULL,
  total_leaves  INT           NOT NULL DEFAULT 20,
  used_leaves   INT           NOT NULL DEFAULT 0,
  INDEX idx_users_email (email),
  INDEX idx_users_role  (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Table: leave_requests
-- ============================================
CREATE TABLE IF NOT EXISTS leave_requests (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  employee_id      BIGINT        NOT NULL,
  start_date       DATE          NOT NULL,
  end_date         DATE          NOT NULL,
  leave_type       ENUM('CASUAL','SICK','ANNUAL','MATERNITY','PATERNITY','EMERGENCY','UNPAID') NOT NULL,
  reason           VARCHAR(500)  NOT NULL,
  status           ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  reviewed_by      BIGINT        NULL,
  rejection_reason VARCHAR(500)  NULL,
  created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_lr_employee  FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_lr_reviewer  FOREIGN KEY (reviewed_by)  REFERENCES users(id) ON DELETE SET NULL,

  INDEX idx_lr_employee   (employee_id),
  INDEX idx_lr_status     (status),
  INDEX idx_lr_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Sample Data (passwords = BCrypt of "password123")
-- ============================================
INSERT INTO users (name, email, password, contact_number, role, department, total_leaves, used_leaves) VALUES
('Sarah Johnson',  'manager@lms.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '9000000001', 'MANAGER',  'Engineering', 20, 0),
('John Smith',     'john@lms.com',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '9000000002', 'EMPLOYEE', 'Engineering', 20, 5),
('Emily Davis',    'emily@lms.com',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '9000000003', 'EMPLOYEE', 'Marketing',   20, 3),
('Michael Brown',  'michael@lms.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '9000000004', 'EMPLOYEE', 'Finance',     20, 2);

-- Sample Leave Requests (adjust dates as needed)
INSERT INTO leave_requests (employee_id, start_date, end_date, leave_type, reason, status, reviewed_by) VALUES
(2, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 11 DAY), 'ANNUAL',  'Family vacation',      'APPROVED', 1),
(2, DATE_ADD(CURDATE(), INTERVAL 5  DAY), DATE_ADD(CURDATE(), INTERVAL 7  DAY), 'CASUAL',  'Personal work',        'PENDING',  NULL),
(3, DATE_SUB(CURDATE(), INTERVAL 8  DAY), DATE_SUB(CURDATE(), INTERVAL 6  DAY), 'SICK',    'Medical appointment',  'APPROVED', 1),
(3, DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'ANNUAL',  'Holiday trip',         'PENDING',  NULL),
(4, DATE_ADD(CURDATE(), INTERVAL 2  DAY), DATE_ADD(CURDATE(), INTERVAL 4  DAY), 'CASUAL',  'Personal matter',      'REJECTED', 1),
(4, DATE_ADD(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 22 DAY), 'ANNUAL',  'Annual vacation',      'PENDING',  NULL);

UPDATE leave_requests SET rejection_reason = 'Critical project deadline. Please reschedule.' WHERE status = 'REJECTED';
