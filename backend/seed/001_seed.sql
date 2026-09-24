INSERT INTO instructors (id, full_name, is_active)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'Анна Петрова', true),
    ('00000000-0000-0000-0000-000000000002', 'Илья Соколов', true),
    ('00000000-0000-0000-0000-000000000003', 'Мария Волкова', true),
    ('00000000-0000-0000-0000-000000000004', 'Дмитрий Орлов', true),
    ('00000000-0000-0000-0000-000000000005', 'Ольга Лебедева', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO equipment_options (id, slot_id, type, name, size, price, total_quantity, available_quantity, is_active)
VALUES
    ('20000000-0000-0000-0000-000000000001', NULL, 'climbing_shoes', 'Скальники', '36', 300, 8, 8, true),
    ('20000000-0000-0000-0000-000000000002', NULL, 'climbing_shoes', 'Скальники', '37', 300, 8, 8, true),
    ('20000000-0000-0000-0000-000000000003', NULL, 'climbing_shoes', 'Скальники', '38', 300, 8, 8, true),
    ('20000000-0000-0000-0000-000000000004', NULL, 'climbing_shoes', 'Скальники', '39', 300, 8, 8, true),
    ('20000000-0000-0000-0000-000000000005', NULL, 'climbing_shoes', 'Скальники', '40', 300, 8, 8, true),
    ('20000000-0000-0000-0000-000000000006', NULL, 'climbing_shoes', 'Скальники', '41', 300, 8, 8, true),
    ('20000000-0000-0000-0000-000000000007', NULL, 'climbing_shoes', 'Скальники', '42', 300, 8, 8, true),
    ('20000000-0000-0000-0000-000000000008', NULL, 'climbing_shoes', 'Скальники', '43', 300, 8, 8, true),
    ('20000000-0000-0000-0000-000000000009', NULL, 'climbing_shoes', 'Скальники', '44', 300, 8, 8, true),
    ('20000000-0000-0000-0000-000000000010', NULL, 'climbing_shoes', 'Скальники', '45', 300, 8, 8, true),
    ('20000000-0000-0000-0000-000000000011', NULL, 'climbing_shoes', 'Скальники', '46', 300, 8, 8, true),
    ('20000000-0000-0000-0000-000000000012', NULL, 'harness_system', 'Страховочная система', 'S', 200, 6, 6, true),
    ('20000000-0000-0000-0000-000000000013', NULL, 'harness_system', 'Страховочная система', 'M', 200, 6, 6, true),
    ('20000000-0000-0000-0000-000000000014', NULL, 'harness_system', 'Страховочная система', 'L', 200, 6, 6, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO training_slots (id, instructor_id, starts_at, ends_at, format, zone, address, capacity, status)
SELECT
    md5('slot-' || day_offset::text || '-novice-am')::uuid,
    (ARRAY[
        '00000000-0000-0000-0000-000000000001'::uuid,
        '00000000-0000-0000-0000-000000000002'::uuid,
        '00000000-0000-0000-0000-000000000003'::uuid,
        '00000000-0000-0000-0000-000000000004'::uuid,
        '00000000-0000-0000-0000-000000000005'::uuid
    ])[1 + (day_offset % 5)::integer],
    date_trunc('day', CURRENT_TIMESTAMP) + day_offset * interval '1 day' + time '10:00',
    date_trunc('day', CURRENT_TIMESTAMP) + day_offset * interval '1 day' + time '11:30',
    'novice_bouldering',
    'Зона A',
    'Москва, ул. Вертикальная, 1',
    8,
    'available'
FROM generate_series(0, 6) AS series(day_offset)
ON CONFLICT (id) DO NOTHING;

INSERT INTO training_slots (id, instructor_id, starts_at, ends_at, format, zone, address, capacity, status)
SELECT
    md5('slot-' || day_offset::text || '-rope-pm')::uuid,
    (ARRAY[
        '00000000-0000-0000-0000-000000000002'::uuid,
        '00000000-0000-0000-0000-000000000003'::uuid,
        '00000000-0000-0000-0000-000000000004'::uuid,
        '00000000-0000-0000-0000-000000000005'::uuid,
        '00000000-0000-0000-0000-000000000001'::uuid
    ])[1 + (day_offset % 5)::integer],
    date_trunc('day', CURRENT_TIMESTAMP) + day_offset * interval '1 day' + time '18:00',
    date_trunc('day', CURRENT_TIMESTAMP) + day_offset * interval '1 day' + time '19:30',
    'rope_routes',
    'Зона B',
    'Москва, ул. Вертикальная, 1',
    16,
    'available'
FROM generate_series(0, 6) AS series(day_offset)
ON CONFLICT (id) DO NOTHING;

INSERT INTO training_slots (id, instructor_id, starts_at, ends_at, format, zone, address, capacity, status)
SELECT
    md5('slot-' || day_offset::text || '-novice-pm')::uuid,
    (ARRAY[
        '00000000-0000-0000-0000-000000000003'::uuid,
        '00000000-0000-0000-0000-000000000004'::uuid,
        '00000000-0000-0000-0000-000000000005'::uuid,
        '00000000-0000-0000-0000-000000000001'::uuid,
        '00000000-0000-0000-0000-000000000002'::uuid
    ])[1 + (day_offset % 5)::integer],
    date_trunc('day', CURRENT_TIMESTAMP) + day_offset * interval '1 day' + time '20:00',
    date_trunc('day', CURRENT_TIMESTAMP) + day_offset * interval '1 day' + time '21:30',
    'novice_bouldering',
    'Зона A',
    'Москва, ул. Вертикальная, 1',
    8,
    'available'
FROM generate_series(0, 6) AS series(day_offset)
ON CONFLICT (id) DO NOTHING;
