package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.PrayerTimeCalculator

@Composable
fun TvDisplaySection(viewModel: MainViewModel, onExitTvMode: () -> Unit) {
    val selectedCity by viewModel.selectedCity.collectAsState()
    val settings by viewModel.currentSettings.collectAsState()
    val dayOfYear by viewModel.dayOfYear.collectAsState()
    
    // Time & Dates
    val timeString by viewModel.timeString.collectAsState()
    val gregDateStr by viewModel.currentDateStr.collectAsState()
    val hijriDateStr by viewModel.currentHijriDateStr.collectAsState()

    // Active prayer times calculations
    val prayerTimes = remember(selectedCity, dayOfYear, settings.calculationMethod) {
        PrayerTimeCalculator.calculatePrayerTimes(selectedCity, dayOfYear, settings.calculationMethod)
    }

    // Determine upcoming highlight slot
    val nextPrayerInfo = remember(prayerTimes, timeString) {
        getNextPrayer(prayerTimes, timeString)
    }

    // Breathing light blink effect for active slot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Simulated Marquee scrolling banner offset
    val marqueeTransition = rememberInfiniteTransition(label = "marquee")
    val marqueeOffset by marqueeTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = -1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "marquee_offset"
    )

    // Public TV widescreen container (Black luxury background)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Deep space slate-900
            .padding(16.dp)
            .testTag("tv_display_root")
    ) {
        // TV Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onExitTvMode, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(100.dp))) {
                    Icon(Icons.Default.FullscreenExit, contentDescription = "Exit TV Mode", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "MASJID RAYA AL-FALAH",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${selectedCity.name.uppercase()} • ${selectedCity.province.uppercase()}",
                        fontSize = 12.sp,
                        color = Color(0xFF10B981), // Emerald-500
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Date Displays
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = hijriDateStr.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFFFBBF24) // Amber golden
                )
                Text(
                    text = gregDateStr,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center visual panels: Big Digital Clock & Announcements
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Big Clock Box (60% width)
            Card(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)) // Slate-800
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WAKTU SHALAT SETEMPAT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 64.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Display Countdown banner to next prayer time
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Menuju ${nextPrayerInfo.name}: -${nextPrayerInfo.countdown}",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // TV Sidebar Announcements / Kegiatan (40% width)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = "Agenda", tint = Color(0xFFFBBF24))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "AGENDAS MASJID", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    }

                    Divider(color = Color.White.copy(alpha = 0.1f))

                    val agendas = listOf(
                        "Kajian Ba'da Maghrib: Ustadz Dr. KH. Miftah" to "Sebab-Sebab Keberkahan Rezeki",
                        "Kerja Bakti Sosial Masjid" to "Ahad, Pukul 07.00 WIB s/d Selesai",
                        "Penerimaan Donasi Ambulans" to "Terbuka - Saluran Rekening Masjid"
                    )

                    agendas.forEach { (title, detail) ->
                        Column {
                            Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            Text(text = detail, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Prayer Grid (Imsak, Subuh, Syuruk, Dzuhur, Ashar, Maghrib, Isya)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tvSlots = listOf(
                "Imsak" to prayerTimes.imsak,
                "Subuh" to prayerTimes.subuh,
                "Syuruk" to prayerTimes.syuruk,
                "Dzuhur" to prayerTimes.dzuhur,
                "Ashar" to prayerTimes.ashar,
                "Maghrib" to prayerTimes.maghrib,
                "Isya" to prayerTimes.isya
            )

            tvSlots.forEach { (name, time) ->
                val isNextHighlight = name.equals(nextPrayerInfo.name, ignoreCase = true)
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNextHighlight) Color(0xFF047857) else Color(0xFF1E293B)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = name.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNextHighlight) Color.White else Color.White.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = time,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isNextHighlight) Color(0xFFFBBF24) else Color.White
                        )
                        
                        // Breathing active slot indicator dot
                        if (isNextHighlight) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFBBF24).copy(alpha = pulseAlpha))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Running Text Marquee Bottom Ticker bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)) // Slate-800
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val bannerText = "PENGUMUMAN URGEN JAMA'AH: Shalat berjamaah merapatkan shaf shalat. Kajian Rutin Tafsir Al-Quran setiap ba'da Maghrib ditiadakan sementara untuk hari Jumat pekan ini karena persiapan renovasi ruang wudhu. Gunakan QR Code MASJID JAMI' AT-TAQWA di tiang masjid untuk donasi pembangunan digital secara transparan dan aman."
                
                // Simulating horizontal marquee slide
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val maxW = maxWidth
                    val translationX = maxW * marqueeOffset
                    Text(
                        text = bannerText,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.offset(x = translationX)
                    )
                }
            }
        }
    }
}
