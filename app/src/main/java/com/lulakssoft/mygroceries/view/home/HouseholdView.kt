package com.lulakssoft.mygroceries.view.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import com.lulakssoft.mygroceries.database.household.ActivityType
import com.lulakssoft.mygroceries.database.household.HouseholdActivity
import com.lulakssoft.mygroceries.database.household.MemberRole
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
                    .padding(top = 0.dp, bottom = 0.dp, start = 4.dp, end = 4.dp)
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
                                Text("Sign out")
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
                            value = "${viewModel.productCount}",
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

            if (viewModel.activityList.isEmpty()) {
                EmptyActivityList()
            } else {
                ActivityList(viewModel.activityList)
            }
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

@Composable
fun ActivityList(activities: List<HouseholdActivity>) {
    Column {
        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp),
        ) {
            items(activities.size) { index ->
                val activity = activities[index]
                ActivityItem(activity)

                if (index < activities.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 1.dp,
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityItem(activity: HouseholdActivity) {
    val iconRes =
        when (activity.activityType) {
            ActivityType.PRODUCT_ADDED -> Icons.Default.Add
            ActivityType.PRODUCT_REMOVED -> Icons.Default.Delete
            ActivityType.MEMBER_JOINED -> Icons.Default.Person
            ActivityType.MEMBER_LEFT -> Icons.Default.Clear
            else -> Icons.Default.Info
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = iconRes,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .padding(end = 8.dp)
                    .size(24.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = activity.details,
                style = MaterialTheme.typography.bodyMedium,
            )

            Row {
                Text(
                    text = activity.userName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatTimestamp(activity.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Hilfsfunktion für Zeitstempel-Formatierung
fun formatTimestamp(timestamp: LocalDateTime): String {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("dd.MM. HH:mm")

    return when {
        timestamp.toLocalDate() == now.toLocalDate() -> {
            "Heute, ${timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        }
        timestamp.toLocalDate() == now.toLocalDate().minusDays(1) -> {
            "Gestern, ${timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        }
        else -> {
            timestamp.format(formatter)
        }
    }
}
