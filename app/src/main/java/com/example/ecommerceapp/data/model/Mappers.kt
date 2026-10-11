package com.example.ecommerceapp.data.model

import com.example.ecommerceapp.data.local.FavoriteEntity
import com.example.ecommerceapp.data.remote.FakeProductDto
import java.util.Locale

const val FAKE_STORE_PREFIX = "fs_"

fun FakeProductDto.toProduct(): Product = Product(
    id = FAKE_STORE_PREFIX + id,
    title = title,
    description = description,
    price = price,
    category = category.replaceFirstChar { it.titlecase(Locale.getDefault()) },
    imageUrls = listOf(image),
    uploaderId = "fakestore",
    uploaderName = "FakeStore API",
    uploaderContact = "https://fakestoreapi.com"
)

fun Product.toFavoriteEntity(): FavoriteEntity = FavoriteEntity(
    id = id,
    title = title,
    description = description,
    price = price,
    imageUrl = imageUrls.firstOrNull().orEmpty(),
    category = category,
    uploaderName = uploaderName,
    uploaderContact = uploaderContact
)

fun FavoriteEntity.toProduct(): Product = Product(
    id = id,
    title = title,
    description = description,
    price = price,
    category = category,
    imageUrls = if (imageUrl.isBlank()) emptyList() else listOf(imageUrl),
    uploaderName = uploaderName,
    uploaderContact = uploaderContact
)
