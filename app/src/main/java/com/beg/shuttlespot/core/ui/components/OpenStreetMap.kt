package com.beg.shuttlespot.core.ui.components

import android.graphics.Color as AndroidColor
import android.graphics.RectF
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.awaitCancellation
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import kotlin.math.roundToInt

private const val OPEN_STREET_MAP_STYLE = """
{
  "version": 8,
  "sources": {
    "openstreetmap": {
      "type": "raster",
      "tiles": [
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      ],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [
    {
      "id": "openstreetmap-layer",
      "type": "raster",
      "source": "openstreetmap"
    }
  ]
}
"""

private const val STOPS_LAYER_ID = "stops-layer"
private const val BUS_SOURCE_ID = "bus-src"
private const val BUS_ICON_ID = "bus-icon"

/**
 * Ikon marker bus: lingkaran [color] dengan cincin putih dan halo tipis, berisi gambar bus tampak depan.
 * Digambar manual (tanpa file gambar) supaya warnanya selalu mengikuti tema.
 */
private fun createBusMarkerBitmap(
    density: Density,
    size: Dp,
    color: Color,
    onColor: Color,
): android.graphics.Bitmap {
    val sizePx = with(density) { size.roundToPx() }
    val image = ImageBitmap(sizePx, sizePx)
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(image), Size(sizePx.toFloat(), sizePx.toFloat())) {
        val r = this.size.minDimension / 2f
        val c = center
        drawCircle(color.copy(alpha = 0.22f), radius = r, center = c)   // halo
        drawCircle(onColor, radius = r * 0.80f, center = c)            // cincin kontras
        drawCircle(color, radius = r * 0.68f, center = c)               // isi warna primary

        // Bus tampak depan: badan putih, kaca, dua lampu, dua roda.
        drawRoundRect(
            onColor,
            topLeft = Offset(c.x - r * 0.36f, c.y - r * 0.40f),
            size = Size(r * 0.72f, r * 0.70f),
            cornerRadius = CornerRadius(r * 0.12f),
        )
        drawRoundRect(
            color,
            topLeft = Offset(c.x - r * 0.28f, c.y - r * 0.31f),
            size = Size(r * 0.56f, r * 0.26f),
            cornerRadius = CornerRadius(r * 0.05f),
        )
        drawCircle(color, radius = r * 0.055f, center = Offset(c.x - r * 0.17f, c.y + 0.12f * r))
        drawCircle(color, radius = r * 0.055f, center = Offset(c.x + r * 0.17f, c.y + 0.12f * r))
        drawRoundRect(
            onColor,
            topLeft = Offset(c.x - r * 0.27f, c.y + r * 0.27f),
            size = Size(r * 0.14f, r * 0.13f),
            cornerRadius = CornerRadius(r * 0.03f),
        )
        drawRoundRect(
            onColor,
            topLeft = Offset(c.x + r * 0.13f, c.y + r * 0.27f),
            size = Size(r * 0.14f, r * 0.13f),
            cornerRadius = CornerRadius(r * 0.03f),
        )
    }
    return image.asAndroidBitmap()
}

private fun MapMarker.toFeature(): Feature =
    Feature.fromGeometry(Point.fromLngLat(longitude, latitude)).apply {
        addStringProperty("id", id)
        addStringProperty("kind", kind.name)
    }

/**
 * Peta OSM. [markers] berisi halte (urut rute) dan, opsional, satu marker bus.
 * Klik halte memanggil [onMarkerClick] dengan id-nya; klik area kosong memanggil dengan null.
 * Pop up ditampilkan untuk [selectedMarkerId].
 */
@Composable
fun OpenStreetMap(
    modifier: Modifier = Modifier,
    latitude: Double = -6.2561,
    longitude: Double = 106.6186,
    zoom: Double = 15.5,
    markers: List<MapMarker> = emptyList(),
    /** Jalur jalan (lat to lng); kosong -> garis lurus antar halte. */
    routePath: List<Pair<Double, Double>> = emptyList(),
    selectedMarkerId: String? = null,
    /** Naikkan nilainya (mis. +1 tiap klik tombol) untuk memindahkan kamera ke bus. */
    focusBusTrigger: Int = 0,
    onMarkerClick: (String?) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)

    val density = LocalDensity.current
    val primary = MaterialTheme.colorScheme.primary
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val busIcon = remember(density, primary, onPrimaryContainer) {
        createBusMarkerBitmap(density, 44.dp, primary, onPrimaryContainer)
    }

    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var anchor by remember { mutableStateOf<Offset?>(null) } // posisi layar (px) marker terpilih

    val stopMarkers = remember(markers) { markers.filter { it.kind != MarkerKind.Bus } }
    val bus = remember(markers) { markers.firstOrNull { it.kind == MarkerKind.Bus } }
    val selected = stopMarkers.firstOrNull { it.id == selectedMarkerId }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            mapView.onStart()
        }
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onResume()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    // Pasang sekali: ambil objek peta dan listener klik.
    LaunchedEffect(mapView) {
        mapView.getMapAsync { m ->
            map = m
            m.addOnMapClickListener { latLng ->
                val p = m.projection.toScreenLocation(latLng)
                val hit = m.queryRenderedFeatures(RectF(p.x - 28f, p.y - 28f, p.x + 28f, p.y + 28f), STOPS_LAYER_ID)
                    .firstOrNull()?.getStringProperty("id")
                currentOnMarkerClick(hit)
                hit != null
            }
        }
    }

    // Pindahkan kamera ke bus setiap kali focusBusTrigger naik (0 = belum pernah diminta).
    LaunchedEffect(focusBusTrigger) {
        if (focusBusTrigger == 0) return@LaunchedEffect
        val m = map ?: return@LaunchedEffect
        val b = bus ?: return@LaunchedEffect
        m.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(b.latitude, b.longitude), maxOf(m.cameraPosition.zoom, 16.0)))
    }

    // Gambar ulang garis rute + halte hanya ketika daftar halte berubah (bukan saat bus bergerak).
    // Harus terpisah dari efek di atas: efek itu jalan sekali, sedangkan data halte baru datang belakangan dari ViewModel.
    LaunchedEffect(map, stopMarkers, routePath, busIcon) {
        val m = map ?: return@LaunchedEffect
        m.setStyle(Style.Builder().fromJson(OPEN_STREET_MAP_STYLE)) { style ->
            val linePoints = if (routePath.size >= 2) routePath.map { (lat, lng) -> Point.fromLngLat(lng, lat) }
            else stopMarkers.map { Point.fromLngLat(it.longitude, it.latitude) }
            if (linePoints.size >= 2) {
                val line = LineString.fromLngLats(linePoints)
                style.addSource(GeoJsonSource("route-src", Feature.fromGeometry(line)))
                style.addLayer(
                    LineLayer("route-layer", "route-src").withProperties(
                        PropertyFactory.lineColor(AndroidColor.parseColor("#2F80ED")),
                        PropertyFactory.lineWidth(4f),
                    )
                )
            }

            style.addSource(GeoJsonSource("stops-src", FeatureCollection.fromFeatures(stopMarkers.map { it.toFeature() })))
            style.addLayer(
                CircleLayer(STOPS_LAYER_ID, "stops-src").withProperties(
                    PropertyFactory.circleRadius(7f),
                    PropertyFactory.circleColor(
                        Expression.match(
                            Expression.get("kind"),
                            Expression.color(AndroidColor.parseColor("#2F80ED")), // default: halte biasa
                            Expression.stop("Pickup", Expression.color(AndroidColor.parseColor("#27AE60"))),
                            Expression.stop("Destination", Expression.color(AndroidColor.parseColor("#EB5757"))),
                        )
                    ),
                    PropertyFactory.circleStrokeColor(AndroidColor.WHITE),
                    PropertyFactory.circleStrokeWidth(2f),
                )
            )

            // Bus: sumber sendiri supaya posisinya bisa diperbarui tanpa memuat ulang peta.
            style.addSource(
                GeoJsonSource(
                    BUS_SOURCE_ID,
                    FeatureCollection.fromFeatures(listOfNotNull(bus?.toFeature())),
                )
            )
            style.addImage(BUS_ICON_ID, busIcon)
            style.addLayer(
                SymbolLayer("bus-layer", BUS_SOURCE_ID).withProperties(
                    PropertyFactory.iconImage(BUS_ICON_ID),
                    PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconIgnorePlacement(true),
                )
            )

            // Kamera: muat semua halte; kosong -> titik default; satu halte -> pusatkan saja.
            when {
                stopMarkers.size >= 2 -> {
                    val bounds = LatLngBounds.Builder()
                        .includes(stopMarkers.map { LatLng(it.latitude, it.longitude) })
                        .build()
                    m.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
                }
                stopMarkers.size == 1 -> m.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(stopMarkers[0].latitude, stopMarkers[0].longitude)).zoom(zoom).build()
                else -> m.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(latitude, longitude)).zoom(zoom).build()
            }
        }
    }

    LaunchedEffect(map, bus) {
        val source = map?.style?.getSourceAs<GeoJsonSource>(BUS_SOURCE_ID) ?: return@LaunchedEffect
        source.setGeoJson(FeatureCollection.fromFeatures(listOfNotNull(bus?.toFeature())))
    }

    // Lacak posisi layar marker terpilih supaya pop up ikut bergeser saat peta digeser/di-zoom.
    LaunchedEffect(map, selected) {
        val m = map
        if (m == null || selected == null) {
            anchor = null
            return@LaunchedEffect
        }
        fun refresh() {
            val p = m.projection.toScreenLocation(LatLng(selected.latitude, selected.longitude))
            anchor = Offset(p.x, p.y)
        }
        refresh()
        val listener = MapLibreMap.OnCameraMoveListener { refresh() }
        m.addOnCameraMoveListener(listener)
        try {
            awaitCancellation()
        } finally {
            m.removeOnCameraMoveListener(listener)
        }
    }

    Box(modifier = modifier) {
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { mapView })

        val a = anchor
        if (selected != null && a != null) {
            var calloutSize by remember { mutableStateOf(IntSize.Zero) }
            val gap = with(LocalDensity.current) { 12.dp.roundToPx() }
            MarkerCallout(
                title = selected.title,
                schedule = selected.schedule,
                modifier = Modifier
                    .offset { IntOffset((a.x + gap).roundToInt(), (a.y - calloutSize.height / 2f).roundToInt()) }
                    .onSizeChanged { calloutSize = it },
            )
        }
    }
}
