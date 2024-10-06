package com.api.client_feedback_hub_kt.dto

data class UserRequestDto(
    var email: String = "",
    var password: String = "",
    var name: String = "",
    var phoneNumber: String = ""
)
