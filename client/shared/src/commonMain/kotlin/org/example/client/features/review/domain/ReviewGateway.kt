package org.example.client.features.review.domain

interface ReviewGateway {
    suspend fun submitRating(bookingId: String, score: Int): Result<Int>
}
