package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object LocaleManager {
    private val idMap = mapOf(
        "app_title" to "MASJID JAMI' AT-TAQWA",
        "sholat" to "Jadwal Sholat",
        "jadwal" to "Petugas Masjid",
        "keuangan" to "Kas & Donasi",
        "inventaris" to "Aset & Keamanan",
        "pengaturan" to "Pengaturan",
        "imsak" to "Imsak",
        "subuh" to "Subuh",
        "syuruk" to "Syuruk",
        "dzuhur" to "Dzuhur",
        "ashar" to "Ashar",
        "maghrib" to "Maghrib",
        "isya" to "Isya",
        "jum'at" to "Jum'at",
        "kiblat" to "Arah Kiblat",
        "masjid" to "Masjid Terdekat",
        "donasi" to "Donasi Digital",
        "analitik" to "Dasbor Analitik",
        "tv_mode" to "Layar TV Publik",
        "role" to "Hak Akses",
        "admin" to "Admin Utama",
        "pengurus" to "Pengurus Masjid",
        "jemaah" to "Jemaah",
        "metode" to "Metode Perhitungan",
        "bahasa" to "Bahasa",
        "keamanan" to "Keamanan 2FA",
        "sinkronisasi" to "Sinkronisasi Cloud",
        "ekspor" to "Ekspor Laporan",
        "pemasukan" to "Pemasukan",
        "pengeluaran" to "Pengeluaran",
        "saldo" to "Total Saldo Kas",
        "aset_fisik" to "Inventaris Aset Fisik",
        "pengingat" to "Kustom Pengingat Adzan",
        "notif_sent" to "Notifikasi Pengingat Terkirim!",
        "sim_online" to "Mode Sinkronisasi Cloud Otomatis",
        "sim_offline" to "Mode Operasional Offline Aktif",
        "encrypt_active" to "Enkripsi AES-256 Aktif",
        "sync_pending" to "Menunggu Jaringan untuk Sinkronisasi"
    )

    private val enMap = mapOf(
        "app_title" to "MASJID JAMI' AT-TAQWA",
        "sholat" to "Prayer Times",
        "jadwal" to "Mosque Officers",
        "keuangan" to "Cash & Donation",
        "inventaris" to "Assets & Security",
        "pengaturan" to "Settings",
        "imsak" to "Imsak",
        "subuh" to "Fajr",
        "syuruk" to "Sunrise",
        "dzuhur" to "Dhuhr",
        "ashar" to "Asr",
        "maghrib" to "Maghrib",
        "isya" to "Isha",
        "jum'at" to "Friday",
        "kiblat" to "Qibla Compass",
        "masjid" to "Nearby Mosques",
        "donasi" to "Digital Donation",
        "analitik" to "Analytics Dashboard",
        "tv_mode" to "Public TV Display",
        "role" to "User Role",
        "admin" to "Main Admin",
        "pengurus" to "Mosque Staff",
        "jemaah" to "Congregation",
        "metode" to "Calculation Method",
        "bahasa" to "Language",
        "keamanan" to "2FA Security",
        "sinkronisasi" to "Cloud Sync",
        "ekspor" to "Export Reports",
        "pemasukan" to "Income",
        "pengeluaran" to "Expense",
        "saldo" to "Total Cash Balance",
        "aset_fisik" to "Physical Asset Inventory",
        "pengingat" to "Adzan Alarm Reminders",
        "notif_sent" to "Reminder Notification Sent!",
        "sim_online" to "Auto Cloud Sync Mode",
        "sim_offline" to "Offline Operational Mode",
        "encrypt_active" to "AES-256 Encryption Active",
        "sync_pending" to "Waiting for Network to Sync"
    )

    fun translate(key: String, lang: String): String {
        val lowerKey = key.lowercase()
        return if (lang == "in") {
            idMap[lowerKey] ?: key
        } else {
            enMap[lowerKey] ?: key
        }
    }
}

object ThemeColors {
    // Dynamic theme color configurations
    fun getThemeScheme(colorName: String, isDark: Boolean): ColorScheme {
        return when (colorName) {
            "EMERALD" -> {
                if (isDark) {
                    darkColorScheme(
                        primary = Color(0xFF10B981), // Emerald 500
                        onPrimary = Color(0xFF047857),
                        secondary = Color(0xFF34D399),
                        background = Color(0xFF090D16), // Dark slate
                        surface = Color(0xFF111827),    // Dark gray
                        onBackground = Color(0xFFF3F4F6),
                        onSurface = Color(0xFFF3F4F6)
                    )
                } else {
                    lightColorScheme(
                        primary = Color(0xFF047857), // Emerald 700
                        onPrimary = Color.White,
                        secondary = Color(0xFF059669),
                        background = Color(0xFFF4F6F4), // Clean soft greenish-gray
                        surface = Color.White,
                        onBackground = Color(0xFF1F2937),
                        onSurface = Color(0xFF1F2937)
                    )
                }
            }
            "GOLD" -> {
                if (isDark) {
                    darkColorScheme(
                        primary = Color(0xFFF59E0B), // Amber 500
                        onPrimary = Color(0xFF78350F),
                        secondary = Color(0xFFFBBF24),
                        background = Color(0xFF0F0B05),
                        surface = Color(0xFF1A140B),
                        onBackground = Color(0xFFFFFBEB),
                        onSurface = Color(0xFFFFFBEB)
                    )
                } else {
                    lightColorScheme(
                        primary = Color(0xFFB45309), // Amber 700
                        onPrimary = Color.White,
                        secondary = Color(0xFFD97706),
                        background = Color(0xFFFDFBF7),
                        surface = Color.White,
                        onBackground = Color(0xFF451A03),
                        onSurface = Color(0xFF451A03)
                    )
                }
            }
            "BLUE" -> {
                if (isDark) {
                    darkColorScheme(
                        primary = Color(0xFF0284C7), // Sky 600
                        onPrimary = Color(0xFF0F172A),
                        secondary = Color(0xFF38BDF8),
                        background = Color(0xFF0B0F17),
                        surface = Color(0xFF1E293B),
                        onBackground = Color(0xFFF0F9FF),
                        onSurface = Color(0xFFF0F9FF)
                    )
                } else {
                    lightColorScheme(
                        primary = Color(0xFF0369A1), // Sky 700
                        onPrimary = Color.White,
                        secondary = Color(0xFF0284C7),
                        background = Color(0xFFF0F7FF),
                        surface = Color.White,
                        onBackground = Color(0xFF0C4A6E),
                        onSurface = Color(0xFF0C4A6E)
                    )
                }
            }
            else -> {
                // Default fallback
                if (isDark) darkColorScheme() else lightColorScheme()
            }
        }
    }
}
