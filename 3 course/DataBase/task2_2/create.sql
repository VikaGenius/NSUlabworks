CREATE TABLE Stations (
    id SERIAL PRIMARY KEY,
    s_name VARCHAR(255) NOT NULL
);

CREATE TABLE Marshrut (
    m_num INTEGER,
    station_id INTEGER REFERENCES Stations(id),
    order1 INTEGER, --порядок станции внутри маршрута
    PRIMARY KEY(m_num, station_id)
);

CREATE TABLE TMarshrut ( --склейка маршрутов
    m_num INTEGER,
    station_id INTEGER REFERENCES Stations(id),
    order1 INTEGER,
    pm_num INTEGER REFERENCES Marshrut(m_num),
    PRIMARY KEY (m_num, station_id, pm_num)
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
    train_num INTEGER REFERENCES Trains(num),
    empl_id INTEGER REFERENCES Employees(id),
    PRIMARY KEY (train_num, empl_id)
);

CREATE TABLE Waitings (
    id SERIAL PRIMARY KEY,
    train_num INTEGER REFERENCES Trains(num) NOT NULL,
    dt DATE NOT NULL,
    napr VARCHAR(255) NOT NULL,
    val INTEGER NOT NULL --время, на которое задерживается поезд
);

CREATE TABLE Timetable (
    id_ INTEGER,
    train_num INTEGER REFERENCES Trains(num),
    station_id INTEGER REFERENCES Stations(id),
    dt1 TIMESTAMP NOT NULL,
    dt2 TIMESTAMP NOT NULL CHECK(dt2 > dt1),
    napr VARCHAR(255) NOT NULL,
    tickets INTEGER NOT NULL, --количество купленных или оставшихся билетов (мне нравится 2 варик)
    PRIMARY KEY (id, train_num, station_id) --id увеличивается, если уже есть запись в расписании с таким поездом и станцией
);

CREATE TABLE Distance (
    src_id INTEGER REFERENCES Stations(id),
    dst_id INTEGER REFERENCES Stations(id),
    distance_km INTEGER NOT NULL,
    PRIMARY KEY (src_id, dst_id)
);

CREATE TABLE Tickets (
    id SERIAL PRIMARY KEY,
    timetable_id_src INTEGER,
    station_id_src INTEGER,
    train_num INTEGER,
    timetable_id_dst INTEGER,
    station_id_dst INTEGER,
    FOREIGN KEY(timetable_id_src, train_num, station_id_src) REFERENCES Timetable(id, train_num, station_id) NOT NULL,
    FOREIGN KEY(timetable_id_dst, train_num, station_id_dst) REFERENCES Timetable(id, train_num, station_id) NOT NULL
);


