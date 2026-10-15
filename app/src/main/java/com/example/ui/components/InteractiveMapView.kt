package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.BuildConfig
import com.example.model.RouteWaypoint
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.util.MapLibreMarkerHelper
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val CARTO_VOYAGER_STYLE_JSON = """{
  "version": 8,
  "name": "CartoVoyager",
  "sources": {
    "carto-voyager": {
      "type": "raster",
      "tiles": [
        "https://a.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}@2x.png",
        "https://b.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}@2x.png",
        "https://c.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}@2x.png"
      ],
      "tileSize": 256,
      "attribution": "CARTO / OpenStreetMap"
    }
  },
  "layers": [
    {
      "id": "carto-voyager-layer",
      "type": "raster",
      "source": "carto-voyager",
      "minzoom": 0,
      "maxzoom": 20
    }
  ]
}"""

private const val ESRI_SATELLITE_STYLE_JSON = """{
  "version": 8,
  "name": "EsriSatellite",
  "sources": {
    "esri-satellite": {
      "type": "raster",
      "tiles": [
        "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
      ],
      "tileSize": 256,
      "attribution": "Esri World Imagery"
    }
  },
  "layers": [
    {
      "id": "esri-satellite-layer",
      "type": "raster",
      "source": "esri-satellite",
      "minzoom": 0,
      "maxzoom": 19
    }
  ]
}"""

enum class MapLayerMode(val label: String) {
    STREETS("Jalan"),
    SATELLITE("Satelit"),
    SCHEMATIC("Radar Skematik")
}

@Composable
fun InteractiveMapView(
    modifier: Modifier = Modifier,
    waypoints: List<RouteWaypoint>,
    vehiclePlate: String = "B 1024 SR",
    isTrackingActive: Boolean = true,
    speedKmh: Float = 42f,
    geofenceAlert: Boolean = false,
    onMapClick: () -> Unit = {}
) {
    var selectedLayer by remember { mutableStateOf(MapLayerMode.STREETS) }
    var maplibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }

    // MapTiler API Key from BuildConfig with fallback
    val maptilerKey = remember {
        val key = try {
            BuildConfig.MAPTILER_API_KEY
        } catch (_: Exception) {
            ""
        }
        if (key.isNotBlank() && key != "your_maptiler_api_key_here") key else ""
    }

    // Default vehicle lat/lng
    val vehicleLatLng = remember(waypoints) {
        if (waypoints.size >= 2) {
            val p0 = waypoints[0]
            val p1 = waypoints[1]
            LatLng(p0.lat + (p1.lat - p0.lat) * 0.45, p0.lng + (p1.lng - p0.lng) * 0.45)
        } else if (waypoints.isNotEmpty()) {
            LatLng(waypoints[0].lat, waypoints[0].lng)
        } else {
            LatLng(-6.2088, 106.8456) // Default Jakarta Timur Base
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE8EEF5))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(20.dp))
            .testTag("interactive_map_canvas")
    ) {
        if (selectedLayer == MapLayerMode.SCHEMATIC) {
            SchematicRadarMapView(
                waypoints = waypoints,
                isTrackingActive = isTrackingActive,
                geofenceAlert = geofenceAlert
            )
        } else {
            // Live MapLibre Native Map with High-Resolution Open/Carto/Satellite Tiles
            MapLibreNativeContainer(
                selectedLayer = selectedLayer,
                maptilerKey = maptilerKey,
                waypoints = waypoints,
                vehicleLatLng = vehicleLatLng,
                vehiclePlate = vehiclePlate,
                isTrackingActive = isTrackingActive,
                speedKmh = speedKmh,
                geofenceAlert = geofenceAlert,
                onMapReady = { map ->
                    maplibreMapInstance = map
                }
            )
        }

        // Top Status Header Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (geofenceAlert) CrimsonRed else EmeraldGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTrackingActive) "GPS LIVE: $vehiclePlate" else "ARMADA STANDBY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = NavyPrimary
                    )
                    if (isTrackingActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${speedKmh.toInt()} km/j",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = EmeraldGreen
                        )
                    }
                }
            }

            // Layer Selector Pill
            Surface(
                color = NavyPrimary,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.clickable {
                    selectedLayer = when (selectedLayer) {
                        MapLayerMode.STREETS -> MapLayerMode.SATELLITE
                        MapLayerMode.SATELLITE -> MapLayerMode.SCHEMATIC
                        MapLayerMode.SCHEMATIC -> MapLayerMode.STREETS
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = "Ganti Tipe Peta",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedLayer.label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Map Control Floating Buttons (Zoom In, Zoom Out, Focus Armada)
        if (selectedLayer != MapLayerMode.SCHEMATIC) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Focus Armada Button
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            maplibreMapInstance?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(vehicleLatLng, 15.0),
                                800
                            )
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Pusatkan ke Armada",
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Zoom In Button
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            maplibreMapInstance?.animateCamera(
                                CameraUpdateFactory.zoomIn(),
                                400
                            )
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Perbesar Peta",
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Zoom Out Button
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            maplibreMapInstance?.animateCamera(
                                CameraUpdateFactory.zoomOut(),
                                400
                            )
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Perkecil Peta",
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Geofencing Warning Banner if alert
        if (geofenceAlert) {
            Surface(
                color = CrimsonRed,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Peringatan",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Geofence Alert: Mobil keluar jalur aman (>1.2 km dari rute).",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MapLibreNativeContainer(
    selectedLayer: MapLayerMode,
    maptilerKey: String,
    waypoints: List<RouteWaypoint>,
    vehicleLatLng: LatLng,
    vehiclePlate: String,
    isTrackingActive: Boolean,
    speedKmh: Float,
    geofenceAlert: Boolean,
    onMapReady: (MapLibreMap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        mapView.onCreate(null)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Refresh markers & polyline when style, layer, or waypoints update
    LaunchedEffect(selectedLayer, maptilerKey, waypoints, geofenceAlert, isTrackingActive) {
        mapView.getMapAsync { maplibreMap ->
            onMapReady(maplibreMap)

            val styleBuilder = when (selectedLayer) {
                MapLayerMode.STREETS -> {
                    if (maptilerKey.isNotBlank()) {
                        Style.Builder().fromUri("https://api.maptiler.com/maps/streets-v2/style.json?key=$maptilerKey")
                    } else {
                        Style.Builder().fromJson(CARTO_VOYAGER_STYLE_JSON)
                    }
                }
                MapLayerMode.SATELLITE -> {
                    if (maptilerKey.isNotBlank()) {
                        Style.Builder().fromUri("https://api.maptiler.com/maps/satellite/style.json?key=$maptilerKey")
                    } else {
                        Style.Builder().fromJson(ESRI_SATELLITE_STYLE_JSON)
                    }
                }
                MapLayerMode.SCHEMATIC -> Style.Builder().fromJson(CARTO_VOYAGER_STYLE_JSON)
            }

            maplibreMap.setStyle(styleBuilder) {
                // Clear old annotations
                maplibreMap.clear()

                // Add Waypoint Markers with custom styled pins
                if (waypoints.isNotEmpty()) {
                    waypoints.forEachIndexed { index, wp ->
                        val isOrigin = index == 0
                        val isDestination = index == waypoints.size - 1
                        val customWpIcon = MapLibreMarkerHelper.createWaypointMarkerIcon(
                            context = context,
                            isOrigin = isOrigin,
                            isDestination = isDestination,
                            labelNumber = index
                        )
                        val markerTitle = if (isOrigin) {
                            "🚩 Titik Awal: ${wp.label}"
                        } else if (isDestination) {
                            "🏁 Tujuan Akhir: ${wp.label}"
                        } else {
                            "📍 Titik ${index + 1}: ${wp.label}"
                        }
                        maplibreMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(wp.lat, wp.lng))
                                .icon(customWpIcon)
                                .title(markerTitle)
                                .snippet(wp.address)
                        )
                    }
                } else {
                    // Default school base marker when no waypoint is assigned yet
                    val baseIcon = MapLibreMarkerHelper.createWaypointMarkerIcon(
                        context = context,
                        isOrigin = true,
                        isDestination = false,
                        labelNumber = 0
                    )
                    maplibreMap.addMarker(
                        MarkerOptions()
                            .position(vehicleLatLng)
                            .icon(baseIcon)
                            .title("🏫 Pangkalan Sekolah Rakyat")
                            .snippet("Armada Standby: $vehiclePlate")
                    )
                }

                // Add Polyline Route
                if (waypoints.size >= 2) {
                    val linePoints = waypoints.map { LatLng(it.lat, it.lng) }
                    val routeColor = if (geofenceAlert) {
                        android.graphics.Color.parseColor("#DC2626")
                    } else {
                        android.graphics.Color.parseColor("#059669")
                    }
                    maplibreMap.addPolyline(
                        PolylineOptions()
                            .addAll(linePoints)
                            .color(routeColor)
                            .width(5f)
                    )
                }

                // Add Custom Vehicle Marker representing current location of the school vehicle
                MapLibreMarkerHelper.addVehicleMarker(
                    maplibreMap = maplibreMap,
                    context = context,
                    position = vehicleLatLng,
                    vehiclePlate = vehiclePlate,
                    speedKmh = speedKmh,
                    isTrackingActive = isTrackingActive,
                    geofenceAlert = geofenceAlert
                )

                // Initial Camera positioning
                val cameraPosition = CameraPosition.Builder()
                    .target(vehicleLatLng)
                    .zoom(if (waypoints.size >= 2) 13.8 else 14.5)
                    .tilt(15.0)
                    .build()
                maplibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000)
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun SchematicRadarMapView(
    waypoints: List<RouteWaypoint>,
    isTrackingActive: Boolean,
    geofenceAlert: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val carProgressAnim by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "car_movement"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Draw Map Grid / Street Blocks
        val gridColor = Color(0xFFD6E0EC)
        val roadColor = Color(0xFFFFFFFF)
        val mainRoadColor = Color(0xFFF1F5F9)

        // Background blocks
        for (i in 0..5) {
            drawLine(
                color = gridColor,
                start = Offset(0f, h * (i / 5f)),
                end = Offset(w, h * (i / 5f)),
                strokeWidth = 2f
            )
            drawLine(
                color = gridColor,
                start = Offset(w * (i / 5f), 0f),
                end = Offset(w * (i / 5f), h),
                strokeWidth = 2f
            )
        }

        // Secondary Roads
        drawLine(
            color = roadColor,
            start = Offset(0f, h * 0.35f),
            end = Offset(w, h * 0.45f),
            strokeWidth = 24f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = roadColor,
            start = Offset(w * 0.3f, 0f),
            end = Offset(w * 0.25f, h),
            strokeWidth = 20f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = roadColor,
            start = Offset(w * 0.7f, 0f),
            end = Offset(w * 0.8f, h),
            strokeWidth = 22f,
            cap = StrokeCap.Round
        )

        // Main Highway
        val highwayPath = Path().apply {
            moveTo(w * 0.05f, h * 0.85f)
            cubicTo(
                w * 0.3f, h * 0.65f,
                w * 0.6f, h * 0.75f,
                w * 0.95f, h * 0.25f
            )
        }
        drawPath(
            path = highwayPath,
            color = mainRoadColor,
            style = Stroke(width = 36f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 2. Waypoint coordinates mapping
        val p0 = Offset(w * 0.15f, h * 0.78f) // Base: Sekolah Rakyat
        val p1 = Offset(w * 0.48f, h * 0.52f) // Tujuan 1
        val p2 = Offset(w * 0.82f, h * 0.28f) // Tujuan 2

        // Route Corridor (Geofencing Safe Zone)
        val corridorPath = Path().apply {
            moveTo(p0.x, p0.y)
            quadraticTo(w * 0.30f, h * 0.68f, p1.x, p1.y)
            quadraticTo(w * 0.65f, h * 0.38f, p2.x, p2.y)
        }

        val routeColor = if (geofenceAlert) CrimsonRed else EmeraldGreen
        val routeHaloColor = if (geofenceAlert) Color(0x33DC2626) else Color(0x33059669)

        // Draw route halo / safe corridor
        drawPath(
            path = corridorPath,
            color = routeHaloColor,
            style = Stroke(width = 28f, cap = StrokeCap.Round)
        )

        // Draw active planned route line
        drawPath(
            path = corridorPath,
            color = routeColor,
            style = Stroke(
                width = 8f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
            )
        )

        // 3. Draw Pins
        // Pin 0: Sekolah Rakyat (Navy)
        drawCircle(color = NavyPrimary.copy(alpha = 0.25f), radius = 24f, center = p0)
        drawCircle(color = NavyPrimary, radius = 12f, center = p0)
        drawCircle(color = Color.White, radius = 5f, center = p0)

        // Pin 1: Tujuan 1 (Amber)
        drawCircle(color = AmberWarning.copy(alpha = 0.25f), radius = 24f, center = p1)
        drawCircle(color = AmberWarning, radius = 12f, center = p1)
        drawCircle(color = Color.White, radius = 5f, center = p1)

        // Pin 2: Tujuan 2 (Emerald)
        drawCircle(color = EmeraldGreen.copy(alpha = 0.25f), radius = 24f, center = p2)
        drawCircle(color = EmeraldGreen, radius = 12f, center = p2)
        drawCircle(color = Color.White, radius = 5f, center = p2)

        // 4. Moving Car Marker along the path
        if (isTrackingActive) {
            val t = carProgressAnim
            val carPos = if (t <= 0.5f) {
                val subT = t * 2f
                Offset(
                    p0.x + (p1.x - p0.x) * subT,
                    p0.y + (p1.y - p0.y) * subT
                )
            } else {
                val subT = (t - 0.5f) * 2f
                Offset(
                    p1.x + (p2.x - p1.x) * subT,
                    p1.y + (p2.y - p1.y) * subT
                )
            }

            val finalCarPos = if (geofenceAlert) {
                Offset(carPos.x - 30f, carPos.y - 45f)
            } else carPos

            drawCircle(
                color = (if (geofenceAlert) CrimsonRed else EmeraldGreen).copy(alpha = 0.35f / pulseAnim),
                radius = 28f * pulseAnim,
                center = finalCarPos
            )
            drawCircle(
                color = Color.White,
                radius = 18f,
                center = finalCarPos
            )
            drawCircle(
                color = if (geofenceAlert) CrimsonRed else EmeraldGreen,
                radius = 14f,
                center = finalCarPos
            )
        }
    }
}
