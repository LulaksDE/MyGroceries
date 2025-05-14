package com.lulakssoft.mygroceries.view.products

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lulakssoft.mygroceries.R
import com.lulakssoft.mygroceries.database.product.Product
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsView(
    viewModel: ProductsViewModel,
    onSyncProducts: () -> Unit,
    onNavigateToCreation: () -> Unit,
    syncing: Boolean,
) {
    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedProducts by remember { mutableStateOf(setOf<Product>()) }

    Scaffold(
        topBar = {
            Column {
                if (selectionMode) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${selectedProducts.size} selected",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Row {
                            IconButton(onClick = {
                                selectionMode = false
                                selectedProducts = emptySet()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel selection")
                            }
                            IconButton(
                                onClick = {
                                    viewModel.deleteSelectedProducts(selectedProducts.toList())
                                    selectionMode = false
                                    selectedProducts = emptySet()
                                },
                                enabled = selectedProducts.isNotEmpty(),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_delete_24),
                                    contentDescription = "Delete selected products",
                                    tint =
                                        if (selectedProducts.isEmpty()) {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        },
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        placeholder = { Text("Search products...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Delete search query",
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${viewModel.productList.size} products",
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Row {
                            IconButton(onClick = { selectionMode = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_select_all_24),
                                    contentDescription = "Select all",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            IconToggleButton(
                                checked = !isGridView,
                                onCheckedChange = { isGridView = !it },
                            ) {
                                if (!isGridView) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = "Listlayout",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_grid_view_24),
                                        contentDescription = "Gridlayout",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider()
                }
            }
        },
        floatingActionButton = {
            if (!selectionMode) {
                FloatingActionButton(
                    onClick = onNavigateToCreation,
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add product",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = syncing,
            onRefresh = onSyncProducts,
        ) {
            if (syncing) {
                selectionMode = false
            }
            if (viewModel.productList.isEmpty()) {
                EmptyProductsView()
            } else {
                val filteredProducts =
                    viewModel.productList.filter {
                        it.productName.contains(searchQuery, ignoreCase = true) ||
                            searchQuery.isEmpty()
                    }

                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(padding),
                    ) {
                        items(count = filteredProducts.size) { size ->
                            val product = filteredProducts[size]
                            ProductGridItem(
                                product = product,
                                isSelected = selectedProducts.contains(product),
                                selectionMode = selectionMode,
                                onSelect = {
                                    if (selectionMode) {
                                        selectedProducts =
                                            if (selectedProducts.contains(product)) {
                                                selectedProducts - product
                                            } else {
                                                selectedProducts + product
                                            }
                                    }
                                },
                                onLongPress = {
                                    if (!selectionMode) {
                                        selectionMode = true
                                        selectedProducts = setOf(product)
                                    }
                                },
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(padding),
                    ) {
                        items(filteredProducts) { product ->
                            ProductListItem(
                                product = product,
                                isSelected = selectedProducts.contains(product),
                                selectionMode = selectionMode,
                                onSelect = {
                                    if (selectionMode) {
                                        selectedProducts =
                                            if (selectedProducts.contains(product)) {
                                                selectedProducts - product
                                            } else {
                                                selectedProducts + product
                                            }
                                    }
                                },
                                onLongPress = {
                                    if (!selectionMode) {
                                        selectionMode = true
                                        selectedProducts = setOf(product)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductGridItem(
    product: Product,
    isSelected: Boolean,
    selectionMode: Boolean,
    onSelect: () -> Unit,
    onLongPress: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.9f)
                .padding(4.dp)
                .combinedClickable(
                    onClick = { onSelect() },
                    onLongClick = { onLongPress() },
                ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                if (product.productImage.width < 4 || product.productImage.height < 4) {
                    Icon(
                        painter = painterResource(id = R.drawable.no_image),
                        contentDescription = "Product image",
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Image(
                        bitmap = product.productImage,
                        contentDescription = "Product image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                if (selectionMode) {
                    Box(
                        modifier =
                            Modifier
                                .size(48.dp)
                                .padding(4.dp)
                                .background(
                                    color =
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                        },
                                    shape = CircleShape,
                                ).align(Alignment.TopEnd),
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier =
                                    Modifier
                                        .size(16.dp)
                                        .align(Alignment.Center),
                            )
                        }
                    }
                }
                if (!product.isSynced) {
                    Icon(
                        painter = painterResource(id = R.drawable.not_synced),
                        contentDescription = "Not synced",
                        modifier =
                            Modifier
                                .size(64.dp)
                                .align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
        ) {
            Text(
                text = product.productName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = product.productBrand,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = product.productBestBeforeDate.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val daysUntilExpiration = product.calculateDaysUntilExpiration()
        if (daysUntilExpiration < 7) {
            Surface(
                color =
                    if (daysUntilExpiration < 3) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    },
                shape = RoundedCornerShape(bottomEnd = 8.dp),
                modifier = Modifier.fillMaxHeight().align(Alignment.End),
            ) {
                Text(
                    text = "BBD: $daysUntilExpiration days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(bottomEnd = 8.dp),
                modifier = Modifier.fillMaxHeight().align(Alignment.End),
            ) {
                Text(
                    text = "BBD: $daysUntilExpiration days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductListItem(
    product: Product,
    isSelected: Boolean,
    selectionMode: Boolean,
    onSelect: () -> Unit,
    onLongPress: () -> Unit,
) {
    Card(
        modifier =
            Modifier.fillMaxWidth().combinedClickable(
                onClick = { onSelect() },
                onLongClick = { onLongPress() },
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(90.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                if (product.productImage.width < 4 || product.productImage.height < 4) {
                    Icon(
                        painter = painterResource(id = R.drawable.no_image),
                        contentDescription = "Product image",
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Image(
                        bitmap = product.productImage,
                        contentDescription = "Product image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                if (selectionMode) {
                    Box(
                        modifier =
                            Modifier
                                .size(48.dp)
                                .padding(4.dp)
                                .background(
                                    color =
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                        },
                                    shape = CircleShape,
                                ).align(Alignment.TopEnd),
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier =
                                    Modifier
                                        .size(16.dp)
                                        .align(Alignment.Center),
                            )
                        }
                    }
                }
                if (!product.isSynced) {
                    Icon(
                        painter = painterResource(id = R.drawable.not_synced),
                        contentDescription = "Not synced",
                        modifier =
                            Modifier
                                .size(64.dp)
                                .align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(12.dp),
            ) {
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    text = product.productBrand,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(4.dp))

                val daysUntilExpiration = product.calculateDaysUntilExpiration()
                val bbdColor =
                    when {
                        daysUntilExpiration < 3 -> MaterialTheme.colorScheme.error
                        daysUntilExpiration < 7 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = bbdColor,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BBD: ${product.productBestBeforeDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = bbdColor,
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyProductsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No products available",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scan products to add them to your list.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

fun Product.calculateDaysUntilExpiration(): Int {
    val today = LocalDate.now()
    return ChronoUnit.DAYS.between(today, productBestBeforeDate).toInt()
}
