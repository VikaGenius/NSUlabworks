
CREATE FUNCTION check_specialization()
RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.teacher_id, NEW.subject_id) NOT IN (SELECT teacher_id, subject_id FROM Specialization) THEN
        RAISE EXCEPTION 'Нет соответствующей специализации для данной пары преподаватель-предмет.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_specialization_trigger
BEFORE INSERT OR UPDATE ON Academic_performance
FOR EACH ROW
EXECUTE FUNCTION check_specialization();

CREATE TRIGGER check_specialization_trigger2
BEFORE INSERT OR UPDATE ON Schedule
FOR EACH ROW
EXECUTE FUNCTION check_specialization();

--разобраться как работает