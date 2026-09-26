package com.beg.shuttlespot.core.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.components.AppButton
import com.beg.shuttlespot.core.ui.components.BusStatusCard
import com.beg.shuttlespot.core.ui.components.BusStatusType
import com.beg.shuttlespot.core.ui.components.ButtonVariant
import com.beg.shuttlespot.core.ui.components.LabelColor
import com.beg.shuttlespot.core.ui.components.StatusBadge

@Composable
fun HomeTemp(
    title: String,
    description: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        BusStatusCard(busStatus = BusStatusType.Passed)

        if(buttonText != null && onButtonClick != null){
            AppButton(
                text = buttonText,
                onClick = onButtonClick,
                variant = ButtonVariant.Primary
            )
        }

    }
}