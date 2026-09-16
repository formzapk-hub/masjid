package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.currentSettings.collectAsState()
            val themeColorScheme = remember(settings.themeColor, settings.isDarkMode) {
                ThemeColors.getThemeScheme(settings.themeColor, settings.isDarkMode)
            }

            MaterialTheme(
                colorScheme = themeColorScheme,
                typography = MaterialTheme.typography
            ) {
                var inTvMode by remember { mutableStateOf(false) }

                if (inTvMode) {
                    // Fullscreen widescreen Public TV Display overlay
                    TvDisplaySection(viewModel) {
                        inTvMode = false
                    }
                } else {
                    // Main Mobile App Layout
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                        topBar = {
                            AppHeaderBar(
                                viewModel = viewModel,
                                onTvModeClick = { inTvMode = true }
                            )
                        },
                        bottomBar = {
                            AppBottomNavBar(viewModel = viewModel)
                        }
                    ) { innerPadding ->
                        val currentTab by viewModel.currentTab.collectAsState()
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            AnimatedContent(
                                targetState = currentTab,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "tab_navigation_anim"
                            ) { tab ->
                                when (tab) {
                                    "sholat" -> PrayerSection(viewModel)
                                    "jadwal" -> OfficerSection(viewModel)
                                    "keuangan" -> FinanceSection(viewModel)
                                    "inventaris" -> AdminSection(viewModel)
                                    "settings" -> CoreSettingsSection(viewModel)
                                    else -> PrayerSection(viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeaderBar(viewModel: MainViewModel, onTvModeClick: () -> Unit) {
    val settings by viewModel.currentSettings.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val syncing by viewModel.isSyncing.collectAsState()

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mosque,
                    contentDescription = "Logo MASJID JAMI' AT-TAQWA",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LocaleManager.translate("app_title", settings.selectedLanguage),
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
        },
        navigationIcon = {
            // Simulated real-time Sync Indicator button
            IconButton(
                onClick = { viewModel.triggerSyncProcess() },
                modifier = Modifier.testTag("sync_indicator_button")
            ) {
                if (syncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = "Sync Indicator",
                        tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        actions = {
            // Enter Public TV mode trigger button
            IconButton(
                onClick = onTvModeClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .testTag("tv_mode_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Mode TV Publik",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

@Composable
fun AppBottomNavBar(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val settings by viewModel.currentSettings.collectAsState()

    val navItems = listOf(
        Triple("sholat", Icons.Default.Schedule, "sholat"),
        Triple("jadwal", Icons.Default.People, "jadwal"),
        Triple("keuangan", Icons.Default.AccountBalanceWallet, "keuangan"),
        Triple("inventaris", Icons.Default.AdminPanelSettings, "inventaris"),
        Triple("settings", Icons.Default.Settings, "pengaturan")
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        navItems.forEach { (tabId, icon, labelKey) ->
            val isSelected = currentTab == tabId
            NavigationBarItem(
                selected = isSelected,
                onClick = { viewModel.currentTab.value = tabId },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tabId,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = LocaleManager.translate(labelKey, settings.selectedLanguage),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                ),
                modifier = Modifier.testTag("nav_item_$tabId")
            )
        }
    }
}

// 5. Settings Screen Composable (Dynamic preferences)
@Composable
fun CoreSettingsSection(viewModel: MainViewModel) {
    val settings by viewModel.currentSettings.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = LocaleManager.translate("pengaturan", settings.selectedLanguage),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = "Kustomisasi tampilan, bahasa, dan preferensi aplikasi MASJID JAMI' AT-TAQWA",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Theme selector block (Emerald / Gold / Blue)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Personalisasi Warna Tema",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = "Pilih aksen warna nuansa islami sesuai preferensi Anda", fontSize = 11.sp, color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val themeOptions = listOf(
                                "EMERALD" to "Hijau Emerald",
                                "GOLD" to "Amber Emas",
                                "BLUE" to "Biru Samudra"
                            )
                            themeOptions.forEach { (code, name) ->
                                val active = settings.themeColor == code
                                val accentColor = when (code) {
                                    "EMERALD" -> Color(0xFF10B981)
                                    "GOLD" -> Color(0xFFF59E0B)
                                    else -> Color(0xFF0284C7)
                                }
                                
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.updateSettings(settings.copy(themeColor = code))
                                        }
                                        .testTag("theme_btn_$code"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (active) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    border = if (active) androidx.compose.foundation.BorderStroke(2.dp, accentColor) else null,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(accentColor))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = name, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dark Mode switch
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Mode Gelap (Dark Theme)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Mengurangi kelelahan mata saat penggunaan malam hari", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = settings.isDarkMode,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(isDarkMode = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("dark_mode_toggle")
                        )
                    }
                }
            }

            // Multi-Language toggle (ID / EN)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = LocaleManager.translate("bahasa", settings.selectedLanguage), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "Pilih bahasa antarmuka aplikasi", fontSize = 11.sp, color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("in" to "Bahasa Indonesia", "en" to "English (US)").forEach { (code, name) ->
                                val active = settings.selectedLanguage == code
                                Button(
                                    onClick = { viewModel.updateSettings(settings.copy(selectedLanguage = code)) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("lang_btn_$code"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // App developer info & Security certification
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = "Security Verified", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Sertifikasi Keamanan & Integritas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Aplikasi MASJID JAMI' AT-TAQWA mematuhi standar enkripsi lokal tingkat lanjut (AES-256) untuk melindungi seluruh data kas donasi, aset inventaris, serta password pengurus secara offline-first dan transparan.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
