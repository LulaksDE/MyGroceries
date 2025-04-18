package com.lulakssoft.mygroceries.view.management

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.lulakssoft.mygroceries.database.household.HouseholdMember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdManagementView(viewModel: HouseholdManagementViewModel) {
    val context = LocalContext.current
    val householdId = viewModel.selectedHouseholdId
    val members by viewModel.getHouseholdMembers(householdId).collectAsState(initial = emptyList())

    var showGenerateCodeDialog by remember { mutableStateOf(false) }
    var generatedCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Haushalt verwalten") })
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(16.dp),
        ) {
            Text("Haushalt-Mitglieder", style = MaterialTheme.typography.titleMedium)

            LazyColumn {
                items(members) { member ->
                    MemberItem(member = member) {
                        viewModel.removeMemberFromHousehold(member)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    showGenerateCodeDialog = true
                    viewModel.generateInvitationCode(householdId) { code ->
                        generatedCode = code
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Einladungscode generieren")
            }
        }
    }

    // Code-Dialog
    if (showGenerateCodeDialog) {
        AlertDialog(
            onDismissRequest = { showGenerateCodeDialog = false },
            title = { Text("Einladungscode") },
            text = {
                Column {
                    if (generatedCode.isEmpty()) {
                        CircularProgressIndicator()
                    } else {
                        Text("Code: $generatedCode")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (generatedCode.isNotEmpty()) {
                            // Moderne Methode zum Teilen verwenden
                            val shareIntent =
                                Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Tritt meinem Haushalt in MyGroceries bei! Verwende den Code: $generatedCode",
                                    )
                                    type = "text/plain"
                                }
                            val chooser = Intent.createChooser(shareIntent, "Code teilen via")
                            context.startActivity(chooser)
                        }
                    },
                    enabled = generatedCode.isNotEmpty(),
                ) {
                    Text("Teilen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGenerateCodeDialog = false }) {
                    Text("Schließen")
                }
            },
        )
    }
}

@Composable
fun MemberItem(
    member: HouseholdMember,
    onRemove: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = member.userName, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "Rolle: ${member.role.name}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Mitglied entfernen",
                )
            }
        }
    }
}
