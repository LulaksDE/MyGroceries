package com.lulakssoft.mygroceries.view.scanner

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.lulakssoft.mygroceries.dto.ProductDto
import com.lulakssoft.mygroceries.dto.ProductInfo
import java.time.LocalDate

@Composable
fun ScannerView(viewModel: ScannerViewModel) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            if (viewModel.scannedSomething) {
                if (viewModel.errorMessage.isEmpty()) {
                    EnhancedProductInfoDialog(viewModel)
                } else {
                    AlertDialog(
                        onDismissRequest = { viewModel.scannedSomething = false },
                        title = { Text("Error") },
                        text = { Text(viewModel.errorMessage, color = MaterialTheme.colorScheme.error) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.scannedSomething = false }) {
                                Text("OK")
                            }
                        },
                    )
                }
            } else {
                EnhancedProductViewWithScanner { qrCode ->
                    viewModel.onQrCodeScanned(qrCode)
                }
            }
        }
    }
}

@Composable
fun EnhancedProductViewWithScanner(onQrCodeScanned: (String) -> Unit) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current

    RequestPermission(permission = android.Manifest.permission.CAMERA) { granted ->
        hasCameraPermission = granted
    }

    if (hasCameraPermission) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Scan barcode",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp),
            )

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                EnhancedQrCodeScanner(onQrCodeScanned = onQrCodeScanned)

                Box(
                    modifier =
                        Modifier
                            .size(250.dp)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(16.dp),
                            ),
                )
                ScanIndicator()
            }

            // Hilfetext
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Place the barcode inside the frame",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The scan will start automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    } else {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Camera permission required",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Please allow camera access in the app settings to use the scanner.",
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }) {
                Text("Open settings")
            }
        }
    }
}

@Composable
fun EnhancedQrCodeScanner(onQrCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val barcodeView = remember { DecoratedBarcodeView(context) }

    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver =
            object : DefaultLifecycleObserver {
                override fun onPause(owner: LifecycleOwner) {
                    barcodeView.pause() // Pause the scanner when the lifecycle pauses
                }

                override fun onResume(owner: LifecycleOwner) {
                    barcodeView.resume() // Resume the scanner when the lifecycle resumes
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    barcodeView.pause()
                    owner.lifecycle.removeObserver(this)
                }
            }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            barcodeView.pause()
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    AndroidView(
        factory = {
            barcodeView.apply {
                decodeContinuous(
                    object : BarcodeCallback {
                        override fun barcodeResult(result: BarcodeResult?) {
                            result?.text?.let { qrCode ->
                                onQrCodeScanned(qrCode)
                            }
                        }

                        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
                    },
                )
                resume()
                cameraSettings.isMeteringEnabled = true
                cameraSettings.isExposureEnabled = true
                cameraSettings.isAutoFocusEnabled = true
            }
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)),
    )
}

@Composable
fun ScanIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "scan_pulse_animation",
    )

    Box(
        modifier =
            Modifier
                .size(240.dp * scale)
                .alpha(1 - scale + 0.3f)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                ),
    )
}

@Composable
fun EnhancedProductInfoDialog(viewModel: ScannerViewModel) {
    val initialCalendar =
        remember {
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 7)
            }
        }
    var selectedDate by remember { mutableStateOf(initialCalendar) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {
            viewModel.scannedSomething = false
        },
        title = {
            Text(
                text = "Add product",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (viewModel.loading) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Image(
                        bitmap = viewModel.productImage,
                        contentDescription = "product image",
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.FillHeight,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = viewModel.product.product.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.product.product.brand,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Best before date",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                        ) {
                            Text(
                                text = selectedDate.formatToString(),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                textAlign = TextAlign.Center,
                            )
                        }

                        IconButton(
                            onClick = {
                                val datePickerDialog =
                                    DatePickerDialog(
                                        context,
                                        { _, selectedYear, selectedMonth, selectedDay ->
                                            val newDate =
                                                Calendar.getInstance().apply {
                                                    set(selectedYear, selectedMonth, selectedDay)
                                                }
                                            viewModel.productBestBefore = newDate.toLocalDate()
                                            selectedDate = newDate
                                        },
                                        selectedDate.get(Calendar.YEAR),
                                        selectedDate.get(Calendar.MONTH),
                                        selectedDate.get(Calendar.DAY_OF_MONTH),
                                    )
                                datePickerDialog.show()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Date Picker",
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.insert()
                    viewModel.scannedSomething = false
                    viewModel.scannedCode = ""
                    viewModel.product = ProductDto("", ProductInfo("", "", ""))
                    viewModel.productImage = ImageBitmap(1, 1)
                },
                enabled = !viewModel.loading,
                modifier = Modifier.fillMaxWidth(0.5f),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.scannedSomething = false
                    viewModel.scannedCode = ""
                    viewModel.product = ProductDto("", ProductInfo("", "", ""))
                    viewModel.productImage = ImageBitmap(1, 1)
                },
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
    )
}

fun Calendar.formatToString(): String {
    val day = this.get(Calendar.DAY_OF_MONTH)
    val month = this.get(Calendar.MONTH) + 1 // Adding 1 since Calendar.MONTH is 0-based
    val year = this.get(Calendar.YEAR)
    return "$day/$month/$year"
}

fun Calendar.toLocalDate(): LocalDate =
    LocalDate.of(
        this.get(Calendar.YEAR),
        this.get(Calendar.MONTH) + 1, // Adding 1 since Calendar.MONTH is 0-based
        this.get(Calendar.DAY_OF_MONTH),
    )

@Composable
fun RequestPermission(
    permission: String,
    onPermissionResult: (Boolean) -> Unit,
) {
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            onPermissionResult(isGranted)
        }

    val context = LocalContext.current
    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(context, permission) -> {
            onPermissionResult(true)
        }
        else -> {
            SideEffect {
                launcher.launch(permission)
            }
        }
    }
}
