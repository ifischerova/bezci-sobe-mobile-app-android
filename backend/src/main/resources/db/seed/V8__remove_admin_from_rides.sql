-- The admin user is a service account; it shouldn't appear as a driver or
-- passenger in carpool seed data. V8 reassigns admin-owned rides to regular
-- users (deterministic rotation across non-admin candidates, skipping any
-- candidate who's already a passenger of that ride to avoid driver==passenger)
-- and strips admin from all passenger lists.

-- Step 1. Decrement occupied_seats for rides where admin currently sits as a
--         passenger, before we delete those passenger rows.
UPDATE rides
SET    occupied_seats = occupied_seats - 1
WHERE  id IN (
        SELECT rp.ride_id
        FROM   ride_passengers rp
        JOIN   users u ON u.id = rp.user_id
        WHERE  u.username = 'admin'
       );

-- Step 2. Remove admin from passenger lists.
DELETE FROM ride_passengers
WHERE  user_id IN (SELECT id FROM users WHERE username = 'admin');

-- Step 3. Reassign admin-owned rides. For each ride we pick the first
--         non-admin user (in a rotated deterministic order) who isn't
--         already a passenger of that ride.
WITH admin_id AS (
    SELECT id FROM users WHERE username = 'admin'
),
non_admins AS (
    SELECT id,
           ROW_NUMBER() OVER (ORDER BY username) AS rn
    FROM   users
    WHERE  id NOT IN (SELECT id FROM admin_id)
),
non_admins_count AS (
    SELECT COUNT(*)::int AS total FROM non_admins
),
admin_rides AS (
    SELECT id,
           ROW_NUMBER() OVER (ORDER BY id) AS idx
    FROM   rides
    WHERE  user_id IN (SELECT id FROM admin_id)
)
UPDATE rides r
SET    user_id = (
        SELECT na.id
        FROM   non_admins na
        WHERE  na.id NOT IN (
                 SELECT user_id FROM ride_passengers WHERE ride_id = r.id
               )
        ORDER  BY (
                 (SELECT idx FROM admin_rides WHERE id = r.id)
                 + na.rn - 1
               ) % (SELECT total FROM non_admins_count)
        LIMIT 1
       )
WHERE  r.user_id IN (SELECT id FROM admin_id);
