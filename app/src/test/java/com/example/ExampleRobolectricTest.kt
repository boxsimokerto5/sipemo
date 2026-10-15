package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DataRepository
import com.example.model.BookingStatus
import com.example.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SiPemo Sekolah Rakyat", appName)
    }

    @Test
    fun `self approval prevention rule blocks guard approving own booking`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = DataRepository()

        // Guard Pak Sukirno (requester of booking SR-JKT-8822)
        val guardUser = repository.users.value.find { it.id == "USR-GUARD-SUKIRNO" }!!
        val ownBooking = repository.bookings.value.find { it.id == "BKG-2026-002" }!!

        // Guard attempts to approve their own checkout at gate
        val success = repository.checkoutAtGate(
            context = context,
            bookingId = ownBooking.id,
            odometerKm = 28400,
            fuelPercent = 90,
            guardUser = guardUser
        )

        // Security rule MUST block self-approval!
        assertFalse("Guard must not be able to self-approve own booking", success)
    }

    @Test
    fun `superadmin approval activates school branch for registration dropdown`() {
        val repository = DataRepository()
        val pendingSchool = repository.schools.value.find { !it.isApprovedBySuperadmin }!!

        repository.approveSchool(pendingSchool.id)

        val approved = repository.schools.value.find { it.id == pendingSchool.id }!!
        assertTrue(approved.isApprovedBySuperadmin)
    }

    @Test
    fun `test qr code generation produces valid bitmap`() {
        val bitmap = com.example.util.QrCodeUtils.generateQrBitmap("SR-JKT-8821", 256)
        org.junit.Assert.assertNotNull(bitmap)
        assertEquals(256, bitmap?.width)
        assertEquals(256, bitmap?.height)
    }
}
