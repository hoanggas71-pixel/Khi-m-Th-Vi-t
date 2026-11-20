package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit test suite verifying Requirement R2:
 * Eliminate mock data auto-loading on new account and facility creation.
 * All new facilities must start with zero records (0 tickets, 0 staff, 0 services, 0 packages, 0 customers, 0 transactions).
 * Root admin account in default tenant must be preserved.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NewAccountFacilityEmptyDataTest {

    @Test
    fun testNewlyProvisionedFacilityHasZeroRecords() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SpaStore(context)

        // Provision a newly created facility/tenant owner
        val ok = store.provisionAndLoginTenantOwner(
            tenantId = "spa_new_branch_test",
            shopName = "Chi nhánh Spa Mới",
            ownerPhone = "0988776655",
            pass = "123456"
        )
        assertTrue(ok)
        assertEquals("spa_new_branch_test", store.currentTenantId)

        // Assert all entity collections are completely blank (0 records)
        assertEquals(0, store.luots.size)      // 0 tickets
        assertEquals(0, store.nvs.size)        // 0 employees / staff
        assertEquals(0, store.dvs.size)        // 0 services
        assertEquals(0, store.gois.size)       // 0 packages
        assertEquals(0, store.khs.size)        // 0 customers
        assertEquals(0, store.dks.size)        // 0 subscriptions
        assertEquals(0, store.tcs.size)        // 0 cashbook transactions

        // Only the single Owner account exists; 0 mock staff accounts
        assertEquals(1, store.taiKhoans.size)
        assertEquals("0988776655", store.taiKhoans.first().username)
        assertEquals(VaiTro.OWNER, store.taiKhoans.first().quyen)
    }

    @Test
    fun testDefaultAdminAccountPreservedInInitialTenant() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SpaStore(context)

        // Default tenant must retain initial system admin account
        val rootAdmin = store.taiKhoans.firstOrNull { it.id == "tk_admin" }
        assertNotNull(rootAdmin)
        assertEquals(DEFAULT_ADMIN_PHONE, rootAdmin?.username)
        assertEquals("0397772963", DEFAULT_ADMIN_PHONE)
        assertTrue(rootAdmin!!.isAdmin())
        assertTrue(rootAdmin.isOwner())
    }

    @Test
    fun testDevCreateAndSwitchToNewTenantHasZeroRecords() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SpaStore(context)

        store.devCreateNewTenant(
            tenantId = "spa_test_dev_01",
            shopName = "Spa Test Dev 01",
            ownerPhone = "0911002233",
            planId = "PLAN_FREE",
            trialDays = 30
        )
        store.devSwitchTenantContext("spa_test_dev_01")

        assertEquals("spa_test_dev_01", store.currentTenantId)
        assertEquals(0, store.luots.size)
        assertEquals(0, store.nvs.size)
        assertEquals(0, store.dvs.size)
        assertEquals(0, store.gois.size)
        assertEquals(0, store.khs.size)
        assertEquals(0, store.dks.size)
        assertEquals(0, store.tcs.size)
    }

    @Test
    fun testCustomTenantNotPollutedByDefaultTenantDataOnReload() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store1 = SpaStore(context)

        // Provision custom tenant
        store1.provisionAndLoginTenantOwner(
            tenantId = "spa_custom_reload_test",
            shopName = "Spa Reload Test",
            ownerPhone = "0922334455",
            pass = "654321"
        )

        // Re-read with a second store instance targeting this tenant
        val store2 = SpaStore(context)
        store2.devSwitchTenantContext("spa_custom_reload_test")

        // Should NOT fall back to default tenant's 10 services, 3 packages, mock tickets, etc.
        assertEquals("spa_custom_reload_test", store2.currentTenantId)
        assertEquals(0, store2.luots.size)
        assertEquals(0, store2.nvs.size)
        assertEquals(0, store2.dvs.size)
        assertEquals(0, store2.gois.size)
        assertEquals(0, store2.khs.size)
    }
}
