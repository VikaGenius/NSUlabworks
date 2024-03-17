--a. Объём выдач по кол-ву и сумме по кредитам, выданным за период, добавить столбцы 
--<Средняя сумма выданного кредита в данном кредитном тарифе> и <Клиент с самым большим 
--выданным кредитом в данном кредитном тарифе>.

SELECT DISTINCT cr.rate_id, 
    COUNT(*) OVER (PARTITION BY cr.rate_id) AS number_of_loans,
    SUM (l.credit_amount) OVER (PARTITION BY cr.rate_id) AS total_amount,
    AVG(l.credit_amount) OVER (PARTITION BY cr.rate_id) AS average_amount,
    MAX(l.credit_amount) OVER (PARTITION BY cr.rate_id) AS max_amount,
    FIRST_VALUE(c.full_name) OVER (PARTITION BY cr.rate_id ORDER BY l.credit_amount DESC) AS max_amount_client
FROM Loans l JOIN Scoring s ON l.scoring_id = s.scoring_id
    JOIN CreditRates cr ON s.credit_rate_id = cr.rate_id
    JOIN Clients c ON s.client_id = c.client_id
WHERE l.date_of_issue BETWEEN '2014-09-17' AND '2023-12-01';
------------------------------------------------------------------------------------------

--b. Объём возвратов по кол-ву и сумме по кредитам, выданным за период, на момент: 
--текущий день (т.е. платежи считаем за всё время). Отчёт с агрегацией последовательно 
--по <дням => месяцам => годам, итого за весь период> (см. ROLLUP).

SELECT 
    client_payment_date AS payment_date,
    TO_CHAR(client_payment_date, 'YYYY-MM') AS month,
    EXTRACT(YEAR FROM client_payment_date) AS year,
    COUNT(payment_id) AS number_of_payments,
    SUM(amount) AS amount_payments
FROM Payments
GROUP BY ROLLUP(year, month, client_payment_date) --собираем дату по кусочкам так сказать, объединение
ORDER BY client_payment_date, month, year;

-- ROLLUP ( e1, e2, e3, ... ) эквивалентно записи ниже
-- GROUPING SETS (
--     ( e1, e2, e3, ... ),
--     ...
--     ( e1, e2 ),
--     ( e1 ),
--     ( )
-- )


---------------------------------------------------------------------
--c. Список сотрудников с иерархией от самого высокого рук-ля (может быть несколько, это граф) до всех нижних. Если не хочется 5, то можно вывести иерархию по одному заданному сотруднику: от него до самого верха.

--1 вариант
WITH RECURSIVE EmployeeHierarchy AS (
    SELECT
        employee_id,
        full_name,
        position,
        manager_id,
        1 AS level,
        ARRAY[employee_id] AS path
    FROM Employees
    WHERE manager_id IS NULL -- начинаем с верхнего уровня (где manager_id IS NULL)
    UNION ALL
    SELECT
        e.employee_id,
        e.full_name,
        e.position,
        e.manager_id,
        eh.level + 1 AS level,
        path || e.employee_id AS path
    FROM Employees e
    INNER JOIN EmployeeHierarchy eh ON e.manager_id = eh.employee_id
)
SELECT 
    employee_id,
    full_name,
    position,
    manager_id,
    level,
    path
FROM EmployeeHierarchy
ORDER BY level, manager_id NULLS FIRST, employee_id;

--2 вариант 
--используя RECURSIVE, запрос WITH может обращаться к собственному результату
WITH RECURSIVE EmployeeHierarchy AS (
    --сначала выбираем сотрудниов первого уровня (ищем самую крупную шишку, владельца бизнеса, или нескольких владельце)
    SELECT
        employee_id,
        manager_id,
        1 AS level,
        employee_id::TEXT AS path
    FROM Employees
    WHERE manager_id IS NULL 

    UNION ALL

    SELECT
        e.employee_id,
        e.manager_id,
        eh.level + 1 AS level,
        e.manager_id || '->' || e.employee_id::TEXT AS path
    FROM Employees e
    INNER JOIN EmployeeHierarchy eh ON e.manager_id = eh.employee_id
)

SELECT 
    level,
    ARRAY_AGG(path ORDER BY path) AS employees_on_level
FROM EmployeeHierarchy
GROUP BY level;

---------------------------------------------------------------------
--d. Процент по кол-ву и по суммам просроченных (в текущий момент просрочен любой из платежей в Графике) кредитов с датой погашения, попадающий в заданный период.

SELECT COUNT(l.loan_id)::DOUBLE PRECISION / (SELECT COUNT(*) FROM Loans)::DOUBLE PRECISION * 100 AS percentage_count_overdue_loans, 
    SUM(l.credit_amount) / (SELECT SUM(credit_amount) FROM Loans) * 100 AS percentage_amount_overdue_loans
FROM Loans l
JOIN LATERAL ( --LATERAL перед подзапросами позволяет ссылаться в них на столбцы предшествующих элементов списка FROM
    SELECT SUM(p.amount)
    FROM Payments p
    WHERE p.loan_id = l.loan_id AND p.payment_date 
) AS amount_payments ON true
JOIN LATERAL (
    SELECT SUM(ps.payment_amount)
    FROM PaymentSchedule ps
    WHERE ps.loan_id = l.loan_id
) AS waited_amount_payments ON true
WHERE l.date_of_maturity BETWEEN '2014-09-17' AND '2023-12-01'
AND amount_payments < waited_amount_payments;

WITH OverdueLoans AS (SELECT
  DISTINCT ps.loan_id AS overdue_loans, --дистинкт для того чтобы считать именно просроченные кредиты, а не платежи
  l.credit_amount AS credit_amount
FROM
  PaymentSchedule ps
LEFT JOIN
  LATERAL (
    SELECT COALESCE(SUM(payment_amount), 0) AS cumulative_payment
    FROM PaymentSchedule
    WHERE loan_id = ps.loan_id AND payment_date <= ps.payment_date
  ) AS waited_payments ON true --считаем кумулятивную сумму всех платежей по графику до текущего
LEFT JOIN
  LATERAL (
    SELECT COALESCE(SUM(amount), 0) AS cumulative_payments
    FROM Payments
    WHERE loan_id = ps.loan_id AND client_payment_date < ps.payment_date
  ) AS payments ON true --считаем кумулятивную сумму всех платежей клиента до текущей даты по графику
  JOIN Loans l ON ps.loan_id = l.loan_id
WHERE
  l.date_of_maturity BETWEEN '2014-09-17' AND '2023-12-01' AND
  payments <= waited_payments -- сравниваем все платежи клиента до даты выплаты по графику и все платежи по графику до этой же даты (таким образом проверяем, не оплатил ли клиент кредит раньше срока)
)
SELECT (SELECT COUNT(*) FROM OverdueLoans)::DOUBLE PRECISION / 
       (SELECT COUNT(loan_id) FROM Loans WHERE date_of_maturity BETWEEN '2014-09-17' AND '2023-12-01')::DOUBLE PRECISION * 100 AS percentage_count_overdue_loans,
       (SELECT SUM(credit_amount) FROM OverdueLoans)::DOUBLE PRECISION / SUM(credit_amount)::DOUBLE PRECISION * 100 AS percentage_amount_overdue_loans
FROM Loans 
WHERE date_of_maturity BETWEEN '2014-09-17' AND '2023-12-01';

WITH OverdueLoans AS (
  SELECT
    DISTINCT ps.loan_id,
    l.credit_amount,
    COALESCE(SUM(ps.payment_amount) OVER (PARTITION BY ps.loan_id ORDER BY ps.payment_date), 0) AS cumulative_payment_schedule, ----считаем кумулятивную сумму всех платежей по графику до текущего
    COALESCE(SUM(p.amount) OVER (PARTITION BY ps.loan_id ORDER BY ps.payment_date), 0) AS cumulative_payment_client ----считаем кумулятивную сумму всех платежей клиента до текущей даты по графику
  FROM
    PaymentSchedule ps
  JOIN
    Loans l ON ps.loan_id = l.loan_id
  LEFT JOIN
    Payments p ON ps.loan_id = p.loan_id AND p.client_payment_date <= ps.payment_date
  WHERE
    l.date_of_maturity BETWEEN '2014-09-17' AND '2023-12-01'
    AND cumulative_payment_client < cumulative_payment_schedule -- сравниваем все платежи клиента до даты выплаты по графику и все платежи по графику до этой же даты (таким образом проверяем, не оплатил ли клиент кредит раньше срока)
)
SELECT
  (SELECT COUNT(loan_id) FROM OverdueLoans)::DOUBLE PRECISION / 
  (SELECT COUNT(loan_id) FROM Loans WHERE date_of_maturity BETWEEN '2014-09-17' AND '2023-12-01')::DOUBLE PRECISION * 100 AS percentage_count_overdue_loans,
  (SELECT SUM(credit_amount) FROM OverdueLoans)::DOUBLE PRECISION / 
   SUM(credit_amount)::DOUBLE PRECISION * 100 AS percentage_amount_overdue_loans
FROM Loans 
WHERE date_of_maturity BETWEEN '2014-09-17' AND '2023-12-01';




--надо по партициям разбивать по месяцам и смотреть по графику (если хотя бы по одному просрочен, значит весь кредит просрочен)!!!!!!!!!!!

---------------------------------------------------------------------

--e. Итоговая прибыльность (<все платежи по кредитам с плановой датой погашения в заданный период> / 
-- <сумма тел кредитов с плановой датой погашения, попадающей в заданный период>) 
-- на момент <текущий день>.

--1 вариант
SELECT (SELECT SUM(amount) FROM Payments p JOIN Loans l ON p.loan_id = l.loan_id 
    WHERE l.date_of_maturity < CURRENT_DATE) / 
    SUM(credit_amount) AS final_profitability
FROM Loans
WHERE date_of_maturity < CURRENT_DATE;

--2 вариант
SELECT (SELECT SUM(amount) FROM Payments p JOIN Loans l ON p.loan_id = l.loan_id --берем платежи по кредитам
    WHERE l.date_of_maturity BETWEEN '2014-09-17' AND '2023-12-01') / --с плановой датой погашения в заданный период
    SUM(credit_amount) AS final_profitability --делим на сумму тел кредитов
FROM Loans
WHERE date_of_maturity BETWEEN '2014-09-17' AND '2023-12-01'; --с плановой датой погашения в заданный период

------------------------------------------------------------------------

--f. Прибыльность +2M (2 месяца с даты погашения по договору по каждому из кредитов) по кредитам, выданным в заданный период.

SELECT ((SELECT SUM(amount) FROM Payments p -- берем платежи
    JOIN Loans l ON p.loan_id = l.loan_id
    WHERE (l.date_of_issue BETWEEN '2014-09-17' AND '2023-12-01')  AND  --по кредитам, выданным в заданный период
    p.client_payment_date <= l.date_of_maturity + interval '2 months')) / --за срок кредита + 2 месяца с даты погашения
    SUM(credit_amount) AS final_profitability_plus_2month --делим на сумму тел кредитов
FROM Loans
WHERE date_of_issue BETWEEN '2014-09-17' AND '2023-12-01'; --выданных в заданный период

-------------------------------------------------------------------------

