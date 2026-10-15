package com.example.data

import android.content.Context
import com.example.model.*
import com.example.service.TrackingForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DataRepository {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    // Preloaded Schools
    private val initialSchools = listOf(
        SchoolBranch(
            id = "SCH-001",
            name = "Sekolah Rakyat Cabang Jakarta Timur",
            province = "DKI Jakarta",
            city = "Jakarta Timur",
            address = "Jl. Raya Penggilingan No. 45, Cakung",
            phone = "021-4892301",
            adminName = "Hendra Setiawan, M.Pd",
            adminNip = "198504122009121003",
            adminEmail = "admin.jaktim@sekolahrakyat.id",
            adminWhatsapp = "081298457891",
            skDocumentUrl = "SK-MENKO-SR-JKT-2024.pdf",
            isApprovedBySuperadmin = true,
            isActive = true
        ),
        SchoolBranch(
            id = "SCH-002",
            name = "Sekolah Rakyat Cabang Bandung",
            province = "Jawa Barat",
            city = "Kota Bandung",
            address = "Jl. Dago Asri No. 12, Coblong",
            phone = "022-2501234",
            adminName = "Dra. Siti Rahmawati",
            adminNip = "198301152008012004",
            adminEmail = "admin.bdg@sekolahrakyat.id",
            adminWhatsapp = "081321456789",
            skDocumentUrl = "SK-MENKO-SR-BDG-2024.pdf",
            isApprovedBySuperadmin = true,
            isActive = true
        ),
        SchoolBranch(
            id = "SCH-003",
            name = "Sekolah Rakyat Cabang Medan Baru (Pengajuan Baru)",
            province = "Sumatera Utara",
            city = "Kota Medan",
            address = "Jl. Iskandar Muda No. 88, Medan Petisah",
            phone = "061-4567890",
            adminName = "Bambang Sudiro, S.Pd",
            adminNip = "198906232014021001",
            adminEmail = "admin.medan@sekolahrakyat.id",
            adminWhatsapp = "081165432109",
            skDocumentUrl = "SK-GUB-SUMUT-SR-MEDAN-089.pdf",
            isApprovedBySuperadmin = false, // Menunggu persetujuan Superadmin!
            isActive = true
        )
    )

    // Preloaded Users
    private val initialUsers = listOf(
        AppUser(
            id = "USR-SUPERADMIN",
            name = "Dr. H. Ahmad Marzuki, M.Ed",
            nipNik = "197508191999031001",
            email = "superadmin@sekolahrakyat.id",
            phoneWhatsapp = "08111223344",
            role = Role.SUPERADMIN,
            schoolBranchId = null,
            schoolBranchName = "Kantor Pusat Nasional (Kemendikbudristek)",
            professionTitle = "Kepala Pengarah Nasional"
        ),
        AppUser(
            id = "USR-ADMIN-JAKTIM",
            name = "Hendra Setiawan, M.Pd",
            nipNik = "198504122009121003",
            email = "admin.jaktim@sekolahrakyat.id",
            phoneWhatsapp = "081298457891",
            role = Role.ADMIN_SEKOLAH,
            schoolBranchId = "SCH-001",
            schoolBranchName = "Sekolah Rakyat Cabang Jakarta Timur",
            professionTitle = "Kepala Operasional Cabang"
        ),
        AppUser(
            id = "USR-STAFF-BUDI",
            name = "Budi Wicaksono, S.Pd",
            nipNik = "199203142018011002",
            email = "budi.guru@sekolahrakyat.id",
            phoneWhatsapp = "085698712345",
            role = Role.STAFF,
            schoolBranchId = "SCH-001",
            schoolBranchName = "Sekolah Rakyat Cabang Jakarta Timur",
            professionTitle = "Koordinator Asrama & Kesiswaan"
        ),
        AppUser(
            id = "USR-GUARD-SUKIRNO",
            name = "Pak Sukirno",
            nipNik = "3175082104800005",
            email = "sukirno.security@sekolahrakyat.id",
            phoneWhatsapp = "087811223344",
            role = Role.VERIFIKATOR,
            schoolBranchId = "SCH-001",
            schoolBranchName = "Sekolah Rakyat Cabang Jakarta Timur",
            professionTitle = "Komandan Regu Keamanan Gerbang"
        ),
        // Pending approval user at Jakarta branch
        AppUser(
            id = "USR-PENDING-1",
            name = "Rina Marlina, S.Pd",
            nipNik = "199511052020122008",
            email = "rina.guru@sekolahrakyat.id",
            phoneWhatsapp = "081233445566",
            role = Role.STAFF,
            schoolBranchId = "SCH-001",
            schoolBranchName = "Sekolah Rakyat Cabang Jakarta Timur",
            isApproved = false,
            professionTitle = "Guru Matematika & Wali Asuh"
        )
    )

    // Preloaded Vehicles
    private val initialVehicles = listOf(
        CarVehicle(
            id = "VEH-001",
            schoolBranchId = "SCH-001",
            plateNumber = "B 1024 SR",
            brandModel = "Toyota Kijang Innova Zenix 2.0 V",
            capacity = 7,
            status = CarStatus.DIPAKAI, // Sedang aktif On Duty
            currentOdometerKm = 42150,
            fuelPercentage = 78,
            lastServiceDate = "01 Okt 2026",
            transmission = "Automatic",
            vehicleIconType = "MPV"
        ),
        CarVehicle(
            id = "VEH-002",
            schoolBranchId = "SCH-001",
            plateNumber = "B 1892 SR",
            brandModel = "Toyota Avanza Veloz 1.5",
            capacity = 7,
            status = CarStatus.TERSEDIA,
            currentOdometerKm = 28400,
            fuelPercentage = 90,
            lastServiceDate = "15 Sep 2026",
            transmission = "Manual",
            vehicleIconType = "MPV"
        ),
        CarVehicle(
            id = "VEH-003",
            schoolBranchId = "SCH-001",
            plateNumber = "B 7781 SR",
            brandModel = "Toyota HiAce Commuter 2.5",
            capacity = 16,
            status = CarStatus.TERSEDIA,
            currentOdometerKm = 59300,
            fuelPercentage = 85,
            lastServiceDate = "20 Agu 2026",
            transmission = "Manual",
            vehicleIconType = "Minibus"
        ),
        CarVehicle(
            id = "VEH-004",
            schoolBranchId = "SCH-001",
            plateNumber = "B 2411 SR",
            brandModel = "Daihatsu Gran Max Blind Van Operasional",
            capacity = 3,
            status = CarStatus.PERAWATAN,
            currentOdometerKm = 64200,
            fuelPercentage = 40,
            lastServiceDate = "10 Okt 2026 (Sedang Ganti Oli & Rem)",
            transmission = "Manual",
            vehicleIconType = "Van"
        )
    )

    // Preloaded Bookings
    private val initialBookings = listOf(
        BookingRequest(
            id = "BKG-2026-001",
            bookingCode = "SR-JKT-8821",
            schoolBranchId = "SCH-001",
            schoolBranchName = "Sekolah Rakyat Cabang Jakarta Timur",
            vehicleId = "VEH-001",
            vehiclePlate = "B 1024 SR",
            vehicleModel = "Toyota Innova Zenix",
            requesterId = "USR-STAFF-BUDI",
            requesterName = "Budi Wicaksono, S.Pd",
            requesterRole = Role.STAFF,
            requesterWhatsapp = "085698712345",
            passengerCount = 4,
            purpose = "Pemeriksaan Kesehatan Berkala Siswa Asrama ke RSUD & Koordinasi Dinkes",
            departureDate = "14 Okt 2026",
            departureTime = "08:30 WIB",
            estimatedReturnTime = "14:00 WIB",
            waypoints = listOf(
                RouteWaypoint("WP-0", "Titik Asal: Sekolah Rakyat Jaktim", "Jl. Raya Penggilingan No. 45", -6.2088, 106.8456, isCompleted = true),
                RouteWaypoint("WP-1", "Tujuan 1: RSUD Duren Sawit", "Jl. Raden Inten II No. 1", -6.2200, 106.8560, isCompleted = true),
                RouteWaypoint("WP-2", "Tujuan 2: Kantor Suku Dinas Kesehatan", "Jl. Matraman Raya No. 220", -6.2300, 106.8650, isCompleted = false)
            ),
            status = BookingStatus.ON_DUTY,
            isEmergency = false,
            checkoutOdometerKm = 42120,
            checkoutFuelPercent = 85,
            verifiedByGuardId = "USR-GUARD-SUKIRNO",
            verifiedByGuardName = "Pak Sukirno",
            checkoutTimestamp = "14 Okt 2026, 08:35 WIB"
        ),
        BookingRequest(
            id = "BKG-2026-002",
            bookingCode = "SR-JKT-8822",
            schoolBranchId = "SCH-001",
            schoolBranchName = "Sekolah Rakyat Cabang Jakarta Timur",
            vehicleId = "VEH-002",
            vehiclePlate = "B 1892 SR",
            vehicleModel = "Toyota Avanza Veloz",
            requesterId = "USR-GUARD-SUKIRNO",
            requesterName = "Pak Sukirno (Dalam Mode Peminjam)",
            requesterRole = Role.VERIFIKATOR,
            requesterWhatsapp = "087811223344",
            passengerCount = 2,
            purpose = "Pengambilan Logistik Beras & Pangan Asrama dari Gudang Bulog",
            departureDate = "14 Okt 2026",
            departureTime = "14:30 WIB",
            estimatedReturnTime = "17:00 WIB",
            waypoints = listOf(
                RouteWaypoint("WP-0", "Titik Asal: Sekolah Rakyat", "Jl. Raya Penggilingan", -6.2088, 106.8456),
                RouteWaypoint("WP-1", "Tujuan 1: Gudang Bulog Divre DKI", "Jl. Perintis Kemerdekaan", -6.1800, 106.8900)
            ),
            status = BookingStatus.APPROVED, // Siap diverifikasi di gerbang (Pencegahan Self-Approval akan diuji di sini!)
            isEmergency = false
        ),
        BookingRequest(
            id = "BKG-2026-003",
            bookingCode = "SR-JKT-8823",
            schoolBranchId = "SCH-001",
            schoolBranchName = "Sekolah Rakyat Cabang Jakarta Timur",
            vehicleId = "VEH-003",
            vehiclePlate = "B 7781 SR",
            vehicleModel = "Toyota HiAce Commuter",
            requesterId = "USR-STAFF-BUDI",
            requesterName = "Budi Wicaksono, S.Pd",
            requesterRole = Role.STAFF,
            requesterWhatsapp = "085698712345",
            passengerCount = 14,
            purpose = "Antar Kontingen Siswa Olimpiade Sains Sekolah Rakyat ke Balai Kota",
            departureDate = "15 Okt 2026",
            departureTime = "07:00 WIB",
            estimatedReturnTime = "16:00 WIB",
            waypoints = listOf(
                RouteWaypoint("WP-0", "Titik Asal: Sekolah Rakyat", "Jl. Raya Penggilingan", -6.2088, 106.8456),
                RouteWaypoint("WP-1", "Tujuan 1: Balai Kota DKI Jakarta", "Jl. Medan Merdeka Selatan No. 8", -6.1815, 106.8285)
            ),
            status = BookingStatus.PENDING_APPROVAL,
            isEmergency = false
        )
    )

    private val initialAuditLogs = listOf(
        AuditLog("LOG-1", "14 Okt 2026, 08:35", "Pak Sukirno", "Verifikator", "Jakarta Timur", "Check-out Gerbang", "Izin keluar diberikan untuk mobil B 1024 SR. Tracking GPS otomatis aktif."),
        AuditLog("LOG-2", "14 Okt 2026, 07:15", "Hendra Setiawan", "Admin Cabang", "Jakarta Timur", "Approval Peminjaman", "Menyetujui peminjaman SR-JKT-8822 tujuan Gudang Bulog."),
        AuditLog("LOG-3", "13 Okt 2026, 16:00", "Dr. H. Ahmad Marzuki", "Superadmin", "Pusat", "Verifikasi Cabang", "Menyetujui pendaftaran Sekolah Rakyat Cabang Bandung.")
    )

    // Reactive StateFlows
    private val _currentUser = MutableStateFlow(initialUsers[0]) // Default: Superadmin
    val currentUser: StateFlow<AppUser> = _currentUser.asStateFlow()

    private val _guardMode = MutableStateFlow(GuardMode.MODE_VERIFIKATOR)
    val guardMode: StateFlow<GuardMode> = _guardMode.asStateFlow()

    private val _schools = MutableStateFlow(initialSchools)
    val schools: StateFlow<List<SchoolBranch>> = _schools.asStateFlow()

    private val _users = MutableStateFlow(initialUsers)
    val users: StateFlow<List<AppUser>> = _users.asStateFlow()

    private val _vehicles = MutableStateFlow(initialVehicles)
    val vehicles: StateFlow<List<CarVehicle>> = _vehicles.asStateFlow()

    private val _bookings = MutableStateFlow(initialBookings)
    val bookings: StateFlow<List<BookingRequest>> = _bookings.asStateFlow()

    private val _auditLogs = MutableStateFlow(initialAuditLogs)
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    // --- Switch User / Role for Demo & Testing ---
    fun switchUser(user: AppUser) {
        _currentUser.value = user
        if (user.role == Role.VERIFIKATOR) {
            _guardMode.value = GuardMode.MODE_VERIFIKATOR
        }
    }

    fun toggleGuardMode() {
        _guardMode.value = if (_guardMode.value == GuardMode.MODE_VERIFIKATOR) {
            GuardMode.MODE_PEMINJAM
        } else {
            GuardMode.MODE_VERIFIKATOR
        }
    }

    // --- Superadmin Operations ---
    fun approveSchool(schoolId: String) {
        val list = _schools.value.toMutableList()
        val index = list.indexOfFirst { it.id == schoolId }
        if (index != -1) {
            val updated = list[index].copy(isApprovedBySuperadmin = true)
            list[index] = updated
            _schools.value = list

            // Automatically create Admin Cabang account for this school
            val adminUser = AppUser(
                id = "USR-ADM-${UUID.randomUUID().toString().take(6)}",
                name = updated.adminName,
                nipNik = updated.adminNip,
                email = updated.adminEmail,
                phoneWhatsapp = updated.adminWhatsapp,
                role = Role.ADMIN_SEKOLAH,
                schoolBranchId = updated.id,
                schoolBranchName = updated.name,
                professionTitle = "Kepala Operasional Cabang"
            )
            _users.value = _users.value + adminUser

            addAuditLog("Superadmin", "Persetujuan Sekolah", "Menyetujui pendaftaran ${updated.name}. Akun Admin Cabang otomatis diterbitkan.")
        }
    }

    fun rejectSchool(schoolId: String) {
        val list = _schools.value.toMutableList()
        val target = list.find { it.id == schoolId }
        if (target != null) {
            list.removeAll { it.id == schoolId }
            _schools.value = list
            addAuditLog("Superadmin", "Penolakan Sekolah", "Menolak pengajuan pendaftaran ${target.name}.")
        }
    }

    fun toggleBranchStatus(schoolId: String) {
        val list = _schools.value.toMutableList()
        val index = list.indexOfFirst { it.id == schoolId }
        if (index != -1) {
            val current = list[index]
            val updated = current.copy(isActive = !current.isActive)
            list[index] = updated
            _schools.value = list
            addAuditLog("Superadmin", "Ubah Status Cabang", "${updated.name} diubah menjadi ${if (updated.isActive) "Aktif" else "Nonaktif"}.")
        }
    }

    // --- School Registration (Calon Admin) ---
    fun registerNewSchool(
        name: String,
        province: String,
        city: String,
        address: String,
        phone: String,
        adminName: String,
        adminNip: String,
        adminEmail: String,
        adminWhatsapp: String,
        skFileName: String
    ): SchoolBranch {
        val newSchool = SchoolBranch(
            id = "SCH-${UUID.randomUUID().toString().take(6).uppercase()}",
            name = name,
            province = province,
            city = city,
            address = address,
            phone = phone,
            adminName = adminName,
            adminNip = adminNip,
            adminEmail = adminEmail,
            adminWhatsapp = adminWhatsapp,
            skDocumentUrl = skFileName,
            isApprovedBySuperadmin = false, // Menunggu review Superadmin
            isActive = true
        )
        _schools.value = _schools.value + newSchool
        addAuditLog(adminName, "Pendaftaran Sekolah Baru", "Mengajukan pendaftaran unit baru: $name.")
        return newSchool
    }

    // --- Branch User Registration (Staff & Verifikator) ---
    fun registerBranchUser(
        schoolBranchId: String,
        role: Role,
        name: String,
        nipNik: String,
        email: String,
        whatsapp: String,
        profession: String
    ): AppUser {
        val school = _schools.value.find { it.id == schoolBranchId }
        val newUser = AppUser(
            id = "USR-${UUID.randomUUID().toString().take(6).uppercase()}",
            name = name,
            nipNik = nipNik,
            email = email,
            phoneWhatsapp = whatsapp,
            role = role,
            schoolBranchId = schoolBranchId,
            schoolBranchName = school?.name ?: "Sekolah Rakyat",
            isApproved = false, // Menunggu persetujuan Admin Cabang
            professionTitle = profession
        )
        _users.value = _users.value + newUser
        addAuditLog(name, "Pendaftaran Akun Cabang", "Mendaftar sebagai ${role.title} di ${school?.name ?: ""}.")
        return newUser
    }

    fun approveBranchUser(userId: String) {
        val list = _users.value.toMutableList()
        val index = list.indexOfFirst { it.id == userId }
        if (index != -1) {
            val user = list[index].copy(isApproved = true)
            list[index] = user
            _users.value = list
            addAuditLog(_currentUser.value.name, "Persetujuan Akun", "Menyetujui akun ${user.name} (${user.role.title}).")
        }
    }

    fun rejectBranchUser(userId: String) {
        val list = _users.value.toMutableList()
        val user = list.find { it.id == userId }
        if (user != null) {
            list.removeAll { it.id == userId }
            _users.value = list
            addAuditLog(_currentUser.value.name, "Penolakan Akun", "Menolak pendaftaran akun ${user.name}.")
        }
    }

    // --- Fleet Management (Admin Cabang) ---
    fun addVehicle(
        schoolBranchId: String,
        plate: String,
        brandModel: String,
        capacity: Int,
        initialKm: Int,
        fuel: Int,
        type: String = "MPV"
    ) {
        val newCar = CarVehicle(
            id = "VEH-${UUID.randomUUID().toString().take(5).uppercase()}",
            schoolBranchId = schoolBranchId,
            plateNumber = plate,
            brandModel = brandModel,
            capacity = capacity,
            status = CarStatus.TERSEDIA,
            currentOdometerKm = initialKm,
            fuelPercentage = fuel,
            lastServiceDate = "14 Okt 2026",
            vehicleIconType = type
        )
        _vehicles.value = _vehicles.value + newCar
        addAuditLog(_currentUser.value.name, "Tambah Armada", "Menambahkan armada baru $brandModel ($plate).")
    }

    fun updateVehicleStatus(vehicleId: String, newStatus: CarStatus) {
        val list = _vehicles.value.toMutableList()
        val idx = list.indexOfFirst { it.id == vehicleId }
        if (idx != -1) {
            list[idx] = list[idx].copy(status = newStatus)
            _vehicles.value = list
            addAuditLog(_currentUser.value.name, "Ubah Status Armada", "Status mobil ${list[idx].plateNumber} diubah ke ${newStatus.label}.")
        }
    }

    // --- Booking Flow ---
    fun createBooking(
        schoolBranchId: String,
        vehicleId: String,
        requester: AppUser,
        passengers: Int,
        purpose: String,
        departureDate: String,
        departureTime: String,
        estimatedReturnTime: String,
        waypoints: List<RouteWaypoint>,
        isEmergency: Boolean = false
    ): BookingRequest {
        val vehicle = _vehicles.value.find { it.id == vehicleId }
        val school = _schools.value.find { it.id == schoolBranchId }

        val newBooking = BookingRequest(
            id = "BKG-${UUID.randomUUID().toString().take(6).uppercase()}",
            bookingCode = "SR-${school?.city?.take(3)?.uppercase() ?: "CAB"}-${(1000..9999).random()}",
            schoolBranchId = schoolBranchId,
            schoolBranchName = school?.name ?: "Sekolah Rakyat",
            vehicleId = vehicleId,
            vehiclePlate = vehicle?.plateNumber ?: "B 1234 SR",
            vehicleModel = vehicle?.brandModel ?: "Mobil Operasional",
            requesterId = requester.id,
            requesterName = requester.name,
            requesterRole = requester.role,
            requesterWhatsapp = requester.phoneWhatsapp,
            passengerCount = passengers,
            purpose = purpose,
            departureDate = departureDate,
            departureTime = departureTime,
            estimatedReturnTime = estimatedReturnTime,
            waypoints = waypoints,
            status = if (isEmergency) BookingStatus.APPROVED else BookingStatus.PENDING_APPROVAL,
            isEmergency = isEmergency
        )

        _bookings.value = listOf(newBooking) + _bookings.value
        addAuditLog(requester.name, "Pengajuan Peminjaman", "Mengajukan peminjaman ${newBooking.bookingCode} untuk ${newBooking.purpose}.")
        return newBooking
    }

    fun approveBookingByAdmin(bookingId: String) {
        val list = _bookings.value.toMutableList()
        val idx = list.indexOfFirst { it.id == bookingId }
        if (idx != -1) {
            list[idx] = list[idx].copy(status = BookingStatus.APPROVED)
            _bookings.value = list
            addAuditLog(_currentUser.value.name, "Persetujuan Peminjaman", "Menyetujui booking ${list[idx].bookingCode}. Siap diverifikasi di gerbang.")
        }
    }

    fun rejectBookingByAdmin(bookingId: String, reason: String = "") {
        val list = _bookings.value.toMutableList()
        val idx = list.indexOfFirst { it.id == bookingId }
        if (idx != -1) {
            list[idx] = list[idx].copy(status = BookingStatus.REJECTED, notes = reason)
            _bookings.value = list
            addAuditLog(_currentUser.value.name, "Penolakan Peminjaman", "Menolak booking ${list[idx].bookingCode}. Alasan: $reason.")
        }
    }

    // --- Gate Verification (Petugas Keamanan) ---
    fun checkoutAtGate(
        context: Context,
        bookingId: String,
        odometerKm: Int,
        fuelPercent: Int,
        guardUser: AppUser
    ): Boolean {
        val list = _bookings.value.toMutableList()
        val idx = list.indexOfFirst { it.id == bookingId }
        if (idx == -1) return false

        val bkg = list[idx]

        // ATURAN KEAMANAN MUTLAK: Pencegahan Self-Approval!
        if (bkg.requesterId == guardUser.id) {
            // Guard cannot approve their own trip!
            return false
        }

        val timestamp = dateFormat.format(Date())
        list[idx] = bkg.copy(
            status = BookingStatus.ON_DUTY,
            checkoutOdometerKm = odometerKm,
            checkoutFuelPercent = fuelPercent,
            verifiedByGuardId = guardUser.id,
            verifiedByGuardName = guardUser.name,
            checkoutTimestamp = timestamp
        )
        _bookings.value = list

        // Update vehicle status to DIPAKAI
        updateVehicleStatus(bkg.vehicleId, CarStatus.DIPAKAI)

        // START ANDROID FOREGROUND SERVICE WITH PERSISTENT NOTIFICATION
        TrackingForegroundService.startTracking(
            context = context,
            bookingCode = bkg.bookingCode,
            vehiclePlate = bkg.vehiclePlate,
            driverName = bkg.requesterName
        )

        addAuditLog(guardUser.name, "Check-out Gerbang (Izinkan Keluar)", "Izin keluar diberikan untuk ${bkg.bookingCode} (${bkg.vehiclePlate}). GPS Tracking aktif.")
        return true
    }

    fun checkinAtGate(
        context: Context,
        bookingId: String,
        odometerKm: Int,
        fuelPercent: Int,
        guardUser: AppUser
    ): Boolean {
        val list = _bookings.value.toMutableList()
        val idx = list.indexOfFirst { it.id == bookingId }
        if (idx == -1) return false

        val bkg = list[idx]

        // Pencegahan self-approval pada check-in
        if (bkg.requesterId == guardUser.id) {
            return false
        }

        val timestamp = dateFormat.format(Date())
        list[idx] = bkg.copy(
            status = BookingStatus.FINISHED,
            checkinOdometerKm = odometerKm,
            checkinFuelPercent = fuelPercent,
            checkinTimestamp = timestamp
        )
        _bookings.value = list

        // Update vehicle status back to TERSEDIA & update odometer and fuel
        val vList = _vehicles.value.toMutableList()
        val vIdx = vList.indexOfFirst { it.id == bkg.vehicleId }
        if (vIdx != -1) {
            vList[vIdx] = vList[vIdx].copy(
                status = CarStatus.TERSEDIA,
                currentOdometerKm = odometerKm,
                fuelPercentage = fuelPercent
            )
            _vehicles.value = vList
        }

        // STOP ANDROID FOREGROUND SERVICE
        TrackingForegroundService.stopTracking(context)

        addAuditLog(guardUser.name, "Check-in Gerbang (Selesai Peminjaman)", "Mobil ${bkg.vehiclePlate} kembali. Jarak tempuh: ${odometerKm - (bkg.checkoutOdometerKm ?: odometerKm)} KM. GPS Tracking dinonaktifkan.")
        return true
    }

    // Emergency Override by Admin Sekolah
    fun adminEmergencyGateOverride(context: Context, bookingId: String, adminUser: AppUser) {
        val list = _bookings.value.toMutableList()
        val idx = list.indexOfFirst { it.id == bookingId }
        if (idx != -1) {
            val bkg = list[idx]
            val timestamp = dateFormat.format(Date())
            list[idx] = bkg.copy(
                status = BookingStatus.ON_DUTY,
                verifiedByGuardId = adminUser.id,
                verifiedByGuardName = "${adminUser.name} (Emergency Override)",
                checkoutTimestamp = timestamp
            )
            _bookings.value = list
            updateVehicleStatus(bkg.vehicleId, CarStatus.DIPAKAI)

            TrackingForegroundService.startTracking(
                context = context,
                bookingCode = bkg.bookingCode,
                vehiclePlate = bkg.vehiclePlate,
                driverName = bkg.requesterName
            )
            addAuditLog(adminUser.name, "Emergency Gate Override", "Izin darurat gerbang diterbitkan oleh Admin untuk ${bkg.bookingCode}.")
        }
    }

    private fun addAuditLog(actor: String, action: String, details: String) {
        val log = AuditLog(
            id = "LOG-${UUID.randomUUID().toString().take(6)}",
            timestamp = dateFormat.format(Date()),
            actorName = actor,
            actorRole = _currentUser.value.role.title,
            branchName = _currentUser.value.schoolBranchName ?: "Pusat Nasional",
            action = action,
            details = details
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }
}
