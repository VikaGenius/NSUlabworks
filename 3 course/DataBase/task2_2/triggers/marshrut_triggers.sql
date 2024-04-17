-- Написать триггер для таблицы маршрутов, который автоматически ставит номер маршрута, 
-- если онный не заполнен при вставке данных. Номер ставится по следующему правилу: 
-- минимальное число, которого нет ни в таблице маршрутов, ни в таблице транзитных маршрутов.

CREATE OR REPLACE FUNCTION assign_route_number()
RETURNS TRIGGER AS $$
DECLARE
    new_route_number INTEGER;
BEGIN
    IF NEW.m_num IS NULL THEN
        SELECT COALESCE(MIN(route_number), 1)
        INTO new_route_number
        FROM generate_series(1, (SELECT GREATEST(MAX(m_num) + 1, 1) FROM (SELECT m_num FROM Marshrut UNION SELECT m_num FROM TMarshrut))) AS route_number
        WHERE NOT EXISTS (
            SELECT 1 FROM Marshrut
            WHERE m_num = route_number
            UNION ALL
            SELECT 1 FROM TMarshrut
            WHERE m_num = route_number
        );
        NEW.m_num := new_route_number;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER assign_route_number_trigger
BEFORE INSERT ON Marshrut
FOR EACH ROW
EXECUTE FUNCTION assign_route_number();
