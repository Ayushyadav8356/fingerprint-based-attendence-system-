-- Create Database
CREATE DATABASE IF NOT EXISTS attendance_db;
USE attendance_db;

-- 1. Users Table (Handles authentication credentials and roles)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL, -- BCrypt hashed passwords
    role VARCHAR(20) NOT NULL, -- 'ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT'
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Teachers Table
CREATE TABLE IF NOT EXISTS teachers (
    id BIGINT PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Students Table (Holds fingerprint templates and RFID UID codes)
CREATE TABLE IF NOT EXISTS students (
    id BIGINT PRIMARY KEY,
    roll_number VARCHAR(50) NOT NULL UNIQUE,
    fingerprint_id INT UNIQUE, -- ID stored in AS608 fingerprint module (1-127 or more)
    rfid_uid VARCHAR(50) UNIQUE, -- RFID UID card code (e.g. Hex string)
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Classes Table
CREATE TABLE IF NOT EXISTS classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_name VARCHAR(100) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    teacher_id BIGINT NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE
);

-- 5. Enrollments Table (Many-to-Many mapping for Students in Classes)
CREATE TABLE IF NOT EXISTS enrollments (
    student_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    PRIMARY KEY (student_id, class_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

-- 6. Attendance Table
CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL, -- 'PRESENT', 'LATE', 'ABSENT'
    method VARCHAR(20) NOT NULL, -- 'FINGERPRINT', 'RFID', 'MANUAL'
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

-- Seed Data (Password for all is 'password' hashed with BCrypt)
-- BCrypt hash for 'password': $2a$10$gRstbXjM8a.PZ499u/sC5uWv0z4T7w9cEqVvCqE0h.F2/qV2X4vK.
INSERT INTO users (id, username, password, role, full_name, email) VALUES
(1, 'admin', '$2a$10$gRstbXjM8a.PZ499u/sC5uWv0z4T7w9cEqVvCqE0h.F2/qV2X4vK.', 'ROLE_ADMIN', 'System Administrator', 'admin@attendance.com')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO users (id, username, password, role, full_name, email) VALUES
(2, 'teacher1', '$2a$10$gRstbXjM8a.PZ499u/sC5uWv0z4T7w9cEqVvCqE0h.F2/qV2X4vK.', 'ROLE_TEACHER', 'Prof. Alan Turing', 'alan@attendance.com')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO users (id, username, password, role, full_name, email) VALUES
(3, 'student1', '$2a$10$gRstbXjM8a.PZ499u/sC5uWv0z4T7w9cEqVvCqE0h.F2/qV2X4vK.', 'ROLE_STUDENT', 'Alice Smith', 'alice@attendance.com')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO users (id, username, password, role, full_name, email) VALUES
(4, 'student2', '$2a$10$gRstbXjM8a.PZ499u/sC5uWv0z4T7w9cEqVvCqE0h.F2/qV2X4vK.', 'ROLE_STUDENT', 'Bob Johnson', 'bob@attendance.com')
ON DUPLICATE KEY UPDATE id=id;

-- Seed Teacher Details
INSERT INTO teachers (id, department) VALUES
(2, 'Computer Science')
ON DUPLICATE KEY UPDATE id=id;

-- Seed Student Details
-- Student 1 (Alice): Fingerprint ID 1, RFID UID: 'E2C1A4B9'
INSERT INTO students (id, roll_number, fingerprint_id, rfid_uid) VALUES
(3, 'CS-2026-001', 1, 'E2C1A4B9')
ON DUPLICATE KEY UPDATE id=id;

-- Student 2 (Bob): Fingerprint ID 2, RFID UID: '43F7D2A1'
INSERT INTO students (id, roll_number, fingerprint_id, rfid_uid) VALUES
(4, 'CS-2026-002', 2, '43F7D2A1')
ON DUPLICATE KEY UPDATE id=id;

-- Seed Classes
INSERT INTO classes (id, class_name, subject, teacher_id) VALUES
(1, 'CS-101', 'Introduction to Computer Science', 2),
(2, 'CS-102', 'Data Structures & Algorithms', 2)
ON DUPLICATE KEY UPDATE id=id;

-- Enroll Students
INSERT INTO enrollments (student_id, class_id) VALUES
(3, 1), -- Alice in CS-101
(3, 2), -- Alice in CS-102
(4, 1)  -- Bob in CS-101
ON DUPLICATE KEY UPDATE student_id=student_id;
