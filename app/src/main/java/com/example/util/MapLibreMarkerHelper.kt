package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

/**
 * Utility helper to generate and manage custom icons as markers on the MapLibre surface,
 * specifically representing the current real-time location and status of the school vehicle.
 */
object MapLibreMarkerHelper {

    /**
     * Creates a custom high-resolution Bitmap and converts it into a MapLibre [Icon]
     * to represent the school vehicle with plate number, status indicator, and heading pointer.
     */
    fun createVehicleMarkerIcon(
        context: Context,
        vehiclePlate: String,
        speedKmh: Float = 0f,
        isTrackingActive: Boolean = true,
        geofenceAlert: Boolean = false
    ): Icon {
        val density = context.resources.displayMetrics.density
        val width = (110 * density).toInt()
        val height = (110 * density).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val mainColor = if (geofenceAlert) {
            Color.parseColor("#DC2626") // Crimson Red (Alert)
        } else if (isTrackingActive) {
            Color.parseColor("#059669") // Emerald Green (On Duty Active)
        } else {
            Color.parseColor("#1B2A4A") // Navy Primary (Standby)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val centerX = width / 2f
        val centerY = height * 0.44f

        // 1. Outer Pulse / Glow Halo
        paint.color = mainColor
        paint.alpha = 50
        canvas.drawCircle(centerX, centerY, 38 * density, paint)

        // 2. Bottom Pointer Anchor Pin
        val pinPath = Path().apply {
            moveTo(centerX - (14 * density), centerY + (16 * density))
            lineTo(centerX, height - (6 * density))
            lineTo(centerX + (14 * density), centerY + (16 * density))
            close()
        }
        paint.alpha = 255
        paint.color = mainColor
        paint.style = Paint.Style.FILL
        canvas.drawPath(pinPath, paint)

        // 3. Central Circular Badge
        paint.color = Color.parseColor("#1B2A4A") // Deep Navy Body
        canvas.drawCircle(centerX, centerY, 28 * density, paint)

        // 4. Accent Ring
        paint.color = mainColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3.5f * density
        canvas.drawCircle(centerX, centerY, 28 * density, paint)

        // 5. Car Silhouette / Graphic
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE

        // Car Roof & Cabin
        val cabinPath = Path().apply {
            moveTo(centerX - (12 * density), centerY - (3 * density))
            lineTo(centerX - (7 * density), centerY - (12 * density))
            lineTo(centerX + (7 * density), centerY - (12 * density))
            lineTo(centerX + (13 * density), centerY - (3 * density))
            close()
        }
        canvas.drawPath(cabinPath, paint)

        // Car Lower Body
        val bodyRect = RectF(
            centerX - (15 * density),
            centerY - (4 * density),
            centerX + (15 * density),
            centerY + (6 * density)
        )
        canvas.drawRoundRect(bodyRect, 3 * density, 3 * density, paint)

        // Car Wheels
        paint.color = Color.parseColor("#0F172A")
        canvas.drawCircle(centerX - (9 * density), centerY + (6.5f * density), 3.5f * density, paint)
        canvas.drawCircle(centerX + (9 * density), centerY + (6.5f * density), 3.5f * density, paint)

        // Wheel inner hub
        paint.color = Color.WHITE
        canvas.drawCircle(centerX - (9 * density), centerY + (6.5f * density), 1.2f * density, paint)
        canvas.drawCircle(centerX + (9 * density), centerY + (6.5f * density), 1.2f * density, paint)

        // 6. Top License Plate Pill
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(4 * density, 0f, 2 * density, Color.parseColor("#40000000"))
        }
        val pillWidth = (70 * density)
        val pillHeight = (18 * density)
        val pillRect = RectF(
            centerX - (pillWidth / 2f),
            4 * density,
            centerX + (pillWidth / 2f),
            4 * density + pillHeight
        )
        canvas.drawRoundRect(pillRect, 9 * density, 9 * density, pillPaint)

        // Pill Border
        pillPaint.clearShadowLayer()
        pillPaint.color = mainColor
        pillPaint.style = Paint.Style.STROKE
        pillPaint.strokeWidth = 1.5f * density
        canvas.drawRoundRect(pillRect, 9 * density, 9 * density, pillPaint)

        // Plate Text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B2A4A")
            textSize = 9.5f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val textY = (4 * density) + (pillHeight / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(vehiclePlate, centerX, textY, textPaint)

        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }

    /**
     * Creates a custom MapLibre [Icon] for a route waypoint (Origin, intermediate stops, or destination).
     */
    fun createWaypointMarkerIcon(
        context: Context,
        isOrigin: Boolean,
        isDestination: Boolean,
        labelNumber: Int = 1
    ): Icon {
        val density = context.resources.displayMetrics.density
        val size = (42 * density).toInt()

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val centerX = size / 2f
        val centerY = size * 0.42f

        val pinColor = when {
            isOrigin -> Color.parseColor("#1B2A4A")       // Navy for Origin
            isDestination -> Color.parseColor("#059669")  // Emerald Green for Final Destination
            else -> Color.parseColor("#D97706")           // Amber for Waypoints
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Pin Pointer
        val pointerPath = Path().apply {
            moveTo(centerX - (8 * density), centerY + (6 * density))
            lineTo(centerX, size - (2 * density))
            lineTo(centerX + (8 * density), centerY + (6 * density))
            close()
        }
        paint.color = pinColor
        paint.style = Paint.Style.FILL
        canvas.drawPath(pointerPath, paint)

        // Pin Head Circle
        canvas.drawCircle(centerX, centerY, 14 * density, paint)

        // Inner White Circle
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, 11 * density, paint)

        // Inner Icon / Text Number
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = pinColor
            textSize = 10 * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val markerText = when {
            isOrigin -> "S" // S for Sekolah
            isDestination -> "F" // F for Finish
            else -> "$labelNumber"
        }
        val textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(markerText, centerX, textY, textPaint)

        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }

    /**
     * Adds or updates the vehicle marker on the specified [MapLibreMap].
     *
     * @param maplibreMap The active MapLibreMap instance.
     * @param context Android context for asset/density scaling.
     * @param position Current GPS coordinate of the vehicle.
     * @param vehiclePlate License plate of the school vehicle.
     * @param speedKmh Current speed in km/h.
     * @param isTrackingActive Whether live tracking is engaged.
     * @param geofenceAlert Whether the vehicle has strayed outside the geofence corridor.
     * @return The created [Marker] instance.
     */
    fun addVehicleMarker(
        maplibreMap: MapLibreMap,
        context: Context,
        position: LatLng,
        vehiclePlate: String,
        speedKmh: Float,
        isTrackingActive: Boolean,
        geofenceAlert: Boolean = false
    ): Marker {
        val customIcon = createVehicleMarkerIcon(
            context = context,
            vehiclePlate = vehiclePlate,
            speedKmh = speedKmh,
            isTrackingActive = isTrackingActive,
            geofenceAlert = geofenceAlert
        )

        val snippetStatus = when {
            geofenceAlert -> "⚠️ Keluar Jalur Aman! Kecepatan: ${speedKmh.toInt()} km/j"
            isTrackingActive -> "✅ On Duty • Kecepatan: ${speedKmh.toInt()} km/j"
            else -> "🅿️ Standby di Pangkalan"
        }

        return maplibreMap.addMarker(
            MarkerOptions()
                .position(position)
                .icon(customIcon)
                .title("Armada: $vehiclePlate")
                .snippet(snippetStatus)
        )
    }
}
