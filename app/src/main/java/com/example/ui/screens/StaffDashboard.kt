package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.model.AppUser
import com.example.model.BookingRequest
import com.example.model.BookingStatus
import com.example.model.CarStatus
import com.example.model.CarVehicle
import com.example.model.RouteWaypoint
import com.example.service.TrackingForegroundService
import com.example.ui.components.BottomSheetType
import com.example.ui.components.GoogleMapBottomSheet
import com.example.ui.components.InteractiveMapView
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.SurfaceSubtle

@Composable
fun StaffDashboard(
    repository: DataRepository,
    currentUser: AppUser
) {
    val context = LocalContext.current
    val branchId = currentUser.schoolBranchId ?: "SCH-001"

    val vehicles by repository.vehicles.collectAsState()
    val bookings by repository.bookings.collectAsState()
    val trackingState by TrackingForegroundService.trackingState.collectAsState()

    val branchVehicles = vehicles.filter { it.schoolBranchId == branchId }
    val myBookings = bookings.filter { it.requesterId == currentUser.id }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pengajuan Peminjaman", "Peminjaman Saya", "Peta Live Tracking")

    var activeBottomSheet by remember { mutableStateOf<BottomSheetType?>(null) }
    var selectedBookingForSheet by remember { mutableStateOf<BookingRequest?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Staff Header
        Surface(
            color = NavyPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Halo, ${currentUser.name}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${currentUser.professionTitle} • ${currentUser.schoolBranchName ?: "Sekolah Rakyat"}",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfacePureWhite,
            contentColor = NavyPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NavyPrimary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        when (selectedTab) {
            0 -> NewBookingFormContent(
                branchVehicles = branchVehicles,
                currentUser = currentUser,
                onSubmitted = {
                    selectedTab = 1
                    Toast.makeText(context, "Pengajuan peminjaman berhasil dikirim ke Admin Cabang!", Toast.LENGTH_LONG).show()
                },
                onRequestAddWaypoint = {
                    activeBottomSheet = BottomSheetType.ADD_WAYPOINT
                },
                repository = repository
            )
            1 -> MyBookingsListContent(
                bookings = myBookings,
                onShowQrCode = { bkg ->
                    selectedBookingForSheet = bkg
                    activeBottomSheet = BottomSheetType.QR_CODE_TICKET
                },
                onShowRoute = { bkg ->
                    selectedBookingForSheet = bkg
                    activeBottomSheet = BottomSheetType.ROUTE_WAYPOINTS
                }
            )
            2 -> StaffLiveTrackingContent(
                currentUser = currentUser,
                myActiveBooking = myBookings.find { it.status == BookingStatus.ON_DUTY },
                branchActiveBooking = bookings.filter { it.schoolBranchId == branchId }.find { it.status == BookingStatus.ON_DUTY },
                branchVehicles = branchVehicles,
                trackingState = trackingState
            )
        }
    }

    // Google Map style bottom sheet slider
    if (activeBottomSheet != null) {
        GoogleMapBottomSheet(
            sheetType = activeBottomSheet!!,
            booking = selectedBookingForSheet,
            currentUser = currentUser,
            onDismiss = { activeBottomSheet = null },
            onAddWaypoint = { label, addr ->
                // Handled in booking flow
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewBookingFormContent(
    branchVehicles: List<CarVehicle>,
    currentUser: AppUser,
    onSubmitted: () -> Unit,
    onRequestAddWaypoint: () -> Unit,
    repository: DataRepository
) {
    val availableCars = branchVehicles.filter { it.status == CarStatus.TERSEDIA }

    var expandedCarDropdown by remember { mutableStateOf(false) }
    var selectedCar by remember { mutableStateOf(availableCars.firstOrNull()) }

    var purpose by remember { mutableStateOf("") }
    var passengerCount by remember { mutableStateOf("4") }
    var departureDate by remember { mutableStateOf("15 Okt 2026") }
    var departureTime by remember { mutableStateOf("09:00 WIB") }
    var returnTime by remember { mutableStateOf("15:00 WIB") }
    var isEmergency by remember { mutableStateOf(false) }

    // Multi-Point Destination list
    val waypoints = remember {
        mutableStateListOf(
            RouteWaypoint("WP-0", "Titik Asal: Sekolah Rakyat Cabang", "Gerbang Utama Sekolah Rakyat", -6.2088, 106.8456),
            RouteWaypoint("WP-1", "Tujuan 1: RSUD / Puskesmas Rujukan", "Jl. Raden Inten II No. 1", -6.2200, 106.8560)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Formulir Peminjaman Mobil Dinas",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
        }

        // Vehicle Selector Dropdown
        item {
            Text("Pilih Armada Mobil (Hanya Unit Berstatus Tersedia):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = expandedCarDropdown,
                onExpandedChange = { expandedCarDropdown = !expandedCarDropdown }
            ) {
                OutlinedTextField(
                    value = selectedCar?.let { "${it.brandModel} (${it.plateNumber}) - Kapasitas: ${it.capacity}" } ?: "Tidak ada mobil tersedia",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCarDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth().testTag("dropdown_car_select")
                )
                ExposedDropdownMenu(
                    expanded = expandedCarDropdown,
                    onDismissRequest = { expandedCarDropdown = false }
                ) {
                    availableCars.forEach { car ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("${car.brandModel} (${car.plateNumber})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Kapasitas: ${car.capacity} Org • BBM: ${car.fuelPercentage}%", fontSize = 11.sp, color = EmeraldGreen)
                                }
                            },
                            onClick = {
                                selectedCar = car
                                expandedCarDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Detail Inputs
        item {
            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("Alasan / Kebutuhan Dinas") },
                placeholder = { Text("Contoh: Antar kontingen lomba sains / belanja logistik dapur") },
                modifier = Modifier.fillMaxWidth().testTag("input_booking_purpose")
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = passengerCount,
                    onValueChange = { passengerCount = it },
                    label = { Text("Jml Penumpang") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = departureDate,
                    onValueChange = { departureDate = it },
                    label = { Text("Tanggal") },
                    modifier = Modifier.weight(1.5f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = departureTime,
                    onValueChange = { departureTime = it },
                    label = { Text("Jam Berangkat") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = returnTime,
                    onValueChange = { returnTime = it },
                    label = { Text("Estimasi Kembali") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Multi-Point Destination Waypoints Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rute Perjalanan (Multi-Point Destination):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NavyPrimary
                        )
                        OutlinedButton(
                            onClick = {
                                val nextNum = waypoints.size
                                waypoints.add(
                                    RouteWaypoint(
                                        id = "WP-$nextNum",
                                        label = "Tujuan $nextNum: Kantor Terkait",
                                        address = "Jl. Merdeka No. ${nextNum * 12}",
                                        lat = -6.2100 + (nextNum * 0.01),
                                        lng = 106.8400 + (nextNum * 0.01)
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_add_waypoint_row")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Titik", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    waypoints.forEachIndexed { idx, wp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (idx == 0) NavyPrimary else EmeraldGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${idx + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(wp.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                Text(wp.address, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (idx > 1) {
                                IconButton(onClick = { waypoints.removeAt(idx) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Submit Button
        item {
            Button(
                onClick = {
                    selectedCar?.let { car ->
                        repository.createBooking(
                            schoolBranchId = currentUser.schoolBranchId ?: "SCH-001",
                            vehicleId = car.id,
                            requester = currentUser,
                            passengers = passengerCount.toIntOrNull() ?: 4,
                            purpose = purpose.ifBlank { "Tugas Dinas Operasional Sekolah Rakyat" },
                            departureDate = departureDate,
                            departureTime = departureTime,
                            estimatedReturnTime = returnTime,
                            waypoints = waypoints.toList(),
                            isEmergency = isEmergency
                        )
                        onSubmitted()
                    }
                },
                enabled = selectedCar != null,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_submit_booking")
            ) {
                Text("Kirim Pengajuan Peminjaman", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MyBookingsListContent(
    bookings: List<BookingRequest>,
    onShowQrCode: (BookingRequest) -> Unit,
    onShowRoute: (BookingRequest) -> Unit
) {
    if (bookings.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Belum ada riwayat peminjaman mobil.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(bookings) { bkg ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("booking_card_${bkg.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (bkg.status) {
                                BookingStatus.PENDING_APPROVAL -> AmberWarning.copy(alpha = 0.15f)
                                BookingStatus.APPROVED -> EmeraldGreen.copy(alpha = 0.15f)
                                BookingStatus.ON_DUTY -> Color(0xFF0284C7).copy(alpha = 0.15f)
                                BookingStatus.FINISHED -> Color(0xFF64748B).copy(alpha = 0.15f)
                                BookingStatus.REJECTED -> CrimsonRed.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = bkg.status.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (bkg.status) {
                                    BookingStatus.PENDING_APPROVAL -> AmberWarning
                                    BookingStatus.APPROVED -> EmeraldGreen
                                    BookingStatus.ON_DUTY -> Color(0xFF0284C7)
                                    BookingStatus.FINISHED -> Color(0xFF64748B)
                                    BookingStatus.REJECTED -> CrimsonRed
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = bkg.bookingCode,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = bkg.purpose,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Unit: ${bkg.vehicleModel} (${bkg.vehiclePlate})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0284C7)
                    )
                    Text(
                        text = "Jadwal: ${bkg.departureDate}, ${bkg.departureTime} - ${bkg.estimatedReturnTime}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onShowRoute(bkg) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lihat Rute", fontSize = 11.sp)
                        }

                        // Button to show QR code for security gate
                        Button(
                            onClick = { onShowQrCode(bkg) },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.weight(1.3f).testTag("btn_show_qr_${bkg.id}"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("QR Gerbang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffLiveTrackingContent(
    currentUser: AppUser,
    myActiveBooking: BookingRequest?,
    branchActiveBooking: BookingRequest?,
    branchVehicles: List<CarVehicle>,
    trackingState: com.example.service.LiveTrackingState
) {
    val context = LocalContext.current
    val targetBooking = myActiveBooking ?: branchActiveBooking
    val isMyDuty = myActiveBooking != null

    val displayWaypoints = remember(targetBooking) {
        if (targetBooking != null && targetBooking.waypoints.isNotEmpty()) {
            targetBooking.waypoints
        } else {
            listOf(
                RouteWaypoint("WP-BASE", "Sekolah Rakyat Cabang", "Pangkalan Utama Armada", -6.2088, 106.8456, isCompleted = true),
                RouteWaypoint("WP-T1", "RSUD / Puskesmas Rujukan", "Jl. Raden Inten II No. 1", -6.2200, 106.8560),
                RouteWaypoint("WP-T2", "Kantor Suku Dinas Pendidikan", "Jl. Matraman Raya No. 220", -6.2300, 106.8650)
            )
        }
    }

    val displayVehiclePlate = remember(targetBooking, branchVehicles) {
        targetBooking?.vehiclePlate ?: branchVehicles.firstOrNull()?.plateNumber ?: "B 1024 SR"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Status & Context Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (targetBooking != null || trackingState.isTrackingActive) EmeraldGreen.copy(alpha = 0.15f) else Color(0xFF64748B).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when {
                                isMyDuty -> "PERJALANAN DINAS ANDA (ON DUTY)"
                                targetBooking != null -> "ARMADA CABANG BERTUGAS"
                                trackingState.isTrackingActive -> "SIMULASI GPS AKTIF"
                                else -> "ARMADA STANDBY DI PANGKALAN"
                            },
                            color = if (targetBooking != null || trackingState.isTrackingActive) EmeraldGreen else Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = targetBooking?.bookingCode ?: "PANGKALAN CABANG",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = NavyPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (targetBooking != null) {
                    Text(
                        text = targetBooking.purpose,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Unit: ${targetBooking.vehicleModel} (${targetBooking.vehiclePlate}) • Peminjam: ${targetBooking.requesterName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Pantauan Rute & Pangkalan Armada Sekolah Rakyat",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Belum ada dinas yang sedang keluar gerbang. Peta interaktif menampilkan pangkalan dan koridor rute aman.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Demo / Simulation Control Button
                    if (trackingState.isTrackingActive) {
                        OutlinedButton(
                            onClick = {
                                TrackingForegroundService.stopTracking(context)
                                Toast.makeText(context, "Pelacakan GPS dihentikan", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_stop_tracking_sim"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hentikan Simulasi GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                TrackingForegroundService.startTracking(
                                    context = context,
                                    bookingCode = "SR-JKT-8821",
                                    vehiclePlate = displayVehiclePlate,
                                    driverName = currentUser.name
                                )
                                Toast.makeText(context, "Simulasi Live Tracking dimulai! Sinyal GPS aktif.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_start_tracking_sim"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mulai Simulasi Pelacakan GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Map View is ALWAYS RENDERED
        InteractiveMapView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            waypoints = displayWaypoints,
            vehiclePlate = displayVehiclePlate,
            isTrackingActive = trackingState.isTrackingActive || targetBooking != null,
            speedKmh = if (trackingState.isTrackingActive) trackingState.speedKmh else (if (targetBooking != null) 38f else 0f),
            geofenceAlert = trackingState.geofenceAlert
        )
    }
}
