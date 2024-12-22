package com.lulakssoft.mygroceries.view.products

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun ProductsView(viewModel: ProductsViewModel) {
    val scannedQrCode by viewModel.scannedQrCode.observeAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            if (viewModel.product.name.isEmpty()) {
                Text("No product selected")
            } else {
                Text("Product: ${viewModel.product.name}")
            }
            // Show scanned QR code or other content
            scannedQrCode?.let {
                Text(
                    text = "Scanned QR Code: $it",
                    modifier = Modifier.padding(16.dp),
                )
            }

            ProductViewWithScanner { qrCode ->
                viewModel.onQrCodeScanned(qrCode) // Pass the QR code to the ViewModel
            }
        }
    }
}

@Composable
fun ProductViewWithScanner(onQrCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }

    // Request Camera Permission
    RequestPermission(permission = android.Manifest.permission.CAMERA) { granted ->
        hasCameraPermission = granted
    }

    if (hasCameraPermission) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Scanner Area
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                QrCodeScanner(onQrCodeScanned = onQrCodeScanned)
            }

            // Other UI elements below the scanner
            Text(
                text = "Other content below the scanner",
                modifier = Modifier.padding(16.dp),
            )
        }
    } else {
        Text("Camera permission is required to use the scanner.")
    }
}

@Composable
fun QrCodeScanner(onQrCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val barcodeView = remember { DecoratedBarcodeView(context) }

    // Observe the lifecycle of the composable
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
                                onQrCodeScanned(qrCode) // Pass scanned QR code to the callback
                            }
                        }

                        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {
                            // Optional: Handle possible result points
                        }
                    },
                )
                resume() // Start the scanner
            }
        },
        modifier = Modifier.fillMaxWidth().height(300.dp).clip(MaterialTheme.shapes.medium),
    )
}

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
