package com.lulakssoft.mygroceries.view.main

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lulakssoft.mygroceries.R

enum class BottomBarNavigation {
    Create,
    Home,
    View,
}

@Composable
fun BottomBar(
    selectedIcon: BottomBarNavigation,
    onIconSelected: (BottomBarNavigation) -> Unit,
    navController: NavController,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        BottomBarIconItem(
            icon = BottomBarNavigation.Create,
            selectedIcon = selectedIcon,
            onClick = {
                onIconSelected(BottomBarNavigation.Create)
                navController.navigate("createView")
            },
            context = LocalContext.current,
            iconRes = R.drawable.outline_add_home_24,
            filledIconRes = R.drawable.baseline_add_home_24,
        )
        BottomBarIconItem(
            icon = BottomBarNavigation.Home,
            selectedIcon = selectedIcon,
            onClick = {
                onIconSelected(BottomBarNavigation.Home)
                navController.navigate("HomeView")
            },
            context = LocalContext.current,
            iconRes = R.drawable.outline_home_24,
            filledIconRes = R.drawable.baseline_home_24,
        )
        BottomBarIconItem(
            icon = BottomBarNavigation.View,
            selectedIcon = selectedIcon,
            onClick = {
                onIconSelected(BottomBarNavigation.View)
                navController.navigate("viewView")
            },
            context = LocalContext.current,
            iconRes = R.drawable.outline_remove_red_eye_24,
            filledIconRes = R.drawable.baseline_remove_red_eye_24,
        )
    }
}

@Composable
fun BottomBarIconItem(
    icon: BottomBarNavigation,
    selectedIcon: BottomBarNavigation,
    onClick: () -> Unit,
    context: Context,
    iconRes: Int,
    filledIconRes: Int,
) {
    val iconToDisplay = if (icon == selectedIcon) filledIconRes else iconRes

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier.clickable(
                onClick = onClick,
                indication = null, // Remove the indication
                interactionSource = remember { MutableInteractionSource() },
            ),
    ) {
        Icon(
            painter = painterResource(id = iconToDisplay),
            contentDescription = icon.name,
        )
        Text(icon.name)
        if (selectedIcon == icon) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary,
                thickness = 2.dp,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(24.dp), // Adjust the width to match the icon size
            )
        }
    }
}
