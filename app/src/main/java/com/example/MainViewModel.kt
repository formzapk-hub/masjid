package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class SyncLog(
    val timestamp: String,
    val device: String,
    val action: String,
    val status: String // "SUCCESS", "PENDING", "FAILED"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize Room Database
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            getApplication(),
            AppDatabase::class.java,
            "sajada_mosque_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val dao: AppDao get() = database.appDao()

    // 1. Core Data Flows
    val allSchedules: StateFlow<List<OfficerSchedule>> = dao.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<FinanceTransaction>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssets: StateFlow<List<InventoryAsset>> = dao.getAllAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawSettings: StateFlow<AppSetting?> = dao.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Fallback UI Settings if DB is loading or empty
    val currentSettings = rawSettings.map { it ?: AppSetting() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSetting())

    // 2. Active Screen/Tab State
    val currentTab = MutableStateFlow("sholat") // "sholat", "jadwal", "keuangan", "inventaris", "settings"

    // 3. User Role Management
    // "JEMAAH" (View Only & QR Donation), "PENGURUS" (Manage Schedules & Inventory), "ADMIN" (Super power - all access)
    val currentUserRole = MutableStateFlow("ADMIN") 

    // 4. Online/Offline & Synchronization System
    val isOnline = MutableStateFlow(true)
    val isSyncing = MutableStateFlow(false)
    
    private val _syncLogs = MutableStateFlow<List<SyncLog>>(emptyList())
    val syncLogs: StateFlow<List<SyncLog>> = _syncLogs.asStateFlow()

    // 5. Active Location (City)
    val selectedCity = MutableStateFlow(PrayerTimeCalculator.cities[0]) // Default is Jakarta

    // 6. Qibla Simulated / Real Angle
    val compassAzimuth = MutableStateFlow(0f) // Compas reading
    val compassAccuracy = MutableStateFlow("Tinggi") // High accuracy

    // 7. Time & Date states
    val currentDateStr = MutableStateFlow("")
    val currentHijriDateStr = MutableStateFlow("")
    val timeString = MutableStateFlow("00:00:00")
    val dayOfYear = MutableStateFlow(1)

    init {
        // Initialize clock timer and pre-populate date info
        updateDateTime()
        viewModelScope.launch {
            while (true) {
                delay(1000)
                updateDateTime()
            }
        }

        // Initialize mock sync logs
        addSyncLog("Sistem Utama", "Inisialisasi Sinkronisasi Offline-First", "SUCCESS")

        // Seed data if DB is empty
        viewModelScope.launch(Dispatchers.IO) {
            seedDatabaseIfNeeded()
        }
    }

    private fun updateDateTime() {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("in", "ID"))
        
        timeString.value = timeFormat.format(calendar.time)
        currentDateStr.value = dateFormat.format(calendar.time)
        dayOfYear.value = calendar.get(Calendar.DAY_OF_YEAR)

        // Convert to Hijri
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        currentHijriDateStr.value = PrayerTimeCalculator.convertToHijri(year, month, day)
    }

    /**
     * Seeds the local Room database with rich sample data on first launch
     */
    private suspend fun seedDatabaseIfNeeded() {
        // Seed default settings
        dao.saveSettings(AppSetting())

        // Check schedules
        val currentSchedules = dao.getAllSchedules().first()
        if (currentSchedules.isEmpty()) {
            val sampleDays = listOf("2026-09-16", "2026-09-17", "2026-09-18", "2026-09-19", "2026-09-20")
            val prayers = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")
            val imams = listOf("Ustadz H. Abdul Somad", "Ustadz Adi Hidayat", "KH. Bahauddin Nursalim", "Ustadz Hanan Attaki", "K.H. Anwar Zahid")
            val muadzins = listOf("Akhi Bilal", "Akhi Muammar", "Akhi Salman", "Akhi Zulfikar", "Akhi Yusuf")

            var count = 0
            for (date in sampleDays) {
                for (prayer in prayers) {
                    dao.insertSchedule(
                        OfficerSchedule(
                            date = date,
                            prayerTime = prayer,
                            imam = imams[count % imams.size],
                            muadzin = muadzins[count % muadzins.size],
                            otherOfficer = "Remaja Masjid (Petugas Kebersihan)",
                            pendingSync = false
                        )
                    )
                    count++
                }
                // Add Friday prayer
                dao.insertSchedule(
                    OfficerSchedule(
                        date = date,
                        prayerTime = "Jum'at",
                        imam = "KH. Miftachul Akhyar",
                        muadzin = "Akhi Hasan",
                        otherOfficer = "Khatib & Imam",
                        bilal = "Akhi Bilal Syarif",
                        pendingSync = false
                    )
                )
            }
        }

        // Check transactions
        val currentTransactions = dao.getAllTransactions().first()
        if (currentTransactions.isEmpty()) {
            val transactions = listOf(
                FinanceTransaction(date = "2026-09-15", type = "IN", category = "Donasi QR", amount = 1500000.0, description = "Infaq QR Code Sholat Jumat"),
                FinanceTransaction(date = "2026-09-14", type = "IN", category = "Donasi Kotak", amount = 750000.0, description = "Infaq Kotak Amal Utama"),
                FinanceTransaction(date = "2026-09-13", type = "OUT", category = "Operasional", amount = 450000.0, description = "Pembayaran Rekening Listrik Masjid"),
                FinanceTransaction(date = "2026-09-12", type = "OUT", category = "Sosial", amount = 1000000.0, description = "Santunan Anak Yatim dan Dhuafa"),
                FinanceTransaction(date = "2026-09-10", type = "IN", category = "Donasi QR", amount = 2500000.0, description = "Donasi Digital Renovasi Tempat Wudhu"),
                FinanceTransaction(date = "2026-09-08", type = "OUT", category = "Kebersihan", amount = 200000.0, description = "Pembelian Sabun Cuci & Pembersih Lantai")
            )
            for (t in transactions) {
                dao.insertTransaction(t)
            }
        }

        // Check assets
        val currentAssets = dao.getAllAssets().first()
        if (currentAssets.isEmpty()) {
            val assets = listOf(
                InventoryAsset(name = "Sajadah Karpet Tebal Turki", quantity = 15, condition = "Baik", location = "Ruang Utama"),
                InventoryAsset(name = "Speaker Wireless Portable", quantity = 2, condition = "Rusak Ringan", location = "Gudang Depan"),
                InventoryAsset(name = "Televisi Smart LED 55 Inch", quantity = 1, condition = "Baik", location = "Ruang Utama (Layar TV Publik)"),
                InventoryAsset(name = "Mikrofon Imam (Shure SM58)", quantity = 3, condition = "Baik", location = "Lemari Pengurus"),
                InventoryAsset(name = "Mesin Vacuum Cleaner", quantity = 1, condition = "Baik", location = "Gudang Belakang"),
                InventoryAsset(name = "Air Conditioner (AC) Daikin 2 PK", quantity = 4, condition = "Baik", location = "Ruang Utama")
            )
            for (a in assets) {
                dao.insertAsset(a)
            }
        }
    }

    // 8. CRUD Actions
    fun insertSchedule(date: String, prayerTime: String, imam: String, muadzin: String, otherOfficer: String, bilal: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val schedule = OfficerSchedule(
                date = date,
                prayerTime = prayerTime,
                imam = imam,
                muadzin = muadzin,
                otherOfficer = otherOfficer,
                bilal = bilal,
                pendingSync = !isOnline.value
            )
            dao.insertSchedule(schedule)
            addSyncLog("Jadwal Petugas", "Menambahkan jadwal petugas $prayerTime tanggal $date", if (isOnline.value) "SUCCESS" else "PENDING")
        }
    }

    fun deleteSchedule(schedule: OfficerSchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteSchedule(schedule)
            addSyncLog("Jadwal Petugas", "Menghapus jadwal petugas id ${schedule.id}", if (isOnline.value) "SUCCESS" else "PENDING")
        }
    }

    fun insertTransaction(type: String, category: String, amount: Double, description: String, date: String = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault()).format(Date())) {
        viewModelScope.launch(Dispatchers.IO) {
            val t = FinanceTransaction(
                date = date,
                type = type,
                category = category,
                amount = amount,
                description = description,
                pendingSync = !isOnline.value
            )
            dao.insertTransaction(t)
            addSyncLog("Keuangan", "Mencatat transaksi $type sebesar Rp ${amount.toInt()}", if (isOnline.value) "SUCCESS" else "PENDING")
        }
    }

    fun deleteTransaction(transaction: FinanceTransaction) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteTransaction(transaction)
            addSyncLog("Keuangan", "Menghapus transaksi keuangan id ${transaction.id}", if (isOnline.value) "SUCCESS" else "PENDING")
        }
    }

    fun insertAsset(name: String, quantity: Int, condition: String, location: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val asset = InventoryAsset(
                name = name,
                quantity = quantity,
                condition = condition,
                location = location,
                pendingSync = !isOnline.value
            )
            dao.insertAsset(asset)
            addSyncLog("Inventaris", "Menambahkan aset fisik $name ($quantity unit)", if (isOnline.value) "SUCCESS" else "PENDING")
        }
    }

    fun updateAsset(id: Int, name: String, quantity: Int, condition: String, location: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val asset = InventoryAsset(
                id = id,
                name = name,
                quantity = quantity,
                condition = condition,
                location = location,
                pendingSync = !isOnline.value
            )
            dao.insertAsset(asset)
            addSyncLog("Inventaris", "Memperbarui aset fisik $name ($quantity unit)", if (isOnline.value) "SUCCESS" else "PENDING")
        }
    }

    fun deleteAsset(asset: InventoryAsset) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteAsset(asset)
            addSyncLog("Inventaris", "Menghapus aset fisik ${asset.name}", if (isOnline.value) "SUCCESS" else "PENDING")
        }
    }

    fun updateSettings(setting: AppSetting) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.saveSettings(setting)
            addSyncLog("Pengaturan", "Memperbarui preferensi aplikasi", "SUCCESS")
        }
    }

    // 9. Sync Logic Simulation
    fun toggleNetworkConnection() {
        isOnline.value = !isOnline.value
        val state = if (isOnline.value) "Online" else "Offline"
        addSyncLog("Sistem Jaringan", "Koneksi beralih ke mode $state", "SUCCESS")
        
        if (isOnline.value) {
            // Trigger automatic sync when network is restored
            triggerSyncProcess()
        }
    }

    fun triggerSyncProcess() {
        if (!isOnline.value) return
        viewModelScope.launch(Dispatchers.IO) {
            isSyncing.value = true
            addSyncLog("Sync Manager", "Memulai Sinkronisasi Otomatis...", "PENDING")
            delay(1800) // Beautiful visual duration

            // Find all pending sync transactions and sync them (setting pendingSync to false)
            val pendingSchedules = dao.getAllSchedules().first().filter { it.pendingSync }
            val pendingTransactions = dao.getAllTransactions().first().filter { it.pendingSync }
            val pendingAssets = dao.getAllAssets().first().filter { it.pendingSync }

            pendingSchedules.forEach { dao.insertSchedule(it.copy(pendingSync = false)) }
            pendingTransactions.forEach { dao.insertTransaction(it.copy(pendingSync = false)) }
            pendingAssets.forEach { dao.insertAsset(it.copy(pendingSync = false)) }

            isSyncing.value = false
            addSyncLog("Sync Manager", "Enkripsi AES-256 diverifikasi. ${pendingSchedules.size + pendingTransactions.size + pendingAssets.size} data disinkronkan ke server cloud.", "SUCCESS")
        }
    }

    fun addSyncLog(device: String, action: String, status: String) {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val log = SyncLog(
            timestamp = formatter.format(Date()),
            device = device,
            action = action,
            status = status
        )
        val currentList = _syncLogs.value.toMutableList()
        currentList.add(0, log) // Add to top
        if (currentList.size > 20) currentList.removeAt(currentList.lastIndex)
        _syncLogs.value = currentList
    }

    // 10. Compass Angle Simulator for smooth Qibla dial rotation
    fun rotateCompass() {
        val targetQibla = PrayerTimeCalculator.calculateQiblaDirection(selectedCity.value.latitude, selectedCity.value.longitude)
        viewModelScope.launch {
            // Smoothly rotate the compass slightly to simulate sensor input
            for (i in 0..10) {
                delay(100)
                compassAzimuth.value = (compassAzimuth.value + (10 - i) * 1.5f + (Math.random() * 2 - 1).toFloat()) % 360f
            }
            // Set close to ideal angle
            compassAzimuth.value = (targetQibla.toFloat() - 25f + (Math.random() * 5).toFloat()) % 360f
        }
    }
}
