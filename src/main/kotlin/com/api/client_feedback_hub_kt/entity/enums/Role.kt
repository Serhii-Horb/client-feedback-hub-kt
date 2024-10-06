package com.api.client_feedback_hub_kt.entity.enums

enum class Role(val role: String) {
    /**
     * Standard user role with basic access rights.
     */
    USER("User"),

    /**
     * Administrator role with elevated privileges and access rights.
     */
    ADMINISTRATOR("Administrator");
}