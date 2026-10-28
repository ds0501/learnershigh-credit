-- 데모 학생 식별자를 'demo-student'에서 '0'으로 바꾼다.
-- V4는 이미 적용된 파일이라 고치지 않고 UPDATE로 반영한다.
UPDATE credit_student_course SET student_id = '0' WHERE student_id = 'demo-student';
