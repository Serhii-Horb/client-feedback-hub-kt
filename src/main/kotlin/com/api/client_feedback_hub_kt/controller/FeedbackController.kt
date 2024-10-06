package com.api.client_feedback_hub_kt.controller

import com.api.client_feedback_hub_kt.dto.FeedbackRequestDto
import com.api.client_feedback_hub_kt.dto.FeedbackResponseDto
import com.api.client_feedback_hub_kt.service.FeedbackService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/api/feedbacks")
class FeedbackController(private val feedbackService: FeedbackService) {

    @GetMapping("/{id}")
    fun getFeedbackById(@PathVariable id: String): CompletableFuture<ResponseEntity<FeedbackResponseDto>> {
        return feedbackService.getFeedbackById(id)
            .thenApply { ResponseEntity.ok(it) }
    }

    @GetMapping("/reviewer/{id}")
    fun getFeedbackByReviewerId(@PathVariable id: String): CompletableFuture<ResponseEntity<List<FeedbackResponseDto>>> {
        return feedbackService.getAllFeedbacksByReviewerId(id)
            .thenApply { ResponseEntity.ok(it) }
    }

    @GetMapping("/recipient/{id}")
    fun getAllFeedbacksByRecipientId(@PathVariable id: String): CompletableFuture<ResponseEntity<List<FeedbackResponseDto>>> {
        return feedbackService.getAllFeedbacksByRecipientId(id)
            .thenApply { ResponseEntity.ok(it) }
    }

    @PostMapping
    fun createFeedback(@Valid @RequestBody feedbackRequestDto: FeedbackRequestDto): CompletableFuture<ResponseEntity<String>> {
        return feedbackService.createFeedback(feedbackRequestDto)
            .thenApply { feedbackId -> ResponseEntity.ok("Feedback created successfully with ID: $feedbackId") }
    }

    @GetMapping
    fun getAllFeedbacks(): CompletableFuture<ResponseEntity<List<FeedbackResponseDto>>> {
        return feedbackService.getAllFeedbacks()
            .thenApply { ResponseEntity.ok(it) }
    }

    @DeleteMapping("/{id}")
    fun deleteFeedback(@PathVariable id: String): CompletableFuture<ResponseEntity<String>> {
        return feedbackService.deleteFeedbackById(id)
            .thenApply { ResponseEntity.ok("Feedback deletion requested for ID: $id") }
    }
}