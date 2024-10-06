package com.api.client_feedback_hub_kt.mapper

import com.api.client_feedback_hub_kt.dto.FeedbackResponseDto
import com.api.client_feedback_hub_kt.entity.Feedback
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class FeedbackMapper(
    private val modelMapper: ModelMapper
) {
    fun convertToDto(feedback: Feedback): FeedbackResponseDto {
        return modelMapper.map(feedback, FeedbackResponseDto::class.java)
    }
}
