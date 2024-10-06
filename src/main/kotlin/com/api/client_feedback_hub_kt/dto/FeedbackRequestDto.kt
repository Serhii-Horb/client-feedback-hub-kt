package com.api.client_feedback_hub_kt.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class FeedbackRequestDto(
    var reviewerId: String,
    var recipientId: String,
    var feedbackText: String,

    @field:Min(value = 1, message = "Grade must be at least 1")
    @field:Max(value = 5, message = "Grade must be no more than 5")
    var grade: Int
) {
    @get:AssertTrue(message = "Reviewer ID must not be the same as Recipient ID")
    var isReviewerIdNotEqualToRecipientId: Boolean = false
        get() = reviewerId != recipientId
}
