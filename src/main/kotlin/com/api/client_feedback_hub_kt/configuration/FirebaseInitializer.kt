package com.api.client_feedback_hub_kt.configuration

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.io.IOException

@Configuration
class FirebaseInitializer {

    @Value("\${firebase.config.path}")
    private lateinit var firebaseConfigPath: String

    @Bean
    @Throws(IOException::class)
    fun firebaseApp(): FirebaseApp {
        // Check that the path to the file is correct and the file exists
        val serviceAccount = FileInputStream(firebaseConfigPath)

        // Creating FirebaseOptions using service account data
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://client-feedback-hub-default-rtdb.europe-west1.firebasedatabase.app/")
            .build()

        // Initializing Firebase application if it has not been initialized yet
        return FirebaseApp.initializeApp(options)
    }
}