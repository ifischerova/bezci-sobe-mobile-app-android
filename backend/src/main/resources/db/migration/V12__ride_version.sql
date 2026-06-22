-- Optimistic-locking version column for rides. Guards the read-check-write on
-- occupied_seats in acceptRide / cancelRideAcceptance against concurrent seat
-- bookings. Existing rows start at 0; Hibernate manages the value from here on.
ALTER TABLE rides
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
