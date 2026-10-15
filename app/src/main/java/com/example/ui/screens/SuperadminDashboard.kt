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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.BookingStatus
import com.example.model.SchoolBranch
import com.example.model.RouteWaypoint
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
fun SuperadminDashboard(
    repository: DataRepository,
    currentUser: AppUser
) {
    val context = LocalContext.current
    val schools by repository.schools.collectAsState()
    val vehicles by repository.vehicles.collectAsState()
    val bookings by repository.bookings.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Ringkasan", "Antrean SK", "Cabang Resmi", "Audit Log")

    val pendingSchools = schools.filter { !it.isApprovedBySuperadmin }
    val approvedSchools = schools.filter { it.isApprovedBySuperadmin }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Superadmin Header
        Surface(
            color = NavyPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Dashboard Superadmin Nasional",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Pengarah Armada Sekolah Rakyat Seluruh Indonesia",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    Surface(
                        color = EmeraldGreen,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "PUSAT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
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
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            if (index == 1 && pendingSchools.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = AmberWarning,
                                    shape = CircleShape,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${pendingSchools.size}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> NationalOverviewContent(
                approvedSchools = approvedSchools,
                vehicles = vehicles,
                bookings = bookings,
                pendingSchoolsCount = pendingSchools.size
            )
            1 -> PendingSchoolQueueContent(
                pendingSchools = pendingSchools,
                onApprove = { sch ->
                    repository.approveSchool(sch.id)
                    Toast.makeText(context, "${sch.name} disetujui! Akun Admin Cabang aktif.", Toast.LENGTH_SHORT).show()
                },
                onReject = { sch ->
                    repository.rejectSchool(sch.id)
                    Toast.makeText(context, "${sch.name} ditolak.", Toast.LENGTH_SHORT).show()
                }
            )
            2 -> ApprovedBranchesListContent(
                branches = approvedSchools,
                vehicles = vehicles,
                onToggleStatus = { repository.toggleBranchStatus(it.id) }
            )
            3 -> AuditLogContent(auditLogs = auditLogs)
        }
    }
}

@Composable
private fun NationalOverviewContent(
    approvedSchools: List<SchoolBranch>,
    vehicles: List<com.example.model.CarVehicle>,
    bookings: List<com.example.model.BookingRequest>,
    pendingSchoolsCount: Int
) {
    val activeOnDuty = bookings.count { it.status == BookingStatus.ON_DUTY }
    val onDutyBookings = bookings.filter { it.status == BookingStatus.ON_DUTY }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Metrik Nasional Armada",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CardIconMetric(
                    title = "Total Armada",
                    value = "${vehicles.size} Unit",
                    subtitle = "Nasional",
                    icon = Icons.Default.DirectionsCar,
                    iconBgColor = NavyPrimary,
                    modifier = Modifier.weight(1f)
                )
                CardIconMetric(
                    title = "Sedang Beroperasi",
                    value = "$activeOnDuty Unit",
                    subtitle = "Live Tracking",
                    icon = Icons.Default.Assessment,
                    iconBgColor = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CardIconMetric(
                    title = "Cabang Terdaftar",
                    value = "${approvedSchools.size} Cabang",
                    subtitle = "Aktif di Indonesia",
                    icon = Icons.Default.School,
                    iconBgColor = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
                CardIconMetric(
                    title = "Antrean SK Baru",
                    value = "$pendingSchoolsCount Cabang",
                    subtitle = "Perlu Verifikasi",
                    icon = Icons.Default.Description,
                    iconBgColor = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live Interactive Map for Superadmin overview
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pemantauan GPS Live Seluruh Cabang:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            InteractiveMapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                waypoints = if (onDutyBookings.isNotEmpty() && onDutyBookings.first().waypoints.isNotEmpty()) {
                    onDutyBookings.first().waypoints
                } else {
                    listOf(
                        RouteWaypoint("WP-CB1", "Sekolah Rakyat Jakarta Timur", "Pangkalan Armada Utama", -6.2088, 106.8456, isCompleted = true),
                        RouteWaypoint("WP-CB2", "Sekolah Rakyat Bandung", "Jl. Diponegoro No. 22", -6.9000, 107.6186),
                        RouteWaypoint("WP-CB3", "Sekolah Rakyat Surabaya", "Jl. Pemuda No. 45", -7.2600, 112.7400)
                    )
                },
                vehiclePlate = onDutyBookings.firstOrNull()?.vehiclePlate ?: vehicles.firstOrNull()?.plateNumber ?: "B 1024 SR",
                isTrackingActive = activeOnDuty > 0,
                speedKmh = if (activeOnDuty > 0) 42f else 0f,
                geofenceAlert = false
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status Operasional Armada Cabang Saat Ini:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
        }

        if (onDutyBookings.isEmpty()) {
            item {
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
                        Surface(
                            color = Color(0xFF64748B).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Semua Armada Sedang Siaga di Pangkalan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = NavyPrimary
                            )
                            Text(
                                text = "Tidak ada kendaraan dinas yang sedang beroperasi di luar saat ini.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(onDutyBookings) { bkg ->
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
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${bkg.vehiclePlate} • ${bkg.vehicleModel}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NavyPrimary
                            )
                            Text(
                                text = "${bkg.schoolBranchName} | Peminjam: ${bkg.requesterName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tujuan: ${bkg.purpose}",
                                fontSize = 11.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingSchoolQueueContent(
    pendingSchools: List<SchoolBranch>,
    onApprove: (SchoolBranch) -> Unit,
    onReject: (SchoolBranch) -> Unit
) {
    if (pendingSchools.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                    tint = Color(0xFFCBD5E1)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Semua pendaftaran sekolah telah diverifikasi!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
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
        items(pendingSchools) { sch ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("pending_school_card_${sch.id}"),
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
                                text = "Menunggu Persetujuan SK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = sch.registeredDate,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = sch.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "${sch.address}, ${sch.city}, ${sch.province}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Calon Admin: ${sch.adminName} (NIP: ${sch.adminNip})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyPrimary
                            )
                            Text(
                                text = "Email: ${sch.adminEmail} | WA: ${sch.adminWhatsapp}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SK Terlampir: ${sch.skDocumentUrl}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onReject(sch) },
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tolak", fontSize = 12.sp, maxLines = 1)
                        }
                        Button(
                            onClick = { onApprove(sch) },
                            modifier = Modifier.weight(1.3f).testTag("btn_approve_school_${sch.id}"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Setujui", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApprovedBranchesListContent(
    branches: List<SchoolBranch>,
    vehicles: List<com.example.model.CarVehicle>,
    onToggleStatus: (SchoolBranch) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(branches) { branch ->
            val branchFleet = vehicles.filter { it.schoolBranchId == branch.id }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavyPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = branch.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NavyPrimary,
                            lineHeight = 17.sp,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Admin: ${branch.adminName} • Armada: ${branchFleet.size} Unit",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (branch.isActive) "Status: Cabang Aktif" else "Status: Nonaktif",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (branch.isActive) EmeraldGreen else CrimsonRed
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = branch.isActive,
                        onCheckedChange = { onToggleStatus(branch) },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen)
                    )
                }
            }
        }
    }
}

@Composable
private fun AuditLogContent(auditLogs: List<com.example.model.AuditLog>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(auditLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = log.action,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NavyPrimary
                        )
                        Text(
                            text = log.timestamp,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Oleh: ${log.actorName} (${log.actorRole}) - ${log.branchName}",
                        fontSize = 11.sp,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = log.details,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
