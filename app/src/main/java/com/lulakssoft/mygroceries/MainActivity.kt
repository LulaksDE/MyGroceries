package com.lulakssoft.mygroceries

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lulakssoft.mygroceries.database.household.HouseholdDatabase
import com.lulakssoft.mygroceries.ui.theme.MyGroceriesTheme
import com.lulakssoft.mygroceries.view.main.MainView
import com.lulakssoft.mygroceries.view.main.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyGroceriesTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                MainView(mainViewState = uiState)
            }
        }
    }
}
