package com.example.ecommerceapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user
            ?: throw IllegalStateException("Registration failed. Please try again.")

        // Best effort: save the display name and the profile (name + phone) for the uploader info.
        runCatching {
            val request = UserProfileChangeRequest.Builder().setDisplayName(name).build()
            user.updateProfile(request).await()
        }
        runCatching {
            val profile = mapOf("name" to name, "email" to email, "phone" to phone)
            firestore.collection("users").document(user.uid).set(profile).await()
        }
        Unit
    }

    fun logout() {
        auth.signOut()
    }
}
