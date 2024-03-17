INSERT INTO Groups (group_number, headman_id) VALUES
    (21201, NULL),
    (21210, NULL),
    (21212, NULL),
    (21211, NULL);

INSERT INTO Students(last_name, first_name, group_number) VALUES
    ('Stepanova', 'Viktoria', 21212),
    ('Ponomareva', 'Ludmila', 21212),
    ('Lanin', 'Daniil', 21212),
    ('Alekseev', 'Alexei', 21212),
    ('Olimpiev', 'Yuri', 21212),
    ('Petrov', 'Sergei', 21211),
    ('Kotelnikova', 'Nadezhda', 21211),
    ('Kosolap', 'Marina', 21211),
    ('Bolotov', 'Kirill', 21211),
    ('Skopintsev', 'Nikita', 21210),
    ('Burym', 'Maksim', 21210),
    ('Traykovskaya', 'Ekaterina', 21210),
    ('Bazarov', 'Bulat', 21201),
    ('Khudorozhkov', 'Yan', 21210);

INSERT INTO Subjects(subject_name) VALUES
    ('Mathematical analysis'),
    ('OS'),
    ('Network technologies');

INSERT INTO Teachers(full_name) VALUES 
    ('Sedipkov Aydys Alekseevich'),
    ('Stolyarov Konstantin Alexandrovich'),
    ('Ippolitov Vadim Dmitrievich'),
    ('Vazheva Daria Viktorovna'),
    ('Chebotarev Sergey Evgenievich'),
    ('Botvinko Vitaly Vyacheslavovich');

INSERT INTO Pairs(pair_number, pair_begin, pair_end) VALUES
    (1, '09:00:00', '10:35:00'),
    (2, '10:50:00', '12:25:00'),
    (3, '12:40:00', '14:15:00'),
    (4, '14:30:00', '16:05:00'),
    (5, '16:20:00', '17:55:00'),
    (6, '18:10:00', '19:45:00');

INSERT INTO Specialization(teacher_id, subject_id) VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 1),
    (5, 2),
    (6, 3);

INSERT INTO Academic_performance(student_id, subject_id, teacher_id, mark, mark_date) VALUES
    (1, 1, 1, 5, '2022-09-25'),
    (1, 2, 2, 4, '2023-02-15'),
    (1, 1, 1, 4, '2022-10-21'),
    (1, 1, 1, 5, '2022-12-01'),
    (1, 2, 2, 5, '2023-02-27'),
    (1, 2, 2, 4, '2023-04-10'),
    (1, 3, 3, 5, '2023-09-20'),
    (1, 3, 3, 5, '2023-10-20'),
    (2, 1, 1, 5, '2022-09-25'),
    (2, 1, 1, 5, '2022-11-21'),
    (2, 2, 2, 4, '2022-10-21'),
    (2, 2, 2, 4, '2022-12-20'),
    (2, 3, 3, 4, '2022-12-27'),
    (3, 1, 1, 5, '2022-09-20'),
    (3, 2, 2, 5, '2022-10-20'),
    (3, 3, 3, 5, '2022-10-20'),
    (4, 3, 3, 5, '2022-10-20'),
    (4, 2, 2, 5, '2022-10-20'),
    (4, 1, 1, 5, '2022-10-20'),
    (5, 1, 1, 5, '2022-10-20'),
    (5, 3, 3, 5, '2022-10-20'),
    (6, 3, 3, 5, '2022-10-20'),
    (7, 3, 3, 5, '2022-10-20'),
    (8, 3, 3, 5, '2022-10-20');

INSERT INTO Schedule(day_of_week, pair_number, subject_id, subject_type, teacher_id, group_number, classroom_number) VALUES
    ('Monday', 1, 1, 'practice', 1, 21212, 3115),
    ('Monday', 1, 1, 'practice', 4, 21210, 3116),
    ('Tuesday', 2, 1, 'practice', 1, 21211, 3120),
    ('Wednesday', 1, 2, 'practice', 2, 21212, 3220),
    ('Wednesday', 3, 3, 'practice', 3, 21212, 2213),
    ('Wednesday', 1, 3, 'practice', 6, 21210, 2213),
    ('Wednesday', 1, 3, 'practice', 6, 21210, 2214),
    ('Thursday', 4, 2, 'practice', 5, 21210, 3213);








