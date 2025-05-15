package com.lulakssoft.mygroceries.view.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.view.account.GoogleAuthUiClient
import com.lulakssoft.mygroceries.view.main.MainViewModel
import com.lulakssoft.mygroceries.view.management.HouseholdManagementViewModel
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdSelectionScreen(
    onHouseholdSelected: (Household) -> Unit,
    viewModel: MainViewModel,
    authClient: GoogleAuthUiClient,
    databaseApp: DatabaseApp,
    context: Context,
) {
    val householdRepository =
        remember {
            HouseholdRepository(
                databaseApp.householdDao,
                databaseApp.householdMemberDao,
                databaseApp.householdInvitationDao,
            )
        }

    val householdManagementViewModel =
        viewModel { HouseholdManagementViewModel(householdRepository) }

    val households by viewModel.households.collectAsState(initial = emptyList())
    val refreshing = viewModel.isSyncing
    val pullRefreshState = rememberPullToRefreshState()
    val onRefresh = { viewModel.syncHouseholds(context) }

    var showCreateHouseholdDialog by remember { mutableStateOf(false) }
    var showJoinHouseholdDialog by remember { mutableStateOf(false) }
    var newHouseholdName by remember { mutableStateOf("") }
    var invitationCode by remember { mutableStateOf("") }

    val animationState =
        remember {
            MutableTransitionState(false).apply { targetState = true }
        }

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Household") },
                modifier = Modifier.fillMaxWidth().clip(shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sign Out") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Sign Out",
                                )
                            },
                            onClick = {
                                viewModel.signOut(authClient)
                                showMenu = false
                            },
                        )
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(top = 0.dp, bottom = 0.dp, start = 4.dp, end = 4.dp),
            onRefresh = onRefresh,
            state = pullRefreshState,
            isRefreshing = refreshing,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (households.isEmpty()) {
                    EmptyHouseholdsList()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding =
                            PaddingValues(
                                top = 8.dp,
                                bottom = 100.dp,
                                start = 8.dp,
                                end = 8.dp,
                            ),
                    ) {
                        itemsIndexed(households) { index, household ->
                            AnimatedVisibility(
                                visibleState = animationState,
                                enter =
                                    fadeIn(
                                        animationSpec =
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow,
                                            ),
                                    ) +
                                        slideInVertically(
                                            initialOffsetY = { it * (index + 1) / 5 },
                                            animationSpec =
                                                spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow,
                                                ),
                                        ),
                            ) {
                                HouseholdItem(
                                    household = household,
                                    onClick = onHouseholdSelected,
                                )
                            }
                        }
                    }
                }

                ActionButtons(
                    onCreateClick = { showCreateHouseholdDialog = true },
                    onJoinClick = { showJoinHouseholdDialog = true },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }

        if (showCreateHouseholdDialog) {
            CreateHouseholdDialog(
                householdName = newHouseholdName,
                onNameChange = { newHouseholdName = it },
                onConfirm = {
                    viewModel.householdText = newHouseholdName
                    viewModel.insert()
                    showCreateHouseholdDialog = false
                    newHouseholdName = ""
                },
                onDismiss = {
                    showCreateHouseholdDialog = false
                    newHouseholdName = ""
                },
            )
        }

        if (showJoinHouseholdDialog) {
            JoinHouseholdDialog(
                invitationCode = invitationCode,
                onCodeChange = { invitationCode = it },
                onConfirm = {
                    householdManagementViewModel.joinHouseholdByCode(invitationCode)
                    showJoinHouseholdDialog = false
                    invitationCode = ""
                },
                onDismiss = {
                    showJoinHouseholdDialog = false
                    invitationCode = ""
                },
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(
            onClick = onJoinClick,
            modifier = Modifier.weight(1f),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Join Household",
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Join Household")
        }

        Button(
            onClick = onCreateClick,
            modifier = Modifier.weight(1f),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Household",
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Household")
        }
    }
}

@Composable
private fun CreateHouseholdDialog(
    householdName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create New Household",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
            ) {
                Text(
                    "Enter a name for your new household.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = householdName,
                    onValueChange = onNameChange,
                    label = { Text("Household Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = householdName.isNotBlank(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
    )
}

@Composable
private fun JoinHouseholdDialog(
    invitationCode: String,
    onCodeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Join Household",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
            ) {
                Text(
                    "Enter the invitation code you received.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = invitationCode,
                    onValueChange = onCodeChange,
                    label = { Text("Invitation Code") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = invitationCode.isNotBlank(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
            ) {
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptyHouseholdsList() {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        item {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Households Found",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create or join a household to get started.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HouseholdItem(
    household: Household,
    onClick: (Household) -> Unit,
) {
    val seed = household.householdName.hashCode().absoluteValue
    val random = Random(seed)
    val startColor =
        Color(
            red = random.nextFloat() * 0.5f + 0.3f,
            green = random.nextFloat() * 0.5f + 0.3f,
            blue = random.nextFloat() * 0.5f + 0.3f,
            alpha = 1.0f,
        )
    val endColor =
        Color(
            red = random.nextFloat() * 0.3f + 0.1f,
            green = random.nextFloat() * 0.3f + 0.1f,
            blue = random.nextFloat() * 0.3f + 0.1f,
            alpha = 1.0f,
        )

    val gradientBrush =
        Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = Offset(0f, 0f),
            end = Offset(1000f, 1000f),
        )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp, top = 0.dp, start = 8.dp, end = 8.dp)
                .height(110.dp)
                .clickable(enabled = true) { onClick(household) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(gradientBrush),
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                            .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = household.householdName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Created: ${household.createdAt.toLocalDate()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to select",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}
