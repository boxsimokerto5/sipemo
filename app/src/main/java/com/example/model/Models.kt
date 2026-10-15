package com.example.model

enum class Role(val title: String, val badgeColorHex: Long) {
    SUPERADMIN("Superadmin Nasional", 0xFF1B2A4A),
    ADMIN_SEKOLAH("Admin Cabang", 0xFF0284C7),
    STAFF("Staff Peminjam", 0xFF059669),
    VERIFIKATOR("Verifikator Gerbang", 0xFFD97706)
}

enum class GuardMode {
    MODE_VERIFIKATOR, // Bertugas di gerbang untuk cek fisik & scan QR
    MODE_PEMINJAM     // Bertindak sebagai peminjam mobil
}

data class AppUser(
    val id: String,
    val name: String,
    val nipNik: String,
    val email: String,
    val phoneWhatsapp: String,
    val role: Role,
    val schoolBranchId: String?,
    val schoolBranchName: String?,
    val isApproved: Boolean = true,
    val professionTitle: String = "Staf Pengajar"
)

data class SchoolBranch(
    val id: String,
    val name: String,
    val province: String,
    val city: String,
    val address: String,
    val phone: String,
    val adminName: String,
    val adminNip: String,
    val adminEmail: String,
    val adminWhatsapp: String,
    val skDocumentUrl: String,
    val isApprovedBySuperadmin: Boolean,
    val isActive: Boolean = true,
    val registeredDate: String = "14 Okt 2026"
)

enum class CarStatus(val label: String) {
    TERSEDIA("Tersedia"),
    DIPAKAI("Sedang Dipakai"),
    PERAWATAN("Dalam Perawatan")
}

data class CarVehicle(
    val id: String,
    val schoolBranchId: String,
    val plateNumber: String,
    val brandModel: String,
    val capacity: Int,
    val status: CarStatus,
    val currentOdometerKm: Int,
    val fuelPercentage: Int, // 0 - 100
    val lastServiceDate: String,
    val isRoadworthy: Boolean = true,
    val transmission: String = "Manual",
    val vehicleIconType: String = "MPV" // MPV, SUV, Minibus, Van
)

enum class BookingStatus(val label: String) {
    PENDING_APPROVAL("Menunggu Persetujuan Admin"),
    APPROVED("Disetujui - Siap Gerbang"),
    ON_DUTY("Dalam Perjalanan (On Duty)"),
    FINISHED("Selesai Peminjaman"),
    REJECTED("Ditolak")
}

data class RouteWaypoint(
    val id: String,
    val label: String, // "Titik Asal: Sekolah Rakyat", "Tujuan 1: RSUD", "Tujuan 2: Kantor Dinas"
    val address: String,
    val lat: Double,
    val lng: Double,
    val isCompleted: Boolean = false
)

data class BookingRequest(
    val id: String,
    val bookingCode: String,
    val schoolBranchId: String,
    val schoolBranchName: String,
    val vehicleId: String,
    val vehiclePlate: String,
    val vehicleModel: String,
    val requesterId: String,
    val requesterName: String,
    val requesterRole: Role,
    val requesterWhatsapp: String,
    val passengerCount: Int,
    val purpose: String,
    val departureDate: String,
    val departureTime: String,
    val estimatedReturnTime: String,
    val waypoints: List<RouteWaypoint>,
    val status: BookingStatus,
    val isEmergency: Boolean = false,
    val checkoutOdometerKm: Int? = null,
    val checkinOdometerKm: Int? = null,
    val checkoutFuelPercent: Int? = null,
    val checkinFuelPercent: Int? = null,
    val verifiedByGuardId: String? = null,
    val verifiedByGuardName: String? = null,
    val checkoutTimestamp: String? = null,
    val checkinTimestamp: String? = null,
    val notes: String = ""
)

data class AuditLog(
    val id: String,
    val timestamp: String,
    val actorName: String,
    val actorRole: String,
    val branchName: String,
    val action: String,
    val details: String
)
