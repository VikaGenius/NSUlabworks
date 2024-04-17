-- добавление маршрутов
DO $$
DECLARE
    i INTEGER;
    j INTEGER;
    station_count INTEGER;
    station_ids INTEGER[];
    array_len INTEGER;
    cur_station_id INTEGER;
BEGIN
    SELECT ARRAY(SELECT id FROM Stations) INTO station_ids;
    array_len := array_length(station_ids, 1);

    FOR i IN 1..10 LOOP
        station_count := FLOOR(RANDOM() * 29) + 2;

        FOR j IN 1..station_count LOOP
            cur_station_id := station_ids[FLOOR(RANDOM() * array_len) + 1];

            IF NOT EXISTS (SELECT 1 FROM Marshrut WHERE m_num = i AND station_id = cur_station_id) THEN
                INSERT INTO Marshrut (m_num, station_id, order1)
                VALUES (i, cur_station_id, j);
            END IF;
        END LOOP;
    END LOOP;
END $$;

-- добавление расстояний между станциями
DO $$
DECLARE
    i INTEGER;
    j INTEGER;
    src_id INTEGER;
    dst_id INTEGER;
    distance INTEGER;
    station_ids INTEGER[];
    array_len INTEGER;
BEGIN
    SELECT ARRAY(SELECT id FROM Stations) INTO station_ids;
    array_len := array_length(station_ids, 1);

    FOR i IN 1..array_len LOOP
        src_id := station_ids[i];
        FOR j IN 1..array_len LOOP
            dst_id := station_ids[j];
            IF dst_id > src_id THEN
                distance := FLOOR(RANDOM() * 299) + 2;
                INSERT INTO Distance (src_id, dst_id, distance_km)
                VALUES (src_id, dst_id, distance);
            END IF;
        END LOOP;
    END LOOP;
END $$;

-- добавление TMarshruts
DO $$
DECLARE
    crossing RECORD;
    m_num INTEGER := 1;
    max_order INTEGER;
    first_station_id INTEGER;
    last_station_id INTEGER;
BEGIN
    FOR crossing IN (
        SELECT m1.m_num AS pm_num1, 
               m2.m_num AS pm_num2,
               m1.station_id, 
               m1.order1
        FROM Marshrut m1
        JOIN Marshrut m2 ON m1.station_id = m2.station_id
        WHERE m1.m_num < m2.m_num
        AND m1.order1 <> 1
        AND m2.order1 <> (SELECT MAX(order1) FROM Marshrut m3 WHERE m3.m_num = m2.m_num)
    ) LOOP

        SELECT MAX(order1) INTO max_order 
        FROM Marshrut 
        WHERE Marshrut.m_num = crossing.pm_num2;

        SELECT station_id INTO first_station_id
        FROM Marshrut
        WHERE Marshrut.m_num = crossing.pm_num1 AND order1 = 1;

        SELECT station_id INTO last_station_id
        FROM Marshrut
        WHERE Marshrut.m_num = crossing.pm_num2 AND order1 = max_order;

        -- вставляем начальную станцию
        INSERT INTO TMarshrut (m_num, station_id, order1, pm_num)
        VALUES (m_num, first_station_id, 1, crossing.pm_num1);

        -- вставляем последнюю станцию
        INSERT INTO TMarshrut (m_num, station_id, order1, pm_num)
        VALUES (m_num, last_station_id, max_order, crossing.pm_num2);
        
        -- вставляем станцию пересадки
        INSERT INTO TMarshrut (m_num, station_id, order1, pm_num)
        VALUES (m_num, crossing.station_id, crossing.order1, crossing.pm_num1);

        -- вставляем станцию пересадки с указанием второго маршрута в качестве pm_num
        INSERT INTO TMarshrut (m_num, station_id, order1, pm_num)
        VALUES (m_num, crossing.station_id, crossing.order1, crossing.pm_num2);

        m_num := m_num + 1; 
    END LOOP;
END $$;

-- добавление поездов
DO $$
DECLARE
    categories VARCHAR[] := ARRAY['passenger seasonal', 'passenger year-round', 'passenger express', 'tourist (commercial)'];
    random_category VARCHAR;
    random_quantity INTEGER;
    random_station_id INTEGER;
    random_m_num INTEGER;
BEGIN
    FOR i IN 1..30 LOOP 
        random_category := categories[1 + floor(random() * 4)];

        random_quantity := floor(random() * 201) + 100;

        SELECT id INTO random_station_id FROM Stations ORDER BY random() LIMIT 1;
        SELECT m_num INTO random_m_num FROM Marshrut ORDER BY random() LIMIT 1;

        INSERT INTO Trains (category, quantity, station_id, m_num)
        VALUES (random_category, random_quantity, random_station_id, random_m_num);
    END LOOP;
END $$;

-- добавление поездов/работников
DO $$
DECLARE
    random_train_num INTEGER;
    random_empl_id INTEGER;
BEGIN
    FOR i IN 1..30 LOOP 
        SELECT num INTO random_train_num FROM Trains ORDER BY random() LIMIT 1;
        -- выбор случайного empl_id из таблицы Employees, у которого совпадает station_id со станцией поезда
        SELECT id INTO random_empl_id FROM Employees WHERE station_id = (SELECT station_id FROM Trains WHERE num = random_train_num) ORDER BY random() LIMIT 1;

        IF random_empl_id IS NULL THEN
            CONTINUE;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM Train_Empl WHERE train_num = random_train_num AND empl_id = random_empl_id) THEN
            INSERT INTO Train_Empl (train_num, empl_id)
            VALUES (random_train_num, random_empl_id);
        END IF;
    END LOOP;
END $$;


-- генерация случайной даты
CREATE OR REPLACE FUNCTION random_timestamp(start_time TIMESTAMP, end_time TIMESTAMP)
RETURNS TIMESTAMP AS $$
BEGIN
    RETURN start_time + (random() * (end_time - start_time));
END;
$$ LANGUAGE plpgsql;

-- заполняем расписание
DO $$
DECLARE
    cur_marsh INTEGER;
    cur_station INTEGER;
    cur_napr BOOLEAN;
    cur_dt1 TIMESTAMP := CURRENT_TIMESTAMP;
    cur_dt2 TIMESTAMP := CURRENT_TIMESTAMP;
    cur_id INTEGER;
    tickets_count INTEGER;
    train INTEGER;
BEGIN
    FOR cur_marsh IN (SELECT DISTINCT m_num FROM Marshrut) LOOP
        cur_napr := random() < 0.5;

        SELECT num INTO train
        FROM Trains WHERE m_num = cur_marsh 
        ORDER BY random() LIMIT 1;

        SELECT MAX(id) INTO cur_id 
        FROM Timetable WHERE train_num = train;
        cur_id := COALESCE(cur_id, 0) + 1;

        SELECT quantity INTO tickets_count
        FROM Trains WHERE num = train;

        FOR cur_station IN (SELECT station_id FROM Marshrut WHERE m_num = cur_marsh ORDER BY order1 ASC) LOOP
            cur_dt1 := random_timestamp(cur_dt2 + INTERVAL '15 minutes', cur_dt2 + INTERVAL '5 days');
            cur_dt2 := random_timestamp(cur_dt1 + INTERVAL '5 minutes', cur_dt1 + INTERVAL '15 minutes');

            INSERT INTO Timetable(id, train_num, station_id, dt1, dt2, napr, tickets)
            VALUES (cur_id, train, cur_station, cur_dt1, cur_dt2, cur_napr, tickets_count);

        END LOOP;
    END LOOP;
END $$;

-- добавление записей в таблицу Waitings
DO $$
DECLARE
    timetable_notes RECORD;
    random_interval INTERVAL;
BEGIN
    FOR timetable_notes IN (SELECT train_num, dt1, napr FROM Timetable) LOOP
        IF random() < 0.1 THEN
            random_interval:= (RANDOM() * (interval '5 hours' - interval '15 minutes') + interval '15 minutes')::INTERVAL;

            INSERT INTO Waitings(train_num, dt, napr, val)
            VALUES (timetable_notes.train_num, timetable_notes.dt1, timetable_notes.napr, random_interval);
        END IF;
    END LOOP;
END $$;

-- добавление билетов
DO $$
DECLARE
    rec RECORD;
    _m_num INTEGER;
    order_src INTEGER;
    order_dst INTEGER;
    order_max INTEGER;
BEGIN
    FOR rec IN
        SELECT t1.id, t1.train_num, t1.station_id AS station_id_src, t2.station_id AS station_id_dst
        FROM Timetable t1
        JOIN Timetable t2 ON t1.id = t2.id AND t1.train_num = t2.train_num
        WHERE t1.station_id <> t2.station_id
        AND t1.dt1 < t2.dt1
    LOOP
        INSERT INTO Tickets (timetable_id, train_num, station_id_src, station_id_dst)
        VALUES (rec.id, rec.train_num, rec.station_id_src, rec.station_id_dst);
    END LOOP;
END $$;