-- Написать SQL-запрос (без PL/SQL) формирования кумулятивного отчёта по датам, 
-- содержащий число перевезённых от даты начала отчёта до текущей даты пассажиров 
-- с подведением итогов по кварталам и по годам.

SELECT Tim.dt1::DATE AS trip_date,
       EXTRACT(QUARTER FROM Tim.dt1) AS trip_quarter,
       EXTRACT(YEAR FROM Tim.dt1) AS trip_year,
       COUNT(DISTINCT Tim.station_id) AS num_trips,
       COUNT(DISTINCT Tic.id) AS num_passengers,
       SUM(D.distance_km) * num_passengers AS passengers_kms
FROM Tickets Tic

JOIN Timetable Tim ON Tic.timetable_id = Tim.id 
AND Tic.train_num = Tim.train_num

JOIN Distance D ON D.src_id = Tim.station_id

WHERE Tim.dt1 <= (SELECT dt1 FROM Timetable WHERE station_id = Tic.station_id_src)
AND Tim.dt1 < (SELECT dt1 FROM Timetable WHERE station_id = Tic.station_id_dst)
AND D.dst_id = (SELECT station_id 
                FROM Marshrut M
                JOIN Train Tr ON Tr.m_num = M.m_num
                WHERE Tr.num = Tim.train_num
                AND M.order1 = (SELECT order1 + 1 FROM Marshrut M2 WHERE M2.m_num = M.m_num AND M2.station_id = Tim.station_id))

GROUP BY ROLLUP (trip_year, trip_quarter, trip_date)
ORDER BY trip_year, trip_quarter, trip_date;

-- 2 вариант
SELECT 
    Tim.dt1::DATE AS trip_date,
    EXTRACT(QUARTER FROM Tim.dt1) AS trip_quarter,
    EXTRACT(YEAR FROM Tim.dt1) AS trip_year,
    COUNT(DISTINCT Tim.station_id) AS num_trips,
    COUNT(Tic.id) AS num_passengers,
    SUM(D.distance_km) * num_passengers AS passengers_kms
FROM 
    Tickets Tic
JOIN 
    Timetable Tim ON Tic.timetable_id = Tim.id 
    AND Tic.train_num = Tim.train_num
JOIN 
    Distance D ON D.src_id = Tim.station_id
WHERE 
    EXISTS (
        SELECT 1
        FROM Timetable AS t_src
        JOIN Timetable AS t_dst ON t_dst.train_num = t_src.train_num AND t_dst.dt1 > t_src.dt1
        JOIN Marshrut AS M ON M.m_num = (
            SELECT Tr.m_num
            FROM Train AS Tr
            WHERE Tr.num = Tim.train_num
        )
        WHERE t_src.station_id = Tic.station_id_src
        AND t_dst.station_id = Tic.station_id_dst
        AND t_src.station_id = Tim.station_id
        AND t_dst.station_id = D.dst_id
    )
    AND EXISTS (
        SELECT 1
        FROM Marshrut AS M1
        JOIN Marshrut AS M2 ON M1.m_num = M2.m_num AND M1.order1 + 1 = M2.order1
        WHERE M1.station_id = Tim.station_id AND M2.station_id = D.dst_id
    )
GROUP BY 
    ROLLUP (trip_year, trip_quarter, trip_date)
ORDER BY 
    trip_year, trip_quarter, trip_date;

-- 3 вариант
WITH StationDistance AS (
    SELECT
        Tim.dt1::DATE AS trip_date,
        EXTRACT(QUARTER FROM Tim.dt1) AS trip_quarter,
        EXTRACT(YEAR FROM Tim.dt1) AS trip_year,
        Tim.station_id AS station_id,
        LEAD(Tim.station_id) OVER (PARTITION BY Tim.train_num ORDER BY Tim.dt1) AS next_station_id,
        D.distance_km AS distance
    FROM 
        Timetable Tim
    JOIN 
        Distance D ON D.src_id = Tim.station_id
    WHERE
        EXISTS (
            SELECT 1
            FROM Timetable AS t_src
            JOIN Timetable AS t_dst ON t_dst.train_num = t_src.train_num AND t_dst.dt1 > t_src.dt1
            JOIN Marshrut AS M ON M.m_num = (
                SELECT Tr.m_num
                FROM Train AS Tr
                WHERE Tr.num = Tim.train_num
            )
            WHERE t_src.station_id = Tim.station_id
            AND t_dst.station_id = D.dst_id
        )
)
SELECT 
    trip_date,
    trip_quarter,
    trip_year,
    COUNT(DISTINCT station_id) AS num_trips,
    COUNT(Tic.id) AS num_passengers,
    SUM(distance) * COUNT(Tic.id) AS passengers_kms
FROM 
    StationDistance SD
JOIN 
    Tickets Tic ON Tic.timetable_id IN (
        SELECT id
        FROM Timetable
        WHERE train_num = SD.train_num
        AND station_id BETWEEN SD.station_id AND COALESCE(SD.next_station_id, SD.station_id)
    )
GROUP BY 
    trip_date, trip_quarter, trip_year
ORDER BY 
    trip_year, trip_quarter, trip_date;



-- Написать запрос из п.1 с выводом данных при помощи PL/SQL в 
-- консоль и только с использованием простейших SQL-запросов 
-- (нет вложенности, коррелированности и т.д.). 
-- Примечание: максимально учесть производительность, 
-- данные выбирать пакетно в коллекцию (не обрабатывать их динамически). 
-- Предусмотреть возможные исключения.
DECLARE
    TYPE DataRec IS RECORD (
        trip_date DATE,
        trip_quarter NUMBER,
        trip_year NUMBER,
        num_passengers NUMBER,
        passengers_kms NUMBER
    );
    
    TYPE DataArray IS TABLE OF DataRec INDEX BY PLS_INTEGER;
    
    l_data DataArray;
BEGIN
    SELECT 
        Tim.dt1 AS trip_date,
        EXTRACT(QUARTER FROM Tim.dt1) AS trip_quarter,
        EXTRACT(YEAR FROM Tim.dt1) AS trip_year,
        COUNT(Tic.id) AS num_passengers,
        SUM(D.distance_km) AS passengers_kms
    BULK COLLECT INTO l_data
    FROM 
        Tickets Tic
    JOIN 
        Timetable Tim ON Tic.timetable_id = Tim.id 
        AND Tic.train_num = Tim.train_num
    JOIN 
        Distance D ON D.src_id = Tim.station_id
    WHERE 
        EXISTS (
            SELECT 1
            FROM Timetable AS t_src
            JOIN Timetable AS t_dst ON t_dst.train_num = t_src.train_num AND t_dst.dt1 > t_src.dt1
            JOIN Marshrut AS M ON M.m_num = (
                SELECT Tr.m_num
                FROM Train AS Tr
                WHERE Tr.num = Tim.train_num
            )
            WHERE t_src.station_id = Tic.station_id_src
            AND t_dst.station_id = Tic.station_id_dst
            AND t_src.station_id = Tim.station_id
            AND t_dst.station_id = D.dst_id
        )
    GROUP BY 
        Tim.dt1, EXTRACT(QUARTER FROM Tim.dt1), EXTRACT(YEAR FROM Tim.dt1);

    FOR i IN 1..l_data.COUNT LOOP
        DBMS_OUTPUT.PUT_LINE('Trip Date: ' || l_data(i).trip_date || 
                             ', Quarter: ' || l_data(i).trip_quarter || 
                             ', Year: ' || l_data(i).trip_year || 
                             ', Num Passengers: ' || l_data(i).num_passengers || 
                             ', Passengers Kms: ' || l_data(i).passengers_kms);
    END LOOP;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('An error occurred: ' || SQLERRM);
END;


-- Написать функцию, которая выбирает все поезда из расписания, 
-- которые задерживаются на заданную в аргументах функции дату, 
-- исправляет время прибытия по расписанию в соответствии с задержкой 
-- и обнуляет задержку. Примечание: все данные обрабатывать надо динамически 
-- (без использования коллекций). Предусмотреть возможные исключения.
CREATE OR REPLACE FUNCTION update_delays(date_to_check DATE) RETURNS VOID AS $$
DECLARE
    train_cursor CURSOR FOR
        SELECT train_num, val, dt
        FROM Waitings
        WHERE dt = date_to_check;
BEGIN
    FOR train_record IN train_cursor LOOP
        UPDATE Timetable
        SET 
            dt1 = dt1 + train_record.val, 
            dt2 = dt2 + train_record.val
        WHERE train_num = train_record.train_num
        AND dt1 = train_record.dt;
        
        DELETE FROM Waitings
        WHERE CURRENT OF train_cursor;
    END LOOP;
    
    RETURN;
EXCEPTION
    WHEN others THEN
        RAISE EXCEPTION 'Ошибка при обновлении задержек: %', SQLERRM;
END;
$$ LANGUAGE plpgsql;

SELECT update_delays('2024-04-05');