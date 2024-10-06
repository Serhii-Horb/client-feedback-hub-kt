package com.api.client_feedback_hub_kt.dto

data class FeedbackResponseDto(
    var feedbackId: String= "",
    var reviewerId: String= "",
    var recipientId: String= "",
    var feedbackText: String= "",
    var grade: Int= 0
)
