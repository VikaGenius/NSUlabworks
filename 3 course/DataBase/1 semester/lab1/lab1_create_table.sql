
CREATE TABLE Groups (
    group_number INTEGER PRIMARY KEY,
    headman_id INTEGER REFERENCES Students(student_id)
);

CREATE TABLE Students (
    student_id SERIAL PRIMARY KEY, 
    last_name VARCHAR(25) NOT NULL,
    first_name VARCHAR(25) NOT NULL,
    group_number INTEGER NOT NULL,
    FOREIGN KEY (group_number) REFERENCES Groups(group_number)
);

CREATE TABLE Subjects (
    subject_id SERIAL PRIMARY KEY,
    subject_name VARCHAR(50) NOT NULL,
    UNIQUE(subject_id, subject_name)
);

CREATE TABLE Teachers (
    teacher_id SERIAL PRIMARY KEY,
    full_name VARCHAR(60) NOT NULL
);

CREATE TABLE Specialization (
    teacher_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id),
    FOREIGN KEY (subject_id) REFERENCES Subjects(subject_id),
    PRIMARY KEY (teacher_id, subject_id)
);

-- написать триггер (если в специализации нет такой пары препода и предмета, значит вставка невозможна)
CREATE TABLE Academic_performance (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    teacher_id INTEGER NOT NULL,
    mark_date DATE,
    mark INTEGER,
    FOREIGN KEY (student_id) REFERENCES Students(student_id),
    FOREIGN KEY (subject_id) REFERENCES Subjects(subject_id),
    FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id)
);

CREATE TABLE Pairs (
    pair_number INTEGER PRIMARY KEY,
    pair_begin TIME NOT NULL,
    pair_end TIME NOT NULL
);

CREATE TABLE Schedule (
    day_of_week VARCHAR(15) NOT NULL,
    pair_number INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    subject_type VARCHAR(50) NOT NULL,
    teacher_id INTEGER NOT NULL, 
    group_number INTEGER NOT NULL,
    classroom_number INTEGER,
    FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id),
    FOREIGN KEY (group_number) REFERENCES Groups(group_number),
    FOREIGN KEY (pair_number) REFERENCES Pairs(pair_number),
    FOREIGN KEY (subject_id) REFERENCES Subjects(subject_id),
    PRIMARY KEY (day_of_week, pair_number, classroom_number)
);
