package org.example.client.features.schedule.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstructorSummary(
    @SerialName("id") val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("is_active") val isActive: Boolean,
)
