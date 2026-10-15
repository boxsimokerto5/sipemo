package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.Role
import com.example.service.TrackingForegroundService
import com.example.ui.components.CardIconMetric
import com.example.ui.components.InteractiveMapView
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.SurfaceSubtle

@Composable
fun AdminSekolahDashboard(
    repository: DataRepository,
    currentUser: AppUser
) {
    val context = LocalContext.current
    val branchId = currentUser.schoolBranchId ?: "SCH-001"

    val vehicles by repository.vehicles.collectAsState()
    val bookings by repository.bookings.collectAsState()
    val users by repository.users.collectAsState()
    val trackingState by TrackingForegroundService.trackingState.collectAsState()

    val branchVehicles = vehicles.filter { it.schoolBranchId == branchId }
    val branchBookings = bookings.filter { it.schoolBranchId == branchId }
    val branchPendingUsers = users.filter { it.schoolBranchId == branchId && !it.isApproved }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Persetujuan Dinas", "Armada Mobil", "Live Tracking & Peta", "Verifikasi Akun")

    var showAddCarDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Branch Header
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
                    text = currentUser.schoolBranchName ?: "Sekolah Rakyat Cabang",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Dashboard Pengelola Armada & Operasional Cabang",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfacePureWhite,
            contentColor = NavyPrimary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NavyPrimary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                val badgeCount = when (index) {
                    0 -> branchBookings.count { it.status == BookingStatus.PENDING_APPROVAL }
                    3 -> branchPendingUsers.size
                    else -> 0
                }
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            if (badgeCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = AmberWarning,
                                    shape = CircleShape,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$badgeCount", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> AdminBookingApprovalContent(
                bookings = branchBookings,
                onApprove = { bkg ->
                    repository.approveBookingByAdmin(bkg.id)
                    Toast.makeText(context, "Peminjaman ${bkg.bookingCode} disetujui! Siap gerbang.", Toast.LENGTH_SHORT).show()
                },
                onReject = { bkg ->
                    repository.rejectBookingByAdmin(bkg.id, "Jadwal operasional diprioritaskan untuk kegiatan sekolah lain.")
                    Toast.makeText(context, "Peminjaman ${bkg.bookingCode} ditolak.", Toast.LENGTH_SHORT).show()
                },
                onEmergencyOverride = { bkg ->
                    repository.adminEmergencyGateOverride(context, bkg.id, currentUser)
                    Toast.makeText(context, "Emergency Override diaktifkan! Mobil keluar gerbang.", Toast.LENGTH_SHORT).show()
                }
            )
            1 -> AdminFleetContent(
                vehicles = branchVehicles,
                onAddCarClick = { showAddCarDialog = true },
                onToggleStatus = { car, newStatus ->
                    repository.updateVehicleStatus(car.id, newStatus)
                }
            )
            2 -> AdminLiveMapTrackingContent(
                branchBookings = branchBookings,
                trackingState = trackingState,
                onToggleGeofenceDeviate = { isDeviated ->
                    TrackingForegroundService.toggleGeofenceSimulation(isDeviated)
                }
            )
            3 -> AdminUserVerificationContent(
                pendingUsers = branchPendingUsers,
                onApprove = { usr ->
                    repository.approveBranchUser(usr.id)
                    Toast.makeText(context, "Akun ${usr.name} diaktifkan!", Toast.LENGTH_SHORT).show()
                },
                onReject = { usr ->
                    repository.rejectBranchUser(usr.id)
                    Toast.makeText(context, "Akun ${usr.name} ditolak.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (showAddCarDialog) {
        AddCarDialog(
            onDismiss = { showAddCarDialog = false },
            onAddCar = { plate, model, cap, km, fuel, type ->
                repository.addVehicle(branchId, plate, model, cap, km, fuel, type)
                showAddCarDialog = false
                Toast.makeText(context, "Armada $model ($plate) berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun AdminBookingApprovalContent(
    bookings: List<BookingRequest>,
    onApprove: (BookingRequest) -> Unit,
    onReject: (BookingRequest) -> Unit,
    onEmergencyOverride: (BookingRequest) -> Unit
) {
    val pendingList = bookings.filter { it.status == BookingStatus.PENDING_APPROVAL }
    val readyList = bookings.filter { it.status == BookingStatus.APPROVED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Persetujuan Izin Dinas Peminjaman Mobil (${pendingList.size} Menunggu)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
        }

        if (pendingList.isEmpty()) {
            item {
                Surface(
                    color = SurfacePureWhite,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tidak ada permohonan antrean peminjaman baru.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        items(pendingList) { bkg ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("pending_booking_card_${bkg.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberWarning)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AmberWarning.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Menunggu Persetujuan Admin Cabang",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = bkg.bookingCode,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NavyPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Keperluan: ${bkg.purpose}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Peminjam: ${bkg.requesterName} (${bkg.requesterRole.title}) • ${bkg.passengerCount} Penumpang",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { onReject(bkg) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed)
                        ) {
                            Text("Tolak")
                        }
                        Button(
                            onClick = { onApprove(bkg) },
                            modifier = Modifier.weight(1.5f).testTag("btn_approve_booking_${bkg.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Setujui Dinas", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Ready For Gate Section (with Emergency Override feature)
        if (readyList.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Telah Disetujui (Siap Menuju Gerbang / Emergency Override):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            }

            items(readyList) { bkg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${bkg.bookingCode} • ${bkg.vehiclePlate}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NavyPrimary
                            )
                            Text(
                                text = "Peminjam: ${bkg.requesterName} • Menuju Pos Gerbang",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { onEmergencyOverride(bkg) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Bypass Gerbang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminFleetContent(
    vehicles: List<CarVehicle>,
    onAddCarClick: () -> Unit,
    onToggleStatus: (CarVehicle, CarStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Armada Cabang (${vehicles.size} Unit)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Button(
                    onClick = onAddCarClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_add_car")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Mobil", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(vehicles) { car ->
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Text(
                            text = car.brandModel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyPrimary
                        )
                        Surface(
                            color = when (car.status) {
                                CarStatus.TERSEDIA -> EmeraldGreen.copy(alpha = 0.15f)
                                CarStatus.DIPAKAI -> Color(0xFF0284C7).copy(alpha = 0.15f)
                                CarStatus.PERAWATAN -> AmberWarning.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = car.status.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (car.status) {
                                    CarStatus.TERSEDIA -> EmeraldGreen
                                    CarStatus.DIPAKAI -> Color(0xFF0284C7)
                                    CarStatus.PERAWATAN -> AmberWarning
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Plat Nomor: ${car.plateNumber} • Kapasitas: ${car.capacity} Penumpang",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("KM: ${car.currentOdometerKm} KM", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("BBM: ${car.fuelPercentage}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val next = if (car.status == CarStatus.PERAWATAN) CarStatus.TERSEDIA else CarStatus.PERAWATAN
                                onToggleStatus(car, next)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (car.status == CarStatus.PERAWATAN) "Selesai Servis" else "Jadwalkan Servis", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminLiveMapTrackingContent(
    branchBookings: List<BookingRequest>,
    trackingState: com.example.service.LiveTrackingState,
    onToggleGeofenceDeviate: (Boolean) -> Unit
) {
    val activeOnDuty = branchBookings.find { it.status == BookingStatus.ON_DUTY }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Pemantauan GPS Real-Time Armada Cabang",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Text(
                text = "Dilengkapi deteksi penyimpangan rute (Geofencing Alert)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            InteractiveMapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                waypoints = activeOnDuty?.waypoints ?: emptyList(),
                vehiclePlate = activeOnDuty?.vehiclePlate ?: "B 1024 SR",
                isTrackingActive = trackingState.isTrackingActive,
                speedKmh = trackingState.speedKmh,
                geofenceAlert = trackingState.geofenceAlert
            )
        }

        item {
            // Geofence Simulation Controls for testing
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Kontrol Simulasi Geofencing & Sinyal:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Uji respons sistem peringatan Admin jika mobil bergerak menyimpang >1 km dari koridor tujuan.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onToggleGeofenceDeviate(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            modifier = Modifier.weight(1f).testTag("btn_trigger_geofence_alert")
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Uji Geofence Alert", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onToggleGeofenceDeviate(false) },
                            modifier = Modifier.weight(1f).testTag("btn_reset_geofence")
                        ) {
                            Text("Kembalikan Rute Normal", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUserVerificationContent(
    pendingUsers: List<AppUser>,
    onApprove: (AppUser) -> Unit,
    onReject: (AppUser) -> Unit
) {
    if (pendingUsers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Semua akun staf dan verifikator telah diverifikasi.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pendingUsers) { user ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("pending_user_card_${user.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberWarning)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyPrimary
                        )
                        Surface(
                            color = AmberWarning.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = user.role.title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Profesi: ${user.professionTitle} • NIP: ${user.nipNik}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Email: ${user.email} • WA: ${user.phoneWhatsapp}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onReject(user) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed)
                        ) {
                            Text("Tolak")
                        }
                        Button(
                            onClick = { onApprove(user) },
                            modifier = Modifier.weight(1.5f).testTag("btn_approve_user_${user.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Setujui Akun", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCarDialog(
    onDismiss: () -> Unit,
    onAddCar: (plate: String, model: String, capacity: Int, initialKm: Int, fuel: Int, type: String) -> Unit
) {
    var plate by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("7") }
    var initialKm by remember { mutableStateOf("30000") }
    var fuel by remember { mutableStateOf("85") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfacePureWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Tambah Armada Mobil Baru", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text("Plat Nomor (Contoh: B 1234 SR)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_car_plate")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Merk & Tipe (Contoh: Toyota Innova)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_car_model")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Kapasitas Penumpang") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = initialKm,
                    onValueChange = { initialKm = it },
                    label = { Text("Odometer Awal (KM)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            if (plate.isNotBlank() && model.isNotBlank()) {
                                onAddCar(
                                    plate.uppercase(),
                                    model,
                                    capacity.toIntOrNull() ?: 7,
                                    initialKm.toIntOrNull() ?: 30000,
                                    fuel.toIntOrNull() ?: 80,
                                    "MPV"
                                )
                            }
                        },
                        enabled = plate.isNotBlank() && model.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.weight(1.5f).testTag("btn_save_car")
                    ) {
                        Text("Simpan Mobil", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
