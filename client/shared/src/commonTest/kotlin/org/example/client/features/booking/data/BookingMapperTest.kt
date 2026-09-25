package org.example.client.features.booking.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.booking.domain.BookingIntent
import org.example.client.features.booking.domain.PaymentMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookingMapperTest {
    @Test
    fun mapsOwnSelectionsToOnSiteRequestWithoutOptionId() {
        val request = BookingMapper.toRequest(
            BookingIntent(
                slotId = "11111111-1111-4111-8111-111111111111",
                shoes = EquipmentSelection.Own,
                harness = EquipmentSelection.Own,
            ),
        )
        val json = Json.encodeToString(request)

        assertEquals("on_site", request.paymentMethod)
        assertFalse(json.contains("option_id"))
    }

    @Test
    fun mapsRentalSelectionsWithOptionIds() {
        val request = BookingMapper.toRequest(
            BookingIntent(
                slotId = "11111111-1111-4111-8111-111111111111",
                shoes = EquipmentSelection.Rental("33333333-3333-4333-8333-333333333333"),
                harness = EquipmentSelection.Rental("44444444-4444-4444-8444-444444444444"),
                paymentMethod = PaymentMethod.ON_SITE,
            ),
        )
        val json = Json.encodeToString(request)

        assertTrue(json.contains("33333333-3333-4333-8333-333333333333"))
        assertTrue(json.contains("44444444-4444-4444-8444-444444444444"))
    }
}
