package com.example.ecommerceapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface FakeStoreApi {

    @GET("products")
    suspend fun getProducts(): List<FakeProductDto>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): FakeProductDto
}
