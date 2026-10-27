package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.robolectric.shadows.ShadowLooper
import java.net.HttpURLConnection
import java.net.URL

abstract class FirestoreEmulatorTestBase {
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    private lateinit var databaseId: String
    private val projectId = "demo-no-project"
    private val firestoreHost = "127.0.0.1"
    private val firestorePort = 8085
    private val authPort = 9099

    @Before
    fun setupEmulator() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        databaseId = context.getString(R.string.firestore_database_id)
        FirebaseApp.clearInstancesForTest()

        val options = FirebaseOptions.Builder()
            .setProjectId(projectId)
            .setApplicationId("1:1234567890:android:abcdef")
            .setApiKey("fake-api-key-for-emulator")
            .build()
        val app = FirebaseApp.initializeApp(context, options)

        db = FirebaseFirestore.getInstance(app, databaseId)
        db.useEmulator(firestoreHost, firestorePort)
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
            .build()

        auth = FirebaseAuth.getInstance(app)
        auth.useEmulator(firestoreHost, authPort)

        clearFirestoreEmulator()
        clearAuthEmulator()
    }

    @After
    fun tearDownEmulator() {
        runCatching {
            db.terminate()
            ShadowLooper.shadowMainLooper().idle()
        }
        FirebaseApp.clearInstancesForTest()
    }

    protected fun runEmulatorTest(block: suspend CoroutineScope.() -> Unit) {
        val scope = CoroutineScope(Dispatchers.IO)
        val deferred = scope.async { block() }
        val mainLooper = ShadowLooper.shadowMainLooper()
        while (deferred.isActive) {
            mainLooper.idle()
            Thread.sleep(10)
        }
        mainLooper.idle()
        runBlocking { deferred.await() }
    }

    protected suspend fun signInTestUser(
        email: String = "user@test.com",
        password: String = "password123"
    ): String {
        val result = try {
            auth.createUserWithEmailAndPassword(email, password).await()
        } catch (e: Exception) {
            auth.signInWithEmailAndPassword(email, password).await()
        }
        return result.user!!.uid
    }

    protected fun signOut() {
        auth.signOut()
    }

    protected suspend fun <T> assertSucceeds(block: suspend () -> T): T = block()

    protected suspend fun assertFails(block: suspend () -> Unit) {
        try {
            block()
            fail("Expected PERMISSION_DENIED, but the operation succeeded.")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(
                "Expected PERMISSION_DENIED but got ${e.code}: ${e.message}",
                FirebaseFirestoreException.Code.PERMISSION_DENIED,
                e.code
            )
        }
    }

    private fun clearFirestoreEmulator() {
        val url = URL("http://$firestoreHost:$firestorePort/emulator/v1/projects/$projectId/databases/$databaseId/documents")
        (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "DELETE"
            connectTimeout = 5000
            readTimeout = 5000
            check(responseCode in 200..299) {
                "Failed to clear Firestore emulator (HTTP $responseCode). Is the emulator running on $firestoreHost:$firestorePort?"
            }
            disconnect()
        }
    }

    private fun clearAuthEmulator() {
        val url = URL("http://$firestoreHost:$authPort/emulator/v1/projects/$projectId/accounts")
        (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "DELETE"
            connectTimeout = 5000
            readTimeout = 5000
            check(responseCode in 200..299) {
                "Failed to clear Auth emulator (HTTP $responseCode). Is the Auth emulator running on $firestoreHost:$authPort?"
            }
            disconnect()
        }
    }
}
