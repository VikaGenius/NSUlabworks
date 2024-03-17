CREATE OR REPLACE FUNCTION check_credit_status()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM Scoring
        WHERE scoring_id = NEW.scoring_id AND credit_status = true
    ) THEN
        RAISE EXCEPTION 'Credit status is false for the specified scoring_id';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER before_insert_loans
BEFORE INSERT ON Loans
FOR EACH ROW
EXECUTE FUNCTION check_credit_status();

------------------------

-- Триггер для создания расписания платежей при вставке нового кредита
CREATE OR REPLACE FUNCTION generate_payment_schedule()
RETURNS TRIGGER AS $$
DECLARE
    amount DECIMAL(10, 2);
    monthly_interest_rate DECIMAL(10, 8);
    annuity_coefficient DECIMAL(10, 8);
    total_payments INTEGER;
    payment_date DATE;
BEGIN
    -- Получаем сумму кредита
    SELECT credit_amount INTO amount
    FROM Loans
    WHERE loan_id = NEW.loan_id;

    -- Получаем процентную ставку из CreditRates
    SELECT interest_rate INTO monthly_interest_rate
    FROM CreditRates
    WHERE rate_id = (SELECT credit_rate_id FROM Scoring WHERE scoring_id = NEW.scoring_id);

    -- Преобразуем годовую процентную ставку в месячную
    monthly_interest_rate := monthly_interest_rate / 100 / 12;

    -- Получаем срок кредита в месяцах из Scoring
    total_payments := (SELECT term_in_month FROM Scoring WHERE scoring_id = NEW.scoring_id);

    -- Рассчитываем коэффициент аннуитета
    annuity_coefficient := (monthly_interest_rate * POWER((1 + monthly_interest_rate), total_payments)) /
                           (POWER((1 + monthly_interest_rate), total_payments) - 1);

    -- Удаляем предыдущее расписание платежей, если оно существует
    DELETE FROM PaymentSchedule WHERE loan_id = NEW.loan_id;

    -- Вставляем новое расписание платежей
    FOR i IN 1..total_payments LOOP
        payment_date := NEW.date_of_issue + INTERVAL '1 month' * (i - 1);
        INSERT INTO PaymentSchedule (loan_id, payment_date, payment_amount)
        VALUES (NEW.loan_id, payment_date, amount * annuity_coefficient);
    END LOOP;
    --bulk-операции (массовые) сделают все за одну транзакцию!!!!!!!!!!!!!!!!1

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Создаем триггер
CREATE TRIGGER generate_payment_schedule_trigger
AFTER INSERT ON Loans
FOR EACH ROW
EXECUTE FUNCTION generate_payment_schedule();


