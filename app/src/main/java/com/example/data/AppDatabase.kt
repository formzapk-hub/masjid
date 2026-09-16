package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Entities
@Entity(tableName = "officer_schedules")
data class OfficerSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,             // "YYYY-MM-DD"
    val prayerTime: String,       // "Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya", "Jum'at"
    val imam: String,
    val muadzin: String,
    val otherOfficer: String,     // e.g., "Khatib" or "Kolektor"
    val bilal: String = "",       // Bilal (specifically for Jumat / Friday)
    val pendingSync: Boolean = false
)

@Entity(tableName = "finance_transactions")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,             // "YYYY-MM-DD"
    val type: String,             // "IN" (Pemasukan) or "OUT" (Pengeluaran)
    val category: String,         // "Donasi QR", "Donasi Kotak", "Sosial", "Operasional", "Kebersihan", "Lainnya"
    val amount: Double,
    val description: String,
    val pendingSync: Boolean = false
)

@Entity(tableName = "inventory_assets")
data class InventoryAsset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: Int,
    val condition: String,        // "Baik", "Rusak Ringan", "Rusak Berat"
    val location: String,
    val pendingSync: Boolean = false
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val id: String = "app_config",
    val calculationMethod: Int = 0, // 0: Kemenag RI, 1: MWL, 2: ISNA, 3: Egypt, 4: Umm al-Qura
    val selectedLanguage: String = "in", // "in" (Indonesian) or "en" (English)
    val themeColor: String = "EMERALD", // "EMERALD", "GOLD", "BLUE"
    val isDarkMode: Boolean = false,
    val imsakReminder: Boolean = true,
    val subuhReminder: Boolean = true,
    val dzuhurReminder: Boolean = true,
    val asharReminder: Boolean = true,
    val maghribReminder: Boolean = true,
    val isyaReminder: Boolean = true,
    val mfaEnabled: Boolean = false // 2FA protection
)

// 2. DAOs
@Dao
interface AppDao {
    // Schedules
    @Query("SELECT * FROM officer_schedules ORDER BY date ASC, id ASC")
    fun getAllSchedules(): Flow<List<OfficerSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: OfficerSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: OfficerSchedule)

    @Query("DELETE FROM officer_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Int)

    // Finance
    @Query("SELECT * FROM finance_transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<FinanceTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinanceTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: FinanceTransaction)

    @Query("DELETE FROM finance_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    // Inventory
    @Query("SELECT * FROM inventory_assets ORDER BY name ASC")
    fun getAllAssets(): Flow<List<InventoryAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: InventoryAsset)

    @Delete
    suspend fun deleteAsset(asset: InventoryAsset)

    @Query("DELETE FROM inventory_assets WHERE id = :id")
    suspend fun deleteAssetById(id: Int)

    // Settings
    @Query("SELECT * FROM app_settings WHERE id = 'app_config' LIMIT 1")
    fun getSettings(): Flow<AppSetting?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(setting: AppSetting)
}

// 3. Database
@Database(
    entities = [OfficerSchedule::class, FinanceTransaction::class, InventoryAsset::class, AppSetting::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
