CREATE TABLE session_attendance_outcome_type
(
    code        VARCHAR(5) NOT NULL PRIMARY KEY,
    description TEXT                DEFAULT NULL,
    attendance  BOOLEAN             DEFAULT NULL,
    compliant   BOOLEAN    NOT NULL DEFAULT FALSE
);

insert into session_attendance_outcome_type (code, description, attendance, compliant)
values ('ATTC', 'Attended - Complied', true, true),
       ('AFTC', 'Attended - Failed to Comply', true, false),
       ('ATSH', 'Attended - Sent Home (behaviour)', true, false),
       ('ATSS', 'Attended - Sent Home (service issues)', true, true),
       ('UAAB', 'Unacceptable Absence', false, false),
       ('AAAA', 'Acceptable Absence - Other', false, true),
       ('AAME', 'Acceptable Absence - Medical', false, true),
       ('AARE', 'Acceptable Absence - Religious', false, true),
       ('AASD', 'Acceptable Absence - Stood Down', false, true),
       ('AAHO', 'Acceptable Absence - Holiday', false, true),
       ('AAEM', 'Acceptable Absence - Employment', false, true),
       ('AAFC', 'Acceptable Absence - Family/ Childcare', false, true),
       ('AACL', 'Acceptable Absence - Court/Legal', false, true),
       ('AARC', 'Acceptable Absence - RIC', false, true),
       ('RSSR', 'Rescheduled - Service Request', false, true),
       ('RSOF', 'Rescheduled - PoP Request', false, true),
       ('IAPSD', 'IAPS deleted record', false, true),
       ('CO05', 'Acceptable Absence-Professional Judgement Decision', false, true),
       ('CO10', 'Acceptable Failure - None in following 12 months', null, true);