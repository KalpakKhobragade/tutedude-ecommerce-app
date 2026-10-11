package com.example.ecommerceapp.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/** Converts exceptions into short messages that are safe to show to the user. */
fun Throwable.toFriendlyMessage(): String = when (this) {
    is FirebaseAuthWeakPasswordException -> "Password is too weak. Use at least 6 characters."
    is FirebaseAuthInvalidUserException -> "No account found with this email."
    is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
    is FirebaseAuthUserCollisionException -> "An account with this email already exists."
    is FirebaseNetworkException -> "Network error. Please check your internet connection."
    else -> message ?: "Something went wrong. Please try again."
}
