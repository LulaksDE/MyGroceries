package com.lulakssoft.mygroceries.view.household.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHouseholdView(
    navController: NavController,
    createViewModel: CreateHouseholdViewModel,
    navigateUp: () -> Unit,
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Haushalt hinzufügen") })
    }) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Bitte geben Sie den Namen des neuen Haushalts ein.", modifier = Modifier.padding(16.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = createViewModel.householdText,
                onValueChange = { createViewModel.householdText = it },
                label = { Text("Haushalt eingabe") },
            )
            Button(onClick = { createViewModel.insert() }, modifier = Modifier.padding(top = 5.dp)) { Text("Haushalt hinzufügen") }
            Button(
                onClick = { navigateUp() },
                modifier = Modifier.padding(top = 5.dp),
            ) { Text("Zurück zur Hauptseite") }
        }
    }
}
