package com.example.ecommerceapp.data.remote

/** One product as returned by https://fakestoreapi.com/products */
data class FakeProductDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String
)
