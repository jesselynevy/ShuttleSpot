package com.beg.shuttlespot.feature.passenger.livetrip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.components.OpenStreetMap

@Composable
fun LiveTripDemoScreen(
    onBoardingIntentClick: () -> Unit
){
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        Text(
            text = "Perjalanan Langsung",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Bus BSD menuju UMN",
            style = MaterialTheme.typography.titleMedium
        )

        OpenStreetMap(
            modifier = Modifier.fillMaxWidth().height(280.dp)
        )

        Text(
            text = "Bus sedang menuju Halte UMN",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Estimasi tiba: 5 menit\nData terakhir diperbarui: baru saja"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBoardingIntentClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Saya ingin naik")
        }
    }
}