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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.model.GuardMode
import com.example.model.Role
import com.example.ui.components.BottomSheetType
import com.example.ui.components.CardIconMetric
import com.example.ui.components.GoogleMapBottomSheet
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.SurfaceSubtle

@Composable
fun VerifikatorDashboard(
    repository: DataRepository,
    currentUser: AppUser
) {
    val context = LocalContext.current
    val guardMode by repository.guardMode.collectAsState()
    val bookings by repository.bookings.collectAsState()
    val vehicles by repository.vehicles.collectAsState()

    val branchId = currentUser.schoolBranchId ?: "SCH-001"
    val branchBookings = bookings.filter { it.schoolBranchId == branchId }

    // If switched to MODE_PEMINJAM, render the Staff booking interface!
    if (guardMode == GuardMode.MODE_PEMINJAM) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Mode Switcher Banner
            GuardModeHeaderBanner(
                guardMode = guardMode,
                currentUser = currentUser,
                onToggleMode = { repository.toggleGuardMode() }
            )
            // Render Staff Dashboard view
            StaffDashboard(
                repository = repository,
                currentUser = currentUser
            )
        }
        return
    }

    // Default: MODE_VERIFIKATOR (Pos Gerbang)
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Antrean Keluar, 1: Antrean Kembali, 2: Cari Kode / Scan
    val tabs = listOf("Izin Keluar Gerbang", "Mobil Kembali (Check-in)", "Cari Kode / Scan")

    val readyForExit = branchBookings.filter { it.status == BookingStatus.APPROVED }
    val onDutyReturning = branchBookings.filter { it.status == BookingStatus.ON_DUTY }

    var selectedBookingForInspection by remember { mutableStateOf<BookingRequest?>(null) }
    var showInspectionSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Mode Switcher Header
        GuardModeHeaderBanner(
            guardMode = guardMode,
            currentUser = currentUser,
            onToggleMode = { repository.toggleGuardMode() }
        )

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
                val badgeCount = when (index) {
                    0 -> readyForExit.size
                    1 -> onDutyReturning.size
                    else -> 0
                }
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            if (badgeCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = if (index == 0) EmeraldGreen else Color(0xFF0284C7),
                                    shape = CircleShape,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$badgeCount", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> GateExitQueueContent(
                bookings = readyForExit,
                currentUser = currentUser,
                onInspect = { bkg ->
                    selectedBookingForInspection = bkg
                    showInspectionSheet = true
                }
            )
            1 -> GateReturnQueueContent(
                bookings = onDutyReturning,
                currentUser = currentUser,
                onInspect = { bkg ->
                    selectedBookingForInspection = bkg
                    showInspectionSheet = true
                }
            )
            2 -> GateSearchCodeContent(
                branchBookings = branchBookings,
                onSelectBooking = { bkg ->
                    selectedBookingForInspection = bkg
                    showInspectionSheet = true
                }
            )
        }
    }

    // Google Maps Slider Bottom Sheet for physical verification & KM/Fuel input
    if (showInspectionSheet && selectedBookingForInspection != null) {
        val car = vehicles.find { it.id == selectedBookingForInspection?.vehicleId }
        GoogleMapBottomSheet(
            sheetType = BottomSheetType.GATE_INSPECTION,
            booking = selectedBookingForInspection,
            vehicle = car,
            currentUser = currentUser,
            onDismiss = { showInspectionSheet = false },
            onConfirmCheckout = { km, fuel ->
                val success = repository.checkoutAtGate(context, selectedBookingForInspection!!.id, km, fuel, currentUser)
                showInspectionSheet = false
                if (success) {
                    Toast.makeText(context, "Mobil Diizinkan Keluar! Foreground Service & GPS Aktif.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Pencegahan Self-Approval: Anda tidak boleh menyetujui peminjaman sendiri!", Toast.LENGTH_LONG).show()
                }
            },
            onConfirmCheckin = { km, fuel ->
                val success = repository.checkinAtGate(context, selectedBookingForInspection!!.id, km, fuel, currentUser)
                showInspectionSheet = false
                if (success) {
                    Toast.makeText(context, "Mobil Berhasil Kembali! GPS Tracking Dimatikan.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Pencegahan Self-Approval: Harus diverifikasi rekan petugas lain!", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

@Composable
private fun GuardModeHeaderBanner(
    guardMode: GuardMode,
    currentUser: AppUser,
    onToggleMode: () -> Unit
) {
    Surface(
        color = NavyPrimary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentUser.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (guardMode == GuardMode.MODE_VERIFIKATOR) "Mode Aktif: POS GERBANG UTAMA" else "Mode Aktif: PEMINJAM MOBIL DINAS",
                    fontSize = 11.sp,
                    color = if (guardMode == GuardMode.MODE_VERIFIKATOR) EmeraldGreen else AmberWarning,
                    fontWeight = FontWeight.Bold
                )
            }

            // Switch Mode Button / Toggle
            Surface(
                color = if (guardMode == GuardMode.MODE_VERIFIKATOR) EmeraldGreen else AmberWarning,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onToggleMode() }
                    .testTag("btn_switch_guard_mode")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = "Beralih Peran",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (guardMode == GuardMode.MODE_VERIFIKATOR) "Ubah ke Peminjam" else "Ubah ke Pos Gerbang",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun GateExitQueueContent(
    bookings: List<BookingRequest>,
    currentUser: AppUser,
    onInspect: (BookingRequest) -> Unit
) {
    if (bookings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tidak ada antrean mobil yang siap keluar gerbang.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        item {
            Text(
                text = "Antrean Pemeriksaan Fisik & Izin Keluar Gerbang:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Text(
                text = "Tekan 'Periksa & Izinkan Keluar' untuk membuka slider Google Map & input KM",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(bookings) { bkg ->
            val isSelf = bkg.requesterId == currentUser.id

            Card(
                modifier = Modifier.fillMaxWidth().testTag("exit_card_${bkg.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelf) CrimsonRed else BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = EmeraldGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Disetujui Admin Cabang",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${bkg.vehicleModel} (${bkg.vehiclePlate})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Pengemudi/Peminjam: ${bkg.requesterName} (${bkg.passengerCount} Org)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tujuan: ${bkg.purpose}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // ATURAN PENCEGAHAN SELF-APPROVAL WARNING:
                    if (isSelf) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = CrimsonRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pencegahan Self-Approval: Ini peminjaman Anda sendiri! Verifikasi harus dilakukan rekan satpam lain.",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onInspect(bkg) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSelf) Color.Gray else EmeraldGreen),
                        modifier = Modifier.fillMaxWidth().testTag("btn_inspect_exit_${bkg.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSelf) "Lihat Detail (Terkunci)" else "Periksa Fisik & Izinkan Keluar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GateReturnQueueContent(
    bookings: List<BookingRequest>,
    currentUser: AppUser,
    onInspect: (BookingRequest) -> Unit
) {
    if (bookings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tidak ada mobil yang sedang On Duty di luar gerbang.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        item {
            Text(
                text = "Mobil Sedang On Duty di Luar Gerbang (${bookings.size} Unit):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Text(
                text = "Saat mobil tiba kembali di sekolah, verifikasi KM akhir & checklist fisik",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(bookings) { bkg ->
            val isSelf = bkg.requesterId == currentUser.id

            Card(
                modifier = Modifier.fillMaxWidth().testTag("return_card_${bkg.id}"),
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
                            color = Color(0xFF0284C7).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ON DUTY (Tracking GPS Aktif)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${bkg.vehicleModel} (${bkg.vehiclePlate})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Pengemudi: ${bkg.requesterName} • Berangkat: ${bkg.checkoutTimestamp ?: "-"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "KM Awal: ${bkg.checkoutOdometerKm ?: 0} KM • BBM Awal: ${bkg.checkoutFuelPercent ?: 0}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onInspect(bkg) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("btn_inspect_return_${bkg.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Konfirmasi Mobil Kembali (Check-in)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GateSearchCodeContent(
    branchBookings: List<BookingRequest>,
    onSelectBooking: (BookingRequest) -> Unit
) {
    var searchCode by remember { mutableStateOf("") }
    val filtered = branchBookings.filter {
        it.bookingCode.contains(searchCode.trim(), ignoreCase = true) ||
        it.vehiclePlate.contains(searchCode.trim(), ignoreCase = true) ||
        it.requesterName.contains(searchCode.trim(), ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchCode,
            onValueChange = { searchCode = it },
            label = { Text("Ketik Kode Booking / Plat Nomor / Nama") },
            leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = NavyPrimary) },
            modifier = Modifier.fillMaxWidth().testTag("search_booking_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { bkg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectBooking(bkg) },
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
                                text = "Peminjam: ${bkg.requesterName} • Status: ${bkg.status.label}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = NavyPrimary)
                    }
                }
            }
        }
    }
}
