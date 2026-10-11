package com.example.ecommerceapp.ui.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ecommerceapp.ui.components.ProductList

@Composable
fun FavoritesTab(
    onProductClick: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    ProductList(
        products = favorites,
        favoriteIds = favorites.map { it.id }.toSet(),
        emptyText = "No favorites yet.\nTap the heart on any product to save it here.",
        onProductClick = onProductClick,
        onFavoriteClick = { viewModel.toggleFavorite(it) }
    )
}
