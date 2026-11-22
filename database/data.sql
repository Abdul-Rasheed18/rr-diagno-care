-- =====================================================================
--  RR Diagno Care - seed data
--  Run AFTER schema.sql:  mysql -u root -p rr_diagno_care < database/data.sql
--
--  DEFAULT LOGIN CREDENTIALS (BCrypt hashed below, strength 10):
--     Role           Username   Password
--     ------------   --------   -----------
--     ROLE_ADMIN     admin      Admin@123
--     ROLE_STAFF     staff      Staff@123
--     ROLE_PATIENT   patient    Patient@123
-- =====================================================================
USE rr_diagno_care;

-- ---------------------------------------------------------------------
-- Users
-- ---------------------------------------------------------------------
INSERT INTO users (id, full_name, username, email, phone, password_hash, role, gender, date_of_birth, address) VALUES
 (1, 'RR Diagno Care Admin', 'admin',   'admin@rrdiagnocare.in',   '9848000001',
     '$2a$10$cDXaFEWd98R25ljdVxACs.uSHFyitJIh/q4sz5if1B5W1OJypxOQW', 'ROLE_ADMIN',   'MALE',   '1985-04-12', 'RR Diagno Care, Main Road, Vijayawada'),   -- Admin@123
 (2, 'Lakshmi Prasanna (Lab Technician)', 'staff', 'staff@rrdiagnocare.in', '9848000002',
     '$2a$10$LZgnYxGnkf8bGOqnDerB4eSkjQCtjx.GNFzA.LGu2IyD/mbDYjkRm', 'ROLE_STAFF',   'FEMALE', '1992-08-25', 'Benz Circle, Vijayawada'),                 -- Staff@123
 (3, 'Ravi Kumar Reddy', 'patient', 'ravi.kumar@example.com', '9876543210',
     '$2a$10$BSYUtb1gUNmWJ.pR4fErwOHND.41WFaajh6hRWbtqdf2ZCJo6ePbK', 'ROLE_PATIENT', 'MALE',   '1978-01-30', 'Flat 302, Sai Residency, Labbipet, Vijayawada'); -- Patient@123

-- ---------------------------------------------------------------------
-- 12 individual diagnostic tests
-- ---------------------------------------------------------------------
INSERT INTO diagnostic_tests (id, code, name, category, price, sample_type, preparation_notes, unit, normal_min, normal_max, turnaround_hours, description) VALUES
 (1,  'BLD-HB',   'Hemoglobin (Hb)',                    'Blood',          120.00, 'Blood (EDTA)', 'No fasting required',                     'g/dL',      13.00,     17.00,   6,  'Measures oxygen-carrying protein in red blood cells; screens for anaemia.'),
 (2,  'BLD-WBC',  'Total WBC Count',                    'Blood',          150.00, 'Blood (EDTA)', 'No fasting required',                     'cells/uL',  4000.00,   11000.00, 6,  'Counts white blood cells; helps detect infection and inflammation.'),
 (3,  'BLD-PLT',  'Platelet Count',                     'Blood',          150.00, 'Blood (EDTA)', 'No fasting required',                     'cells/uL',  150000.00, 410000.00, 6, 'Assesses clotting ability; useful in dengue and bleeding disorders.'),
 (4,  'THY-TSH',  'TSH (Ultrasensitive)',               'Thyroid',        350.00, 'Serum',        'Early morning sample preferred',          'uIU/mL',    0.27,      4.20,    24, 'Primary screening test for hypo- and hyperthyroidism.'),
 (5,  'THY-FT3',  'Free T3 (FT3)',                      'Thyroid',        300.00, 'Serum',        'No fasting required',                     'pg/mL',     2.00,      4.40,    24, 'Active thyroid hormone level.'),
 (6,  'THY-FT4',  'Free T4 (FT4)',                      'Thyroid',        300.00, 'Serum',        'No fasting required',                     'ng/dL',     0.93,      1.70,    24, 'Free thyroxine; complements TSH for thyroid evaluation.'),
 (7,  'DIA-FBS',  'Fasting Blood Sugar (FBS)',          'Diabetes',        80.00, 'Fluoride Plasma', '10-12 hrs fasting required',           'mg/dL',     70.00,     100.00,  4,  'Glucose level after overnight fast; diabetes screening.'),
 (8,  'DIA-PPBS', 'Post Prandial Blood Sugar (PPBS)',   'Diabetes',        80.00, 'Fluoride Plasma', 'Sample exactly 2 hrs after a meal',    'mg/dL',     70.00,     140.00,  4,  'Glucose level 2 hours after a meal.'),
 (9,  'DIA-A1C',  'HbA1c (Glycated Hemoglobin)',        'Diabetes',       450.00, 'Blood (EDTA)', 'No fasting required',                     '%',         4.00,      5.60,    24, 'Average blood sugar over the last 2-3 months.'),
 (10, 'LK-CREAT', 'Serum Creatinine',                   'Liver & Kidney', 200.00, 'Serum',        'Avoid heavy exercise 24 hrs before test', 'mg/dL',     0.70,      1.30,    12, 'Kidney function marker.'),
 (11, 'LK-UREA',  'Blood Urea',                         'Liver & Kidney', 180.00, 'Serum',        '8 hrs fasting recommended',               'mg/dL',     17.00,     43.00,   12, 'Kidney function and hydration marker.'),
 (12, 'LK-SGPT',  'SGPT / ALT',                         'Liver & Kidney', 250.00, 'Serum',        '10-12 hrs fasting required',              'U/L',       7.00,      56.00,   12, 'Liver enzyme; raised in liver cell injury.');

-- ---------------------------------------------------------------------
-- 3 combo health packages
-- ---------------------------------------------------------------------
INSERT INTO health_packages (id, name, description, price, preparation_notes, featured) VALUES
 (1, 'RR Basic Health Checkup',
     'Essential screening: blood count, fasting sugar and kidney function. Ideal for annual check-ups.',
     499.00, '10-12 hrs fasting required', 1),
 (2, 'RR Thyroid & Diabetes Care',
     'Complete thyroid profile with fasting, post-prandial sugar and HbA1c.',
     1199.00, '10-12 hrs fasting required; PPBS sample 2 hrs after meal', 1),
 (3, 'RR Full Body Advanced Checkup',
     'All 12 parameters across blood, thyroid, diabetes, liver and kidney panels at the best price.',
     1799.00, '10-12 hrs fasting required', 1);

INSERT INTO package_tests (package_id, test_id) VALUES
 (1, 1), (1, 2), (1, 3), (1, 7), (1, 10),
 (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9),
 (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11), (3, 12);

-- ---------------------------------------------------------------------
-- Time slots (capacity = max bookings per slot per day)
-- ---------------------------------------------------------------------
INSERT INTO time_slots (slot_label, sort_order, capacity) VALUES
 ('07:00 AM - 08:00 AM', 1, 6),
 ('08:00 AM - 09:00 AM', 2, 6),
 ('09:00 AM - 10:00 AM', 3, 6),
 ('10:00 AM - 11:00 AM', 4, 5),
 ('11:00 AM - 12:00 PM', 5, 5),
 ('04:00 PM - 05:00 PM', 6, 4),
 ('05:00 PM - 06:00 PM', 7, 4);

-- ---------------------------------------------------------------------
-- Demo bookings so the dashboards are not empty on first launch
--   #1 completed yesterday (with results, one abnormal value)
--   #2 booked for today (appears on the staff worklist)
-- ---------------------------------------------------------------------
INSERT INTO bookings (id, booking_ref, patient_id, appointment_date, time_slot, visit_mode, collection_address, contact_phone,
                      payment_mode, payment_status, status, total_amount, report_remarks, completed_at) VALUES
 (1, 'RRDC-DEMO-0001', 3, CURDATE() - INTERVAL 1 DAY, '08:00 AM - 09:00 AM', 'WALK_IN', NULL, '9876543210',
     'PAY_AT_CENTER', 'PAID', 'COMPLETED', 499.00, 'Mildly raised fasting sugar. Please consult your physician.', NOW()),
 (2, 'RRDC-DEMO-0002', 3, CURDATE(), '09:00 AM - 10:00 AM', 'HOME_COLLECTION', 'Flat 302, Sai Residency, Labbipet, Vijayawada', '9876543210',
     'PAY_ON_COLLECTION', 'PENDING', 'BOOKED', 800.00, NULL, NULL);

INSERT INTO booking_items (booking_id, item_type, item_id, item_name, price) VALUES
 (1, 'PACKAGE', 1, 'RR Basic Health Checkup', 499.00),
 (2, 'TEST',    4, 'TSH (Ultrasensitive)',    350.00),
 (2, 'TEST',    9, 'HbA1c (Glycated Hemoglobin)', 450.00);

INSERT INTO test_results (booking_id, test_id, result_value, numeric_value, remarks, entered_by, entered_at) VALUES
 (1, 1,  '14.2',   14.20,    NULL,                    'staff', NOW()),
 (1, 2,  '7800',   7800.00,  NULL,                    'staff', NOW()),
 (1, 3,  '245000', 245000.00, NULL,                   'staff', NOW()),
 (1, 7,  '118',    118.00,   'Above fasting range',   'staff', NOW()),
 (1, 10, '0.9',    0.90,     NULL,                    'staff', NOW()),
 (2, 4,  NULL, NULL, NULL, NULL, NULL),
 (2, 9,  NULL, NULL, NULL, NULL, NULL);

INSERT INTO audit_logs (username, action, entity_type, entity_id, details, outcome, execution_ms) VALUES
 ('staff', 'RESULT_COMPLETED', 'BOOKING', 1, 'Seed data: report RRDC-DEMO-0001 completed', 'SUCCESS', 12);
