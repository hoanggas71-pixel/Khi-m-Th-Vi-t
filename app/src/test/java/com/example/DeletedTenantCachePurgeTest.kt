package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeletedTenantCachePurgeTest {

    private lateinit var context: Context
    private lateinit var store: SpaStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences(SpaStore.PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
        store = SpaStore(context)
    }

    @Test
    fun deletedTenantCacheIsPurgedAndCannotReloginOffline() {
        // 1. Provision & login a customer tenant 12345 / 12345 (like old spa_12345)
        val loggedIn = store.provisionAndLoginTenantOwner(
            tenantId = "spa_12345",
            shopName = "jiji",
            ownerPhone = "12345",
            pass = "12345",
            planId = "Gói 1 Tháng",
            daysLeft = 30
        )
        assertTrue("Initial login for spa_12345 should succeed", loggedIn)

        // 2. Simulate Web Portal deleting spa_12345 -> clearDeletedTenantCache + logout
        store.clearDeletedTenantCache("spa_12345", "12345")
        store.logout()

        assertTrue("spa_12345 must be marked deleted", store.isTenantMarkedDeleted("spa_12345"))
        assertTrue("12345 phone must be marked deleted", store.isTenantMarkedDeleted("12345"))

        // 3. Re-instantiate SpaStore (simulating app restart) and verify 12345/12345 cannot log in locally
        val freshStore = SpaStore(context)
        assertFalse("Deleted tenant 12345/12345 must NOT log in locally after purge", freshStore.login("12345", "12345"))

        // 4. Root Admin (0397772963 / 247208) must remain completely unaffected
        assertFalse("Root Admin must never be marked deleted", freshStore.isTenantMarkedDeleted(DEFAULT_ADMIN_PHONE))
        assertTrue("Root Admin must still log in offline", freshStore.login(DEFAULT_ADMIN_PHONE, DEFAULT_ADMIN_PASS))
    }
}
