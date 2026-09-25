package org.example.client.features.schedule.data

fun testSlotResponse(items: List<TrainingSlotSummary> = listOf(testSlotSummary())) = SlotListResponse(
    from = "2026-09-25T00:00:00Z",
    to = "2026-10-02T00:00:00Z",
    items = items,
)

fun testSlotSummary(
    availablePlaces: Int = 3,
    status: String = "available",
) = TrainingSlotSummary(
    id = "11111111-1111-4111-8111-111111111111",
    startsAt = "2026-09-25T10:00:00Z",
    endsAt = "2026-09-25T12:00:00Z",
    format = "novice_bouldering",
    zone = "Зона A",
    address = "Москва, ул. Вертикальная, 1",
    instructor = InstructorSummary(
        id = "22222222-2222-4222-8222-222222222222",
        fullName = "Анна Петрова",
        isActive = true,
    ),
    capacity = 8,
    availablePlaces = availablePlaces,
    status = status,
)
