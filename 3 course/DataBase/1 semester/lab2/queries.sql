--1. Вывод серийного номера компьютера и его стоимость.
SELECT DISTINCT C.serial_number, Cc.price_computer
FROM Computers C
LEFT JOIN Computers_components Cc ON C.serial_number = Cc.serial_number;

SELECT DISTINCT CC.serial_number, 
                SUM(C.price) OVER (PARTITION BY serial_number) AS price_ 
FROM Computers_components CC LEFT JOIN Components C ON CC.component_id = C.component_id;
--GROUP BY эффективнее

SELECT CC.serial_number, SUM(C.price) AS price_ 
FROM Computers_components CC LEFT JOIN Components C ON CC.component_id = C.component_id
GROUP BY serial_number;

--2. Найти для заданного комплектующего замену.
SELECT * 
FROM Components C
WHERE C.category_id = (
    SELECT C1.category_id 
    FROM Components C1
    WHERE C1.component_id = 7)
AND C.component_id <> 7;

--3. Найти самое дешевое комплектующее для каждой категории.
SELECT Cat.category_id, Cat.category_name, Com.component_id, Com.component_name, Com.price 
FROM Category Cat
LEFT JOIN Components Com ON Cat.category_id = Com.category_id
WHERE (Com.category_id, Com.price) IN (
    SELECT category_id, MIN(price)
    FROM Components
    GROUP BY category_id
);

SELECT Cat.category_id, 
       Cat.category_name, 
       Com.component_name, 
       Com.price, 
       FIRST_VALUE(Com.component_id) over (PARTITION BY Cat.category_id ORDER BY Com.price) AS min_price_id
FROM Category Cat LEFT JOIN Components Com ON Cat.category_id = Com.category_id;

--4. Вывести комплектующие, которые находятся на первых 3 местах по уровню востребованности (наиболее часто используемые во всех собранных компьютерах). Примечание: если уровень востребованности у двух комплектующих одинаковый, то обе находят-ся на одном месте.
WITH Components_counts AS (
    SELECT component_id, COUNT(*) AS component_count 
    FROM Computers_components
    GROUP BY component_id)     
SELECT popular_level, component_name, component_id, component_count FROM (
    SELECT DENSE_RANK() OVER (ORDER BY CC.component_count DESC) AS popular_level, 
        C.component_name, 
        C.component_id, 
        CC.component_count
    FROM Components_counts CC JOIN Components C ON CC.component_id = C.component_id)
WHERE popular_level <= 3;

--5. Вывести компьютеры с рентабельностью свыше 30% (цена продажи на 30% больше стоимости производства).
WITH Computers_components_price AS (
    SELECT CC.serial_number, CC.price_computer, CC.component_id, C.price AS component_price
    FROM Computers_components CC LEFT JOIN Components C ON CC.component_id = C.component_id
)
SELECT DISTINCT serial_number, price_computer, sum_price_components
FROM (SELECT serial_number, 
       price_computer,
       component_id,
       component_price,
       SUM(component_price) OVER (PARTITION BY serial_number) AS sum_price_components
FROM Computers_components_price) 
WHERE price_computer >= (sum_price_components * 1.3);

WITH Computers_components_price AS (
    SELECT CC.serial_number, CC.price_computer, CC.component_id, C.price AS component_price
    FROM Computers_components CC LEFT JOIN Components C ON CC.component_id = C.component_id
)
SELECT serial_number, 
       price_computer,
       SUM(component_price) AS sum_price_components
FROM Computers_components_price 
WHERE price_computer >= (sum_price_components * 1.3)
GROUP BY serial_number, price_computer;

