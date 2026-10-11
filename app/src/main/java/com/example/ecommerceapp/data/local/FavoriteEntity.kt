package com.example.ecommerceapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val uploaderName: String,
    val uploaderContact: String,
    val addedAt: Long = System.currentTimeMillis()
)
