-- Таблица "Employees"
CREATE TABLE Employees (
    employee_id SERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    position VARCHAR(255) NOT NULL,
    manager_id INTEGER REFERENCES Employees(employee_id)
);

-- Таблица "CreditRates"
CREATE TABLE CreditRates (
    rate_id SERIAL PRIMARY KEY,
    credit_type VARCHAR(255) NOT NULL,
    min_term_in_months INTEGER NOT NULL CHECK (min_term_in_months > 0),
    max_term_in_months INTEGER NOT NULL CHECK(max_term_in_months >= min_term_in_months),
    interest_rate DECIMAL(3, 2) NOT NULL,
    min_amount DECIMAL(10, 2) NOT NULL,
    max_amount DECIMAL(10, 2) NOT NULL CHECK(max_amount >= min_amount)
);

-- Таблица "DepositRates"
CREATE TABLE DepositRates (
    rate_id SERIAL PRIMARY KEY,
    deposit_type VARCHAR(255) NOT NULL,
    min_term_in_months INTEGER NOT NULL CHECK (min_term_in_months > 0),
    max_term_in_months INTEGER NOT NULL CHECK (max_term_in_months >= min_term_in_months),
    interest_rate DECIMAL(3, 2) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL
);

-- Таблица "Clients"
CREATE TABLE Clients (
    client_id SERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    personal_data VARCHAR(255) NOT NULL,
    employment_type VARCHAR(255) NOT NULL
);

-- Таблица "Scoring"
CREATE TABLE Scoring (
    scoring_id SERIAL PRIMARY KEY,
    credit_rate_id INTEGER REFERENCES CreditRates(rate_id) NOT NULL,
    client_id INTEGER REFERENCES Clients(client_id) NOT NULL,
    credit_status BOOLEAN NOT NULL,
    term_in_month INTEGER NOT NULL,
    amount DECIMAL(10, 2) NOT NULL
);

-- Таблица "Loans"
CREATE TABLE Loans (
    loan_id SERIAL PRIMARY KEY,
    scoring_id INTEGER REFERENCES Scoring(scoring_id) NOT NULL,
    date_of_issue DATE NOT NULL,
    date_of_maturity DATE NOT NULL,
    credit_amount DECIMAL(10, 2) NOT NULL
);

-- Таблица "PaymentSchedule"
CREATE TABLE PaymentSchedule (
    schedule_id SERIAL PRIMARY KEY,
    loan_id INTEGER REFERENCES Loans(loan_id) NOT NULL,
    payment_date DATE NOT NULL,
    payment_amount DECIMAL(10, 2) NOT NULL
);

CREATE TABLE Channels (
    channel_id SERIAL PRIMARY KEY,
    channel_name VARCHAR(255) NOT NULL,
    commission_interest DECIMAL(3, 2) NOT NULL
);

-- Таблица "Payments"
CREATE TABLE Payments (
    payment_id SERIAL PRIMARY KEY,
    loan_id INTEGER REFERENCES Loans(loan_id) NOT NULL,
    channel_id INTEGER REFERENCES Channels(channel_id) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    client_payment_date DATE NOT NULL
);

