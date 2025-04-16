package com.lulakssoft.mygroceries.view.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lulakssoft.mygroceries.R
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.database.product.Household
import com.lulakssoft.mygroceries.dataservice.FirestoreManager
import com.lulakssoft.mygroceries.view.account.GoogleAuthUiClient
import com.lulakssoft.mygroceries.view.main.MainViewModel
import com.lulakssoft.mygroceries.view.management.HouseholdManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdSelectionScreen(
    onHouseholdSelected: (Household) -> Unit,
    viewModel: MainViewModel,
    authClient: GoogleAuthUiClient,
    databaseApp: DatabaseApp,
) {
    val householdRepository =
        remember {
            HouseholdRepository(
                viewModel.productRepository.householdDao,
                databaseApp.householdMemberDao,
                databaseApp.householdInvitationDao,
                FirestoreManager(),
            )
        }

    val householdManagementViewModel = viewModel { HouseholdManagementViewModel(householdRepository) }
    val households by viewModel.households.collectAsState(initial = emptyList())

    var showCreateHouseholdDialog by remember { mutableStateOf(false) }
    var showJoinHouseholdDialog by remember { mutableStateOf(false) }
    var newHouseholdName by remember { mutableStateOf("") }
    var invitationCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Household") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Your Households",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            if (households.isEmpty()) {
                EmptyHouseholdsList()
            } else {
                HouseholdsList(
                    households = households,
                    onHouseholdSelected = onHouseholdSelected,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    onClick = { showJoinHouseholdDialog = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join Household")
                }

                Button(
                    onClick = { showCreateHouseholdDialog = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Household")
                }
            }
        }
    }

    // Create Household Dialog
    if (showCreateHouseholdDialog) {
        AlertDialog(
            onDismissRequest = { showCreateHouseholdDialog = false },
            title = { Text("Create New Household") },
            text = {
                TextField(
                    value = newHouseholdName,
                    onValueChange = { newHouseholdName = it },
                    label = { Text("Household Name") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.householdText = newHouseholdName
                        viewModel.insert()
                        showCreateHouseholdDialog = false
                        newHouseholdName = ""
                    },
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateHouseholdDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Join Household Dialog
    if (showJoinHouseholdDialog) {
        AlertDialog(
            onDismissRequest = { showJoinHouseholdDialog = false },
            title = { Text("Join Household") },
            text = {
                TextField(
                    value = invitationCode,
                    onValueChange = { invitationCode = it },
                    label = { Text("Invitation Code") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        householdManagementViewModel.joinHouseholdByCode(invitationCode)
                        showJoinHouseholdDialog = false
                        invitationCode = ""
                    },
                ) {
                    Text("Join")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinHouseholdDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun EmptyHouseholdsList() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No households yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
            Text(
                "Create or join a household to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun HouseholdsList(
    households: List<Household>,
    onHouseholdSelected: (Household) -> Unit,
) {
    LazyColumn {
        items(households) { household ->
            HouseholdItem(
                household = household,
                onClick = { onHouseholdSelected(household) },
            )
        }
    }
}

@Composable
private fun HouseholdItem(
    household: Household,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = household.householdName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Created: ${household.createdAt.toLocalDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
