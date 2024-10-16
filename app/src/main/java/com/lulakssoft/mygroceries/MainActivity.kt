package com.lulakssoft.mygroceries

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lulakssoft.mygroceries.ui.theme.MyGroceriesTheme

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
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "mainView") {
        composable(route = "mainView") {
            MainView(navController = navController)
        }
        composable(route = "secondView") {
            SecondView()
        }
    }
}
