package com.lulakssoft.mygroceries.view.products

import android.content.Context
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import com.lulakssoft.mygroceries.R
import com.lulakssoft.mygroceries.view.scanner.toLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsCreationView(
    viewModel: ProductsCreationViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current

    ProductCreationForm(
        viewModel = viewModel,
        onSaveComplete = onNavigateBack,
        context = context,
    )
}

@Composable
fun ProductCreationForm(
    viewModel: ProductsCreationViewModel,
    onSaveComplete: () -> Unit,
    context: Context,
) {
    var productName by remember { mutableStateOf("") }
    var productBrand by remember { mutableStateOf("") }
    var productQuantitySlider by remember { mutableFloatStateOf(1f) }
    var productBestBeforeDate by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var isFormValid by remember { mutableStateOf(false) }
    var productImage by remember { mutableStateOf<ImageBitmap?>(null) }

    val scrollState = rememberScrollState()

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            uri?.let {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    if (bitmap.width > 800 || bitmap.height > 800) {
                        Log.d("ProductCreationForm", "Image is too large, scaling down...")
                        val scale = 800f / maxOf(bitmap.width, bitmap.height)
                        val newWidth = (bitmap.width * scale).toInt()
                        val newHeight = (bitmap.height * scale).toInt()
                        val scaledBitmap = bitmap.scale(newWidth, newHeight)
                        productImage = scaledBitmap.asImageBitmap()
                    } else {
                        productImage = bitmap.asImageBitmap()
                    }
                    inputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    // Validate form
    LaunchedEffect(productName, productBrand) {
        isFormValid = productName.isNotBlank() && productBrand.isNotBlank()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Product Image
        ImageSelectionCard(
            productImage = productImage,
            onSelectImage = { imagePickerLauncher.launch("image/*") },
        )

        // Product Name
        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            isError = productName.isEmpty(),
        )

        // Product Brand
        OutlinedTextField(
            value = productBrand,
            onValueChange = { productBrand = it },
            label = { Text("Brand") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            isError = productBrand.isEmpty(),
        )

        // Product Quantity
        Text(
            text = "Quantity: ${productQuantitySlider.toInt()}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )

        Slider(
            value = productQuantitySlider,
            onValueChange = { productQuantitySlider = it },
            valueRange = 1f..100f,
            steps = 99,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )

        // Best Before Date
        DateSelector(
            selectedDate = productBestBeforeDate,
            onDateSelected = { productBestBeforeDate = it },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                viewModel.saveProduct(
                    name = productName,
                    brand = productBrand,
                    quantity = productQuantitySlider.toInt(),
                    bestBeforeDate = productBestBeforeDate,
                    image = productImage,
                )
                onSaveComplete()
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save Product")
        }
    }
}

@Composable
fun ImageSelectionCard(
    productImage: ImageBitmap?,
    onSelectImage: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable(onClick = onSelectImage),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        if (productImage != null) {
            Image(
                bitmap = productImage,
                contentDescription = "Product image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.no_image),
                        contentDescription = "Add image",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Tap to add an image",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                FloatingActionButton(
                    onClick = onSelectImage,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add image",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    val initialCalendar =
        remember {
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 7)
            }
        }
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = initialCalendar.timeInMillis,
            initialDisplayMode = DisplayMode.Picker,
        )
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Best Before Date",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )

                Text(
                    text = selectedDate.format(formatter),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showDatePicker = false
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date =
                                    Calendar.getInstance().apply {
                                        timeInMillis = millis
                                    }
                                onDateSelected(date.toLocalDate())
                            }
                        }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancle")
                        }
                    },
                ) {
                    DatePicker(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        state = datePickerState,
                        showModeToggle = true,
                    )
                }
            }
            IconButton(
                onClick = { showDatePicker = true },
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date Picker",
                )
            }
        }
    }
}
