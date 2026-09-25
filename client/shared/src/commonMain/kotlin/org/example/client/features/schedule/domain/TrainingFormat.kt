package org.example.client.features.schedule.domain

enum class TrainingFormat(
    val apiValue: String,
    val displayName: String,
    val maxCapacity: Int,
) {
    NOVICE_BOULDERING("novice_bouldering", "Новичковый болдеринг", 8),
    ROPE_ROUTES("rope_routes", "Трассы с верёвкой", 16),
    ;

    companion object {
        fun fromApi(value: String): TrainingFormat? = values().firstOrNull { it.apiValue == value }
    }
}
