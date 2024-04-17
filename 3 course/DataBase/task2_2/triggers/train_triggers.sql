CREATE OR REPLACE FUNCTION log_deleted_trains()
RETURNS TRIGGER AS $$
DECLARE
    tickets_count INTEGER;
BEGIN
    -- проверяем создана ли уже такая табличка
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'Deleted_Trains') THEN
        CREATE TABLE Deleted_Trains (
            train_num INTEGER NOT NULL,
            tickets_sold INTEGER NOT NULL,
            deleted_at TIMESTAMP DEFAULT current_timestamp,
            deleted_by TEXT DEFAULT current_user
        );
    END IF;

    SELECT COUNT(*) INTO tickets_count
    FROM Tickets WHERE train_num = OLD.num;

    IF tickets_count > 300 THEN
        INSERT INTO Deleted_Trains (train_num, tickets_sold)
        VALUES (OLD.num, tickets_count);
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER log_deleted_trains_trigger
AFTER DELETE ON Trains
FOR EACH ROW
EXECUTE FUNCTION log_deleted_trains();

