package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Security
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
import com.example.data.InventoryAsset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSection(viewModel: MainViewModel) {
    val context = LocalContext.current
    val assets by viewModel.allAssets.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val syncing by viewModel.isSyncing.collectAsState()
    val syncLogs by viewModel.syncLogs.collectAsState()
    val settings by viewModel.currentSettings.collectAsState()

    var activeSubTab by remember { mutableStateOf("ASET") } // "ASET", "ROLE", "SYNC", "MFA"
    var showAddAssetDialog by remember { mutableStateOf(false) }
    var editingAsset by remember { mutableStateOf<InventoryAsset?>(null) }

    // 2FA Lock states
    var is2faVerified by remember { mutableStateOf(!settings.mfaEnabled) }
    var showPinPrompt by remember { mutableStateOf(false) }

    // Re-verify 2FA status when settings change
    LaunchedEffect(settings.mfaEnabled) {
        is2faVerified = !settings.mfaEnabled
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Section Header Sub tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                "ASET" to Icons.Default.Inventory,
                "ROLE" to Icons.Default.AdminPanelSettings,
                "SYNC" to Icons.Default.CloudSync,
                "MFA" to Icons.Default.Security
            )

            tabs.forEach { (tab, icon) ->
                val active = activeSubTab == tab
                Button(
                    onClick = { activeSubTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("admin_sub_tab_${tab.lowercase()}"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, contentDescription = tab, modifier = Modifier.size(16.dp))
                        Text(
                            text = when (tab) {
                                "ASET" -> "Aset Fisik"
                                "ROLE" -> "Akses Role"
                                "SYNC" -> "Cloud Sync"
                                else -> "Sandi 2FA"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Intercept with 2FA Pin Keypad lock if tab is ASET and MFA is enabled and not yet verified
        if (activeSubTab == "ASET" && settings.mfaEnabled && !is2faVerified) {
            MfaLockScreen(
                onCorrectPin = {
                    is2faVerified = true
                    Toast.makeText(context, "Autentikasi Dua Faktor Berhasil!", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Main Sub Tab panels
            Box(modifier = Modifier.weight(1f)) {
                when (activeSubTab) {
                    "ASET" -> AssetsSubSection(viewModel, assets, userRole, onAddClick = { showAddAssetDialog = true }, onEditClick = { editingAsset = it })
                    "ROLE" -> RoleSubSection(viewModel, userRole)
                    "SYNC" -> SyncSubSection(viewModel, isOnline, syncing, syncLogs)
                    "MFA" -> MfaSetupSubSection(viewModel, settings)
                }
            }
        }
    }

    // Add Asset Dialog Form
    if (showAddAssetDialog) {
        var nameVal by remember { mutableStateOf("") }
        var qtyVal by remember { mutableStateOf("1") }
        var conditionVal by remember { mutableStateOf("Baik") }
        var locVal by remember { mutableStateOf("Ruang Utama") }

        AlertDialog(
            onDismissRequest = { showAddAssetDialog = false },
            title = { Text(text = "Daftarkan Aset Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nameVal,
                        onValueChange = { nameVal = it },
                        label = { Text("Nama Barang / Aset") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = qtyVal,
                        onValueChange = { qtyVal = it },
                        label = { Text("Jumlah (Unit)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Condition Dropdown
                    var expandedCond by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = conditionVal,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kondisi Fisik") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { expandedCond = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Drop")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expandedCond,
                            onDismissRequest = { expandedCond = false }
                        ) {
                            listOf("Baik", "Rusak Ringan", "Rusak Berat").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        conditionVal = opt
                                        expandedCond = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = locVal,
                        onValueChange = { locVal = it },
                        label = { Text("Lokasi Penyimpanan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qtyParsed = qtyVal.toIntOrNull()
                        if (nameVal.isEmpty() || qtyParsed == null || qtyParsed <= 0) {
                            Toast.makeText(context, "Silakan masukkan data aset yang valid!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.insertAsset(nameVal, qtyParsed, conditionVal, locVal)
                        showAddAssetDialog = false
                    }
                ) {
                    Text("Daftarkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAssetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Edit Asset Dialog Form
    if (editingAsset != null) {
        val asset = editingAsset!!
        var nameVal by remember { mutableStateOf(asset.name) }
        var qtyVal by remember { mutableStateOf(asset.quantity.toString()) }
        var conditionVal by remember { mutableStateOf(asset.condition) }
        var locVal by remember { mutableStateOf(asset.location) }

        AlertDialog(
            onDismissRequest = { editingAsset = null },
            title = { Text(text = "Ubah Data Aset Fisik", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nameVal,
                        onValueChange = { nameVal = it },
                        label = { Text("Nama Barang / Aset") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = qtyVal,
                        onValueChange = { qtyVal = it },
                        label = { Text("Jumlah (Unit)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Condition Dropdown
                    var expandedCond by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = conditionVal,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kondisi Fisik") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { expandedCond = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Drop")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expandedCond,
                            onDismissRequest = { expandedCond = false }
                        ) {
                            listOf("Baik", "Rusak Ringan", "Rusak Berat").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        conditionVal = opt
                                        expandedCond = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = locVal,
                        onValueChange = { locVal = it },
                        label = { Text("Lokasi Penyimpanan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qtyParsed = qtyVal.toIntOrNull()
                        if (nameVal.isEmpty() || qtyParsed == null || qtyParsed <= 0) {
                            Toast.makeText(context, "Silakan masukkan data aset yang valid!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.updateAsset(asset.id, nameVal, qtyParsed, conditionVal, locVal)
                        editingAsset = null
                    }
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingAsset = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun AssetsSubSection(
    viewModel: MainViewModel,
    assets: List<InventoryAsset>,
    userRole: String,
    onAddClick: () -> Unit,
    onEditClick: (InventoryAsset) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Aset & Inventaris Fisik", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Daftar inventaris sarana & prasarana milik organisasi masjid", fontSize = 11.sp, color = Color.Gray)
            }
            if (userRole == "ADMIN" || userRole == "PENGURUS") {
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Daftar Baru", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            if (assets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Belum ada inventaris terdaftar", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(assets) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Handyman, contentDescription = "Asset", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${item.quantity} Unit",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "•  ${item.location}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val condColor = when (item.condition) {
                                    "Baik" -> Color(0xFF10B981)
                                    "Rusak Ringan" -> Color(0xFFF59E0B)
                                    else -> Color(0xFFEF4444)
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = condColor.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(100.dp)
                                ) {
                                    Text(
                                        text = item.condition,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = condColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                
                                if (userRole == "ADMIN" || userRole == "PENGURUS") {
                                    IconButton(
                                        onClick = { onEditClick(item) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteAsset(item) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoleSubSection(viewModel: MainViewModel, activeRole: String) {
    val roles = listOf(
        "ADMIN" to "Akses penuh mencakup pengelolaan keuangan, edit jadwal petugas, edit aset, dan modifikasi pengaturan kalkulasi waktu sholat utama.",
        "PENGURUS" to "Akses menengah untuk mengelola pencatatan keuangan harian, menyusun jadwal petugas sholat, serta menginventarisasi aset masjid.",
        "JEMAAH" to "Akses lihat saja (Read-Only) yang aman. Dirancang ramah pengguna bagi jemaah untuk berdonasi QR digital dan melihat jadwal kegiatan ibadah."
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Multi-User Role Clearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = "Sesuaikan hak akses aplikasi untuk menyimulasikan keamanan data pengurus", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        roles.forEach { (role, desc) ->
            val isSelected = activeRole == role
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        viewModel.currentUserRole.value = role
                        viewModel.addSyncLog("Otoritas", "Perubahan peran pengguna ke $role", "SUCCESS")
                    }
                    .testTag("role_pill_$role"),
                shape = RoundedCornerShape(16.dp),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            viewModel.currentUserRole.value = role
                            viewModel.addSyncLog("Otoritas", "Perubahan peran pengguna ke $role", "SUCCESS")
                        }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (role == "ADMIN") "ADMINISTRATOR (Super Admin)" else if (role == "PENGURUS") "PENGURUS (Staff)" else "JEMAAH (Congregation)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = desc, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SyncSubSection(
    viewModel: MainViewModel,
    isOnline: Boolean,
    isSyncing: Boolean,
    logs: List<com.example.SyncLog>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Sinc Log & Offline Database", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = "Simulasi sinkronisasi data real-time ketika jaringan terhubung kembali", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(12.dp))

        // Large Switch Card for Offline / Online Simulation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOnline) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isOnline) "KONEKSI INTERNET: AKTIF" else "KONEKSI INTERNET: MATI",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = if (isOnline) Color(0xFF065F46) else Color(0xFF991B1B)
                    )
                    Text(
                        text = if (isOnline) "Data otomatis tersinkron ke Cloud." else "Semua perubahan disimpan ke database lokal Room.",
                        fontSize = 11.sp,
                        color = if (isOnline) Color(0xFF047857) else Color(0xFFB91C1C)
                    )
                }

                Switch(
                    checked = isOnline,
                    onCheckedChange = { viewModel.toggleNetworkConnection() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF10B981),
                        uncheckedThumbColor = Color(0xFFEF4444)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Trigger Sync Button
        if (isOnline) {
            Button(
                onClick = { viewModel.triggerSyncProcess() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSyncing,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Menghubungkan & Enkripsi data...")
                } else {
                    Icon(Icons.Default.Sync, contentDescription = "Sync")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PAKSA SINKRONISASI SEKARANG")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sync logs feed
        Text(text = "LOG AKTIVITAS DATA & JARINGAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            LazyColumn(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logs) { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = log.action, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${log.device} • ${log.timestamp}", fontSize = 10.sp, color = Color.Gray)
                        }

                        val badgeColor = when (log.status) {
                            "SUCCESS" -> Color(0xFF10B981)
                            "PENDING" -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text(
                                text = log.status,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MfaSetupSubSection(viewModel: MainViewModel, settings: com.example.data.AppSetting) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Keamanan Autentikasi Dua Faktor (2FA)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = "Melindungi data sensitif organisasi dari modifikasi yang tidak terotorisasi", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Security, contentDescription = "MFA", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Aktifkan PIN 2FA", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Switch(
                        checked = settings.mfaEnabled,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(mfaEnabled = it))
                            val action = if (it) "mengaktifkan" else "menonaktifkan"
                            viewModel.addSyncLog("Keamanan 2FA", "Pengguna $action autentikasi pin ganda", "SUCCESS")
                            Toast.makeText(context, "Sandi 2FA berhasil diubah!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Text(
                    text = "Ketika diaktifkan, modul sensitif seperti Inventaris Aset Fisik akan terkunci di balik sandi PIN 2FA (Bawaan: '2026'). Verifikasi ini disimulasikan sebagai pengaman berlapis ganda demi privasi keuangan organisasi.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun MfaLockScreen(onCorrectPin: () -> Unit) {
    var pinValue by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Keamanan 2FA Aktif", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = "Masukkan PIN Autentikasi Pengurus untuk Membuka", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        // Password bullets representation
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            for (i in 0..3) {
                val isFilled = i < pinValue.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isError) Color.Red 
                            else if (isFilled) MaterialTheme.colorScheme.primary 
                            else Color.LightGray
                        )
                )
            }
        }

        if (isError) {
            Text(text = "PIN Salah! Petunjuk: '2026'", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dynamic simulated pin keypad
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("Clear", "0", "OK")
            )

            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { char ->
                        Button(
                            onClick = {
                                isError = false
                                when (char) {
                                    "Clear" -> {
                                        if (pinValue.isNotEmpty()) pinValue = pinValue.dropLast(1)
                                    }
                                    "OK" -> {
                                        if (pinValue == "2026") {
                                            onCorrectPin()
                                        } else {
                                            isError = true
                                            pinValue = ""
                                        }
                                    }
                                    else -> {
                                        if (pinValue.length < 4) {
                                            pinValue += char
                                        }
                                    }
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (char == "OK" || char == "Clear") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                contentColor = if (char == "OK" || char == "Clear") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Text(text = char, fontSize = if (char.length > 2) 10.sp else 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
