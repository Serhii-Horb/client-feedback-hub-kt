package com.api.client_feedback_hub_kt.service

import com.api.client_feedback_hub_kt.dto.FeedbackRequestDto
import com.api.client_feedback_hub_kt.dto.FeedbackResponseDto
import com.api.client_feedback_hub_kt.entity.Feedback
import com.api.client_feedback_hub_kt.mapper.FeedbackMapper
import com.google.firebase.database.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class FeedbackService(private val feedbackMapper: FeedbackMapper) {
    private val logger: Logger = LoggerFactory.getLogger(FeedbackService::class.java)

    private fun logDatabaseError(databaseError: DatabaseError, context: String) {
        logger.error("Database error during $context: ${databaseError.message}")
    }

    private fun logNoDataFound(context: String) {
        logger.info("No data found, returning an empty list. Context: $context")
    }

    private fun logFeedbackAdded(feedbackId: String) {
        logger.info("Feedback added: $feedbackId.")
    }

    fun getAllFeedbacks(): CompletableFuture<List<FeedbackResponseDto>> {
        val ref = FirebaseDatabase.getInstance().getReference("feedbacks")
        val feedbackList = mutableListOf<FeedbackResponseDto>()
        val futureFeedbacks = CompletableFuture<List<FeedbackResponseDto>>()

        logger.info("Starting to fetch all feedbacks from the database")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                logger.info("DataSnapshot received from database")
                if (dataSnapshot.exists()) {
                    logger.info("Data found, processing users...")
                    dataSnapshot.children.forEach { snapshot ->
                        val feedback = snapshot.getValue(Feedback::class.java)
                        if (feedback != null) {
                            feedbackList.add(feedbackMapper.convertToDto(feedback))
                            logFeedbackAdded(feedback.feedbackId)
                        } else {
                            logger.warn("Failed to parse data as Feedback object...")
                        }
                    }
                    futureFeedbacks.complete(feedbackList)
                } else {
                    logNoDataFound("getAllFeedbacks")
                    futureFeedbacks.complete(emptyList())
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                logDatabaseError(databaseError, "getAllFeedbacks")
                futureFeedbacks.completeExceptionally(RuntimeException())
            }
        })
        return futureFeedbacks
    }

    fun getFeedbackById(id: String): CompletableFuture<FeedbackResponseDto> {
        val ref = FirebaseDatabase.getInstance().getReference("feedbacks")
        val future = CompletableFuture<FeedbackResponseDto>()

        ref.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val feedback = dataSnapshot.getValue(Feedback::class.java)
                    if (feedback != null) {
                        future.complete(feedbackMapper.convertToDto(feedback))
                    } else {
                        future.completeExceptionally(RuntimeException("Feedback data found but failed to parse it"))
                    }
                } else {
                    logger.warn("No feedback found with ID: $id")
                    future.completeExceptionally(RuntimeException("No feedback found with the provided ID: $id"))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logDatabaseError(databaseError, "getFeedbackById")
                future.completeExceptionally(RuntimeException("Error occurred while accessing the database for feedback ID: $id"))
            }
        })
        return future
    }

    fun getAllFeedbacksByReviewerId(id: String): CompletableFuture<List<FeedbackResponseDto>> {
        val ref = FirebaseDatabase.getInstance().getReference("feedbacks")
        val feedbackList = mutableListOf<FeedbackResponseDto>()
        val futureFeedbacks = CompletableFuture<List<FeedbackResponseDto>>()

        logger.info("Starting to fetch all feedbacks by reviewer with ID: $id")
        ref.orderByChild("reviewerId").equalTo(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.children.forEach { snapshot ->
                        val feedback = snapshot.getValue(Feedback::class.java)
                        if (feedback != null) {
                            feedbackList.add(feedbackMapper.convertToDto(feedback))
                            logFeedbackAdded(feedback.feedbackId!!)
                        } else {
                            logger.warn("Failed to parse data as Feedback object.")
                        }
                    }
                    futureFeedbacks.complete(feedbackList)
                } else {
                    logNoDataFound("getAllFeedbacksByReviewerId")
                    futureFeedbacks.complete(emptyList())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logDatabaseError(databaseError, "getAllFeedbacksByReviewerId")
                futureFeedbacks.completeExceptionally(RuntimeException("Database read failed"))
            }
        })
        return futureFeedbacks
    }

    fun getAllFeedbacksByRecipientId(id: String): CompletableFuture<List<FeedbackResponseDto>> {
        val ref = FirebaseDatabase.getInstance().getReference("feedbacks")
        val feedbackList = mutableListOf<FeedbackResponseDto>()
        val futureFeedbacks = CompletableFuture<List<FeedbackResponseDto>>()

        logger.info("Starting to fetch all feedbacks by recipient with ID: $id")
        ref.orderByChild("recipientId").equalTo(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.children.forEach { snapshot ->
                        val feedback = snapshot.getValue(Feedback::class.java)
                        if (feedback != null) {
                            feedbackList.add(feedbackMapper.convertToDto(feedback))
                            logger.info("Feedback added: ${feedback.feedbackId}")
                        } else {
                            logger.warn("Failed to parse data as Feedback object")
                        }
                    }
                    futureFeedbacks.complete(feedbackList)
                } else {
                    logNoDataFound("getAllFeedbacksByRecipientId")
                    futureFeedbacks.complete(emptyList())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logDatabaseError(databaseError, "getAllFeedbacksByRecipientId")
                futureFeedbacks.completeExceptionally(RuntimeException("Database read failed"))
            }
        })
        return futureFeedbacks
    }

    fun createFeedback(feedbackRequestDto: FeedbackRequestDto): CompletableFuture<String> {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        val feedbackRef = FirebaseDatabase.getInstance().getReference("feedbacks")
        val future = CompletableFuture<String>()

        checkUserExists(usersRef, feedbackRequestDto.reviewerId, "Reviewer")
            .thenCompose<Boolean?> {
                checkUserExists(
                    usersRef,
                    feedbackRequestDto.recipientId,
                    "Recipient"
                )
            }
            .thenAccept {
                val uniqueFeedbackId = feedbackRef.push().key
                val newFeedback = Feedback(
                    uniqueFeedbackId, feedbackRequestDto.reviewerId,
                    feedbackRequestDto.recipientId, feedbackRequestDto.feedbackText,
                    feedbackRequestDto.grade, System.currentTimeMillis()
                )
                updateRecipientData(usersRef, feedbackRequestDto.recipientId, feedbackRequestDto.grade)
                    .thenAccept {
                        saveFeedback(
                            feedbackRef,
                            uniqueFeedbackId,
                            newFeedback,
                            future
                        )
                    }
                    .exceptionally { e: Throwable? ->
                        future.completeExceptionally(e)
                        null
                    }
            }.exceptionally { e: Throwable? ->
                future.completeExceptionally(e)
                null
            }

        return future
    }

    private fun checkUserExists(
        usersRef: DatabaseReference,
        userId: String,
        userType: String,
    ): CompletableFuture<Boolean?> {
        val future = CompletableFuture<Boolean?>()
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    future.complete(true)
                } else {
                    future.completeExceptionally(java.lang.RuntimeException("$userType ID does not exist: $userId"))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logDatabaseError(databaseError, "checkUserExists")
                future.completeExceptionally(java.lang.RuntimeException("Failed to check $userType ID: $userId"))
            }
        })
        return future
    }

    private fun updateRecipientData(
        usersRef: DatabaseReference,
        recipientId: String,
        grade: Int,
    ): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        usersRef.child(recipientId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var currentNumberReviewers = snapshot.child("numberReviewers").getValue(Int::class.java)
                var averageRating = snapshot.child("averageRating").getValue(Double::class.java)

                currentNumberReviewers++
                averageRating =
                    Math.round((averageRating * (currentNumberReviewers - 1) + grade) / currentNumberReviewers * 100.0) / 100.0

                val updates: MutableMap<String, Any> = HashMap()
                updates["numberReviewers"] = currentNumberReviewers
                updates["averageRating"] = averageRating

                usersRef.child(recipientId).updateChildren(
                    updates
                ) { error: DatabaseError?, ref: DatabaseReference? ->
                    if (error != null) {
                        logDatabaseError(error, "updateRecipientData")
                        future.completeExceptionally(java.lang.RuntimeException("Failed to update recipient data"))
                    } else {
                        future.complete(null)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logDatabaseError(databaseError, "updateRecipientData")
                future.completeExceptionally(java.lang.RuntimeException("Failed to retrieve recipient data"))
            }
        })
        return future
    }

    private fun saveFeedback(
        feedbackRef: DatabaseReference,
        feedbackId: String,
        feedback: Feedback,
        future: CompletableFuture<String>,
    ) {
        feedbackRef.child(feedbackId).setValue(
            feedback
        ) { error: DatabaseError?, ref: DatabaseReference? ->
            if (error != null) {
                logDatabaseError(error, "saveFeedback")
                future.completeExceptionally(java.lang.RuntimeException("Feedback creation failed"))
            } else {
                logger.info("Feedback successfully saved with ID: {}", feedbackId)
                future.complete("Feedback created with ID: $feedbackId")
            }
        }
    }

    fun deleteFeedbackById(feedbackId: String): CompletableFuture<Unit> {
        val feedbackRef = FirebaseDatabase.getInstance().getReference("feedbacks").child(feedbackId)
        val future = CompletableFuture<Unit>()

        feedbackRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(feedbackSnapshot: DataSnapshot) {
                if (!feedbackSnapshot.exists()) {
                    logger.error("Feedback with ID: {} does not exist.", feedbackId)
                    future.completeExceptionally(java.lang.RuntimeException("Feedback with ID: $feedbackId does not exist."))
                    return
                }

                val feedback = feedbackSnapshot.getValue(Feedback::class.java)
                if (feedback?.recipientId == null) {
                    future.completeExceptionally(java.lang.RuntimeException("Failed to parse feedback data or recipientId is missing."))
                    return
                }

                updateRecipientDataOnDeletion(feedback.recipientId, feedback.grade)
                    .thenAccept {
                        deleteFeedback(
                            feedbackRef,
                            feedbackId,
                            future
                        )
                    }
                    .exceptionally { e: Throwable? ->
                        future.completeExceptionally(e)
                        null
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logDatabaseError(databaseError, "deleteFeedbackById")
                future.completeExceptionally(java.lang.RuntimeException("Failed to check feedback ID"))
            }
        })

        return future
    }

    private fun deleteFeedback(feedbackRef: DatabaseReference, feedbackId: String, future: CompletableFuture<Unit>) {
        feedbackRef.removeValue { error: DatabaseError?, ref: DatabaseReference? ->
            if (error != null) {
                logDatabaseError(error, "deleteFeedback")
                future.completeExceptionally(java.lang.RuntimeException("Failed to delete feedback with ID: $feedbackId"))
            } else {
                logger.info("Feedback with ID: {} was deleted successfully.", feedbackId)
                future.complete(null)
            }
        }
    }

    private fun updateRecipientDataOnDeletion(recipientId: String, grade: Int): CompletableFuture<Unit> {
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(recipientId)
        val future = CompletableFuture<Unit>()

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentNumberReviewers = snapshot.child("numberReviewers").getValue(Int::class.java) ?: 0
                val averageRating = snapshot.child("averageRating").getValue(Double::class.java) ?: 0.0

                val updatedNumberReviewers = currentNumberReviewers - 1

                val updatedAverageRating = if (updatedNumberReviewers > 0) {
                    Math.round((averageRating * currentNumberReviewers - grade) / updatedNumberReviewers * 100.0) / 100.0
                } else {
                    0.0
                }

                val updates = mapOf(
                    "numberReviewers" to updatedNumberReviewers,
                    "averageRating" to updatedAverageRating
                )

                usersRef.updateChildren(updates) { error: DatabaseError?, ref: DatabaseReference? ->
                    if (error != null) {
                        logDatabaseError(error, "updateRecipientDataOnDeletion")
                        future.completeExceptionally(RuntimeException("Failed to update recipient data after feedback deletion"))
                    } else {
                        future.complete(Unit)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logDatabaseError(databaseError, "updateRecipientDataOnDeletion")
                future.completeExceptionally(RuntimeException("Failed to retrieve recipient data after feedback deletion"))
            }
        })
        return future
    }
}
