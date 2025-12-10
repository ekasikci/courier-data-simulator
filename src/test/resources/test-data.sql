-- Test data for unit tests
-- Completed package (on time)
INSERT INTO packages (
    id, created_at, last_updated_at, cancelled, eta, status,
    collected, collected_at, picked_up_at, completed_at, delivery_date
) VALUES (
    1,
    '2024-12-10 10:00:00.000000',
    '2024-12-10 10:30:00.000000',
    0,
    30,
    'COMPLETED',
    1,
    '2024-12-10 10:05:00.000000',
    '2024-12-10 10:10:00.000000',
    '2024-12-10 10:28:00.000000',
    '2024-12-10'
);

-- Completed package (late)
INSERT INTO packages (
    id, created_at, last_updated_at, cancelled, eta, status,
    collected, collected_at, picked_up_at, completed_at, delivery_date
) VALUES (
    2,
    '2024-12-10 11:00:00.000000',
    '2024-12-10 11:45:00.000000',
    0,
    30,
    'COMPLETED',
    1,
    '2024-12-10 11:05:00.000000',
    '2024-12-10 11:10:00.000000',
    '2024-12-10 11:40:00.000000',
    '2024-12-10'
);

-- In-progress package
INSERT INTO packages (
    id, created_at, last_updated_at, cancelled, eta, status,
    collected, collected_at, picked_up_at
) VALUES (
    3,
    '2024-12-10 12:00:00.000000',
    '2024-12-10 12:15:00.000000',
    0,
    30,
    'IN_DELIVERY',
    1,
    '2024-12-10 12:05:00.000000',
    '2024-12-10 12:10:00.000000'
);

-- Cancelled package (should be filtered out)
INSERT INTO packages (
    id, created_at, last_updated_at, cancelled, eta, status,
    cancel_reason, cancelled_at
) VALUES (
    4,
    '2024-12-10 13:00:00.000000',
    '2024-12-10 13:05:00.000000',
    1,
    30,
    'CANCELLED',
    'Customer request',
    '2024-12-10 13:05:00.000000'
);