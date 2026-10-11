package com.example.ecommerceapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerceapp.data.model.Product
import com.example.ecommerceapp.data.repository.FavoriteRepository
import com.example.ecommerceapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendedViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    val favoriteIds: StateFlow<Set<String>> = favoriteRepository.observeFavorites()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProductsUiState(isLoading = true)
            productRepository.getRecommended()
                .onSuccess { _uiState.value = ProductsUiState(isLoading = false, products = it) }
                .onFailure {
                    _uiState.value = ProductsUiState(
                        isLoading = false,
                        error = "Could not load recommended products. Check your internet connection."
                    )
                }
        }
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch { favoriteRepository.toggle(product) }
    }
}
