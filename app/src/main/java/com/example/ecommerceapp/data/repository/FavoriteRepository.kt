package com.example.ecommerceapp.data.repository

import com.example.ecommerceapp.data.local.FavoriteDao
import com.example.ecommerceapp.data.model.Product
import com.example.ecommerceapp.data.model.toFavoriteEntity
import com.example.ecommerceapp.data.model.toProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val dao: FavoriteDao
) {

    fun observeFavorites(): Flow<List<Product>> =
        dao.observeAll().map { list -> list.map { it.toProduct() } }

    fun observeIsFavorite(productId: String): Flow<Boolean> = dao.observeIsFavorite(productId)

    suspend fun toggle(product: Product) {
        if (dao.isFavorite(product.id)) {
            dao.deleteById(product.id)
        } else {
            dao.insert(product.toFavoriteEntity())
        }
    }
}
