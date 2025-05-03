package com.lulakssoft.mygroceries.view.account

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lulakssoft.mygroceries.R

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // Respond to authentication state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                Log.d("LoginScreen", "Authentication successful, navigating...")
                onAuthSuccess()
            }
            is AuthState.Error -> {
                Log.e("LoginScreen", "Authentication error: ${(authState as AuthState.Error).message}")
            }
            else -> {
                // Handle other states
            }
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (authState) {
            is AuthState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                )
            }
            is AuthState.Error -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Error: ${(authState as AuthState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.resetState() }) {
                        Text("Try Again")
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Welcome to MyGroceries",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.signIn(context) },
                        modifier = Modifier.fillMaxWidth(0.8f),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Icon",
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Sign in with Google")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { viewModel.signInAnonymously() },
                        modifier = Modifier.fillMaxWidth(0.8f),
                    ) {
                        Text("Continue as Guest")
                    }
                }
            }
        }
    }
}
