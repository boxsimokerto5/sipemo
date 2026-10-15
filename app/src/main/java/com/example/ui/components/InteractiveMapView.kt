package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.model.RouteWaypoint
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.util.LocationProviderManager
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

enum class MapLayerMode(val label: String) {
    STREETS("Jalan (OSM)"),
    SATELLITE("Satelit (ESRI)"),
    SCHEMATIC("Radar Skematik")
}

@Composable
fun InteractiveMapView(
    modifier: Modifier = Modifier,
    waypoints: List<RouteWaypoint> = emptyList(),
    vehiclePlate: String = "B 1024 SR",
    isTrackingActive: Boolean = true,
    speedKmh: Float = 42f,
    geofenceAlert: Boolean = false,
    onMapClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedLayer by remember { mutableStateOf(MapLayerMode.STREETS) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Real device GPS coordinates from LocationProviderManager
    val userGps by LocationProviderManager.currentCoordinate.collectAsState()

    // Calculate effective vehicle position
    val vehicleLat = remember(waypoints, userGps) {
        if (waypoints.size >= 2) {
            val p0 = waypoints[0]
            val p1 = waypoints[1]
            p0.lat + (p1.lat - p0.lat) * 0.45
        } else if (waypoints.isNotEmpty()) {
            waypoints[0].lat
        } else {
            -6.2088 // Base Jakarta Timur
        }
    }
    val vehicleLng = remember(waypoints, userGps) {
        if (waypoints.size >= 2) {
            val p0 = waypoints[0]
            val p1 = waypoints[1]
            p0.lng + (p1.lng - p0.lng) * 0.45
        } else if (waypoints.isNotEmpty()) {
            waypoints[0].lng
        } else {
            106.8456 // Base Jakarta Timur
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
            // Leaflet Native WebView Container using bundled local assets
            LeafletMapContainer(
                selectedLayer = selectedLayer,
                userLat = userGps.latitude,
                userLng = userGps.longitude,
                userAccuracy = userGps.accuracyMeters,
                waypoints = waypoints,
                vehicleLat = vehicleLat,
                vehicleLng = vehicleLng,
                vehiclePlate = vehiclePlate,
                speedKmh = speedKmh,
                isTrackingActive = isTrackingActive,
                geofenceAlert = geofenceAlert,
                onWebViewReady = { wv -> webViewInstance = wv }
            )
        }

        // Top Status Header Overlay with Real GPS Coordinates Display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Vehicle / Standby Pill
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
                                .background(if (geofenceAlert) CrimsonRed else (if (isTrackingActive) EmeraldGreen else Color(0xFF0284C7)))
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

            Spacer(modifier = Modifier.height(6.dp))

            // User Coordinate Indicator Pill
            Surface(
                color = Color.White.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (userGps.isRealGps) EmeraldGreen else Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "📍 Koordinat Anda: ${String.format(Locale.US, "%.5f, %.5f", userGps.latitude, userGps.longitude)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary
                    )
                }
            }
        }

        // Map Control Floating Buttons (Center to User, Center to School, Zoom In/Out)
        if (selectedLayer != MapLayerMode.SCHEMATIC) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Focus User Location Button (Titik Koordinat Saya)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable {
                            webViewInstance?.evaluateJavascript("flyToUser();", null)
                        }
                        .testTag("btn_focus_user_gps")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Pusatkan ke Titik Koordinat Saya",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Focus School Base Button
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable {
                            webViewInstance?.evaluateJavascript("flyToSchool();", null)
                        }
                        .testTag("btn_focus_school_base")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = "Pusatkan ke Pangkalan Sekolah",
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Focus Vehicle Button if active
                if (isTrackingActive) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable {
                                webViewInstance?.evaluateJavascript("flyToVehicle();", null)
                            }
                            .testTag("btn_focus_vehicle")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = "Pusatkan ke Armada",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Zoom In Button
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable {
                            webViewInstance?.evaluateJavascript("zoomIn();", null)
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
                        .size(38.dp)
                        .clickable {
                            webViewInstance?.evaluateJavascript("zoomOut();", null)
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LeafletMapContainer(
    selectedLayer: MapLayerMode,
    userLat: Double,
    userLng: Double,
    userAccuracy: Float,
    waypoints: List<RouteWaypoint>,
    vehicleLat: Double,
    vehicleLng: Double,
    vehiclePlate: String,
    speedKmh: Float,
    isTrackingActive: Boolean,
    geofenceAlert: Boolean,
    onWebViewReady: (WebView) -> Unit
) {
    val context = LocalContext.current

    val leafletCss = remember(context) {
        try {
            context.assets.open("leaflet/leaflet.css").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }
    val leafletJs = remember(context) {
        try {
            context.assets.open("leaflet/leaflet.js").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    val initialHtml = remember(leafletCss, leafletJs) {
        getLeafletHtml(cssContent = leafletCss, jsContent = leafletJs)
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }

    // Update markers and properties whenever inputs change
    LaunchedEffect(isMapLoaded, selectedLayer, userLat, userLng, userAccuracy, waypoints, vehicleLat, vehicleLng, speedKmh, isTrackingActive, geofenceAlert) {
        val wv = webViewRef ?: return@LaunchedEffect
        if (!isMapLoaded) return@LaunchedEffect

        // 1. Update user location
        wv.evaluateJavascript(
            "updateUserLocation($userLat, $userLng, $userAccuracy, false);",
            null
        )

        // 2. Update map layer
        val layerArg = if (selectedLayer == MapLayerMode.SATELLITE) "'SATELLITE'" else "'STREETS'"
        wv.evaluateJavascript("setLayer($layerArg);", null)

        // 3. Update vehicle position
        wv.evaluateJavascript(
            "updateVehicle($vehicleLat, $vehicleLng, '$vehiclePlate', $speedKmh, $isTrackingActive);",
            null
        )

        // 4. Update waypoints list
        val jsonArray = JSONArray()
        waypoints.forEach { wp ->
            val obj = JSONObject()
            obj.put("id", wp.id)
            obj.put("label", wp.label)
            obj.put("address", wp.address)
            obj.put("lat", wp.lat)
            obj.put("lng", wp.lng)
            jsonArray.put(obj)
        }
        val safeJson = jsonArray.toString().replace("'", "\\'")
        wv.evaluateJavascript("updateWaypoints('$safeJson', $geofenceAlert);", null)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    allowFileAccessFromFileURLs = true
                    allowUniversalAccessFromFileURLs = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    setSupportZoom(true)
                    builtInZoomControls = false
                    displayZoomControls = false
                    cacheMode = WebSettings.LOAD_DEFAULT
                    userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 SiPemoApp/1.0"
                }

                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                        android.util.Log.d("SiPemoMap", "${consoleMessage?.message()} [line ${consoleMessage?.lineNumber()}]")
                        return super.onConsoleMessage(consoleMessage)
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isMapLoaded = true
                        onWebViewReady(this@apply)
                        // Center to user GPS or default location
                        evaluateJavascript("updateUserLocation($userLat, $userLng, $userAccuracy, true);", null)
                    }

                    override fun onReceivedError(view: WebView?, request: android.webkit.WebResourceRequest?, error: android.webkit.WebResourceError?) {
                        super.onReceivedError(view, request, error)
                        android.util.Log.w("SiPemoMap", "WebView load error: ${error?.description}")
                    }
                }

                loadDataWithBaseURL("https://sipemo.app/", initialHtml, "text/html", "UTF-8", null)
                webViewRef = this
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun getLeafletHtml(cssContent: String, jsContent: String): String {
    return """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <style>
    $cssContent
  </style>
  <style>
    html, body {
      height: 100%;
      width: 100%;
      margin: 0;
      padding: 0;
      background: #e2e8f0;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      overflow: hidden;
    }
    #map {
      height: 100%;
      width: 100%;
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
    }
    .pulse-pin-container {
      position: relative;
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .pulse-dot {
      width: 14px;
      height: 14px;
      background: #0284c7;
      border: 2.5px solid #ffffff;
      border-radius: 50%;
      box-shadow: 0 0 10px rgba(2,132,199,0.9);
      z-index: 2;
    }
    .pulse-ring {
      position: absolute;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: rgba(2,132,199,0.35);
      animation: pulseRing 2s infinite ease-out;
      z-index: 1;
    }
    @keyframes pulseRing {
      0% { transform: scale(0.3); opacity: 1; }
      100% { transform: scale(1.7); opacity: 0; }
    }
    .badge-marker {
      background: #0f172a;
      color: white;
      padding: 4px 8px;
      border-radius: 12px;
      font-size: 11px;
      font-weight: bold;
      box-shadow: 0 2px 8px rgba(0,0,0,0.35);
      border: 1.5px solid white;
      white-space: nowrap;
      text-align: center;
    }
    .badge-marker.vehicle { background: #059669; }
    .badge-marker.school { background: #1e3a8a; }
    .badge-marker.dest { background: #d97706; }
    .badge-marker.user { background: #0284c7; }
    .leaflet-popup-content-wrapper {
      border-radius: 12px;
      font-size: 12px;
      padding: 4px;
    }
  </style>
  <script>
    $jsContent
  </script>
</head>
<body>
  <div id="map"></div>
  <script>
    if (typeof L === 'undefined') {
      document.write('<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>');
      document.write('<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"><\\/script>');
    }
  </script>
  <script>
    var map;
    try {
      map = L.map('map', {
        zoomControl: false,
        attributionControl: false
      }).setView([-6.2088, 106.8456], 14);
    } catch(e) {
      console.error("Map init error:", e);
    }

    // Street Tiles: Carto Voyager
    var streetTiles = L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
      maxZoom: 19,
      subdomains: 'abcd',
      crossOrigin: true
    });
    if (map) streetTiles.addTo(map);

    // OpenStreetMap tile fallback
    var osmTiles = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      subdomains: ['a', 'b', 'c'],
      crossOrigin: true
    });

    // Satellite Imagery: ESRI World Imagery
    var satTiles = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
      maxZoom: 19,
      crossOrigin: true
    });

    var userMarker = null;
    var userCircle = null;
    var vehicleMarker = null;
    var schoolMarker = null;
    var waypointMarkers = [];
    var routePolyline = null;

    // School Base Marker
    if (map) {
      var schoolIcon = L.divIcon({
        className: 'custom-icon',
        html: '<div class="badge-marker school">🏫 Pangkalan Sekolah Rakyat</div>',
        iconSize: [170, 26],
        iconAnchor: [85, 13]
      });
      schoolMarker = L.marker([-6.2088, 106.8456], { icon: schoolIcon }).addTo(map)
        .bindPopup('<b>Sekolah Rakyat Cabang</b><br>Pangkalan Utama Armada Siaga');
    }

    function updateUserLocation(lat, lng, accuracy, autoCenter) {
      if (!map) return;
      if (!userMarker) {
        var userIcon = L.divIcon({
          className: 'pulse-pin-container',
          html: '<div class="pulse-ring"></div><div class="pulse-dot"></div>',
          iconSize: [32, 32],
          iconAnchor: [16, 16]
        });
        userMarker = L.marker([lat, lng], { icon: userIcon }).addTo(map)
          .bindPopup('<b>📍 Lokasi Anda (Titik GPS)</b><br>Lat: ' + lat.toFixed(5) + '<br>Lng: ' + lng.toFixed(5) + '<br>Akurasi: ' + Math.round(accuracy) + ' m');
        userCircle = L.circle([lat, lng], {
          radius: accuracy || 25,
          color: '#0284c7',
          fillColor: '#0284c7',
          fillOpacity: 0.15,
          weight: 1.5
        }).addTo(map);
      } else {
        userMarker.setLatLng([lat, lng]);
        userMarker.setPopupContent('<b>📍 Lokasi Anda (Titik GPS)</b><br>Lat: ' + lat.toFixed(5) + '<br>Lng: ' + lng.toFixed(5) + '<br>Akurasi: ' + Math.round(accuracy) + ' m');
        if (userCircle) {
          userCircle.setLatLng([lat, lng]);
          userCircle.setRadius(accuracy || 25);
        }
      }
      if (autoCenter) {
        map.setView([lat, lng], 15);
      }
    }

    function setLayer(mode) {
      if (!map) return;
      if (mode === 'SATELLITE') {
        if (map.hasLayer(streetTiles)) map.removeLayer(streetTiles);
        if (map.hasLayer(osmTiles)) map.removeLayer(osmTiles);
        if (!map.hasLayer(satTiles)) satTiles.addTo(map);
      } else {
        if (map.hasLayer(satTiles)) map.removeLayer(satTiles);
        if (!map.hasLayer(streetTiles)) streetTiles.addTo(map);
      }
    }

    function updateVehicle(lat, lng, plate, speed, active) {
      if (!map) return;
      if (!active) {
        if (vehicleMarker) { map.removeLayer(vehicleMarker); vehicleMarker = null; }
        return;
      }
      var carIcon = L.divIcon({
        className: 'custom-icon',
        html: '<div class="badge-marker vehicle">🚗 ' + plate + ' (' + Math.round(speed) + ' km/j)</div>',
        iconSize: [140, 26],
        iconAnchor: [70, 13]
      });
      if (!vehicleMarker) {
        vehicleMarker = L.marker([lat, lng], { icon: carIcon }).addTo(map)
          .bindPopup('<b>Armada Aktif: ' + plate + '</b><br>Kecepatan: ' + Math.round(speed) + ' km/j');
      } else {
        vehicleMarker.setLatLng([lat, lng]);
        vehicleMarker.setIcon(carIcon);
      }
    }

    function updateWaypoints(wpListJson, alert) {
      if (!map) return;
      waypointMarkers.forEach(function(m) { map.removeLayer(m); });
      waypointMarkers = [];
      if (routePolyline) { map.removeLayer(routePolyline); routePolyline = null; }

      var pts = [];
      try {
        var wps = JSON.parse(wpListJson);
        for (var i = 0; i < wps.length; i++) {
          var wp = wps[i];
          pts.push([wp.lat, wp.lng]);
          var iconHtml = '<div class="badge-marker ' + (i === 0 ? 'school' : 'dest') + '">' +
            (i === 0 ? '🚩 ' : (i === wps.length - 1 ? '🏁 ' : '📍 ')) + wp.label + '</div>';
          var mIcon = L.divIcon({
            className: 'custom-icon',
            html: iconHtml,
            iconSize: [140, 26],
            iconAnchor: [70, 13]
          });
          var m = L.marker([wp.lat, wp.lng], { icon: mIcon }).addTo(map)
            .bindPopup('<b>' + wp.label + '</b><br>' + wp.address);
          waypointMarkers.push(m);
        }
        if (pts.length >= 2) {
          routePolyline = L.polyline(pts, {
            color: alert ? '#dc2626' : '#059669',
            weight: 5,
            opacity: 0.85,
            dashArray: '8, 8'
          }).addTo(map);
          map.fitBounds(routePolyline.getBounds(), { padding: [40, 40] });
        }
      } catch(e){}
    }

    function flyToUser() {
      if (!map) return;
      if (userMarker) {
        map.flyTo(userMarker.getLatLng(), 16);
      } else {
        map.flyTo([-6.2088, 106.8456], 15);
      }
    }

    function flyToSchool() {
      if (!map) return;
      map.flyTo([-6.2088, 106.8456], 15);
    }

    function flyToVehicle() {
      if (!map) return;
      if (vehicleMarker) {
        map.flyTo(vehicleMarker.getLatLng(), 16);
      } else {
        flyToSchool();
      }
    }

    function zoomIn() { if (map) map.zoomIn(); }
    function zoomOut() { if (map) map.zoomOut(); }
  </script>
</body>
</html>
    """.trimIndent()
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
