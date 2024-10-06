package com.api.client_feedback_hub_kt.entity

import kotlin.properties.Delegates

class User {
    var userId: Long by Delegates.notNull()
    lateinit var email: String
    lateinit var name: String
    lateinit var phoneNumber: String
    var role: String? = null
    lateinit var hashedPassword: String
    var averageRating: Double by Delegates.notNull()
    var numberReviewers: Int by Delegates.notNull()

    constructor()

    constructor(
        userId: Long,
        email: String,
        name: String,
        phoneNumber: String,
        role: String?,
        hashedPassword: String,
        averageRating: Double,
        numberReviewers: Int,
    ) {
        this.userId = userId
        this.email = email
        this.name = name
        this.phoneNumber = phoneNumber
        this.role = role
        this.hashedPassword = hashedPassword
        this.averageRating = averageRating
        this.numberReviewers = numberReviewers
    }
}