package com.api.client_feedback_hub_kt.dto

data class UserResponseDto(
    var userId: Long = 0,
    var email: String = "",
    var name: String = "",
    var phoneNumber: String = "",
    var password: String = "",
    var averageRating: Double = 0.0,
    var numberReviewers: Int = 0,
)
