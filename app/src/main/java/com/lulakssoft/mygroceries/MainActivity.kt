package com.lulakssoft.mygroceries

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.ui.theme.MyGroceriesTheme
import com.lulakssoft.mygroceries.view.account.AuthState
import com.lulakssoft.mygroceries.view.account.AuthViewModel
import com.lulakssoft.mygroceries.view.account.GoogleAuthUiClient
import com.lulakssoft.mygroceries.view.account.SignInScreen
import com.lulakssoft.mygroceries.view.main.MainView
import com.lulakssoft.mygroceries.view.main.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        enableEdgeToEdge()
        setContent {
            MyGroceriesTheme {
                MyGroceriesAppUI()
            }
        }
    }
}

@Composable
fun MyGroceriesAppUI() {
    val context = LocalContext.current
    val authClient = GoogleAuthUiClient(context)
    val authViewModel = viewModel { AuthViewModel(authClient) }
    val authState by authViewModel.authState.collectAsState()

    val mainViewModel =
        viewModel<MainViewModel>().apply {
            setOnSignOutCallback { authViewModel.resetState() }
        }

    val onSignOut: () -> Unit = {
        authViewModel.resetState()
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    Log.d("Notifications", "Permission granted")
                } else {
                    Log.d("Notifications", "Permission denied")
                }
            },
        )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    when (authState) {
        is AuthState.Authenticated -> {
            // Pass authenticated user to MainView
            mainViewModel.initialize(DatabaseApp.getInstance(context))
            val userData = (authState as AuthState.Authenticated).userData
            mainViewModel.setCurrentUser(userData)
            MainView(mainViewModel, onSignOut)
        }
        else -> {
            SignInScreen(
                viewModel = authViewModel,
                onAuthSuccess = { /* Navigation wird durch authState behandelt */ },
            )
        }
    }
}
