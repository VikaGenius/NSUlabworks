import psycopg2
from faker import Faker
import random
from datetime import datetime, timedelta
from dateutil.relativedelta import relativedelta

db_params = {
    'host': 'localhost',
    'port': '5430',
    'database': 'postgres',
    'user': 'postgres',
    'password': '1234',  
}

conn = psycopg2.connect(**db_params)

cur = conn.cursor()
fake = Faker()

num_clients = 150000
num_rates = 150
num_scoring = 200000
num_loans = 50000
num_emploees = 5000

def random_date(start_date, end_date):
    if isinstance(start_date, datetime):
        start_date = start_date.date()
    if isinstance(end_date, datetime):
        end_date = end_date.date()
    return start_date + timedelta(seconds=random.randint(0, int((end_date - start_date).total_seconds())))

def generate_client_data():
    fake = Faker()
    full_name = fake.name()
    passport_data = fake.ssn()
    employment_types = ['Full-time', 'Part-time', 'Contract', 'Freelance']
    employment_type = random.choice(employment_types)
    return full_name, passport_data, employment_type

def generate_credit_rate_data():
    credit_types = ['Consumer loan', 'Mortgage loan', 'Car loan']
    credit_type = random.choice(credit_types)
    min_term_in_months = random.randint(1, 120)
    max_term_in_months = random.randint(min_term_in_months, min_term_in_months + 120)
    interest_rate = round(random.uniform(1, 10), 2)
    min_amount = round(random.uniform(100000, 1000000), 2)
    max_amount = round(random.uniform(min_amount, min_amount + 9000000), 2)
    return credit_type, min_term_in_months, max_term_in_months, interest_rate, min_amount, max_amount

def generate_scoring_data():
    credit_rate_id = random.randint(1, num_rates) 
    client_id = random.randint(1, num_clients)  
    credit_status = random.choice([True, False])
    cur.execute("SELECT min_term_in_months, max_term_in_months, min_amount, max_amount FROM CreditRates WHERE rate_id = %s", (credit_rate_id,))
    result = cur.fetchone()
    term_in_month = random.randint(result[0], result[1])
    amount = round(random.uniform(float(result[2]), float(result[3])), 2)
    return credit_rate_id, client_id, credit_status, term_in_month, amount

def generate_loans_data():
    #берем случайный scoring_id из таблицы Scoring (одобренную заявку)
    cur.execute("SELECT scoring_id FROM Scoring WHERE credit_status = true ORDER BY random() LIMIT 1")
    scoring_id = cur.fetchone()[0]

    # Получаем credit_rate_id из таблицы Scoring
    cur.execute("SELECT term_in_month, amount FROM Scoring WHERE scoring_id = %s", (scoring_id,))
    result = cur.fetchone()
    term_in_month = result[0]
    credit_amount = result[1]
 
    # Генерируем случайную дату выдачи кредита
    start_date = datetime(2012, 1, 1)
    end_date = datetime(2023, 1, 1)
    date_of_issue = random_date(start_date, end_date)
    date_of_maturity = date_of_issue + relativedelta(months=term_in_month)
  
    return scoring_id, date_of_issue, date_of_maturity, credit_amount

def fill_clients_table(num_records):
    for _ in range(num_records):
        full_name, passport_data, employment_type = generate_client_data()
        cur.execute(
            "INSERT INTO Clients (full_name, personal_data, employment_type) VALUES (%s, %s, %s)",
            (full_name, passport_data, employment_type)
        )
    conn.commit()

def fill_credit_rates_table(num_records):
    for _ in range(num_records):
        credit_type, min_term_in_months, max_term_in_months, interest_rate, min_amount, max_amount = generate_credit_rate_data()
        cur.execute(
            "INSERT INTO CreditRates (credit_type, min_term_in_months, max_term_in_months, interest_rate, min_amount, max_amount) VALUES (%s, %s, %s, %s, %s, %s)",
            (credit_type, min_term_in_months, max_term_in_months, interest_rate, min_amount, max_amount)
        )
    conn.commit()

def fill_scoring_table(num_records):
    for _ in range(num_records):
        credit_rate_id, client_id, credit_status, term_in_month, amount = generate_scoring_data()
        cur.execute(
            "INSERT INTO Scoring (credit_rate_id, client_id, credit_status, term_in_month, amount) VALUES (%s, %s, %s, %s, %s)",
            (credit_rate_id, client_id, credit_status, term_in_month, amount)
        )
    conn.commit()

def fill_loans_table(num_records):
    for _ in range(num_records):
        scoring_id, date_of_issue, date_of_maturity, credit_amount = generate_loans_data()
        cur.execute(
            "INSERT INTO Loans (scoring_id, date_of_issue, date_of_maturity, credit_amount) VALUES (%s, %s, %s, %s)",
            (scoring_id, date_of_issue, date_of_maturity, credit_amount)
        )
    conn.commit()

def fill_payments_table():
    cur.execute("SELECT * FROM Loans")
    loans = cur.fetchall()
    cur.execute("SELECT * FROM Channels")
    channels = cur.fetchall()

    for loan in loans:
        loan_id, _, date_of_issue, _, credit_amount = loan

        # Получаем оставшуюся сумму для данного кредита
        cur.execute("SELECT COALESCE(SUM(amount), 0) FROM Payments WHERE loan_id = %s", (loan_id,))
        total_payments = cur.fetchone()[0]
        remaining_amount = max(float(credit_amount) - float(total_payments) + 0.05 * float(credit_amount), 0)

        # Получаем случайное количество платежей для данного кредита
        num_payments = random.randint(1, 200)

        for _ in range(num_payments):
            # Если оставшаяся сумма равна 0, выходим из чатика...
            if remaining_amount == 0:
                break

            channel_id = random.choice(channels)[0]
            payment_amount = round(random.uniform(100, float(remaining_amount)), 2)
            client_payment_date = random_date(date_of_issue, datetime.now())

            cur.execute(
                "INSERT INTO Payments (loan_id, channel_id, amount, client_payment_date) VALUES (%s, %s, %s, %s)",
                (loan_id, channel_id, payment_amount, client_payment_date)
            )

            # Пересчитываем оставшуюся сумму после каждого платежа
            remaining_amount = max(float(remaining_amount) - float(payment_amount), 0)

    conn.commit()

def fill_employees_table(num_records):
    # Добавим первого сотрудника, который является владельцем бизнеса и у него нет руководителя
    fake = Faker()
    business_owner_name = fake.name()
    cur.execute(
        "INSERT INTO Employees (full_name, position) VALUES (%s, %s) RETURNING employee_id",
        (business_owner_name, "Owner")
    )
    business_owner_id = cur.fetchone()[0]

    added_employee_ids = [business_owner_id]

    # Заполним остальных сотрудников
    for _ in range(num_records - 1): 
        manager_id = random.choice(added_employee_ids) 
        full_name = fake.name()
        cur.execute(
            "INSERT INTO Employees (full_name, position, manager_id) VALUES (%s, %s, %s) RETURNING employee_id",
            (full_name, "Employee", manager_id)
        )
        new_employee_id = cur.fetchone()[0]
        added_employee_ids.append(new_employee_id)

    conn.commit()

#Заполняем таблицу Employees с иерархией
fill_employees_table(num_emploees)
fill_clients_table(num_clients)
fill_credit_rates_table(num_rates)
fill_scoring_table(num_scoring)
fill_loans_table(num_loans)
fill_payments_table()

cur.close()
conn.close()
