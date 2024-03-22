--Написать триггер, проверяющий, корректные ли данные вносятся в timetable в части 
--соответствия поезда и станции назначенному на данный поезд маршруту.

CREATE OR REPLACE FUNCTION check_consistency_trains_and_stations
RETURNS TRIGGER AS $$
BEGIN 
    IF NOT EXIST (
        SELECT 1
        FROM Trains T JOIN Marshrut M ON T.m_num = M.m_num
        WHEN T.num = NEW.train_num AND M.station_id = NEW.station_id
    ) THEN
        RAISE EXCEPTION 'Несоответствие поезда и станции назначенному на данный поезд маршруту';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER check_timetable 
BEFORE INSERT ON Timetable 
FOR EACH ROW
EXECUTE FUNCTION check_consistency_trains_and_stations();

-----------------------------------------------

--Написать триггер, проверяющий, верно ли в таблице расписаний стоит 
--время при внесении новых данных (новой строки): оно должно быть больше времени, 
--стоящего для предыдущей (в соответствии с маршрутом) станции данного поезда. 
--Если текущее время меньше предыдущего триггер должен автоматически сделать 
--текущее время больше предыдущего на заданный интервал. Интервал ищется триггером 
--автоматически путём поиска онного у других поездов, передвигающихся между этими же 
--двумя точками. Если таковых нет, интервал берётся дефолтный (константа).

CREATE OR REPLACE FUNCTION check_schedule_time() RETURNS TRIGGER AS $$
DECLARE
    prev_time TIMESTAMP;
    specified_interval INTERVAL;
    new_time TIMESTAMP;
    marshrut_num INTEGER;
    station_order INTEGER;
    prev_station_id INTEGER;

BEGIN
    SELECT m_num INTO marshrut_num 
    FROM Trains 
    WHERE num = NEW.train_num;

    SELECT order1 INTO station_order
    FROM Marshrut 
    WHERE m_num = marshrut_num AND station_id = NEW.station_id;

    -- если станция первая, можно закончить выполнение триггера
    IF station_order = 1 THEN
        RETURN NEW;
    END IF;

    SELECT station_id INTO prev_station_id
    FROM Marshrut
    WHERE m_num = marshrut_num AND order1 = station_order - 1;

    -- получаю предыдущее время для данного поезда 
    SELECT dt2 INTO prev_time
    FROM Timetable T
    WHERE train_num = NEW.train_num AND id = NEW.id
    AND station_id = prev_station_id;

    IF prev_time IS NOT NULL AND NEW.dt1 <= prev_time THEN
    -- ищу интервал
        SELECT COALESCE(T2.dt1 - T1.dt2, '15 minutes') INTO specified_interval
        FROM (
            SELECT *
            FROM Timetable
            WHERE station_id = prev_station_id
        ) AS T1
        JOIN (
            SELECT *
            FROM Timetable
            WHERE station_id = NEW.station_id
        ) AS T2 ON T1.train_num = T2.train_num
        JOIN Marshrut M1 ON T1.station_id = M1.station_id
        JOIN Marshrut M2 ON T2.station_id = M2.station_id
        WHERE M1.m_num = M2.m_num
        AND M1.order1 + 1 = M2.order1
        AND T1.train_num != NEW.train_num;
        
        NEW.dt1 := prev_time + specified_interval;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;