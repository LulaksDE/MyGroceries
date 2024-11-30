package com.lulakssoft.mygroceries.view.main

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
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lulakssoft.mygroceries.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    navController: NavController,
    viewModel: MainViewModel,
) {
    val households by viewModel.households.collectAsState(initial = emptyList())
    val expanded = remember { mutableStateOf(false) }
    val selectedOption = remember { mutableStateOf("") }

    var selectedIcon = BottomBarNavigation.Home


    Scaffold(
        topBar = {
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
                        for (household in households) {
                            DropdownMenuItem(
                                text = { Text(household.householdName) },
                                onClick = {
                                    selectedOption.value = household.householdName
                                    expanded.value = false
                                },
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        bottomBar = {
            NavigationBar {
                BottomAppBar(
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BottomBar(selectedIcon = selectedIcon, onIconSelected = { icon -> selectedIcon = icon }, navController)
                }
            }
        },
    ) { innerPadding ->
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
