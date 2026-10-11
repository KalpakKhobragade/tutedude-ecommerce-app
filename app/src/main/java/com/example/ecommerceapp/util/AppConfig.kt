package com.example.ecommerceapp.util

object AppConfig {
    /**
     * false -> product images are compressed and saved inside Firestore (works on the FREE
     *          Firebase Spark plan, no billing needed).
     * true  -> product images are uploaded to Firebase Storage (requires the Blaze plan,
     *          because Google now requires a billing account for Cloud Storage).
     */
    val USE_FIREBASE_STORAGE: Boolean = false

    val MIN_IMAGES: Int = 3
    val MAX_IMAGES: Int = if (USE_FIREBASE_STORAGE) 10 else 6

    val CATEGORIES: List<String> = listOf("Electronics", "Fashion", "Home", "Books", "Sports", "Other")
    val FILTER_CATEGORIES: List<String> = listOf("All") + CATEGORIES
}
