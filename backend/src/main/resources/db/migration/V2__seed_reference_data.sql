-- Track lengths
INSERT INTO track_lengths (id, name) VALUES
    (1, '5K'),
    (2, '10K'),
    (3, 'Půlmaraton'),
    (4, 'Maraton'),
    (5, 'Ultra');

-- Track types
INSERT INTO track_types (id, name) VALUES
    (1, 'Silnice'),
    (2, 'Trail'),
    (3, 'Dráha');

-- Certifications
INSERT INTO certifications (id, name) VALUES
    (1, 'IAAF'),
    (2, 'AIMS');

-- Race calendars
INSERT INTO race_calendars (id, year, is_active) VALUES
    (1, 2026, TRUE);

-- Reset sequences
SELECT setval('track_lengths_id_seq', 5);
SELECT setval('track_types_id_seq', 3);
SELECT setval('certifications_id_seq', 2);
SELECT setval('race_calendars_id_seq', 1);
