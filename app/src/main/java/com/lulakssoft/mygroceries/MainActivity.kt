package com.lulakssoft.mygroceries

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lulakssoft.mygroceries.database.HouseholdDatabase
import com.lulakssoft.mygroceries.ui.theme.MyGroceriesTheme
import com.lulakssoft.mygroceries.view.main.MainView
import com.lulakssoft.mygroceries.view.main.MainViewModel
import com.lulakssoft.mygroceries.view.main.SecondView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val viewModel = viewModel<MainViewModel>()
    viewModel.initialize(HouseholdDatabase.getInstance(LocalContext.current))

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "mainView") {
        composable(route = "mainView") {
            MainView(navController = navController, viewModel = viewModel)
        }
        composable(route = "secondView") {
            SecondView(navController = navController, viewModel = viewModel)
        }
    }
}
