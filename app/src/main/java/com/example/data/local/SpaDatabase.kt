package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.converter.SpaConverters
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.CashbookDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.PackageDao
import com.example.data.local.dao.PackageSubDao
import com.example.data.local.dao.ServiceDao
import com.example.data.local.dao.StaffDao
import com.example.data.local.dao.TenantDao
import com.example.data.local.dao.TicketDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CashbookEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.PackageEntity
import com.example.data.local.entity.PackageSubEntity
import com.example.data.local.entity.ServiceEntity
import com.example.data.local.entity.StaffEntity
import com.example.data.local.entity.TenantEntity
import com.example.data.local.entity.TicketEntity

/**
 * Room Database cục bộ cho toàn bộ ứng dụng Spa Khiếm Thị Việt.
 * Hỗ trợ lưu trữ offline 100%, cách ly dữ liệu Multi-Tenant và đồng bộ 2 chiều với Firebase Cloud Firestore.
 */
@Database(
    entities = [
        AccountEntity::class,
        StaffEntity::class,
        ServiceEntity::class,
        PackageEntity::class,
        CustomerEntity::class,
        PackageSubEntity::class,
        TicketEntity::class,
        CashbookEntity::class,
        TenantEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(SpaConverters::class)
abstract class SpaDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun staffDao(): StaffDao
    abstract fun serviceDao(): ServiceDao
    abstract fun packageDao(): PackageDao
    abstract fun customerDao(): CustomerDao
    abstract fun packageSubDao(): PackageSubDao
    abstract fun ticketDao(): TicketDao
    abstract fun cashbookDao(): CashbookDao
    abstract fun tenantDao(): TenantDao

    companion object {
        private const val DATABASE_NAME = "khiem_thi_viet_spa.db"

        @Volatile
        private var INSTANCE: SpaDatabase? = null

        fun getInstance(context: Context): SpaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SpaDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
