package com.beg.shuttlespot.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme

enum class BusStatusType { PassedStops, WaitingAtStop, EnRoute, Passed, NoBusOperating, Riding }
@Composable
fun BusStatusCard(
    busStatus : BusStatusType,
    onBoardingIntent: () -> Unit = {},
    onLiveTrip: () -> Unit = {},
    showLiveTrip : Boolean = true,
){
    val (labelText, labelColor, labelStyle) = when (busStatus) {
        BusStatusType.PassedStops -> Triple("Sedang Berjalan", LabelColor.Success, LabelStyle.Soft)
        BusStatusType.WaitingAtStop -> Triple("Sudah Tiba", LabelColor.Success, LabelStyle.Solid)
        BusStatusType.EnRoute -> Triple("Sedang Berjalan", LabelColor.Success, LabelStyle.Soft)
        BusStatusType.Passed -> Triple("Sudah Berangkat", LabelColor.Failed, LabelStyle.Soft)
        BusStatusType.Riding -> Triple("Sedang Naik", LabelColor.Blue, LabelStyle.Soft)
        BusStatusType.NoBusOperating -> Triple("Tidak Beroperasi", LabelColor.Grey, LabelStyle.Soft)
    }

    val (labelPojokKanan, valuePojokKanan) = when (busStatus) {
        BusStatusType.WaitingAtStop -> "Waktu Menunggu" to "5:00"
        BusStatusType.Riding -> "ETA ke Tujuan" to "5 Menit"
        else -> "ETA" to "5 Menit"
    }

    Column(
        modifier = Modifier
            .background(LocalExtendedColors.current.groupingColor)
            .border(border = BorderStroke(1.dp,
                color=(LocalExtendedColors.current.secondary3) ),
                shape= MaterialTheme.shapes.medium)
            .fillMaxWidth()
            .padding(12.dp),
    ){
        if(!showLiveTrip){
            AppButton(
                text = "Konfirmasi naik Bus",
                modifier = Modifier.fillMaxWidth(),
                onClick = onBoardingIntent
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        Column(
            modifier = Modifier
                .background(LocalExtendedColors.current.groupingColor)
                .border(border = BorderStroke(1.dp,
                    color=(LocalExtendedColors.current.secondary3) ),
                    shape= MaterialTheme.shapes.medium)
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ){
                    BusIconWithStatus()

                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ){
                        Text(text="PLAT BUS DI SINI",
                            fontWeight = FontWeight.SemiBold
                        )
                        StatusBadge(
                            text = labelText,
                            color = labelColor,
                            style = labelStyle
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = labelPojokKanan,
                        color = Color.Gray
                    )
                    Text(
                        text = valuePojokKanan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if(busStatus == BusStatusType.PassedStops || busStatus == BusStatusType.EnRoute || busStatus == BusStatusType.WaitingAtStop){
                BusRouteProgress(
                    progress = 0.3f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if(busStatus != BusStatusType.Riding){
                BusStatusRow(
                    type = busStatus
                )
            }

        }

        if (showLiveTrip) {
            ButtonsToShow(busStatus, onBoardingIntent, onLiveTrip)
        }
    }
}

@Composable
fun ButtonsToShow(
    busStatus : BusStatusType,
    onBoardingIntent: () -> Unit = {},
    onLiveTrip: () -> Unit = {},
){

    when(busStatus){
        BusStatusType.Passed -> {
            AppButton(
                text = "Lihat Posisi Bus Realtime",
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary,
                onClick = onLiveTrip
            )
        }
        BusStatusType.NoBusOperating -> {
            // no buttons at all
        }
        BusStatusType.Riding -> {
            AppButton(
                text = "Lihat Posisi Bus Realtime",
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary,
                onClick = onLiveTrip
            )
        }
        else -> {
            Spacer(modifier = Modifier.height(6.dp))
            AppButton(
                text = "Konfirmasi naik Bus",
                modifier = Modifier.fillMaxWidth(),
                onClick = onBoardingIntent
            )
            Spacer(modifier = Modifier.height(6.dp))
            AppButton(
                text = "Lihat Posisi Bus Realtime",
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary,
                onClick = onLiveTrip
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BusStatusCardPreview(){
    ShuttleSpotTheme()
    {
        BusStatusCard(busStatus = BusStatusType.EnRoute)
    }
}