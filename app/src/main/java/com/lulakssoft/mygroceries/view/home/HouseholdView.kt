package com.lulakssoft.mygroceries.view.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lulakssoft.mygroceries.database.household.MemberRole
import java.time.LocalDate

@Composable
fun HouseholdView(
    viewModel: HouseholdViewModel,
    navigateToManagement: () -> Unit,
) {
    val userData = viewModel.userData
    val currentMember = viewModel.currentMemberRole

    Scaffold(
        floatingActionButton = {
            if (currentMember?.role == MemberRole.OWNER || currentMember?.role == MemberRole.ADMIN) {
                FloatingActionButton(onClick = navigateToManagement) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Haushalt verwalten",
                    )
                }
            } else {
                Text("(You have the role ${currentMember?.role})")
            }
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
            // User profile section
            userData?.let { user ->
                // In HouseholdView.kt - im userData-Card-Bereich erweitern
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = user.username ?: "Gast",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = "Mitglied seit ${LocalDate.now().year}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Button(
                                onClick = { viewModel.signOut() },
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                    ),
                            ) {
                                Text("Abmelden")
                            }
                        }
                    }
                }
            }

            // Household statistics
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    Text(
                        "Household Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatisticItem(
                            title = "Products",
                            value = "0",
                            icon = Icons.Default.ShoppingCart,
                            modifier = Modifier.weight(1f),
                        )
                        StatisticItem(
                            title = "Members",
                            value = "${viewModel.memberCount}",
                            icon = Icons.Default.Person,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Recent activity
            Text(
                "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )

            // This would be a list of recent activities
            // For now just a placeholder
            EmptyActivityList()
        }
    }
}

@Composable
private fun StatisticItem(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun EmptyActivityList() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "No recent activity",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}
