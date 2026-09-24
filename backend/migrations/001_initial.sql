CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE clients (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    phone text NOT NULL UNIQUE,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT clients_phone_e164 CHECK (phone ~ '^[+][7][0-9]{10}$')
);

CREATE TABLE instructors (
    id uuid PRIMARY KEY,
    full_name text NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE training_slots (
    id uuid PRIMARY KEY,
    instructor_id uuid NOT NULL REFERENCES instructors(id),
    starts_at timestamptz NOT NULL,
    ends_at timestamptz NOT NULL,
    format text NOT NULL,
    zone text NOT NULL,
    address text NOT NULL,
    capacity integer NOT NULL,
    booked_count integer NOT NULL DEFAULT 0,
    status text NOT NULL DEFAULT 'available',
    cancellation_reason text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT training_slots_time CHECK (ends_at > starts_at),
    CONSTRAINT training_slots_format CHECK (format IN ('novice_bouldering', 'rope_routes')),
    CONSTRAINT training_slots_status CHECK (status IN ('available', 'cancelled', 'completed')),
    CONSTRAINT training_slots_capacity CHECK (
        capacity > 0 AND
        ((format = 'novice_bouldering' AND capacity <= 8) OR (format = 'rope_routes' AND capacity <= 16))
    ),
    CONSTRAINT training_slots_booked_count CHECK (booked_count >= 0 AND booked_count <= capacity)
);

CREATE TABLE equipment_options (
    id uuid PRIMARY KEY,
    slot_id uuid REFERENCES training_slots(id) ON DELETE CASCADE,
    type text NOT NULL,
    name text NOT NULL,
    size text,
    price numeric(10, 2) NOT NULL,
    currency char(3) NOT NULL DEFAULT 'RUB',
    total_quantity integer NOT NULL,
    available_quantity integer NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    CONSTRAINT equipment_options_type CHECK (type IN ('climbing_shoes', 'harness_system')),
    CONSTRAINT equipment_options_price CHECK (price >= 0),
    CONSTRAINT equipment_options_quantity CHECK (total_quantity >= 0 AND available_quantity >= 0 AND available_quantity <= total_quantity)
);

CREATE TABLE bookings (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id uuid NOT NULL REFERENCES clients(id),
    slot_id uuid NOT NULL REFERENCES training_slots(id),
    status text NOT NULL DEFAULT 'confirmed',
    payment_method text NOT NULL DEFAULT 'on_site',
    cancel_deadline timestamptz NOT NULL,
    cancellation_reason text,
    created_at timestamptz NOT NULL DEFAULT now(),
    cancelled_at timestamptz,
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT bookings_status CHECK (status IN ('confirmed', 'cancelled_by_client', 'cancelled_by_venue', 'completed', 'rated')),
    CONSTRAINT bookings_payment_method CHECK (payment_method = 'on_site'),
    CONSTRAINT bookings_cancelled_time CHECK (cancelled_at IS NULL OR status IN ('cancelled_by_client', 'cancelled_by_venue'))
);

CREATE TABLE booking_equipment (
    booking_id uuid NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    equipment_option_id uuid NOT NULL REFERENCES equipment_options(id),
    quantity integer NOT NULL DEFAULT 1,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (booking_id, equipment_option_id),
    CONSTRAINT booking_equipment_quantity CHECK (quantity = 1)
);

CREATE TABLE ratings (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id uuid NOT NULL UNIQUE REFERENCES bookings(id),
    instructor_id uuid NOT NULL REFERENCES instructors(id),
    score integer NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ratings_score CHECK (score BETWEEN 1 AND 5)
);

CREATE TABLE notification_devices (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id uuid NOT NULL REFERENCES clients(id),
    platform text NOT NULL,
    push_token text NOT NULL UNIQUE,
    is_enabled boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT notification_devices_platform CHECK (platform IN ('ios', 'android'))
);
