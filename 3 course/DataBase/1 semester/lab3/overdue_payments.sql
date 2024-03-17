--1 ВАРИАНТ
WITH OverdueLoans AS (SELECT
  DISTINCT ps.loan_id AS overdue_loans, --дистинкт для того чтобы считать именно просроченные кредиты, а не платежи
  l.credit_amount AS credit_amount
FROM
  PaymentSchedule ps
LEFT JOIN
  LATERAL (
    SELECT COALESCE(SUM(payment_amount), 0) AS cumulative_payment
    FROM PaymentSchedule
    WHERE loan_id = ps.loan_id AND payment_date < ps.payment_date
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

-----------------------------------------------------------------------
--2 ВАРИАНТ

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
