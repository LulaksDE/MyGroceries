package com.lulakssoft.mygroceries.view.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.lulakssoft.mygroceries.R

@Composable
fun HomeView(viewModel: HomeViewModel) {
    Scaffold(
        floatingActionButton = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = "Add Household",
                )
            }
        },
    ) { padding ->

        Column(
            modifier = Modifier.padding(padding),
        ) {
            Text("You've clicked on the home tab!")
        }
    }
}
