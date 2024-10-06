package com.api.client_feedback_hub_kt.entity

import kotlin.properties.Delegates

class Feedback {
    lateinit var feedbackId: String
    lateinit var reviewerId: String
    lateinit var recipientId: String
    lateinit var feedbackText: String
    var grade: Int by Delegates.notNull()
    var timestamp: Long by Delegates.notNull()

    constructor()

    constructor(
        feedbackId: String,
        reviewerId: String,
        recipientId: String,
        feedbackText: String,
        grade: Int,
        timestamp: Long,
    ) {
        this.feedbackId = feedbackId
        this.reviewerId = reviewerId
        this.recipientId = recipientId
        this.feedbackText = feedbackText
        this.grade = grade
        this.timestamp = timestamp
    }
}
