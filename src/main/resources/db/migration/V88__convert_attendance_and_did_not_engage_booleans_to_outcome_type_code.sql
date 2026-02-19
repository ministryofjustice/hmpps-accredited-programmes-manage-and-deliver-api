update session_attendance
set outcome_type_code = 'ATTC'
where id in (select id from session_attendance where attended = true);

update session_attendance
set outcome_type_code = 'AFTC'
where id in (select id from session_attendance where attended = true and did_not_engage = true);

update session_attendance
set outcome_type_code = 'UAAB'
where id in (select id from session_attendance where attended = false);

ALTER TABLE session_attendance DROP COLUMN attended;
ALTER TABLE session_attendance DROP COLUMN did_not_engage;
