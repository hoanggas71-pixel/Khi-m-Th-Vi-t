package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SpaWorkspaceRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun `unauthenticated device cannot read or write shared spa workspace`() = runEmulatorTest {
        signOut()
        assertFails {
            SpaCloudRepository.getWorkspaceSnapshot(db)
        }
    }

    @Test
    fun `admin device can sync workspace and secondary staff device can read and update in real time`() = runEmulatorTest {
        val adminUid = signInTestUser("admin@khiemthiviet.vn")
        assertSucceeds {
            SpaCloudRepository.saveWorkspaceSnapshot(
                db = db,
                auth = auth,
                payloadJson = """{"taiKhoans":[{"id":"tk_admin"},{"id":"tk_nv_new"}]}""",
                accountsCount = 2,
                customersCount = 5,
                ticketsCount = 8,
                updatedByName = "Admin Quản trị"
            )
        }

        // Máy phụ nhân viên đăng nhập và đọc dữ liệu đồng bộ từ máy Admin
        signInTestUser("staff@khiemthiviet.vn")
        val fetched = assertSucceeds {
            SpaCloudRepository.getWorkspaceSnapshot(db)
        }
        assertNotNull(fetched)
        assertEquals(2L, fetched?.get("accountsCount"))

        // Máy phụ nhân viên mở vé mới và đồng bộ ngược lại lên đám mây
        assertSucceeds {
            SpaCloudRepository.saveWorkspaceSnapshot(
                db = db,
                auth = auth,
                payloadJson = """{"taiKhoans":[{"id":"tk_admin"},{"id":"tk_nv_new"}],"luots":[{"id":"ve_new"}]}""",
                accountsCount = 2,
                customersCount = 5,
                ticketsCount = 9,
                updatedByName = "KTV Nguyễn Văn Minh"
            )
        }

        // Bất kỳ người dùng nào cố tình giả mạo updatedByUid của người khác đều bị từ chối
        assertFails {
            SpaCloudRepository.saveWorkspaceSnapshot(
                db = db,
                auth = auth,
                payloadJson = """{"taiKhoans":[]}""",
                accountsCount = 1,
                customersCount = 0,
                ticketsCount = 0,
                updatedByName = "Giả mạo",
                overrideUpdatedByUid = adminUid // Đang đăng nhập bằng staffUid nhưng giả mạo adminUid
            )
        }

        // Không ai được phép xóa toàn bộ workspace chung
        assertFails {
            SpaCloudRepository.deleteWorkspaceSnapshot(db)
        }
    }

    @Test
    fun `user profile rules enforce strict owner-only read and write`() = runEmulatorTest {
        val aliceUid = signInTestUser("alice@khiemthiviet.vn")
        assertSucceeds {
            SpaCloudRepository.saveUserProfile(
                db = db,
                auth = auth,
                displayName = "Admin Quản trị",
                email = "alice@khiemthiviet.vn",
                spaPhone = "0397772963",
                spaRole = "ADMIN"
            )
        }

        val profile = assertSucceeds {
            SpaCloudRepository.getUserProfile(db, aliceUid)
        }
        assertNotNull(profile)
        assertEquals("0397772963", profile?.get("spaPhone"))

        // Người dùng khác không được đọc hoặc ghi đè hồ sơ của Alice
        signInTestUser("bob@khiemthiviet.vn")
        assertFails {
            SpaCloudRepository.getUserProfile(db, aliceUid)
        }
        assertFails {
            SpaCloudRepository.saveUserProfile(
                db = db,
                auth = auth,
                targetUserId = aliceUid,
                displayName = "Hacked",
                email = "bob@khiemthiviet.vn",
                spaPhone = "0912345678",
                spaRole = "STAFF"
            )
        }
    }
}
