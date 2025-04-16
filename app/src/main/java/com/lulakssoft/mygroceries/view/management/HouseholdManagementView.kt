package com.lulakssoft.mygroceries.view.management

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HouseholdManagementView(viewModel: HouseholdManagementViewModel) {
    var showInviteDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var inviteCode by remember { mutableStateOf("") }
    var householdName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Household Management", style = MaterialTheme.typography.headlineMedium)

        Button(
            onClick = { showJoinDialog = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        ) {
            Text("Join Household")
        }

        Button(
            onClick = { showInviteDialog = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        ) {
            Text("Create Household")
        }

        // Dialog zum Erstellen eines Haushalts
        if (showInviteDialog) {
            AlertDialog(
                onDismissRequest = { showInviteDialog = false },
                title = { Text("Create New Household") },
                text = {
                    TextField(
                        value = householdName,
                        onValueChange = { householdName = it },
                        label = { Text("Household Name") },
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.createHousehold(householdName)
                        showInviteDialog = false
                        householdName = ""
                    }) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    Button(onClick = { showInviteDialog = false }) {
                        Text("Cancel")
                    }
                },
            )
        }

        // Dialog zum Beitreten eines Haushalts
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                title = { Text("Join Household") },
                text = {
                    TextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it },
                        label = { Text("Invitation Code") },
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.joinHouseholdByCode(inviteCode)
                        showJoinDialog = false
                        inviteCode = ""
                    }) {
                        Text("Join")
                    }
                },
                dismissButton = {
                    Button(onClick = { showJoinDialog = false }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}
