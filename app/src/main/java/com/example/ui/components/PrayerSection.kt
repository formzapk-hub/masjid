package com.example.ui.components

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.CityCoordinates
import com.example.data.PrayerTimeCalculator
import com.example.data.PrayerTimes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerSection(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settings by viewModel.currentSettings.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val dayOfYear by viewModel.dayOfYear.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val compassAzimuth by viewModel.compassAzimuth.collectAsState()

    // Active calculations based on settings and day of year
    val prayerTimes = remember(selectedCity, dayOfYear, settings.calculationMethod) {
        PrayerTimeCalculator.calculatePrayerTimes(selectedCity, dayOfYear, settings.calculationMethod)
    }

    var selectedMethodDialog by remember { mutableStateOf(false) }
    var selectedLocationDialog by remember { mutableStateOf(false) }
    var activeSubTab by remember { mutableStateOf("WAKTU") } // "WAKTU", "KIBLAT", "MASJID", "WIDGET"

    // Next prayer helper
    val timeString by viewModel.timeString.collectAsState()
    val nextPrayerInfo = remember(prayerTimes, timeString) {
        getNextPrayer(prayerTimes, timeString)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Main Location & Date Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable { selectedLocationDialog = true }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = selectedCity.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedCity.province,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Location"
                        )
                    }

                    // Network/Offline status pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(
                                if (isOnline) MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.errorContainer
                            )
                            .padding(vertical = 4.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOnline) "Cloud Online" else "Offline Mode",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOnline) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hijri & Gregorian Dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        val hijriStr by viewModel.currentHijriDateStr.collectAsState()
                        Text(
                            text = hijriStr,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val gregStr by viewModel.currentDateStr.collectAsState()
                        Text(
                            text = gregStr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    // Next countdown badge
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Berikutnya: ${nextPrayerInfo.name}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "-${nextPrayerInfo.countdown}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Sub Tabs row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                "WAKTU" to Icons.Default.Schedule,
                "KIBLAT" to Icons.Default.CompassCalibration,
                "MASJID" to Icons.Default.Mosque,
                "WIDGET" to Icons.Default.Widgets
            )

            tabs.forEach { (tab, icon) ->
                val active = activeSubTab == tab
                Button(
                    onClick = { 
                        activeSubTab = tab
                        if (tab == "KIBLAT") {
                            viewModel.rotateCompass()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("tab_${tab.lowercase()}"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, contentDescription = tab, modifier = Modifier.size(16.dp))
                        Text(
                            text = LocaleManager.translate(tab, settings.selectedLanguage),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sub Tab Content Panel
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                "WAKTU" -> PrayerTimesSubSection(viewModel, prayerTimes, settings, nextPrayerInfo.name) {
                    selectedMethodDialog = true
                }
                "KIBLAT" -> QiblaSubSection(viewModel)
                "MASJID" -> NearMosquesSubSection(viewModel)
                "WIDGET" -> WidgetPreviewSubSection(viewModel, prayerTimes)
            }
        }
    }

    // Calculations Methods Selector dialog
    if (selectedMethodDialog) {
        AlertDialog(
            onDismissRequest = { selectedMethodDialog = false },
            title = { Text(LocaleManager.translate("metode", settings.selectedLanguage)) },
            text = {
                Column {
                    val methods = listOf(
                        0 to "Kemenag RI (Indonesia)",
                        1 to "Muslim World League (MWL)",
                        2 to "ISNA (North America)",
                        3 to "Egyptian General Authority",
                        4 to "Umm al-Qura (Makkah)"
                    )
                    methods.forEach { (id, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSettings(settings.copy(calculationMethod = id))
                                    selectedMethodDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.calculationMethod == id,
                                onClick = {
                                    viewModel.updateSettings(settings.copy(calculationMethod = id))
                                    selectedMethodDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMethodDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Location Selector dialog
    if (selectedLocationDialog) {
        AlertDialog(
            onDismissRequest = { selectedLocationDialog = false },
            title = { Text("Pilih Wilayah / Kota") },
            text = {
                Box(modifier = Modifier.height(300.dp)) {
                    LazyColumn {
                        items(PrayerTimeCalculator.cities) { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectedCity.value = city
                                        viewModel.addSyncLog("Wilayah", "Mengubah wilayah ke ${city.name}, ${city.province}", "SUCCESS")
                                        selectedLocationDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = city.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text(text = city.province, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                if (selectedCity.name == city.name) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedLocationDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun PrayerTimesSubSection(
    viewModel: MainViewModel,
    times: PrayerTimes,
    settings: com.example.data.AppSetting,
    activeNext: String,
    onMethodSelect: () -> Unit
) {
    val context = LocalContext.current
    val items = remember(times) {
        listOf(
            Triple("Imsak", times.imsak, settings.imsakReminder),
            Triple("Subuh", times.subuh, settings.subuhReminder),
            Triple("Syuruk", times.syuruk, false), // No adzan for syuruk sunrise
            Triple("Dzuhur", times.dzuhur, settings.dzuhurReminder),
            Triple("Ashar", times.ashar, settings.asharReminder),
            Triple("Maghrib", times.maghrib, settings.maghribReminder),
            Triple("Isya", times.isya, settings.isyaReminder)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Jadwal Shalat Hari Ini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = onMethodSelect,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Calculation Method", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                val methodName = when (settings.calculationMethod) {
                    0 -> "Kemenag"
                    1 -> "MWL"
                    2 -> "ISNA"
                    3 -> "Egypt"
                    else -> "UmmAlQura"
                }
                Text(text = "Metode: $methodName", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Adzan customizable list
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { (name, time, reminder) ->
                    val isActive = name.equals(activeNext, ignoreCase = true)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (name) {
                                    "Imsak" -> Icons.Default.ModeNight
                                    "Subuh" -> Icons.Default.WbTwilight
                                    "Syuruk" -> Icons.Default.WbSunny
                                    "Dzuhur" -> Icons.Default.LightMode
                                    "Ashar" -> Icons.Default.FilterDrama
                                    "Maghrib" -> Icons.Default.WbCloudy
                                    else -> Icons.Default.NightsStay
                                },
                                contentDescription = name,
                                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = LocaleManager.translate(name, settings.selectedLanguage),
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isActive) {
                                    Text(text = "Waktu Berikutnya", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = time,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            // Custom reminder sound selector icon button
                            if (name != "Syuruk") {
                                IconButton(
                                    onClick = {
                                        // Save reminder toggle state
                                        val updatedSettings = when (name) {
                                            "Imsak" -> settings.copy(imsakReminder = !settings.imsakReminder)
                                            "Subuh" -> settings.copy(subuhReminder = !settings.subuhReminder)
                                            "Dzuhur" -> settings.copy(dzuhurReminder = !settings.dzuhurReminder)
                                            "Ashar" -> settings.copy(asharReminder = !settings.asharReminder)
                                            "Maghrib" -> settings.copy(maghribReminder = !settings.maghribReminder)
                                            else -> settings.copy(isyaReminder = !settings.isyaReminder)
                                        }
                                        viewModel.updateSettings(updatedSettings)
                                        
                                        val nextState = if (reminder) "Diam" else "Adzan"
                                        Toast.makeText(context, "${LocaleManager.translate("notif_sent", settings.selectedLanguage)} $name: $nextState", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (reminder) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                        contentDescription = "Notification Toggle",
                                        tint = if (reminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QiblaSubSection(viewModel: MainViewModel) {
    val selectedCity by viewModel.selectedCity.collectAsState()
    val compassAzimuth by viewModel.compassAzimuth.collectAsState()
    val accuracy by viewModel.compassAccuracy.collectAsState()

    val targetQibla = remember(selectedCity) {
        PrayerTimeCalculator.calculateQiblaDirection(selectedCity.latitude, selectedCity.longitude)
    }

    // Relative angle between Qibla Mecca and current azimuth
    // If phone rotates, needle rotates relative to device azimuth
    val relativeNeedleAngle = (targetQibla - compassAzimuth + 360f) % 360f

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kiblat Terkalibrasi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = "Accurate", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Akurasi: $accuracy", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large physical Qibla compass dial simulator
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .clickable { viewModel.rotateCompass() }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Compass Ring (drawn with custom Canvas for exquisite look)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(-compassAzimuth) // Rotates whole dial as sensor values change
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f
                
                // Outer circle border
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.4f),
                    radius = radius - 2.dp.toPx(),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )

                // Outer ticks (cardinal lines)
                val cardinals = listOf(0f to "N", 90f to "E", 180f to "S", 270f to "W")
                cardinals.forEach { (angle, text) ->
                    rotate(angle, center) {
                        // Draw tick mark
                        drawLine(
                            color = if (text == "N") Color.Red else Color.Gray,
                            start = Offset(center.x, 8.dp.toPx()),
                            end = Offset(center.x, 20.dp.toPx()),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }
            }

            // High precision Kaaba pointer needle (Relative angle)
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "Qibla Direction Pointer",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .rotate(relativeNeedleAngle.toFloat()) // Rotates needle to Mecca
            )

            // Inner Center Kaaba Icon Logo representation
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mosque,
                    contentDescription = "Kaaba",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Analytical details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Arah Kakbah", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(text = "${targetQibla.roundToInt()}° UTARA", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Sensor Kompas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(text = "${compassAzimuth.roundToInt()}° UTARA", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        
        Text(
            text = "Tip: Letakkan ponsel di permukaan datar dan jauhkan dari perangkat logam untuk akurasi sensor geomagnetik maksimal.",
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)
        )
    }
}

@Composable
fun NearMosquesSubSection(viewModel: MainViewModel) {
    val selectedCity by viewModel.selectedCity.collectAsState()
    val mosques = remember(selectedCity) {
        PrayerTimeCalculator.getNearbyMosques(selectedCity)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Daftar Masjid Terdekat Offline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Berdasarkan estimasi radius lokasi GPS saat ini (${selectedCity.name})",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(mosques) { mosque ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Mosque, contentDescription = "Mosque Icon", tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = mosque.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text(text = mosque.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }

                        // Distance badge
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${mosque.distanceKm} km",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    // Simulated Navigation intent launcher
                                    viewModel.addSyncLog("Peta", "Membuka navigasi ke ${mosque.name}", "SUCCESS")
                                }
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = "Rute", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "RUTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetPreviewSubSection(viewModel: MainViewModel, times: PrayerTimes) {
    val selectedCity by viewModel.selectedCity.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Widget Layar Utama (Home Screen)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "MASJID JAMI' AT-TAQWA menyediakan widget transparan interaktif untuk aksesibilitas waktu sholat instan dari beranda ponsel Anda.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Beautiful Simulated Home Screen Widget Layout
            Text(
                text = "PRATINJAU WIDGET (UKURAN 4x2)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Simulated Widget Frame
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)) // Slate-800
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Widget Top Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Mosque, contentDescription = "Logo", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "AT-TAQWA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            }
                            Text(text = selectedCity.name, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Widget Center Display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Maghrib Shalat", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Text(text = "Countdown: -00:14:20", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(text = times.maghrib, color = Color(0xFF10B981), fontWeight = FontWeight.Black, fontSize = 28.sp)
                        }

                        // Widget Bottom Prayer Slots Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val widgetPrayers = listOf("Subuh" to times.subuh, "Dzuhur" to times.dzuhur, "Ashar" to times.ashar, "Maghrib" to times.maghrib, "Isya" to times.isya)
                            widgetPrayers.forEach { (name, time) ->
                                val isHighlight = name == "Maghrib"
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isHighlight) Color(0xFF10B981).copy(alpha = 0.2f) else Color.Transparent)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(text = name, color = if (isHighlight) Color(0xFF10B981) else Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                                    Text(text = time, color = if (isHighlight) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Widget Action Button
        Button(
            onClick = {
                // Instantly simulated PIN widget launcher
                viewModel.addSyncLog("Widget Utama", "Memasang widget interaktif MASJID JAMI' AT-TAQWA ke beranda Launcher", "SUCCESS")
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Pasang")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "PASANG WIDGET DI LAYAR UTAMA")
        }
    }
}

// Helper models for countdown calculations
data class NextPrayerInfo(
    val name: String,
    val time: String,
    val countdown: String
)

fun getNextPrayer(times: PrayerTimes, timeString: String): NextPrayerInfo {
    val simpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formatShort = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    try {
        val currTime = simpleDateFormat.parse(timeString) ?: Date()
        val prayers = listOf(
            "Imsak" to times.imsak,
            "Subuh" to times.subuh,
            "Syuruk" to times.syuruk,
            "Dzuhur" to times.dzuhur,
            "Ashar" to times.ashar,
            "Maghrib" to times.maghrib,
            "Isya" to times.isya
        )

        for ((name, pTime) in prayers) {
            val prayerDate = formatShort.parse(pTime) ?: Date()
            val prayerCal = Calendar.getInstance().apply {
                time = currTime
                set(Calendar.HOUR_OF_DAY, prayerDate.hours)
                set(Calendar.MINUTE, prayerDate.minutes)
                set(Calendar.SECOND, 0)
            }
            
            val diffMs = prayerCal.timeInMillis - Calendar.getInstance().apply { time = currTime }.timeInMillis
            if (diffMs > 0) {
                // Found next prayer
                val hours = (diffMs / (1000 * 60 * 60)).toInt()
                val mins = ((diffMs / (1000 * 60)) % 60).toInt()
                val secs = ((diffMs / 1000) % 60).toInt()
                val cdStr = String.format("%02d:%02d:%02d", hours, mins, secs)
                return NextPrayerInfo(name, pTime, cdStr)
            }
        }
        
        // If all prayers passed, next is Imsak tomorrow
        val imsakDate = formatShort.parse(times.imsak) ?: Date()
        val imsakTomorrow = Calendar.getInstance().apply {
            time = currTime
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, imsakDate.hours)
            set(Calendar.MINUTE, imsakDate.minutes)
            set(Calendar.SECOND, 0)
        }
        val diffMs = imsakTomorrow.timeInMillis - Calendar.getInstance().apply { time = currTime }.timeInMillis
        val hours = (diffMs / (1000 * 60 * 60)).toInt()
        val mins = ((diffMs / (1000 * 60)) % 60).toInt()
        val secs = ((diffMs / 1000) % 60).toInt()
        val cdStr = String.format("%02d:%02d:%02d", hours, mins, secs)
        return NextPrayerInfo("Imsak (Besok)", times.imsak, cdStr)
        
    } catch (e: Exception) {
        return NextPrayerInfo("Dzuhur", "12:00", "02:15:00")
    }
}
