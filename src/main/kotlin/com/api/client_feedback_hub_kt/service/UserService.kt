package com.api.client_feedback_hub_kt.service

import com.api.client_feedback_hub_kt.dto.UserRequestDto
import com.api.client_feedback_hub_kt.dto.UserResponseDto
import com.api.client_feedback_hub_kt.entity.User
import com.api.client_feedback_hub_kt.mapper.UserMapper
import com.google.firebase.database.*

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError

import org.mindrot.jbcrypt.BCrypt
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class UserService(private val userMapper: UserMapper) {

    companion object {
        private const val DEFAULT_ROLE = "USER"
        private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    }

    fun getAllUsers(): CompletableFuture<List<UserResponseDto>> {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        val futureUsers = CompletableFuture<List<UserResponseDto>>()
        val userList = mutableListOf<UserResponseDto>()

        logger.info("Starting to fetch all users from database")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                logger.info("DataSnapshot received from database")
                if (dataSnapshot.exists()) {
                    logger.info("Data found, processing users...")
                    dataSnapshot.children.forEach { snapshot ->
                        val user = snapshot.getValue(User::class.java)
                        if (user != null) {
                            userList.add(userMapper.convertToDto(user))
                            logger.info("User added: {}", user.userId)
                        } else {
                            logger.warn("Failed to parse data as User object")
                        }
                    }
                    futureUsers.complete(userList)
                } else {
                    logger.info("No data found")
                    futureUsers.complete(emptyList())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logger.error("Database error: {}", databaseError.message)
                futureUsers.completeExceptionally(RuntimeException("Database read failed"))
            }
        })

        return futureUsers
    }

    fun getUserById(id: Long): CompletableFuture<UserResponseDto> {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        val future = CompletableFuture<UserResponseDto>()

        ref.child(id.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        future.complete(userMapper.convertToDto(user))
                    } else {
                        future.completeExceptionally(RuntimeException("User data found but failed to parse it"))
                    }
                } else {
                    logger.warn("No user found with ID: {}", id)
                    future.completeExceptionally(RuntimeException("No user found with the provided ID: $id"))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logger.error("Database error occurred while retrieving user with ID {}: {}", id, databaseError.message)
                future.completeExceptionally(RuntimeException("Error occurred while accessing the database for user ID: $id"))
            }
        })
        return future
    }

    fun createUser(userRegisterDto: UserRequestDto): CompletableFuture<String> {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        val counterRef = FirebaseDatabase.getInstance().getReference("userIdCounter")
        val future = CompletableFuture<String>()

        counterRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var currentId = dataSnapshot.getValue(Long::class.java) ?: 0L
                val uniqueUserId = currentId + 1

                val hashedPassword = BCrypt.hashpw(userRegisterDto.password, BCrypt.gensalt())
                val newUser = User(
                    uniqueUserId, userRegisterDto.email, userRegisterDto.name,
                    userRegisterDto.phoneNumber, DEFAULT_ROLE, hashedPassword, 0.0, 0
                )

                ref.child(uniqueUserId.toString()).setValue(newUser) { databaseError, _ ->
                    if (databaseError != null) {
                        logger.error("Failed to save user: {}", databaseError.message)
                        future.completeExceptionally(RuntimeException("User creation failed: ${databaseError.message}"))
                    } else {
                        logger.info("User saved successfully")
                        counterRef.setValue(uniqueUserId) { databaseError1, _ ->
                            if (databaseError1 != null) {
                                logger.error("Failed to update user ID counter: {}", databaseError1.message)
                            }
                        }
                        future.complete("User created with ID: $uniqueUserId")
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                future.completeExceptionally(RuntimeException("Failed to read user ID counter: ${databaseError.message}"))
            }
        })

        return future
    }

    fun updateUser(id: Long, userRequestDto: UserRequestDto): CompletableFuture<String> {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        val future = CompletableFuture<String>()

        ref.child(id.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)?.apply {
                        email = userRequestDto.email
                        name = userRequestDto.name
                        phoneNumber = userRequestDto.phoneNumber
                        hashedPassword = BCrypt.hashpw(userRequestDto.password, BCrypt.gensalt())
                    }
                    ref.child(id.toString()).setValue(user) { databaseError, _ ->
                        if (databaseError != null) {
                            logger.error("Failed to update user: {}", databaseError.message)
                            future.completeExceptionally(RuntimeException("User update failed: ${databaseError.message}"))
                        } else {
                            logger.info("User updated successfully")
                            future.complete(id.toString())
                        }
                    }
                } else {
                    logger.warn("User with ID: {} does not exist", id)
                    future.completeExceptionally(RuntimeException("User not found: $id"))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logger.error("Failed to check user existence: {}", databaseError.message)
                future.completeExceptionally(RuntimeException("Failed to check user existence"))
            }
        })

        return future
    }

    fun deleteUser(id: Long): CompletableFuture<Unit> {
        val ref = FirebaseDatabase.getInstance().getReference("users").child(id.toString())
        val future = CompletableFuture<Unit>()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    ref.removeValue { databaseError, _ ->
                        if (databaseError != null) {
                            logger.error("Failed to delete user with ID: {}. Error: {}", id, databaseError.message)
                            future.completeExceptionally(RuntimeException("Failed to delete user with ID: $id. Error: ${databaseError.message}"))
                        } else {
                            logger.info("User with ID: {} was deleted successfully.", id)
                            future.complete(Unit)
                        }
                    }
                } else {
                    logger.warn("User with ID: {} does not exist.", id)
                    future.completeExceptionally(RuntimeException("User with ID: $id does not exist."))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logger.error("Error checking for user with ID: {}. Error: {}", id, databaseError.message)
                future.completeExceptionally(RuntimeException("Error checking for user with ID: $id. Error: ${databaseError.message}"))
            }
        })
        return future
    }
}