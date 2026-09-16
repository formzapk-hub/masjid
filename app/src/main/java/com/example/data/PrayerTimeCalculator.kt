package com.example.data

import kotlin.math.*

data class CityCoordinates(
    val name: String,
    val province: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: Double
)

data class PrayerTimes(
    val imsak: String,
    val subuh: String,
    val syuruk: String,
    val dzuhur: String,
    val ashar: String,
    val maghrib: String,
    val isya: String
)

object PrayerTimeCalculator {
    // Kaaba Coordinates
    private const val KAABA_LAT = 21.422524
    private const val KAABA_LON = 39.826218

    // List of major cities in Indonesia
    val cities = listOf(
        CityCoordinates("Jakarta", "DKI Jakarta", -6.2088, 106.8456, 7.0),
        CityCoordinates("Serang", "Banten", -6.1153, 106.1511, 7.0),
        CityCoordinates("Surabaya", "Jawa Timur", -7.2575, 112.7521, 7.0),
        CityCoordinates("Bandung", "Jawa Barat", -6.9175, 107.6191, 7.0),
        CityCoordinates("Medan", "Sumatera Utara", 3.5952, 98.6722, 7.0),
        CityCoordinates("Makassar", "Sulawesi Selatan", -5.1477, 119.4327, 8.0),
        CityCoordinates("Yogyakarta", "DI Yogyakarta", -7.7956, 110.3695, 7.0),
        CityCoordinates("Palembang", "Sumatera Selatan", -2.9761, 104.7754, 7.0),
        CityCoordinates("Semarang", "Jawa Tengah", -6.9667, 110.4167, 7.0),
        CityCoordinates("Denpasar", "Bali", -8.6705, 115.2126, 8.0),
        CityCoordinates("Banda Aceh", "Aceh", 5.5483, 95.3238, 7.0),
        CityCoordinates("Pontianak", "Kalimantan Barat", -0.0263, 109.3425, 7.0),
        CityCoordinates("Jayapura", "Papua", -2.5337, 140.7181, 9.0)
    )

    /**
     * Calculates the Qibla bearing from North (0 - 360 degrees)
     */
    fun calculateQiblaDirection(lat: Double, lon: Double): Double {
        val latRad = Math.toRadians(lat)
        val mLatRad = Math.toRadians(KAABA_LAT)
        val dLonRad = Math.toRadians(KAABA_LON - lon)

        val y = sin(dLonRad)
        val x = cos(latRad) * tan(mLatRad) - sin(latRad) * cos(dLonRad)
        
        var qiblaDeg = Math.toDegrees(atan2(y, x))
        qiblaDeg = (qiblaDeg + 360.0) % 360.0
        return qiblaDeg
    }

    /**
     * Estimates accurate prayer times mathematically.
     * D: Day of year (1-365)
     * method: 0 = Kemenag RI (Fajr -20, Isha -18), 1 = MWL (Fajr -18, Isha -17), 2 = ISNA (Fajr -15, Isha -15)
     */
    fun calculatePrayerTimes(
        city: CityCoordinates,
        dayOfYear: Int,
        method: Int
    ): PrayerTimes {
        val lat = city.latitude
        val lon = city.longitude
        val tz = city.timezone

        // 1. Solar calculations
        // Angle b representing the day of the year
        val b = (2.0 * Math.PI / 365.0) * (dayOfYear - 81)
        
        // Equation of Time (EoT) in minutes
        val eot = 9.87 * sin(2.0 * b) - 7.53 * cos(b) - 1.5 * sin(b)
        
        // Declination (delta) in radians
        val declination = Math.toRadians(23.45 * sin(b))

        // Latitude in radians
        val latRad = Math.toRadians(lat)

        // Local Noon (Dzuhur) transit in hours
        val noonLocal = 12.0 + tz - (lon / 15.0) - (eot / 60.0)

        // Fajr/Subuh angle based on method
        val fajrAngle = when (method) {
            0 -> -20.0  // Kemenag RI
            1 -> -18.0  // MWL
            2 -> -15.0  // ISNA
            3 -> -19.5  // Egypt
            else -> -18.5 // Umm al-Qura
        }

        // Isha angle based on method
        val ishaAngle = when (method) {
            0 -> -18.0  // Kemenag RI
            1 -> -17.0  // MWL
            2 -> -15.0  // ISNA
            3 -> -17.5  // Egypt
            else -> -18.0 // Umm al-Qura / General
        }

        // Helper to calculate Hour Angle (H) for a given altitude angle (in degrees)
        fun getHourAngle(angle: Double): Double {
            val angleRad = Math.toRadians(angle)
            val denom = cos(latRad) * cos(declination)
            if (denom == 0.0) return 6.0 // Fallback

            val cosH = (sin(angleRad) - sin(latRad) * sin(declination)) / denom
            // Clip between -1 and 1
            val cosHClipped = max(-1.0, min(1.0, cosH))
            return Math.toDegrees(acos(cosHClipped)) / 15.0
        }

        // 2. Compute times
        val hSunrise = getHourAngle(-0.833) // Sunrise
        val hFajr = getHourAngle(fajrAngle)
        val hIsha = getHourAngle(ishaAngle)

        // Asr Angle calculation
        val tanDeltaLatDiff = abs(tan(latRad - declination))
        val asrAngleRad = atan(1.0 / (1.0 + tanDeltaLatDiff))
        val asrAngle = Math.toDegrees(asrAngleRad)
        val hAsr = getHourAngle(-asrAngle) // Angle below horizon for Asr

        // 3. Format times as "HH:MM" (adding Indonesian ihtiyati / safety buffers of ~2-3 mins)
        fun formatTime(hourValue: Double, bufferMinutes: Int = 0): String {
            val totalMinutes = (hourValue * 60.0).roundToInt() + bufferMinutes
            val wrappedMinutes = (totalMinutes + 1440) % 1440
            val h = wrappedMinutes / 60
            val m = wrappedMinutes % 60
            return String.format("%02d:%02d", h, m)
        }

        // Calculate individual timings
        val dzuhurVal = noonLocal
        val syurukVal = noonLocal - hSunrise
        val maghribVal = noonLocal + hSunrise
        val subuhVal = noonLocal - hFajr
        val asharVal = noonLocal + hAsr
        val isyaVal = noonLocal + hIsha

        // Add typical buffers used in Indonesia for safety (Kemenag RI practices):
        // Dzuhur: +2 mins, Ashar: +2 mins, Maghrib: +2 mins, Isya: +2 mins, Subuh: +2 mins
        val subuhStr = formatTime(subuhVal, 2)
        val imsakStr = formatTime(subuhVal, -8) // Imsak is 10 mins before Subuh (so we do Subuh - 10, including the +2 buffer we subtract 8)
        val syurukStr = formatTime(syurukVal, -2)
        val dzuhurStr = formatTime(dzuhurVal, 2)
        val asharStr = formatTime(asharVal, 2)
        val maghribStr = formatTime(maghribVal, 2)
        val isyaStr = formatTime(isyaVal, 2)

        return PrayerTimes(
            imsak = imsakStr,
            subuh = subuhStr,
            syuruk = syurukStr,
            dzuhur = dzuhurStr,
            ashar = asharStr,
            maghrib = maghribStr,
            isya = isyaStr
        )
    }

    /**
     * Converts Gregorian date to a simple simulated Hijri date
     * Standard Islamic calendar converter.
     */
    fun convertToHijri(year: Int, month: Int, day: Int): String {
        // Tabular Islamic Calendar Approximation
        var jd = if (month <= 2) {
            val y = year - 1
            val m = month + 12
            (365.25 * y).toInt() + (30.6001 * (m + 1)).toInt() + day + 1720996.5
        } else {
            val y = year
            val m = month
            (365.25 * y).toInt() + (30.6001 * (m + 1)).toInt() + day + 1720996.5
        }

        // Adjust for Gregorian calendar transition
        if (year > 1582 || (year == 1582 && (month > 10 || (month == 10 && day >= 15)))) {
            val a = (year / 100).toInt()
            val b = 2 - a + (a / 4).toInt()
            jd += b
        }

        val l = jd.toLong() - 1948440L
        val n = (l / 10631L)
        val lRemaining = l % 10631L
        val j = (lRemaining / 354L)
        val jRemaining = lRemaining % 354L
        val hYear = (n * 30L) + j + 1L

        val islamicMonths = listOf(
            "Muharram", "Safar", "Rabi'ul Awwal", "Rabi'ul Akhir",
            "Jumadil Awwal", "Jumadil Akhir", "Rajab", "Sya'ban",
            "Ramadhan", "Syawwal", "Dzulqa'dah", "Dzulhijjah"
        )

        // Find Islamic day and month based on remaining days
        var daysAccum = 0
        var hMonth = 1
        var hDay = 1
        val isLeapYear = ((11 * hYear + 14) % 30) < 11

        for (m in 1..12) {
            val monthDays = when (m) {
                12 -> if (isLeapYear) 30 else 29
                else -> if (m % 2 != 0) 30 else 29
            }
            if (jRemaining < daysAccum + monthDays) {
                hMonth = m
                hDay = (jRemaining - daysAccum + 1).toInt()
                break
            }
            daysAccum += monthDays
        }

        val monthName = islamicMonths.getOrElse(hMonth - 1) { "Ramadhan" }
        return "$hDay $monthName ${hYear} H"
    }

    /**
     * Returns simulated local nearby mosques
     */
    fun getNearbyMosques(city: CityCoordinates): List<MosqueItem> {
        return listOf(
            MosqueItem("Masjid Agung Al-Falah", "Jl. Jenderal Sudirman No. 12", 0.4, -6.21, 106.84),
            MosqueItem("Masjid Raya Baiturrahman", "Jl. Merdeka Barat No. 5", 0.9, -6.22, 106.85),
            MosqueItem("Masjid Jami An-Nur", "Jl. Diponegoro Raya No. 45", 1.2, -6.20, 106.83),
            MosqueItem("Masjid Istiqomah", "Jl. Kebon Sirih No. 100", 1.8, -6.19, 106.86),
            MosqueItem("Masjid Baitul Mukminin", "Jl. Raden Saleh No. 8", 2.5, -6.23, 106.82)
        ).map {
            // Apply scale offsets based on actual city lat/lon to simulate distance
            it.copy(
                latitude = city.latitude + (it.latitude - (-6.21)),
                longitude = city.longitude + (it.longitude - 106.84)
            )
        }
    }
}

data class MosqueItem(
    val name: String,
    val address: String,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double
)
