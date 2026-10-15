package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppUser
import com.example.model.BookingRequest
import com.example.model.BookingStatus
import com.example.model.CarVehicle
import com.example.model.Role
import com.example.model.RouteWaypoint
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SurfaceSubtle

enum class BottomSheetType {
    GATE_INSPECTION,     // Verifikator: Check-out (Izinkan Keluar) atau Check-in
    ROUTE_WAYPOINTS,     // Detail rute multi-point
    QR_CODE_TICKET,      // Tiket booking barcode/QR
    ADD_WAYPOINT         // Tambah titik tujuan baru
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapBottomSheet(
    sheetType: BottomSheetType,
    booking: BookingRequest? = null,
    vehicle: CarVehicle? = null,
    currentUser: AppUser,
    onDismiss: () -> Unit,
    onConfirmCheckout: (odometerKm: Int, fuelPercent: Int) -> Unit = { _, _ -> },
    onConfirmCheckin: (odometerKm: Int, fuelPercent: Int) -> Unit = { _, _ -> },
    onAddWaypoint: (label: String, address: String) -> Unit = { _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFCBD5E1))
            )
        },
        modifier = Modifier.testTag("google_map_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (sheetType) {
                BottomSheetType.GATE_INSPECTION -> {
                    GateInspectionSheetContent(
                        booking = booking,
                        vehicle = vehicle,
                        currentUser = currentUser,
                        onConfirmCheckout = onConfirmCheckout,
                        onConfirmCheckin = onConfirmCheckin,
                        onClose = onDismiss
                    )
                }
                BottomSheetType.ROUTE_WAYPOINTS -> {
                    RouteWaypointsSheetContent(
                        booking = booking,
                        onClose = onDismiss
                    )
                }
                BottomSheetType.QR_CODE_TICKET -> {
                    QrCodeTicketSheetContent(
                        booking = booking,
                        onClose = onDismiss
                    )
                }
                BottomSheetType.ADD_WAYPOINT -> {
                    AddWaypointSheetContent(
                        onAddWaypoint = onAddWaypoint,
                        onClose = onDismiss
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GateInspectionSheetContent(
    booking: BookingRequest?,
    vehicle: CarVehicle?,
    currentUser: AppUser,
    onConfirmCheckout: (Int, Int) -> Unit,
    onConfirmCheckin: (Int, Int) -> Unit,
    onClose: () -> Unit
) {
    if (booking == null) return

    val isCheckoutAction = booking.status == BookingStatus.APPROVED
    val isCheckinAction = booking.status == BookingStatus.ON_DUTY

    // ATURAN PENCEGAHAN SELF-APPROVAL:
    val isSelfApprovalAttempt = (booking.requesterId == currentUser.id) && (currentUser.role == Role.VERIFIKATOR)

    var odometerText by remember {
        mutableStateOf(
            if (isCheckoutAction) {
                (vehicle?.currentOdometerKm ?: 42150).toString()
            } else {
                ((booking.checkoutOdometerKm ?: 42120) + 24).toString()
            }
        )
    }
    var fuelSlider by remember {
        mutableFloatStateOf(
            if (isCheckoutAction) {
                (vehicle?.fuelPercentage ?: 80).toFloat()
            } else {
                75f
            }
        )
    }

    var checkStnk by remember { mutableStateOf(true) }
    var checkTire by remember { mutableStateOf(true) }
    var checkCleanliness by remember { mutableStateOf(true) }

    // Header Title
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (isCheckoutAction) "Verifikasi Gerbang: Izinkan Keluar" else "Verifikasi Gerbang: Mobil Kembali",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Text(
                text = "Pos Keamanan Gerbang Utama • ${booking.schoolBranchName}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            color = if (isCheckoutAction) EmeraldGreen.copy(alpha = 0.15f) else NavyPrimary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = booking.bookingCode,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCheckoutAction) EmeraldGreen else NavyPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Car & Driver Card
    Surface(
        color = SurfaceSubtle,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${booking.vehicleModel} (${booking.vehiclePlate})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Text(
                    text = "Peminjam: ${booking.requesterName} (${booking.passengerCount} Penumpang)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tujuan: ${booking.purpose}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // ATURAN PENCEGAHAN SELF-APPROVAL WARNING BANNER:
    if (isSelfApprovalAttempt) {
        Surface(
            color = CrimsonRed.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Block,
                    contentDescription = "Dilarang",
                    tint = CrimsonRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Proteksi Keamanan: Pencegahan Self-Approval",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CrimsonRed
                    )
                    Text(
                        text = "Anda tidak boleh menyetujui peminjaman diri sendiri. Verifikasi gerbang wajib dilakukan oleh rekan petugas keamanan lain atau Admin Cabang.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
    }

    // Input Odometer KM
    Text(
        text = if (isCheckoutAction) "Odometer / Kilometer Berangkat (KM):" else "Odometer / Kilometer Kembali (KM):",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = NavyPrimary
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = odometerText,
        onValueChange = { odometerText = it },
        leadingIcon = {
            Icon(Icons.Default.Speed, contentDescription = null, tint = NavyPrimary)
        },
        trailingIcon = { Text("KM", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
        modifier = Modifier.fillMaxWidth().testTag("odometer_input")
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Input Level BBM (Slider)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Indikator Bensin / BBM:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavyPrimary
        )
        Text(
            text = "${fuelSlider.toInt()}%",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldGreen
        )
    }
    Slider(
        value = fuelSlider,
        onValueChange = { fuelSlider = it },
        valueRange = 0f..100f,
        colors = SliderDefaults.colors(
            thumbColor = EmeraldGreen,
            activeTrackColor = EmeraldGreen
        ),
        modifier = Modifier.testTag("fuel_slider")
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Physical Inspection Checklist
    Text(
        text = "Checklist Kelayakan Fisik & Dokumen:",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = NavyPrimary
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checkStnk, onCheckedChange = { checkStnk = it })
        Text("Kunci Mobil & STNK Lengkap Asli", fontSize = 12.sp)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checkTire, onCheckedChange = { checkTire = it })
        Text("Kondisi Ban, Dongkrak, & Tekanan Udara Normal", fontSize = 12.sp)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checkCleanliness, onCheckedChange = { checkCleanliness = it })
        Text("Fisik Bersih & Tidak Ada Kerusakan Baru", fontSize = 12.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Action Buttons
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.weight(1f)
        ) {
            Text("Tutup")
        }

        if (isCheckoutAction) {
            Button(
                onClick = {
                    val km = odometerText.toIntOrNull() ?: 42150
                    onConfirmCheckout(km, fuelSlider.toInt())
                },
                enabled = !isSelfApprovalAttempt,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("btn_confirm_checkout")
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Izinkan Keluar", fontWeight = FontWeight.Bold)
            }
        } else if (isCheckinAction) {
            Button(
                onClick = {
                    val km = odometerText.toIntOrNull() ?: 42180
                    onConfirmCheckin(km, fuelSlider.toInt())
                },
                enabled = !isSelfApprovalAttempt,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("btn_confirm_checkin")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Selesai Peminjaman", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RouteWaypointsSheetContent(
    booking: BookingRequest?,
    onClose: () -> Unit
) {
    if (booking == null) return

    Text(
        text = "Rute Perjalanan Multi-Point",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = NavyPrimary
    )
    Text(
        text = "Kode Booking: ${booking.bookingCode} • ${booking.vehiclePlate}",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    booking.waypoints.forEachIndexed { index, wp ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (index == 0) NavyPrimary else if (index == booking.waypoints.size - 1) EmeraldGreen else AmberWarning),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wp.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyPrimary
                )
                Text(
                    text = wp.address,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (index < booking.waypoints.size - 1) {
            Box(
                modifier = Modifier
                    .padding(start = 13.dp)
                    .width(2.dp)
                    .height(20.dp)
                    .background(Color(0xFFCBD5E1))
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
    Button(
        onClick = onClose,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
    ) {
        Text("Tutup Rute")
    }
}

@Composable
private fun QrCodeTicketSheetContent(
    booking: BookingRequest?,
    onClose: () -> Unit
) {
    if (booking == null) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "E-Tiket & Kode QR Gerbang",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )
        Text(
            text = "Tunjukkan barcode/QR ini kepada Petugas Keamanan di pos gerbang",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Visual QR Simulation Box
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, NavyPrimary),
            shadowElevation = 6.dp,
            modifier = Modifier.size(210.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.QrCode,
                    contentDescription = "QR Code",
                    modifier = Modifier.size(130.dp),
                    tint = NavyPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = booking.bookingCode,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                    color = NavyPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = SurfaceSubtle,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Mobil: ${booking.vehicleModel} (${booking.vehiclePlate})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Text(
                    text = "Peminjam: ${booking.requesterName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Jadwal: ${booking.departureDate}, ${booking.departureTime} - ${booking.estimatedReturnTime}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Status: ${booking.status.label}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (booking.status == BookingStatus.APPROVED) EmeraldGreen else NavyPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kembali")
        }
    }
}

@Composable
private fun AddWaypointSheetContent(
    onAddWaypoint: (String, String) -> Unit,
    onClose: () -> Unit
) {
    var labelText by remember { mutableStateOf("") }
    var addressText by remember { mutableStateOf("") }

    Text(
        text = "Tambah Titik Tujuan (Multi-Point)",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = NavyPrimary
    )
    Text(
        text = "Tentukan titik singgah atau destinasi perjalanan dinas armada",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = labelText,
        onValueChange = { labelText = it },
        label = { Text("Nama Destinasi (Contoh: RSUD / Kantor Dinas)") },
        modifier = Modifier.fillMaxWidth().testTag("waypoint_label_input")
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = addressText,
        onValueChange = { addressText = it },
        label = { Text("Alamat / Jalan Lengkap") },
        modifier = Modifier.fillMaxWidth().testTag("waypoint_address_input")
    )

    Spacer(modifier = Modifier.height(18.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.weight(1f)
        ) {
            Text("Batal")
        }
        Button(
            onClick = {
                if (labelText.isNotBlank()) {
                    onAddWaypoint(labelText, addressText.ifBlank { "Jl. Protokol No. 10" })
                    onClose()
                }
            },
            enabled = labelText.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            modifier = Modifier.weight(1f).testTag("btn_save_waypoint")
        ) {
            Text("Simpan Titik")
        }
    }
}
