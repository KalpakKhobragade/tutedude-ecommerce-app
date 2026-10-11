package com.example.ecommerceapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ecommerceapp.ui.components.ErrorBox
import com.example.ecommerceapp.ui.components.LoadingBox
import com.example.ecommerceapp.ui.components.ProductList
import com.example.ecommerceapp.ui.favorites.FavoritesTab
import com.example.ecommerceapp.util.AppConfig

private data class TabItem(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    onUploadClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf(
        TabItem("Products", Icons.Filled.Home),
        TabItem("Recommended", Icons.Filled.Star),
        TabItem("Favorites", Icons.Filled.Favorite)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TuteDude Shop") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    TextButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Text("Logout", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = onUploadClick) {
                    Icon(Icons.Filled.Add, contentDescription = "Upload product")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> ProductsTab(onProductClick = onProductClick, viewModel = viewModel)
                1 -> RecommendedTab(onProductClick = onProductClick)
                else -> FavoritesTab(onProductClick = onProductClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductsTab(
    onProductClick: (String) -> Unit,
    viewModel: HomeViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onQueryChange(it) },
            label = { Text("Search products") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AppConfig.FILTER_CATEGORIES) { item ->
                FilterChip(
                    selected = item == category,
                    onClick = { viewModel.onCategoryChange(item) },
                    label = { Text(item) }
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            val error = state.error
            when {
                state.isLoading -> LoadingBox()
                error != null -> ErrorBox(message = error)
                else -> ProductList(
                    products = state.products,
                    favoriteIds = favoriteIds,
                    emptyText = "No products found.\nTap + to upload the first one!",
                    onProductClick = onProductClick,
                    onFavoriteClick = { viewModel.toggleFavorite(it) }
                )
            }
        }
    }
}

@Composable
private fun RecommendedTab(
    onProductClick: (String) -> Unit,
    viewModel: RecommendedViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val error = state.error

    when {
        state.isLoading -> LoadingBox()
        error != null -> ErrorBox(message = error, onRetry = { viewModel.load() })
        else -> ProductList(
            products = state.products,
            favoriteIds = favoriteIds,
            emptyText = "No recommended products available.",
            onProductClick = onProductClick,
            onFavoriteClick = { viewModel.toggleFavorite(it) }
        )
    }
}
