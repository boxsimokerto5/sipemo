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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import com.example.model.Role
import com.example.model.SchoolBranch
import com.example.ui.components.ProgressBarStepper
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.SurfaceSubtle

@Composable
fun LandingAndAuthScreen(
    repository: DataRepository,
    approvedSchools: List<SchoolBranch>,
    onLoginSuccess: (AppUser) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Daftar Sekolah Baru, 2: Daftar Akun Pengguna

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 36.dp)
    ) {
        // App Header Banner
        Surface(
            color = NavyPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(EmeraldGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "SiPemo Sekolah Rakyat",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Sistem Peminjaman Armada & Live GPS Tracking Nasional",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Navigation Tabs (Login, Daftar Sekolah, Daftar Pengguna)
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
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Masuk", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Daftar Sekolah", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Daftar Pengguna", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            when (selectedTab) {
                0 -> LoginTabContent(repository = repository, onLoginSuccess = onLoginSuccess)
                1 -> RegisterSchoolTabContent(
                    repository = repository,
                    onRegistered = {
                        Toast.makeText(context, "Pendaftaran Sekolah diajukan! Menunggu verifikasi Superadmin.", Toast.LENGTH_LONG).show()
                        selectedTab = 0
                    }
                )
                2 -> RegisterUserTabContent(
                    repository = repository,
                    approvedSchools = approvedSchools,
                    onRegistered = {
                        Toast.makeText(context, "Pendaftaran Akun diajukan! Menunggu verifikasi Admin Cabang.", Toast.LENGTH_LONG).show()
                        selectedTab = 0
                    }
                )
            }
        }
    }
}

@Composable
private fun LoginTabContent(
    repository: DataRepository,
    onLoginSuccess: (AppUser) -> Unit
) {
    var emailInput by remember { mutableStateOf("superadmin@sekolahrakyat.id") }
    var passwordInput by remember { mutableStateOf("••••••••") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Masuk Akun",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Text(
                text = "Pilih akun simulasi di bawah atau masukkan email akun Anda",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Email Operasional") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NavyPrimary) },
                modifier = Modifier.fillMaxWidth().testTag("login_email_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("Kata Sandi") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary) },
                modifier = Modifier.fillMaxWidth().testTag("login_password_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val user = repository.users.value.find { it.email.equals(emailInput.trim(), ignoreCase = true) }
                        ?: repository.users.value.first()
                    onLoginSuccess(user)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_login_submit"),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Masuk ke Sistem", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DEMO FAST SWITCHER - Card Icon Mode
            Text(
                text = "Akses Cepat Pengujian Role (Demo 1-Tap):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val demoUsers = repository.users.value.filter { it.isApproved }
            demoUsers.forEach { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            emailInput = user.email
                            onLoginSuccess(user)
                        }
                        .testTag("demo_role_${user.role.name}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(user.role.badgeColorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when (user.role) {
                                    Role.SUPERADMIN -> Icons.Default.School
                                    Role.ADMIN_SEKOLAH -> Icons.Default.Business
                                    Role.STAFF -> Icons.Default.DirectionsCar
                                    Role.VERIFIKATOR -> Icons.Default.Security
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = "${user.role.title} • ${user.schoolBranchName ?: "Pusat"}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterSchoolTabContent(
    repository: DataRepository,
    onRegistered: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val steps = listOf("Info Sekolah", "Calon Admin", "Unggah SK")

    // Form states
    var schoolName by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("Jawa Timur") }
    var city by remember { mutableStateOf("Kota Surabaya") }
    var address by remember { mutableStateOf("") }
    var schoolPhone by remember { mutableStateOf("") }

    var adminName by remember { mutableStateOf("") }
    var adminNip by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var adminWhatsapp by remember { mutableStateOf("") }

    var skFileName by remember { mutableStateOf("SK-OPERASIONAL-RESMI-2026.pdf") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Pendaftaran Sekolah Rakyat Baru",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Text(
                text = "Khusus calon Admin Cabang. Wajib diverifikasi Superadmin.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Step Progress Bar
            ProgressBarStepper(currentStep = step, steps = steps)

            Spacer(modifier = Modifier.height(18.dp))

            when (step) {
                0 -> {
                    // Step 1: Info Sekolah
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("Nama Unit Sekolah Rakyat") },
                        placeholder = { Text("Contoh: Sekolah Rakyat Cabang Malang") },
                        modifier = Modifier.fillMaxWidth().testTag("reg_school_name")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = province,
                        onValueChange = { province = it },
                        label = { Text("Provinsi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Kabupaten / Kota") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat Lengkap Sekolah") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = schoolPhone,
                        onValueChange = { schoolPhone = it },
                        label = { Text("Nomor Kontak Dinas Sekolah") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { step = 1 },
                        enabled = schoolName.isNotBlank() && address.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Lanjut ke Data Calon Admin")
                    }
                }
                1 -> {
                    // Step 2: Calon Admin
                    OutlinedTextField(
                        value = adminName,
                        onValueChange = { adminName = it },
                        label = { Text("Nama Lengkap Calon Admin") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = adminNip,
                        onValueChange = { adminNip = it },
                        label = { Text("NIP / NIK Pegawai") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = adminEmail,
                        onValueChange = { adminEmail = it },
                        label = { Text("Email Operasional Admin") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = adminWhatsapp,
                        onValueChange = { adminWhatsapp = it },
                        label = { Text("Nomor WhatsApp Aktif") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { step = 0 }, modifier = Modifier.weight(1f)) {
                            Text("Kembali")
                        }
                        Button(
                            onClick = { step = 2 },
                            enabled = adminName.isNotBlank() && adminEmail.isNotBlank(),
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Text("Lanjut Unggah Dokumen")
                        }
                    }
                }
                2 -> {
                    // Step 3: Unggah SK
                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Surat Keputusan (SK) Operasional Resmi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = "Dokumen resmi penunjukan unit Sekolah Rakyat (Format PDF/Gambar)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = EmeraldGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Terpilih: $skFileName",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { step = 1 }, modifier = Modifier.weight(1f)) {
                            Text("Kembali")
                        }
                        Button(
                            onClick = {
                                repository.registerNewSchool(
                                    name = schoolName,
                                    province = province,
                                    city = city,
                                    address = address,
                                    phone = schoolPhone,
                                    adminName = adminName,
                                    adminNip = adminNip,
                                    adminEmail = adminEmail,
                                    adminWhatsapp = adminWhatsapp,
                                    skFileName = skFileName
                                )
                                onRegistered()
                            },
                            modifier = Modifier.weight(1.5f).testTag("btn_submit_school_reg"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Kirim Pendaftaran", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterUserTabContent(
    repository: DataRepository,
    approvedSchools: List<SchoolBranch>,
    onRegistered: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val steps = listOf("Pilih Sekolah", "Data Diri & Role")

    // ATURAN KHUSUS: Dropdown HANYA MENAMPILKAN sekolah yang SUDAH DISETUJUI Superadmin!
    val activeApprovedSchools = approvedSchools.filter { it.isApprovedBySuperadmin }

    var selectedSchool by remember { mutableStateOf(activeApprovedSchools.firstOrNull()) }
    var expandedSchoolDropdown by remember { mutableStateOf(false) }

    var selectedRole by remember { mutableStateOf(Role.STAFF) }
    var nameInput by remember { mutableStateOf("") }
    var nipInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var whatsappInput by remember { mutableStateOf("") }
    var professionInput by remember { mutableStateOf("Guru Matematika") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Pendaftaran Akun Pengguna Cabang",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Text(
                text = "Untuk Staff Peminjam & Verifikator/Petugas Keamanan",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProgressBarStepper(currentStep = step, steps = steps)

            Spacer(modifier = Modifier.height(18.dp))

            when (step) {
                0 -> {
                    // Step 1: Pilih Sekolah dari dropdown terfilter
                    Text(
                        text = "Pilih Sekolah Rakyat Penugasan:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Catatan: Menu ini HANYA menampilkan sekolah resmi yang sudah disetujui Superadmin Pusat.",
                        fontSize = 11.sp,
                        color = EmeraldGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedSchoolDropdown,
                        onExpandedChange = { expandedSchoolDropdown = !expandedSchoolDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedSchool?.name ?: "Pilih Sekolah",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSchoolDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth().testTag("dropdown_school_select")
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSchoolDropdown,
                            onDismissRequest = { expandedSchoolDropdown = false }
                        ) {
                            activeApprovedSchools.forEach { sch ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(sch.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${sch.city}, ${sch.province}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        selectedSchool = sch
                                        expandedSchoolDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { step = 1 },
                        enabled = selectedSchool != null,
                        modifier = Modifier.fillMaxWidth().testTag("btn_next_user_reg"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Lanjut ke Profil & Role")
                    }
                }
                1 -> {
                    // Step 2: Role & Data Diri
                    Text(
                        text = "Pilih Peran Akun:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Staff Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedRole = Role.STAFF },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedRole == Role.STAFF) EmeraldGreen.copy(alpha = 0.15f) else SurfaceSubtle
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (selectedRole == Role.STAFF) EmeraldGreen else Color.Transparent
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Staff Peminjam", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                                Text("Guru, Asrama, Dapur", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Verifikator Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedRole = Role.VERIFIKATOR },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedRole == Role.VERIFIKATOR) AmberWarning.copy(alpha = 0.15f) else SurfaceSubtle
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (selectedRole == Role.VERIFIKATOR) AmberWarning else Color.Transparent
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Verifikator Gerbang", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                                Text("Petugas Keamanan / Satpam", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth().testTag("reg_user_name")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nipInput,
                        onValueChange = { nipInput = it },
                        label = { Text("NIP / NIK") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth().testTag("reg_user_email")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = whatsappInput,
                        onValueChange = { whatsappInput = it },
                        label = { Text("Nomor WhatsApp") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { step = 0 }, modifier = Modifier.weight(1f)) {
                            Text("Kembali")
                        }
                        Button(
                            onClick = {
                                selectedSchool?.let { sch ->
                                    repository.registerBranchUser(
                                        schoolBranchId = sch.id,
                                        role = selectedRole,
                                        name = nameInput,
                                        nipNik = nipInput,
                                        email = emailInput,
                                        whatsapp = whatsappInput,
                                        profession = professionInput
                                    )
                                    onRegistered()
                                }
                            },
                            enabled = nameInput.isNotBlank() && emailInput.isNotBlank(),
                            modifier = Modifier.weight(1.5f).testTag("btn_submit_user_reg"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Daftar Akun", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
