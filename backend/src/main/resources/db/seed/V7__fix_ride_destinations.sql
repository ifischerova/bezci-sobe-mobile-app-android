-- V5 and V6 picked destination_to from a random list of large Czech cities,
-- which left OFFER rides pointing at the wrong city for the race (e.g. "Plzeň →
-- Zlín" for a race held in Praha). The driver always actually goes to the race
-- venue, so destination_to should equal the race's place.
--
-- REQUEST rides leave destination_to NULL and are skipped.

UPDATE rides r
SET    destination_to = ra.place
FROM   races ra
WHERE  r.race_id = ra.id
  AND  r.type = 'OFFER'
  AND  (r.destination_to IS NULL OR r.destination_to <> ra.place);
