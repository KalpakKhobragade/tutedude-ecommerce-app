package com.example.ecommerceapp.ui.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerceapp.data.repository.ProductRepository
import com.example.ecommerceapp.util.AppConfig
import com.example.ecommerceapp.util.toFriendlyMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UploadUiState(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val category: String = "Other",
    val images: List<Uri> = emptyList(),
    val isUploading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, error = null) }

    fun onDescriptionChange(value: String) =
        _uiState.update { it.copy(description = value, error = null) }

    fun onPriceChange(value: String) = _uiState.update { it.copy(price = value, error = null) }

    fun onCategoryChange(value: String) = _uiState.update { it.copy(category = value) }

    fun addImages(uris: List<Uri>) {
        _uiState.update {
            it.copy(
                images = (it.images + uris).distinct().take(AppConfig.MAX_IMAGES),
                error = null
            )
        }
    }

    fun removeImage(uri: Uri) {
        _uiState.update { it.copy(images = it.images - uri) }
    }

    fun upload() {
        val state = _uiState.value
        val price = state.price.toDoubleOrNull()

        val validationError = when {
            state.title.isBlank() -> "Please enter a title."
            state.description.isBlank() -> "Please enter a description."
            price == null || price <= 0.0 -> "Please enter a valid price greater than 0."
            state.images.size < AppConfig.MIN_IMAGES ->
                "Please select at least ${AppConfig.MIN_IMAGES} images."
            else -> null
        }
        if (validationError != null || price == null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            repository.uploadProduct(
                title = state.title.trim(),
                description = state.description.trim(),
                price = price,
                category = state.category,
                imageUris = state.images
            ).onSuccess {
                _uiState.update { it.copy(isUploading = false, isSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isUploading = false, error = e.toFriendlyMessage()) }
            }
        }
    }
}
