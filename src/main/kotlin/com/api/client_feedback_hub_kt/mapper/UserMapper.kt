package com.api.client_feedback_hub_kt.mapper

import com.api.client_feedback_hub_kt.dto.UserResponseDto
import com.api.client_feedback_hub_kt.entity.User
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class UserMapper(
    private val modelMapper: ModelMapper
) {
    fun convertToDto(user: User): UserResponseDto {
        return modelMapper.map(user, UserResponseDto::class.java)
    }
}