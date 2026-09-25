package org.example.client.features.schedule.presentation

import org.example.client.core.ui.ArtworkStyle
import org.example.client.core.ui.artworkSeed
import org.example.client.features.schedule.domain.TrainingFormat
import org.example.client.features.schedule.domain.TrainingSlotItem

internal fun TrainingFormat.artworkStyle(): ArtworkStyle = when (this) {
    TrainingFormat.NOVICE_BOULDERING -> ArtworkStyle.MORNING_BOULDERS
    TrainingFormat.ROPE_ROUTES -> ArtworkStyle.EVENING_SPIRES
}

internal fun TrainingSlotItem.artworkSeed(): Int = artworkSeed(id, instructor.id)
