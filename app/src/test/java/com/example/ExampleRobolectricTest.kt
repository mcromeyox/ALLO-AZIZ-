package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.OrderEntity
import com.example.data.localization.AppStrings
import com.example.data.model.AppLanguage
import com.example.data.model.CatalogData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun readStringFromContext_matchesAlloAziz() {
        val appName = context.getString(R.string.app_name)
        assertEquals("ALLO AZIZ", appName)
    }

    @Test
    fun catalogStoresAndProducts_arePopulated() {
        assertTrue(CatalogData.stores.isNotEmpty())
        assertTrue(CatalogData.products.isNotEmpty())

        val tajineStore = CatalogData.stores.find { it.id == "store_tajine" }
        assertNotNull(tajineStore)
        assertEquals("طاجين البركة البلدي - قصبة المحمدية", tajineStore?.nameAr)

        val mcdoStore = CatalogData.stores.find { it.id == "store_mcdonalds" }
        assertNotNull(mcdoStore)
        assertEquals("ماكدونالدز ميرامار المحمدية", mcdoStore?.nameAr)
    }

    @Test
    fun supportAndAdmin_canActivateDriverAccount() = runBlocking {
        val pendingDriver = com.example.data.local.UserAccountEntity(
            id = "drv_test_1",
            name = "طارق المهدوي",
            email = "tarik@driver.com",
            phone = "+212 677-112233",
            password = "testpassword",
            role = "DRIVER",
            status = "PENDING_APPROVAL",
            vehicleType = "سكوتر 150cc"
        )
        database.userDao().insertUser(pendingDriver)

        var driver = database.userDao().getUserById("drv_test_1").first()
        assertEquals("PENDING_APPROVAL", driver?.status)

        // Support Agent or Admin activates driver account
        database.userDao().updateUserStatus("drv_test_1", "ACTIVE")

        driver = database.userDao().getUserById("drv_test_1").first()
        assertEquals("ACTIVE", driver?.status)
    }

    @Test
    fun superAdmin_credentialsVerification() = runBlocking {
        val admin = com.example.data.local.UserAccountEntity(
            id = "usr_super_admin",
            name = "المدير العام",
            email = "mcromeyox@gmail.com",
            phone = "+212 600-000001",
            password = "abdomery",
            role = "SUPER_ADMIN",
            status = "ACTIVE"
        )
        database.userDao().insertUser(admin)

        val retrievedAdmin = database.userDao().getUserByEmail("mcromeyox@gmail.com")
        assertNotNull(retrievedAdmin)
        assertEquals("mcromeyox@gmail.com", retrievedAdmin?.email)
        assertEquals("abdomery", retrievedAdmin?.password)
        assertEquals("SUPER_ADMIN", retrievedAdmin?.role)
        assertEquals("ACTIVE", retrievedAdmin?.status)
    }

    @Test
    fun multilingualStrings_renderCorrectly() {
        val arTitle = AppStrings.tabHome(AppLanguage.ARABIC)
        val frTitle = AppStrings.tabHome(AppLanguage.FRENCH)
        val enTitle = AppStrings.tabHome(AppLanguage.ENGLISH)

        assertEquals("الرئيسية", arTitle)
        assertEquals("Accueil", frTitle)
        assertEquals("Home", enTitle)

        assertTrue(AppLanguage.ARABIC.isRtl)
        assertTrue(!AppLanguage.FRENCH.isRtl)
        assertTrue(!AppLanguage.ENGLISH.isRtl)
    }

    @Test
    fun orderDao_insertAndQueryOrder() = runBlocking {
        val order = OrderEntity(
            id = "AZ-9999",
            storeId = "store_burger",
            storeName = "برجر سماش أكسبريس",
            itemsSummary = "1x دبل سماش برجر",
            itemsCount = 1,
            subtotal = 48.0,
            deliveryFee = 8.0,
            discount = 0.0,
            total = 56.0,
            status = "RECEIVED",
            paymentMethod = "بطاقة بنكية",
            deliveryAddress = "شارع النخيل",
            deliveryNotes = "الاتصال عند الباب",
            timestamp = System.currentTimeMillis(),
            courierName = "عزيز برادة",
            courierPhone = "+212 661-234567",
            courierRating = 4.95,
            courierVehicle = "دراجة نارية هوندا 125cc",
            courierProgress = 0.1f,
            estimatedMinsLeft = 18
        )

        database.orderDao().insertOrder(order)
        val ordersList = database.orderDao().getAllOrders().first()
        assertEquals(1, ordersList.size)
        assertEquals("AZ-9999", ordersList[0].id)
        assertEquals("عزيز برادة", ordersList[0].courierName)

        database.orderDao().updateOrderStatus("AZ-9999", "ON_THE_WAY", 0.6f, 8)
        val updated = database.orderDao().getOrderById("AZ-9999").first()
        assertNotNull(updated)
        assertEquals("ON_THE_WAY", updated?.status)
        assertEquals(8, updated?.estimatedMinsLeft)
    }
}
