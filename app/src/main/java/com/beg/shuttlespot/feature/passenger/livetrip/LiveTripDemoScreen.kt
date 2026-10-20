package com.beg.shuttlespot.feature.passenger.livetrip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.beg.shuttlespot.feature.passenger.boarding.BoardingIntentDialog
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.model.TripStatus
import com.beg.shuttlespot.core.ui.components.BusStatusCard
import com.beg.shuttlespot.core.ui.components.BusStatusType
import com.beg.shuttlespot.core.ui.components.MapMarker
import com.beg.shuttlespot.core.ui.components.MarkerKind
import com.beg.shuttlespot.core.ui.components.OpenStreetMap
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.CircularShape
import kotlinx.coroutines.launch

/**
 * Layar pelacak bus: peta memenuhi seluruh layar, judul rute melayang di atas,
 * dan kartu status bus ada di bottom sheet yang bisa ditarik naik/turun (sampai tersembunyi).
 */
@Composable
fun LiveTripDemoScreen(
    onBack: () -> Unit,
    viewModel: LiveTripViewModel = hiltViewModel(),
) {
    val mapState by viewModel.mapState.collectAsState()
    val trip by viewModel.trip.collectAsState()
    var showBoardingIntent by remember { mutableStateOf(false) }
    LiveTripContent(
        trip = trip,
        onBack = onBack,
        onBoardingIntent = { showBoardingIntent = true },
        mapState = mapState,
        onSelectStop = viewModel::selectStop,
    )
    if (showBoardingIntent) BoardingIntentDialog(onDismiss = { showBoardingIntent = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiveTripContent(
    trip: ActiveTripSnapshot?,
    onBack: () -> Unit,
    onBoardingIntent: () -> Unit,
    mapState: RouteMapUiState,
    onSelectStop: (String?) -> Unit,
) {
    // skipHiddenState = false -> sheet boleh ditarik ke bawah sampai hilang sepenuhnya.
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = false,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val scope = rememberCoroutineScope()
    val location = mapState.busLocation

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 300.dp,
        sheetContainerColor = MaterialTheme.colorScheme.background,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
            ) {
                BusStatusCard(
                    busStatus = trip?.status.toBusStatusType(),
                    onBoardingIntent = onBoardingIntent,
                    showLiveTrip = false
                )
            }
        },
    ) { _ ->
        // Padding dari scaffold sengaja diabaikan: peta menutupi satu layar penuh.
        var focusBusTrigger by remember { mutableIntStateOf(0) }
        val routePath = remember(mapState.path) { mapState.path.map { it.latitude to it.longitude } }
        Box(Modifier.fillMaxSize()) {
            OpenStreetMap(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "Peta posisi bus" },
                latitude = location?.latitude ?: -6.2561,
                longitude = location?.longitude ?: 106.6186,
                markers = mapState.markers,
                routePath = routePath,
                selectedMarkerId = mapState.selectedStopId,
                focusBusTrigger = focusBusTrigger,
                onMarkerClick = onSelectStop,
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FloatingRouteBar(
                    title = mapState.routeName.ifBlank { "Rute" },
                    onBack = onBack,
                )
                if (location != null) {
                    Surface(
                        onClick = { focusBusTrigger++ },
                        modifier = Modifier.align(Alignment.End).size(48.dp),
                        shape = CircularShape,
                        color = LocalExtendedColors.current.groupingColor,
                        shadowElevation = 4.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Outlined.MyLocation,
                                contentDescription = "Pindah ke posisi bus",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            // Sheet sudah ditarik habis ke bawah: beri jalan untuk memunculkannya lagi.
            if (sheetState.currentValue == SheetValue.Hidden) {
                Surface(
                    onClick = { scope.launch { sheetState.partialExpand() } },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = LocalExtendedColors.current.groupingColor,
                    shadowElevation = 4.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                        Text("Lihat status bus", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

/** Kotak melayang di atas peta: tombol kembali di kiri, nama rute di tengah. */
@Composable
private fun FloatingRouteBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = LocalExtendedColors.current.groupingColor,
        shadowElevation = 4.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            }
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 48.dp),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun TripStatus?.toBusStatusType(): BusStatusType = when (this) {
    TripStatus.EN_ROUTE -> BusStatusType.EnRoute
    TripStatus.AT_PICKUP -> BusStatusType.WaitingAtStop
    TripStatus.MISSED -> BusStatusType.Passed
    TripStatus.RIDING -> BusStatusType.Riding
    else -> BusStatusType.NoBusOperating
}
