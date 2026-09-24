CREATE UNIQUE INDEX bookings_one_active_client_slot
    ON bookings (client_id, slot_id)
    WHERE status = 'confirmed';

CREATE INDEX training_slots_schedule_idx
    ON training_slots (starts_at, format, instructor_id);

CREATE INDEX equipment_options_slot_idx
    ON equipment_options (slot_id, type, is_active);

CREATE INDEX bookings_client_idx
    ON bookings (client_id, created_at DESC);
