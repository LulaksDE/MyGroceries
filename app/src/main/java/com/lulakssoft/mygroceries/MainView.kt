package com.lulakssoft.mygroceries

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(navController: NavController) {
    val expanded = remember { mutableStateOf(false) }
    val selectedOption = remember { mutableStateOf("") }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        if (selectedOption.value.isEmpty()) "Bitte Haushalt wählen" else selectedOption.value,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { expanded.value = true },
                    )
                    IconButton(onClick = { expanded.value = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_drop_down_24),
                            contentDescription = "Dropdown Menu",
                        )
                    }
                }
                DropdownMenu(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Haushalt 01") },
                        onClick = {
                            selectedOption.value = "Haushalt 01"
                            expanded.value = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Haushalt 02") },
                        onClick = {
                            selectedOption.value = "Haushalt 02"
                            expanded.value = false
                        },
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Content(navController, selectedOption)
        }
    }
}

@Composable
fun Content(
    navController: NavController,
    selectedOption: MutableState<String>,
) {
    AnimatedVisibility(visible = !selectedOption.value.isEmpty()) {
        Row(
            modifier =
                Modifier
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(25.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Dont forget to buy groceries!",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            OutlinedButton(
                onClick = { navController.navigate("secondView") },
                modifier = Modifier.padding(top = 2.dp),
            ) { Text(selectedOption.value, color = MaterialTheme.colorScheme.onPrimary) }
        }
    }
}
