package com.api.client_feedback_hub_kt.mapper

import com.api.client_feedback_hub_kt.entity.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.modelmapper.ModelMapper

class UserMapperTest {

    private val modelMapper = ModelMapper()
    private val userMapper = UserMapper(modelMapper)

    @Test
    fun testConvertToDto() {
        val user = User(
            userId = 1,
            email = "test@example.com",
            name = "John Doe",
            phoneNumber = "1234567890",
            role = "USER", // Provide a default role if needed
            hashedPassword = "password",
            averageRating = 4.5,
            numberReviewers = 10
        )

        val userResponseDto = userMapper.convertToDto(user)

        assertEquals("test@example.com", userResponseDto.email)
        assertEquals("John Doe", userResponseDto.name) // Additional assertions
        assertEquals(4.5, userResponseDto.averageRating, 0.01) // Allow a delta for doubles
        assertEquals(10, userResponseDto.numberReviewers)
    }
}