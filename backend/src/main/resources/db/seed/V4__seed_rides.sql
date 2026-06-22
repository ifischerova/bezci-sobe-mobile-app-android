-- Ride 1: jana.novakova requests a ride to Pražský maraton
INSERT INTO rides (id, race_id, user_id, type, departure_from, available_seats, occupied_seats, created_at) VALUES
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 1, 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22',
     'REQUEST', 'Praha', 1, 0, NOW());

-- Ride 2: admin offers a ride to Pražský půlmaraton (jana.novakova is a passenger)
INSERT INTO rides (id, race_id, user_id, type, departure_from, destination_to, car, available_seats, occupied_seats, notes, created_at) VALUES
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 2, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'OFFER', 'Brno', 'Praha', 'Škoda Octavia', 3, 1, 'Vyjíždím v 8:00', NOW());

INSERT INTO ride_passengers (ride_id, user_id) VALUES
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22');
