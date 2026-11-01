package com.beg.shuttlespot.core.ui.components

// Overview: Defines bus status card for the ui components area of ShuttleSpot.

// Dependencies used by this file.
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
import com.beg.shuttlespot.core.model.TripStatus
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme

@Composable
// Renders the bus status card UI section.
fun BusStatusCard(
    busStatus : TripStatus,
    onBoardingIntent: () -> Unit = {},
    onLiveTrip: () -> Unit = {},
    showLiveTrip : Boolean = true,
    boardingConfirmed: Boolean = false,
    /** Fraksi 0f..1f posisi bus pada rute, dipakai [BusRouteProgress]. */
    progress: Float = 0.3f,
    fromStopName: String? = null,
    toStopName: String? = null,
    /** Konteks halte untuk [BusStatusRow]: menuju mana (EN_ROUTE) / menunggu di mana (WAITING_AT_OTHERSTOP). */
    stopName: String? = null,
    /** Estimasi waktu tiba. Dipakai [BusStatusRow] dan display pojok kanan. Null = pakai placeholder "5 menit". */
    eta: String? = null,
){
    val (labelText, labelColor, labelStyle) = when (busStatus) {
        TripStatus.EN_ROUTE -> Triple("Sedang Menuju Haltemu", LabelColor.Success, LabelStyle.Soft)
        TripStatus.WAITING_AT_OTHERSTOP -> Triple("Sedang Menunggu", LabelColor.Pending, LabelStyle.Soft)
        TripStatus.AT_PICKUP -> Triple("Sudah Tiba", LabelColor.Success, LabelStyle.Solid)
        TripStatus.MISSED -> Triple("Sudah Berangkat", LabelColor.Failed, LabelStyle.Soft)
        TripStatus.RIDING -> Triple("Sedang Naik", LabelColor.Blue, LabelStyle.Soft)
        TripStatus.COMPLETED -> Triple("Selesai", LabelColor.Success, LabelStyle.Soft)
        TripStatus.NO_ACTIVE_BUS, TripStatus.NOT_STARTED -> Triple("Tidak Beroperasi", LabelColor.Grey, LabelStyle.Soft)
    }

    val etaDisplay = eta ?: "5 Menit"
    val (labelPojokKanan, valuePojokKanan) = when (busStatus) {
        TripStatus.AT_PICKUP -> "Waktu Menunggu" to "5:00"
        TripStatus.RIDING -> "ETA ke Tujuan" to etaDisplay
        else -> "ETA" to etaDisplay
    }

    Column(
        modifier = Modifier
            .background(
                color = LocalExtendedColors.current.groupingColor,
                shape = MaterialTheme.shapes.medium)
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
                enabled = !boardingConfirmed,
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
                        Text(text="PLAT BUS DI SINI", style = MaterialTheme.typography.titleSmall)
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = valuePojokKanan,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            if(busStatus == TripStatus.EN_ROUTE || busStatus == TripStatus.AT_PICKUP || busStatus == TripStatus.WAITING_AT_OTHERSTOP){
                BusRouteProgress(
                    progress = progress,
                    fromStopName = fromStopName ?: "Halte Sebelum Sekarang",
                    toStopName = toStopName ?: "Halte Setelah",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if(busStatus != TripStatus.RIDING){
                BusStatusRow(
                    type = busStatus,
                    stopName = stopName,
                    eta = eta,
                )
            }

        }

        if (showLiveTrip) {
            ButtonsToShow(busStatus, onBoardingIntent, onLiveTrip, boardingConfirmed)
        }
    }
}

@Composable
// Renders the buttons to show UI section.
fun ButtonsToShow(
    busStatus : TripStatus,
    onBoardingIntent: () -> Unit = {},
    onLiveTrip: () -> Unit = {},
    boardingConfirmed: Boolean = false,
){

    when(busStatus){
        TripStatus.MISSED -> {
            AppButton(
                text = "Lihat Posisi Bus Realtime",
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary,
                onClick = onLiveTrip
            )
        }
        TripStatus.NO_ACTIVE_BUS, TripStatus.NOT_STARTED, TripStatus.COMPLETED -> {
            // no buttons at all
        }
        TripStatus.RIDING -> {
            AppButton(
                text = "Lihat Posisi Bus Realtime",
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary,
                onClick = onLiveTrip
            )
        }
        else -> { // EN_ROUTE, AT_PICKUP, WAITING_AT_OTHERSTOP
            Spacer(modifier = Modifier.height(6.dp))
            AppButton(
                text = "Konfirmasi naik Bus",
                modifier = Modifier.fillMaxWidth(),
                enabled = !boardingConfirmed,
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
// Renders the bus status card preview UI section.
fun BusStatusCardPreview(){
    ShuttleSpotTheme()
    {
        BusStatusCard(busStatus = TripStatus.EN_ROUTE)
    }
}
