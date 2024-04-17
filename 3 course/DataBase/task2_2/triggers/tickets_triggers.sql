CREATE OR REPLACE FUNCTION check_ticket_availability(stations INTEGER[], _m_num INTEGER,
                                                     timetable_id INTEGER, _train_num INTEGER, station_id_dst INTEGER)
RETURNS BOOLEAN AS $$
DECLARE
    tickets_count INTEGER;
    cur_station INTEGER;
    i INTEGER;
BEGIN
    FOR i IN 1..array_length(stations, 1) LOOP
        cur_station := stations[i];

        SELECT tickets INTO tickets_count 
        FROM Timetable
        WHERE id = timetable_id
        AND train_num = _train_num
        AND station_id = cur_station;

        IF tickets_count = 0 THEN
            RETURN false;
        END IF;

    END LOOP;
    RETURN true;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION fix_buying_a_ticket()
RETURNS TRIGGER AS $$
DECLARE
    stations INTEGER[];
    cur_station INTEGER;
    _m_num INTEGER;
    _napr BOOLEAN;
    begin_order INTEGER;
    end_order INTEGER;
    i INTEGER;
BEGIN
    SELECT m_num INTO _m_num
    FROM Trains
    WHERE num = NEW.train_num;

    SELECT napr INTO _napr
    FROM Timetable
    WHERE id = NEW.timetable_id 
    AND train_num = NEW.train_num
    LIMIT 1;

    SELECT order1 INTO begin_order FROM Marshrut
    WHERE m_num = _m_num AND station_id = NEW.station_id_src;

    SELECT order1 INTO end_order FROM Marshrut
    WHERE m_num = _m_num AND station_id = NEW.station_id_dst;

 
    SELECT ARRAY(SELECT station_id 
                 FROM Marshrut 
                 WHERE m_num = _m_num 
                 AND order1 >= begin_order
                 AND order1 < end_order) INTO stations;

    IF stations IS NULL THEN
        RAISE NOTICE 'Нельзя приобрести такой билет';
        RETURN NULL;
    END IF;

    IF check_ticket_availability(stations, _m_num, 
       NEW.timetable_id, NEW.train_num, NEW.station_id_dst) = false THEN
       RAISE NOTICE 'Билеты закончились';
    END IF;

    FOR i IN 1..array_length(stations, 1) LOOP
        cur_station := stations[i];

        UPDATE Timetable
        SET tickets = tickets - 1
        WHERE id = NEW.timetable_id
        AND train_num = NEW.train_num
        AND station_id = cur_station;
        
    END LOOP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE TRIGGER buying_a_ticket_trigger
BEFORE INSERT ON Tickets
FOR EACH ROW
EXECUTE FUNCTION fix_buying_a_ticket();