import psycopg2
from faker import Faker
import random

db_params = {
    'host': 'localhost',
    'port': '5431',
    'database': 'postgres',
    'user': 'postgres',
    'password': '1234',  
}

conn = psycopg2.connect(**db_params)

cur = conn.cursor()
faker = Faker()

def generate_station_name():
    return faker.word()

def insert_stations(num_records):
    for _ in range(num_records):
        station_name = generate_station_name()
        cur.execute("INSERT INTO Stations (s_name) VALUES (%s)", (station_name,))
    conn.commit()

def get_station_ids():
    cur.execute("SELECT id FROM Stations")
    station_ids = [row[0] for row in cur.fetchall()] #извлечение первого элемента из каждой строки, возвращенной из курсора базы данных
    return station_ids

def generate_full_name():
    return faker.name()

def generate_place():
    return faker.city()

def insert_employees(num_records):
    station_ids = get_station_ids()
    for _ in range(num_records):
        full_name = generate_full_name()
        place = generate_place()
        station_id = random.choice(station_ids)
        cur.execute("INSERT INTO Employees (full_name, place, station_id) VALUES (%s, %s, %s)", (full_name, place, station_id))
    conn.commit()

insert_stations(30)
insert_employees(30)

cur.close()
conn.close()

