package com.lulakssoft.mygroceries.view.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lulakssoft.mygroceries.R

@Composable
fun HouseholdView(viewModel: HouseholdViewModel) {
    Scaffold(
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            Text("You're on the household tab!")
        }
    }
}
