package org.example.client.features.profile.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileContentTest {
    @Test
    fun clubRulesCoverSafetyFootwearMagneziumAndCancellation() {
        val titles = clubRules.map { it.title }

        assertTrue(titles.containsAll(listOf("Техника безопасности", "Сменная обувь", "Магнезия", "Отмена записи")))
        assertTrue(clubRules.all { it.description.isNotBlank() })
    }

    @Test
    fun supportContactsAreExposed() {
        assertEquals("+7 (999) 000-00-00", SupportPhone)
        assertEquals("@vertical_climb", SupportTelegram)
    }

    @Test
    fun versionLabelContainsProductAndRelease() {
        assertEquals("Скалодром Вертикаль v1.0.0 (Surf Summer School 2026)", AppVersionLabel)
    }
}
