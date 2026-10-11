package com.example.ecommerceapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerceapp.data.model.Product
import com.example.ecommerceapp.data.repository.AuthRepository
import com.example.ecommerceapp.data.repository.FavoriteRepository
import com.example.ecommerceapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductsUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _category = MutableStateFlow("All")
    val category: StateFlow<String> = _category.asStateFlow()

    private val productsFlow: Flow<ProductsUiState> = productRepository.observeProducts()
        .map { ProductsUiState(isLoading = false, products = it) }
        .catch { e ->
            emit(
                ProductsUiState(
                    isLoading = false,
                    error = "Could not load products: ${e.message}"
                )
            )
        }

    /** Products after applying the search text and the category filter. */
    val uiState: StateFlow<ProductsUiState> =
        combine(productsFlow, _query, _category) { state, query, category ->
            state.copy(
                products = state.products.filter { product ->
                    val matchesCategory =
                        category == "All" || product.category.equals(category, ignoreCase = true)
                    val matchesQuery = query.isBlank() ||
                        product.title.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true)
                    matchesCategory && matchesQuery
                }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductsUiState())

    val favoriteIds: StateFlow<Set<String>> = favoriteRepository.observeFavorites()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onCategoryChange(value: String) {
        _category.value = value
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch { favoriteRepository.toggle(product) }
    }

    fun logout() {
        authRepository.logout()
    }
}
