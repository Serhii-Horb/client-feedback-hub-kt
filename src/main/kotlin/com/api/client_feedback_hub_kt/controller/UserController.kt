package com.api.client_feedback_hub_kt.controller

import com.api.client_feedback_hub_kt.dto.UserRequestDto
import com.api.client_feedback_hub_kt.dto.UserResponseDto
import com.api.client_feedback_hub_kt.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): CompletableFuture<ResponseEntity<UserResponseDto>> {
        return userService.getUserById(id).handle { userResponseDto, throwable ->
            when {
                throwable != null -> {
                    logger.error("Error retrieving user with ID: {}", id, throwable)
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
                }
                userResponseDto != null -> {
                    ResponseEntity.ok(userResponseDto)
                }
                else -> {
                    ResponseEntity.notFound().build()
                }
            }
        }
    }

    @PostMapping
    fun createUser(@RequestBody userRegisterDto: UserRequestDto): CompletableFuture<ResponseEntity<String>> {
        return userService.createUser(userRegisterDto)
            .thenApply { userId -> ResponseEntity.ok("User created successfully with ID: $userId") }
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody userRegisterDto: UserRequestDto,
    ): CompletableFuture<ResponseEntity<String>> {
        return userService.updateUser(id, userRegisterDto)
            .thenApply { userId -> ResponseEntity.ok("User updated successfully with ID: $userId") }
    }

    @GetMapping
    fun getAllUsers(): CompletableFuture<ResponseEntity<List<UserResponseDto>>> {
        return userService.getAllUsers()
            .thenApply { ResponseEntity.ok(it) }
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): CompletableFuture<ResponseEntity<String>> {
        return userService.deleteUser(id)
            .thenApply { ResponseEntity.ok("User deletion requested for ID: $id") }
    }
}
