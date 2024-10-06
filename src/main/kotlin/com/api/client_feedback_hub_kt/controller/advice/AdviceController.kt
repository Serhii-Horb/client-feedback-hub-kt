package com.api.client_feedback_hub_kt.controller.advice

import org.modelmapper.spi.ErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AdviceController {

    /**
     * Handles runtime exceptions.
     * Returns a 404 Not Found status with an error message.
     *
     * @param exception the thrown [RuntimeException]
     * @return a [ResponseEntity] with status 404 and an [ErrorMessage]
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleException(ex: RuntimeException): ResponseEntity<String> {
        val errorMessage = ex.message ?: "An unknown error occurred"
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage)
    }

}