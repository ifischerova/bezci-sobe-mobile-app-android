-- Users (BCrypt cost-10 hashes, regenerated with bcryptjs and verified)
-- admin123    -> $2a$10$mGsI68a2foSCV/fW.wX4Verb4vYuedKEcx8pcddqKwsHgroCiaD96
-- password123 -> $2a$10$XovRDfRfVvmgiXd1rXL7bOTRi/m6KPVmNitbnyUoR97tq8hXh5xfy
-- ivka123     -> $2a$10$4zCsCZ29WwEFxJr6/1DmEeWR.pMMRDzb4ixRtF15u3QPE5W4Gku.y

INSERT INTO users (id, username, email, password, first_name, last_name, city) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin', 'admin@bezcisobe.cz',
     '$2a$10$mGsI68a2foSCV/fW.wX4Verb4vYuedKEcx8pcddqKwsHgroCiaD96',
     'Admin', 'User', NULL),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'jana.novakova', 'jana@example.cz',
     '$2a$10$XovRDfRfVvmgiXd1rXL7bOTRi/m6KPVmNitbnyUoR97tq8hXh5xfy',
     'Jana', 'Nováková', 'Praha'),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'ivka', 'ivka@bezcisobe.cz',
     '$2a$10$4zCsCZ29WwEFxJr6/1DmEeWR.pMMRDzb4ixRtF15u3QPE5W4Gku.y',
     'Ivka', 'Fischerová', 'Praha');

INSERT INTO user_roles (user_id, role) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ROLE_USER'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ROLE_ADMIN'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'ROLE_USER'),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'ROLE_USER');

-- Races
INSERT INTO races (id, name, place, date, start_time, web, track_length_id, track_type_id, race_calendar_id) VALUES
    (1,  'Pražský maraton',              'Praha',                    '2026-05-10', '09:00', 'https://www.runczech.com/cs/akce/maraton/informace/', 4, 1, 1),
    (2,  'Pražský půlmaraton',           'Praha',                    '2026-04-04', '09:00', 'https://www.runczech.com/cs/akce/pulmaraton/',        3, 1, 1),
    (3,  'Kolem Příhlupu',               'Příhlup u Lednice',        '2026-06-13', '10:00', 'https://www.kolempriglupu.cz/',                      3, 2, 1),
    (4,  'PIM - Půlmaraton Olomouc',     'Olomouc',                  '2026-06-20', '09:30', 'https://www.pim.cz/',                                3, 1, 1),
    (5,  'Běhej lesy - Kunratický les',  'Praha - Kunratice',        '2026-03-07', '10:00', 'https://www.behejlesy.cz/',                          2, 2, 1),
    (6,  'Běhej lesy - Hvězdárna',       'Praha - Petřín',           '2026-04-11', '10:00', 'https://www.behejlesy.cz/',                          2, 2, 1),
    (7,  'Běhej lesy - Divoká Šárka',    'Praha - Šárka',            '2026-05-16', '10:00', 'https://www.behejlesy.cz/',                          2, 2, 1),
    (8,  'Běhej lesy - Prokopské údolí', 'Praha - Prokopské údolí',  '2026-09-12', '10:00', 'https://www.behejlesy.cz/',                          2, 2, 1),
    (9,  'Běhej lesy - Stromovka',       'Praha - Stromovka',        '2026-10-10', '10:00', 'https://www.behejlesy.cz/',                          2, 2, 1),
    (10, 'Běhej lesy - Finále',          'Praha',                    '2026-11-14', '10:00', 'https://www.behejlesy.cz/',                          2, 2, 1);

SELECT setval('races_id_seq', 10);

-- Race certifications (only first two races have IAAF and AIMS)
INSERT INTO race_certifications (race_id, certification_id) VALUES
    (1, 1), (1, 2),
    (2, 1), (2, 2);
