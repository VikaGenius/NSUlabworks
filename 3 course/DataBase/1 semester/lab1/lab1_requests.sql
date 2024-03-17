--Реализовать следующие запросы:

--a. Выбрать всех студентов с фамилией, начинающейся с буквы, задаваемой в запросе.
SELECT * FROM Students WHERE last_name LIKE 'S%'

--b. Найти всех студентов-однофамильцев.
SELECT last_name, first_name 
FROM students WHERE last_name IN (
SELECT last_name
FROM students
GROUP BY last_name
HAVING COUNT(*) > 1);

SELECT S1.first_name, S1.last_name
FROM Students S1
JOIN Students S2 ON S1.last_name = S2.last_name AND S1.student_id <> S2.student_id;

--c. Список всех студентов у преподавателя.
SELECT last_name, first_name, group_number
FROM Students  
WHERE group_number IN (
    SELECT group_number
    FROM Schedule
    WHERE teacher_id = 5
);

--d. Найти группы, в которых нет старосты.
SELECT group_number FROM Groups WHERE headman_id IS NULL;

--e. Вывести все группы и среднюю успеваемость в них.
SELECT G.group_number, AVG(AP.mark) AS average_performance
FROM Groups G
LEFT JOIN Students S ON G.group_number = S.group_number
LEFT JOIN Academic_performance AP ON S.student_id = AP.student_id
GROUP BY G.group_number;

SELECT G.group_number, 
       CASE 
           WHEN AVG(AP.mark) IS NOT NULL THEN AVG(AP.mark)
           ELSE 0
       END AS average_performance
FROM Groups G
LEFT JOIN Students S ON G.group_number = S.group_number
LEFT JOIN Academic_performance AP ON S.student_id = AP.student_id
GROUP BY G.group_number;

--f. Вывести N лучших студентов по ср. баллу (N – параметр запроса).
SELECT last_name, first_name, AVG(mark) AS average_mark
FROM Students S
JOIN Academic_performance AP ON S.student_id = AP.student_id
GROUP BY S.student_id
ORDER BY average_mark DESC
LIMIT N;


--g. Выбрать группу с самой высокой успеваемостью.
SELECT G.group_number, AVG(AP.mark) AS average_performance
FROM Groups G
JOIN Students S ON G.group_number = S.group_number
JOIN Academic_performance AP ON S.student_id = AP.student_id
GROUP BY G.group_number
ORDER BY average_performance DESC
LIMIT 1;


--h. Посчитать количество студентов у каждого преподавателя.
SELECT T.full_name AS teacher_name, COUNT(DISTINCT S.student_id) AS student_count
FROM Teachers T
LEFT JOIN Schedule Sch ON T.teacher_id = Sch.teacher_id
LEFT JOIN Students S ON Sch.group_number = S.group_number
GROUP BY T.teacher_id, T.full_name;


--i. Выбрать преподавателей, у которого студентов-отличников больше 10.
SELECT T.teacher_id, T.full_name
FROM Teachers AS T
WHERE (
    SELECT COUNT(DISTINCT S.student_id)
    FROM Students AS S
    JOIN Academic_performance AS A ON S.student_id = A.student_id
    WHERE A.teacher_id = T.teacher_id
    GROUP BY S.student_id
    HAVING AVG(A.mark) = 5
) > 10;

--разобраться с коррелированными запросами
--Коррелированным подзапросом называется подзапрос, который ссылается на значения столбцов внешнего запроса. 
--Коррелированный подзапрос выполняется для каждой строки основного запроса. 
--В момент выполнения подзапроса значения столбцов внешнего запроса являются константами.

