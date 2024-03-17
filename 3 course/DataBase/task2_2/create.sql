CREATE TABLE Stations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE Marshrut (
    m_num INTEGER PRIMARY KEY,
    station_id INTEGER REFERENCES Stations(id) NOT NULL,
    order1 INTEGER --порядок станции внутри маршрута
);

CREATE TABLE TMarshrut ( --склейка маршрутов
    m_num INTEGER PRIMARY KEY,
    station_id INTEGER REFERENCES Stations(id) NOT NULL,
    order1 INTEGER,
    pm_num INTEGER REFERENCES Marshrut(m_num) NOT NULL
);

CREATE TABLE Employees (
    id SERIAL PRIMARY KEY,
    FIO VARCHAR(255) NOT NULL,
    place VARCHAR(255) NOT NULL, --местожительства
    station_id INTEGER REFERENCES Stations(id) NOT NULL
);

CREATE TABLE Trains (
    num SERIAL PRIMARY KEY,
    category VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    station_id INTEGER REFERENCES Stations(id) NOT NULL,
    m_num INTEGER NOT NULL --ссылается на маршрут
);

CREATE TABLE Train_Empl (
    train_num INTEGER REFERENCES Trains(num) NOT NULL,
    empl_id INTEGER REFERENCES Employees(id) NOT NULL
);

CREATE TABLE Waitings (
    id SERIAL PRIMARY KEY,
    train_num INTEGER REFERENCES Trains(num) NOT NULL,
    dt DATE NOT NULL,
    napr VARCHAR(255) NOT NULL,
    value INTEGER NOT NULL --время, на которое задерживается поезд
);

CREATE TABLE TIMETABLE (
    id SERIAL PRIMARY KEY,
    train_num INTEGER REFERENCES Trains(num) NOT NULL,
    station_id INTEGER REFERENCES Stations(id) NOT NULL,
    dt1 DATE NOT NULL,
    dt2 DATE NOT NULL CHECK(dt2 > dt1),
    napr VARCHAR(255) NOT NULL,
    tickets INTEGER NOT NULL --количество купленных или оставшихся билетов (мне нравится 2 варик)
);

--добавить таблицу с билетами
--добавить таблицу с пассажирами
