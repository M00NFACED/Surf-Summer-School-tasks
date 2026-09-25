package org.example.client.features.review.presentation

import org.example.client.features.review.domain.MaxReviewScore

data class ReviewState(
    val score: Int = 0,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val isForbidden: Boolean = false,
    val isCompleted: Boolean = false,
) {
    val canSubmit: Boolean
        get() = score in 1..MaxReviewScore && !isSubmitting
}
