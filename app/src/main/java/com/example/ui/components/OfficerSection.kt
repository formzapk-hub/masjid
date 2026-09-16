package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.OfficerSchedule
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerSection(viewModel: MainViewModel) {
    val context = LocalContext.current
    val schedules by viewModel.allSchedules.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val settings by viewModel.currentSettings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedScheduleForWa by remember { mutableStateOf<OfficerSchedule?>(null) }
    var filterDate by remember { mutableStateOf("") } // Date filter (YYYY-MM-DD)

    // Distinct dates present in schedule
    val dates = remember(schedules) {
        schedules.map { it.date }.distinct().sorted()
    }

    // Set initial date filter if empty
    LaunchedEffect(dates) {
        if (filterDate.isEmpty() && dates.isNotEmpty()) {
            filterDate = dates[0]
        }
    }

    val filteredSchedules = remember(schedules, filterDate) {
        schedules.filter { it.date == filterDate }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = LocaleManager.translate("jadwal", settings.selectedLanguage),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kelola Imam, Muadzin, dan Petugas Masjid",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Add button (Allowed for Admin & Pengurus)
            if (userRole == "ADMIN" || userRole == "PENGURUS") {
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_schedule_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Schedule", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Jadwal Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // Display informative label for Jemaah
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = "Mode Lihat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Horizontal Date Selector Slider
        if (dates.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Dates", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                
                // Render Dates horizontal row
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(dates) { date ->
                        val isSelected = filterDate == date
                        // Format date to local friendly
                        val formattedDate = try {
                            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val formatter = SimpleDateFormat("dd MMM (EEEE)", Locale("in", "ID"))
                            formatter.format(parser.parse(date) ?: Date())
                        } catch (e: Exception) {
                            date
                        }

                        Card(
                            modifier = Modifier
                                .clickable { filterDate = date }
                                .testTag("date_tab_$date"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Empty dates list placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Belum ada jadwal petugas yang tercatat. Silakan ganti peran ke Pengurus / Admin untuk membuat jadwal.",
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main List Content
        Box(modifier = Modifier.weight(1f)) {
            if (filteredSchedules.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = "Empty Schedule",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tidak ada jadwal shalat tercatat pada tanggal ini",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSchedules) { schedule ->
                        OfficerScheduleCard(
                            schedule = schedule,
                            userRole = userRole,
                            onDelete = { viewModel.deleteSchedule(schedule) },
                            onSimulateWa = { selectedScheduleForWa = schedule },
                            onShare = {
                                val isFriday = schedule.prayerTime.equals("Jum'at", ignoreCase = true)
                                val bilalLine = if (isFriday && schedule.bilal.isNotEmpty()) "\nBilal: ${schedule.bilal}" else ""
                                val text = "JADWAL PETUGAS MASJID\nHari/Tgl: ${schedule.date}\nSholat: ${schedule.prayerTime}\nImam: ${schedule.imam}\nMuadzin: ${schedule.muadzin}$bilalLine\nPetugas Lain: ${schedule.otherOfficer}\n\nDisinkronkan otomatis oleh MASJID JAMI' AT-TAQWA App."
                                viewModel.addSyncLog("Bagikan", "Membagikan jadwal ${schedule.prayerTime} via teks tim", "SUCCESS")
                                Toast.makeText(context, "Jadwal disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Schedule Dialog Form
    if (showAddDialog) {
        var dateVal by remember { mutableStateOf(if (filterDate.isNotEmpty()) filterDate else "2026-09-16") }
        var prayerVal by remember { mutableStateOf("Subuh") }
        var imamVal by remember { mutableStateOf("") }
        var muadzinVal by remember { mutableStateOf("") }
        var otherVal by remember { mutableStateOf("Remaja Masjid") }
        var bilalVal by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = "Tambah Jadwal Petugas", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = dateVal,
                        onValueChange = { dateVal = it },
                        label = { Text("Tanggal (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Simple select slot
                    var expandedPrayer by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = prayerVal,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Waktu Shalat") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { expandedPrayer = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expandedPrayer,
                            onDismissRequest = { expandedPrayer = false }
                        ) {
                            val options = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya", "Jum'at")
                            options.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        prayerVal = opt
                                        expandedPrayer = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = imamVal,
                        onValueChange = { imamVal = it },
                        label = { Text(if (prayerVal == "Jum'at") "Nama Khatib / Imam" else "Nama Imam") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = muadzinVal,
                        onValueChange = { muadzinVal = it },
                        label = { Text("Nama Muadzin") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (prayerVal == "Jum'at") {
                        OutlinedTextField(
                            value = bilalVal,
                            onValueChange = { bilalVal = it },
                            label = { Text("Nama Bilal") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = otherVal,
                        onValueChange = { otherVal = it },
                        label = { Text("Petugas Tambahan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (imamVal.isEmpty() || muadzinVal.isEmpty()) {
                            Toast.makeText(context, "Silakan lengkapi nama Imam & Muadzin!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (prayerVal == "Jum'at" && bilalVal.isEmpty()) {
                            Toast.makeText(context, "Silakan lengkapi nama Bilal!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.insertSchedule(dateVal, prayerVal, imamVal, muadzinVal, otherVal, if (prayerVal == "Jum'at") bilalVal else "")
                        showAddDialog = false
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // WhatsApp Reminder Simulator Dialog
    if (selectedScheduleForWa != null) {
        val sch = selectedScheduleForWa!!
        val parsedWaMessage = remember(sch) {
            val isFriday = sch.prayerTime.equals("Jum'at", ignoreCase = true)
            val bilalPart = if (isFriday && sch.bilal.isNotEmpty()) " dan Bilal *${sch.bilal}*" else ""
            "Assalamu'alaikum Wr. Wb. Yth. Bapak/Saudara *${sch.imam}* (Imam/Khatib), *${sch.muadzin}* (Muadzin)${bilalPart},\n\nMengingatkan kembali jadwal tugas Anda pada sholat *${sch.prayerTime}* tanggal *${sch.date}* di MASJID JAMI' AT-TAQWA.\n\nMohon hadir 15 menit sebelum adzan berkumandang. Terima kasih atas kesediaan dan khidmatnya.\n\n_Pemberitahuan otomatis oleh MASJID JAMI' AT-TAQWA App._"
        }

        AlertDialog(
            onDismissRequest = { selectedScheduleForWa = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = "WA Icon", tint = Color(0xFF25D366))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "WhatsApp Reminder Simulator", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Simulasi pengiriman notifikasi WhatsApp otomatis kepada petugas terjadwal:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    // Simulated chat bubble
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFECE5DD)) // WhatsApp background
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCF8C6)) // WA outgoing bubble
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "AT-TAQWA Bot",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF075E54),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = parsedWaMessage,
                                color = Color.Black,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Text(
                        text = "Notifikasi status WhatsApp otomatis kepada petugas: AKTIF. Petugas akan menerima alert WA instan 30 menit sebelum adzan.",
                        fontSize = 11.sp,
                        color = Color(0xFF075E54),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                    onClick = {
                        val isFriday = sch.prayerTime.equals("Jum'at", ignoreCase = true)
                        val targets = if (isFriday && sch.bilal.isNotEmpty()) "${sch.imam}, ${sch.muadzin} & ${sch.bilal}" else "${sch.imam} & ${sch.muadzin}"
                        viewModel.addSyncLog("WhatsApp Bot", "Mengirim WhatsApp alert ke $targets", "SUCCESS")
                        Toast.makeText(context, "WhatsApp reminder berhasil disimulasikan!", Toast.LENGTH_LONG).show()
                        selectedScheduleForWa = null
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Kirim WA", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kirim Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedScheduleForWa = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun OfficerScheduleCard(
    schedule: OfficerSchedule,
    userRole: String,
    onDelete: () -> Unit,
    onSimulateWa: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_card_${schedule.prayerTime.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Card Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Prayer Slot",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = schedule.prayerTime.uppercase(),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp
                    )
                }

                // Sync status indicator
                if (schedule.pendingSync) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SyncProblem, contentDescription = "Offline pending", tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tertunda", color = Color(0xFFF59E0B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDone, contentDescription = "Synced", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sinkron", color = Color(0xFF10B981), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Officers list grid representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isFriday = schedule.prayerTime.equals("Jum'at", ignoreCase = true)
                // Imam Col
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    Text(text = if (isFriday) "KHATIB" else "IMAM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    Text(text = schedule.imam, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                // Muadzin Col
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    Text(text = "MUADZIN", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    Text(text = schedule.muadzin, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                // Bilal Col (if Friday)
                if (isFriday) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(8.dp)
                    ) {
                        Text(text = "BILAL", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Text(text = if (schedule.bilal.isNotEmpty()) schedule.bilal else "-", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }

            // Other Officer
            if (schedule.otherOfficer.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Other info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Petugas Tambahan: ${schedule.otherOfficer}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Actions: WA Simulator & Share
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onSimulateWa,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "WhatsApp", modifier = Modifier.size(14.dp), tint = Color(0xFF25D366))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Notif WA", fontSize = 11.sp, color = Color(0xFF25D366), fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = onShare,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Delete Action (Allowed for Admin/Pengurus only)
                if (userRole == "ADMIN" || userRole == "PENGURUS") {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_schedule_${schedule.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
