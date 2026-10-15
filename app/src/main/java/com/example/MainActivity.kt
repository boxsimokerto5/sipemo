package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.model.AppUser
import com.example.model.Role
import com.example.service.TrackingForegroundService
import com.example.ui.screens.AdminSekolahDashboard
import com.example.ui.screens.LandingAndAuthScreen
import com.example.ui.screens.StaffDashboard
import com.example.ui.screens.SuperadminDashboard
import com.example.ui.screens.VerifikatorDashboard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.SurfaceSubtle

enum class AppScreen {
    AUTH_LANDING,
    DASHBOARD
}

class MainActivity : ComponentActivity() {

    private val repository = DataRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        org.maplibre.android.MapLibre.getInstance(this)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContainer(repository = repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(repository: DataRepository) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) } // start directly in dashboard for quick showcase
    val currentUser by repository.currentUser.collectAsState()
    val schools by repository.schools.collectAsState()
    val users by repository.users.collectAsState()
    val trackingState by TrackingForegroundService.trackingState.collectAsState()

    var showRoleSwitcherSheet by remember { mutableStateOf(false) }

    // Request permissions launcher for Foreground Service & Location
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            com.example.util.LocationProviderManager.initialize(context)
            Toast.makeText(context, "Sinyal GPS aktif mendeteksi titik koordinat Anda", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(perms.toTypedArray())
        com.example.util.LocationProviderManager.initialize(context)
    }

    BackHandler(enabled = currentScreen != AppScreen.AUTH_LANDING) {
        currentScreen = AppScreen.AUTH_LANDING
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            if (currentScreen == AppScreen.DASHBOARD) {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SiPemo",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(currentUser.role.badgeColorHex),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = currentUser.role.title,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = currentUser.schoolBranchName ?: "Pusat Nasional",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        // Tracking Active Pill Indicator
                        if (trackingState.isTrackingActive) {
                            Surface(
                                color = EmeraldGreen,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.GpsFixed,
                                        contentDescription = "GPS Aktif",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("GPS ON", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Fast Switch Role button (bottom sheet)
                        IconButton(
                            onClick = { showRoleSwitcherSheet = true },
                            modifier = Modifier.testTag("btn_open_role_switcher")
                        ) {
                            Icon(
                                Icons.Default.SwapVert,
                                contentDescription = "Ganti Role Demo",
                                tint = Color.White
                            )
                        }

                        // Logout button
                        IconButton(
                            onClick = { currentScreen = AppScreen.AUTH_LANDING },
                            modifier = Modifier.testTag("btn_logout")
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Keluar",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NavyPrimary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            when (currentScreen) {
                AppScreen.AUTH_LANDING -> {
                    LandingAndAuthScreen(
                        repository = repository,
                        approvedSchools = schools,
                        onLoginSuccess = { user ->
                            repository.switchUser(user)
                            currentScreen = AppScreen.DASHBOARD
                        }
                    )
                }
                AppScreen.DASHBOARD -> {
                    when (currentUser.role) {
                        Role.SUPERADMIN -> SuperadminDashboard(
                            repository = repository,
                            currentUser = currentUser
                        )
                        Role.ADMIN_SEKOLAH -> AdminSekolahDashboard(
                            repository = repository,
                            currentUser = currentUser
                        )
                        Role.STAFF -> StaffDashboard(
                            repository = repository,
                            currentUser = currentUser
                        )
                        Role.VERIFIKATOR -> VerifikatorDashboard(
                            repository = repository,
                            currentUser = currentUser
                        )
                    }
                }
            }
        }
    }

    // Role Switcher Modal Bottom Sheet (Slider from bottom)
    if (showRoleSwitcherSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showRoleSwitcherSheet = false },
            sheetState = sheetState,
            containerColor = SurfacePureWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Beralih Peran (Demo RBAC 1-Tap)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Text(
                    text = "Pilih akun di bawah untuk menguji dashboard dan hak akses masing-masing role:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                users.filter { it.isApproved }.forEach { user ->
                    val isSelected = user.id == currentUser.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                repository.switchUser(user)
                                showRoleSwitcherSheet = false
                                Toast.makeText(context, "Beralih ke: ${user.name} (${user.role.title})", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("switch_to_${user.role.name}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) NavyPrimary.copy(alpha = 0.1f) else SurfaceSubtle
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) NavyPrimary else BorderLight
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
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
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = "${user.role.title} • ${user.schoolBranchName ?: "Pusat"}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Surface(
                                    color = NavyPrimary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Aktif",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
