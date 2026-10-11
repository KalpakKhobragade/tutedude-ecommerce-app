package com.example.ecommerceapp.data.model

/**
 * Product model used everywhere in the app.
 * Properties are `var` with default values so Firestore can convert documents to this class.
 */
data class Product(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var category: String = "Other",
    var imageUrls: List<String> = emptyList(),
    var uploaderId: String = "",
    var uploaderName: String = "",
    var uploaderContact: String = "",
    var timestamp: Long = 0L
)
