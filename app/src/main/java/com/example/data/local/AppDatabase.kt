package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserAccountEntity::class,
        StoreEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        ReviewEntity::class,
        NotificationEntity::class,
        WalletProfileEntity::class,
        SupportTicketEntity::class,
        AddressEntity::class,
        UserProfile::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserAccountDao
    abstract fun storeDao(): StoreDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun reviewDao(): ReviewDao
    abstract fun notificationDao(): NotificationDao
    abstract fun walletDao(): WalletDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun addressDao(): AddressDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        // Version 8 -> 9 contains no schema changes; older unsupported versions fail safely.
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "allo_aziz_database.db"
                ).addMigrations(MIGRATION_8_9).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
