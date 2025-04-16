package com.lulakssoft.mygroceries

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
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
        enableEdgeToEdge()
        setContent {
            MyGroceriesTheme {
                MyGroceriesApp()
            }
        }
    }
}

@Composable
fun MyGroceriesApp() {
    val context = LocalContext.current
    val authClient = GoogleAuthUiClient(context)
    val authViewModel = viewModel { AuthViewModel(authClient) }
    val authState by authViewModel.authState.collectAsState()

    val mainViewModel = viewModel<MainViewModel>()
    mainViewModel.initialize(DatabaseApp.getInstance(context))

    when (authState) {
        is AuthState.Authenticated -> {
            // Pass authenticated user to MainView
            val userData = (authState as AuthState.Authenticated).userData
            mainViewModel.setCurrentUser(userData)
            MainView(mainViewModel)
        }
        else -> {
            SignInScreen(
                viewModel = authViewModel,
                onAuthSuccess = { /* Navigation wird durch authState behandelt */ },
            )
        }
    }
}
