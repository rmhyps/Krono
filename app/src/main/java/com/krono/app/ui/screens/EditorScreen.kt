package com.krono.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.krono.app.ui.components.ActionPickerDialog
import com.krono.app.ui.components.BluetoothDevicePickerDialog
import com.krono.app.ui.components.LogicBlock
import com.krono.app.ui.components.MapPickerDialog
import com.krono.app.ui.components.TriggerPickerDialog
import com.krono.app.ui.components.WallpaperCropDialog
import com.krono.app.ui.theme.KronoBackground
import com.krono.app.ui.theme.KronoError
import com.krono.app.ui.theme.KronoOnPrimary
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoOutline
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoSecondary
import com.krono.app.ui.theme.KronoSurface
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoSurfaceHigh
import com.krono.app.ui.theme.KronoTertiary
import com.krono.app.ui.theme.KronoWarning
import com.krono.app.ui.viewmodel.KronoViewModel
import com.krono.core.fixtures.KronoFixtures
import com.krono.core.model.ActionDefinition
import com.krono.core.model.ActionKind
import com.krono.core.model.BranchDefinition
import com.krono.core.model.BranchKind
import com.krono.core.model.ConditionPredicate
import com.krono.core.model.PredicateKind
import com.krono.core.model.RuleDefinition
import com.krono.core.model.RuleMode
import com.krono.core.model.TriggerDefinition
import com.krono.core.model.TriggerFamily
import com.krono.core.model.TriggerKind
import com.krono.core.model.WallpaperTarget
import com.krono.core.validation.RuleValidationResult
import com.krono.core.validation.RuleValidator
import kotlinx.coroutines.launch
import java.util.UUID

class EditableActionState(
    val actionId: String = "act-" + UUID.randomUUID().toString().take(8),
    initialKind: ActionKind = ActionKind.SET_MEDIA_VOLUME,
    initialVolume: Int = 20,
    initialWallpaperTarget: WallpaperTarget = WallpaperTarget.SYSTEM,
    initialAssetId: String = KronoFixtures.ASSET_IMAGE_A_ID
) {
    var kind by mutableStateOf(initialKind)
    var volumePercent by mutableFloatStateOf(initialVolume.toFloat())
    var wallpaperTarget by mutableStateOf(initialWallpaperTarget)
    var assetId by mutableStateOf(initialAssetId)

    fun toDomain(): ActionDefinition {
        return when (kind) {
            ActionKind.SET_MEDIA_VOLUME -> ActionDefinition(
                actionId = actionId,
                kind = ActionKind.SET_MEDIA_VOLUME,
                volumePercent = volumePercent.toInt()
            )
            ActionKind.SET_WALLPAPER -> ActionDefinition(
                actionId = actionId,
                kind = ActionKind.SET_WALLPAPER,
                assetId = assetId.ifBlank { KronoFixtures.ASSET_IMAGE_A_ID },
                wallpaperTarget = wallpaperTarget
            )
        }
    }
}

class EditableBranchState(
    val branchId: String = "branch-" + UUID.randomUUID().toString().take(8),
    initialConditionKind: PredicateKind = PredicateKind.ALWAYS,
    initialTargetId: String = "",
    initialLatitude: String = "-7.894303",
    initialLongitude: String = "112.595458",
    initialRadiusM: String = "150",
    initialActions: List<EditableActionState> = emptyList()
) {
    var conditionKind by mutableStateOf(initialConditionKind)
    var targetId by mutableStateOf(initialTargetId)
    var latitude by mutableStateOf(initialLatitude)
    var longitude by mutableStateOf(initialLongitude)
    var radiusM by mutableStateOf(initialRadiusM)
    val actions = mutableStateListOf<EditableActionState>().apply { addAll(initialActions) }

    fun toDomain(kind: BranchKind): BranchDefinition {
        val resolvedTargetId = targetId.trim().ifBlank { "area-$branchId" }
        val predicate = when (conditionKind) {
            PredicateKind.LOCATION_IN_AREA -> ConditionPredicate(
                kind = PredicateKind.LOCATION_IN_AREA,
                areaId = resolvedTargetId,
                latitude = latitude.toDoubleOrNull() ?: -7.894303,
                longitude = longitude.toDoubleOrNull() ?: 112.595458,
                radiusM = radiusM.toDoubleOrNull() ?: 150.0
            )
            PredicateKind.BLUETOOTH_IS_CONNECTED -> ConditionPredicate(
                kind = PredicateKind.BLUETOOTH_IS_CONNECTED,
                deviceId = targetId.ifBlank { KronoFixtures.TWS_DEVICE_ID }
            )
            PredicateKind.BLUETOOTH_IS_DISCONNECTED -> ConditionPredicate(
                kind = PredicateKind.BLUETOOTH_IS_DISCONNECTED,
                deviceId = targetId.ifBlank { KronoFixtures.TWS_DEVICE_ID }
            )
            PredicateKind.ALWAYS -> ConditionPredicate(kind = PredicateKind.ALWAYS)
            PredicateKind.NONE -> ConditionPredicate(kind = PredicateKind.NONE)
        }

        return BranchDefinition(
            branchId = branchId,
            kind = kind,
            condition = predicate,
            actions = actions.map { it.toDomain() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    initialRule: RuleDefinition?,
    viewModel: KronoViewModel,
    onNavigateBack: () -> Unit,
    onOpenSimulation: (RuleDefinition) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val ruleId = remember { initialRule?.ruleId ?: "rule-" + UUID.randomUUID().toString() }
    var ruleName by remember { mutableStateOf(initialRule?.name ?: "") }
    var selectedFamily by remember { mutableStateOf<TriggerFamily?>(initialRule?.trigger?.family) }
    var selectedKind by remember { mutableStateOf<TriggerKind?>(initialRule?.trigger?.kind) }
    var triggerTitle by remember {
        mutableStateOf(
            if (initialRule?.trigger?.family == TriggerFamily.LOCATION) "Perubahan Area Geofence"
            else if (initialRule?.trigger?.family == TriggerFamily.BLUETOOTH) "Bluetooth TWS Tersambung"
            else ""
        )
    }

    var triggerDeviceId by remember { mutableStateOf(initialRule?.trigger?.deviceId ?: "") }
    var triggerDeviceName by remember { mutableStateOf("") }

    var isAdvancedBranching by remember {
        mutableStateOf(initialRule != null && (initialRule.branches.size > 1 || initialRule.elseBranch != null))
    }

    var showTriggerPicker by remember { mutableStateOf(false) }
    var showBluetoothPicker by remember { mutableStateOf(false) }
    var actionPickerTargetBranchIndex by remember { mutableStateOf<Int?>(null) }
    var isAddingActionToElse by remember { mutableStateOf(false) }
    var mapPickerTargetBranch by remember { mutableStateOf<EditableBranchState?>(null) }

    val branches = remember {
        mutableStateListOf<EditableBranchState>().apply {
            if (initialRule != null) {
                for (b in initialRule.branches) {
                    val branchActions = b.actions.map { a ->
                        EditableActionState(
                            actionId = a.actionId,
                            initialKind = a.kind,
                            initialVolume = a.volumePercent ?: 20,
                            initialWallpaperTarget = a.wallpaperTarget ?: WallpaperTarget.SYSTEM,
                            initialAssetId = a.assetId ?: KronoFixtures.ASSET_IMAGE_A_ID
                        )
                    }
                    val target = b.condition.areaId ?: b.condition.deviceId ?: ""
                    val lat = b.condition.latitude?.let { String.format(java.util.Locale.US, "%.6f", it) } ?: "-7.894303"
                    val lng = b.condition.longitude?.let { String.format(java.util.Locale.US, "%.6f", it) } ?: "112.595458"
                    val rad = b.condition.radiusM?.toInt()?.toString() ?: "150"
                    add(
                        EditableBranchState(
                            branchId = b.branchId,
                            initialConditionKind = b.condition.kind,
                            initialTargetId = target,
                            initialLatitude = lat,
                            initialLongitude = lng,
                            initialRadiusM = rad,
                            initialActions = branchActions
                        )
                    )
                }
            } else {
                add(
                    EditableBranchState(
                        branchId = "branch-1",
                        initialConditionKind = PredicateKind.ALWAYS,
                        initialTargetId = "",
                        initialActions = emptyList()
                    )
                )
            }
        }
    }

    var hasElseBranch by remember { mutableStateOf(initialRule?.elseBranch != null) }
    val elseActions = remember {
        mutableStateListOf<EditableActionState>().apply {
            val initialElse = initialRule?.elseBranch
            if (initialElse != null) {
                addAll(initialElse.actions.map { a ->
                    EditableActionState(
                        actionId = a.actionId,
                        initialKind = a.kind,
                        initialVolume = a.volumePercent ?: 20,
                        initialWallpaperTarget = a.wallpaperTarget ?: WallpaperTarget.SYSTEM,
                        initialAssetId = a.assetId ?: KronoFixtures.ASSET_IMAGE_A_ID
                    )
                })
            }
        }
    }

    var validationErrorMessage by remember { mutableStateOf<String?>(null) }
    var activeBranchForLocationPermission by remember { mutableStateOf<EditableBranchState?>(null) }
    var pendingRuleToSave by remember { mutableStateOf<RuleDefinition?>(null) }
    var showLocationPermissionDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val grantedFine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val grantedCoarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        val pending = pendingRuleToSave
        if (pending != null) {
            pendingRuleToSave = null
            if (grantedFine) {
                viewModel.saveRule(pending, active = true)
            } else {
                viewModel.saveRule(pending, active = false)
            }
            onNavigateBack()
            return@rememberLauncherForActivityResult
        }

        if (grantedFine || grantedCoarse) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        activeBranchForLocationPermission?.latitude = String.format(java.util.Locale.US, "%.6f", loc.latitude)
                        activeBranchForLocationPermission?.longitude = String.format(java.util.Locale.US, "%.6f", loc.longitude)
                    }
                }
            } catch (_: SecurityException) {}
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationForBranch(branch: EditableBranchState) {
        activeBranchForLocationPermission = branch
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    branch.latitude = String.format(java.util.Locale.US, "%.6f", loc.latitude)
                    branch.longitude = String.format(java.util.Locale.US, "%.6f", loc.longitude)
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    fun buildRuleFromState(): RuleDefinition? {
        val family = selectedFamily
        if (family == null) {
            validationErrorMessage = "Pilih pemicu (KETIKA) terlebih dahulu"
            return null
        }

        val name = ruleName.trim().ifBlank { "Aturan Baru" }

        if (!isAdvancedBranching) {
            val singleBranch = branches.firstOrNull()
            if (singleBranch == null || singleBranch.actions.isEmpty()) {
                validationErrorMessage = "Tambahkan minimal satu aksi pada LAKUKAN"
                return null
            }

            val actions = singleBranch.actions.map { it.toDomain() }

            return when (family) {
                TriggerFamily.BLUETOOTH -> {
                    val devId = triggerDeviceId.trim().ifBlank { KronoFixtures.TWS_DEVICE_ID }
                    RuleDefinition(
                        ruleId = ruleId,
                        name = name,
                        mode = RuleMode.SIMPLE,
                        trigger = TriggerDefinition(
                            family = TriggerFamily.BLUETOOTH,
                            kind = selectedKind ?: TriggerKind.BLUETOOTH_CONNECTED,
                            deviceId = devId
                        ),
                        branches = listOf(
                            BranchDefinition(
                                branchId = "b1",
                                kind = BranchKind.ALWAYS,
                                condition = ConditionPredicate(kind = PredicateKind.ALWAYS),
                                actions = actions
                            )
                        ),
                        elseBranch = null
                    )
                }
                TriggerFamily.LOCATION -> {
                    val areaId = singleBranch.targetId.trim().ifBlank { "area-1" }
                    RuleDefinition(
                        ruleId = ruleId,
                        name = name,
                        mode = RuleMode.BRANCHED,
                        trigger = TriggerDefinition(
                            family = TriggerFamily.LOCATION,
                            kind = TriggerKind.GEOFENCE_MEMBERSHIP_CHANGED,
                            areaIds = listOf(areaId)
                        ),
                        branches = listOf(
                            BranchDefinition(
                                branchId = "branch-1",
                                kind = BranchKind.IF,
                                condition = ConditionPredicate(
                                    kind = PredicateKind.LOCATION_IN_AREA,
                                    areaId = areaId,
                                    latitude = singleBranch.latitude.toDoubleOrNull() ?: -7.894303,
                                    longitude = singleBranch.longitude.toDoubleOrNull() ?: 112.595458,
                                    radiusM = singleBranch.radiusM.toDoubleOrNull() ?: 150.0
                                ),
                                actions = actions
                            )
                        ),
                        elseBranch = null
                    )
                }
            }
        } else {
            val trigger = when (family) {
                TriggerFamily.BLUETOOTH -> TriggerDefinition(
                    family = TriggerFamily.BLUETOOTH,
                    kind = selectedKind ?: TriggerKind.BLUETOOTH_CONNECTED,
                    deviceId = triggerDeviceId.ifBlank { KronoFixtures.TWS_DEVICE_ID }
                )
                TriggerFamily.LOCATION -> {
                    val areaIds = branches.map { it.targetId.trim().ifBlank { "area-${it.branchId}" } }.distinct()
                    TriggerDefinition(
                        family = TriggerFamily.LOCATION,
                        kind = TriggerKind.GEOFENCE_MEMBERSHIP_CHANGED,
                        areaIds = areaIds
                    )
                }
            }

            val domainBranches = branches.mapIndexed { index, b ->
                val branchKind = if (index == 0) BranchKind.IF else BranchKind.ELSE_IF
                b.toDomain(branchKind)
            }

            val domainElse = if (hasElseBranch && elseActions.isNotEmpty()) {
                BranchDefinition(
                    branchId = "branch-else",
                    kind = BranchKind.ELSE,
                    condition = ConditionPredicate(kind = PredicateKind.NONE),
                    actions = elseActions.map { it.toDomain() }
                )
            } else null

            return RuleDefinition(
                ruleId = ruleId,
                name = name,
                mode = RuleMode.BRANCHED,
                trigger = trigger,
                branches = domainBranches,
                elseBranch = domainElse
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialRule == null) "Buat Aturan" else "Edit Aturan",
                        color = KronoOnSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = KronoOnSurface)
                    }
                },
                actions = {
                    if (initialRule != null) {
                        IconButton(onClick = {
                            viewModel.deleteRule(ruleId)
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Aturan", tint = KronoError)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KronoSurface)
            )
        },
        containerColor = KronoBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = ruleName,
                onValueChange = {
                    ruleName = it
                    validationErrorMessage = null
                },
                label = { Text("Nama Aturan") },
                placeholder = { Text("Contoh: Otomasi TWS, Otomasi Kampus") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KronoPrimary,
                    unfocusedBorderColor = KronoOutline,
                    focusedTextColor = KronoOnSurface,
                    unfocusedTextColor = KronoOnSurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LogicBlock(
                tag = "KETIKA",
                title = if (selectedFamily == null) "Pemicu Kejadian" else triggerTitle
            ) {
                if (selectedFamily == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = KronoSurfaceHigh, shape = RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Belum ada pemicu dipilih. Tekan tombol di bawah untuk memilih kejadian yang akan memicu aturan ini.",
                                color = KronoOnSurfaceVariant,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showTriggerPicker = true },
                                colors = ButtonDefaults.buttonColors(containerColor = KronoSecondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Pilih Pemicu", color = KronoOnPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedFamily == TriggerFamily.BLUETOOTH) "Perangkat Bluetooth" else "Sensor Lokasi / Geofence",
                                color = KronoOnSurfaceVariant,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = triggerTitle,
                                color = KronoSecondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = { showTriggerPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = KronoSecondary.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Ubah Pemicu", color = KronoSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedFamily == TriggerFamily.BLUETOOTH) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = KronoSurfaceHigh),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (triggerDeviceName.isNotBlank()) triggerDeviceName else "Perangkat Bluetooth",
                                        color = KronoOnSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = triggerDeviceId.ifBlank { "Belum ada perangkat dipilih" },
                                        color = KronoOnSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Button(
                                    onClick = { showBluetoothPicker = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = KronoSecondary.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Pilih Perangkat", color = KronoSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (selectedFamily == TriggerFamily.LOCATION && !isAdvancedBranching && branches.isNotEmpty()) {
                        val branch0 = branches[0]
                        branch0.conditionKind = PredicateKind.LOCATION_IN_AREA

                        OutlinedTextField(
                            value = branch0.targetId,
                            onValueChange = { branch0.targetId = it },
                            label = { Text("Nama Area Geofence") },
                            placeholder = { Text("Contoh: Kampus, Rumah, Kantor") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KronoPrimary,
                                unfocusedBorderColor = KronoOutline,
                                focusedTextColor = KronoOnSurface,
                                unfocusedTextColor = KronoOnSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Koordinat: ${branch0.latitude}, ${branch0.longitude}",
                                color = KronoOnSurfaceVariant,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Radius: ${branch0.radiusM}m",
                                color = KronoTertiary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { requestLocationForBranch(branch0) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = KronoPrimary.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = KronoPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GPS Terkini", color = KronoPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { mapPickerTargetBranch = branch0 },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = KronoSecondary.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = KronoSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pilih di Peta", color = KronoSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isAdvancedBranching) {
                val singleBranch = branches.firstOrNull() ?: remember {
                    val nb = EditableBranchState(branchId = "branch-1")
                    branches.add(nb)
                    nb
                }

                LogicBlock(
                    tag = "LAKUKAN",
                    title = "Aksi yang Dijalankan"
                ) {
                    if (singleBranch.actions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = KronoSurfaceHigh, shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Belum ada aksi yang ditambahkan. Tekan tombol di bawah untuk menambahkan aksi.",
                                color = KronoOnSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        singleBranch.actions.forEachIndexed { actionIndex, actionState ->
                            ActionEditorRow(
                                actionState = actionState,
                                viewModel = viewModel,
                                onDelete = { singleBranch.actions.removeAt(actionIndex) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            actionPickerTargetBranchIndex = 0
                            isAddingActionToElse = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = KronoTertiary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Aksi", color = KronoTertiary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = {
                        isAdvancedBranching = true
                        if (selectedFamily == TriggerFamily.LOCATION && branches.isNotEmpty()) {
                            branches[0].conditionKind = PredicateKind.LOCATION_IN_AREA
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Tambah Percabangan Syarat (Tingkat Lanjut)", color = KronoOnSurfaceVariant, fontSize = 13.sp)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Percabangan Syarat (IF / ELSE IF)",
                            color = KronoOnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "First-match: cabang pertama yang cocok akan dieksekusi",
                            color = KronoOnSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    TextButton(onClick = {
                        isAdvancedBranching = false
                        hasElseBranch = false
                        elseActions.clear()
                        while (branches.size > 1) {
                            branches.removeAt(branches.size - 1)
                        }
                    }) {
                        Text("Sederhanakan", color = KronoPrimary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                branches.forEachIndexed { branchIndex, branchState ->
                    val branchTag = if (branchIndex == 0) "JIKA (IF)" else "KALAU TIDAK, JIKA (ELSE IF)"

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = KronoSurfaceContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = branchTag,
                                    color = KronoPrimary,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                Row {
                                    if (branchIndex > 0) {
                                        IconButton(onClick = {
                                            val temp = branches[branchIndex]
                                            branches[branchIndex] = branches[branchIndex - 1]
                                            branches[branchIndex - 1] = temp
                                        }) {
                                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Naikkan", tint = KronoOnSurfaceVariant)
                                        }
                                    }
                                    if (branchIndex < branches.size - 1) {
                                        IconButton(onClick = {
                                            val temp = branches[branchIndex]
                                            branches[branchIndex] = branches[branchIndex + 1]
                                            branches[branchIndex + 1] = temp
                                        }) {
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Turunkan", tint = KronoOnSurfaceVariant)
                                        }
                                    }
                                    if (branches.size > 1) {
                                        IconButton(onClick = {
                                            branches.removeAt(branchIndex)
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus Cabang", tint = KronoError)
                                        }
                                    }
                                }
                            }

                            if (selectedFamily == TriggerFamily.LOCATION) {
                                Spacer(modifier = Modifier.height(6.dp))
                                branchState.conditionKind = PredicateKind.LOCATION_IN_AREA

                                OutlinedTextField(
                                    value = branchState.targetId,
                                    onValueChange = { branchState.targetId = it },
                                    label = { Text("Nama Area Geofence") },
                                    placeholder = { Text("Contoh: Kampus, Rumah, Kantor") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = KronoPrimary,
                                        unfocusedBorderColor = KronoOutline,
                                        focusedTextColor = KronoOnSurface,
                                        unfocusedTextColor = KronoOnSurface
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Koordinat: ${branchState.latitude}, ${branchState.longitude}",
                                        color = KronoOnSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Radius: ${branchState.radiusM}m",
                                        color = KronoTertiary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { requestLocationForBranch(branchState) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = KronoPrimary.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = KronoPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("GPS Terkini", color = KronoPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { mapPickerTargetBranch = branchState },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = KronoSecondary.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = KronoSecondary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pilih di Peta", color = KronoSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "LAKUKAN (Aksi di Cabang Ini):",
                                color = KronoTertiary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (branchState.actions.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = KronoSurfaceHigh, shape = RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Belum ada aksi di cabang ini. Tekan tombol di bawah untuk menambahkan aksi.",
                                        color = KronoOnSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            } else {
                                branchState.actions.forEachIndexed { actionIndex, actionState ->
                                    ActionEditorRow(
                                        actionState = actionState,
                                        viewModel = viewModel,
                                        onDelete = { branchState.actions.removeAt(actionIndex) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    actionPickerTargetBranchIndex = branchIndex
                                    isAddingActionToElse = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = KronoTertiary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tambah Aksi ke Cabang Ini", color = KronoTertiary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val nextNum = branches.size + 1
                        branches.add(
                            EditableBranchState(
                                branchId = "branch-$nextNum",
                                initialConditionKind = if (selectedFamily == TriggerFamily.LOCATION) PredicateKind.LOCATION_IN_AREA else PredicateKind.BLUETOOTH_IS_CONNECTED,
                                initialTargetId = "",
                                initialActions = emptyList()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = KronoPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Cabang (IF / ELSE IF)", color = KronoPrimary, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (hasElseBranch) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = KronoSurfaceContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "KALAU TIDAK (ELSE)",
                                    color = KronoWarning,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = {
                                    hasElseBranch = false
                                    elseActions.clear()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus ELSE", tint = KronoError)
                                }
                            }

                            Text(
                                text = "Aksi dijalankan jika semua kondisi IF/ELSE IF di atas tidak cocok.",
                                color = KronoOnSurfaceVariant,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (elseActions.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = KronoSurfaceHigh, shape = RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Belum ada aksi cadangan. Tekan tombol di bawah untuk menambahkan aksi.",
                                        color = KronoOnSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            } else {
                                elseActions.forEachIndexed { actionIndex, actionState ->
                                    ActionEditorRow(
                                        actionState = actionState,
                                        viewModel = viewModel,
                                        onDelete = { elseActions.removeAt(actionIndex) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    actionPickerTargetBranchIndex = null
                                    isAddingActionToElse = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = KronoWarning)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tambah Aksi ke ELSE", color = KronoWarning, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            hasElseBranch = true
                            elseActions.clear()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = KronoWarning)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Cabang ELSE (Aksi Cadangan)", color = KronoWarning)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (validationErrorMessage != null) {
                Text(
                    text = "Gagal Validasi: $validationErrorMessage",
                    color = KronoError,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        val currentRule = buildRuleFromState()
                        if (currentRule != null) {
                            onOpenSimulation(currentRule)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Simulasikan", color = KronoPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = {
                        val firstAction = branches.firstOrNull()?.actions?.firstOrNull()?.toDomain()
                        if (firstAction != null) {
                            viewModel.executeTestAction(firstAction)
                        } else {
                            validationErrorMessage = "Tambahkan minimal satu aksi untuk diuji"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Uji Aksi Nyata", color = KronoTertiary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val finalRule = buildRuleFromState()
                    if (finalRule != null) {
                        val validation = RuleValidator.validate(finalRule)
                        if (validation is RuleValidationResult.Invalid) {
                            validationErrorMessage = validation.errors.firstOrNull()?.message ?: "Konfigurasi aturan tidak valid"
                        } else {
                            if (finalRule.trigger.family == TriggerFamily.LOCATION) {
                                val hasFine = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasFine) {
                                    pendingRuleToSave = finalRule
                                    showLocationPermissionDialog = true
                                    return@Button
                                }
                            }
                            viewModel.saveRule(finalRule, active = true)
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KronoPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan & Aktifkan Aturan", color = KronoOnPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showTriggerPicker) {
            TriggerPickerDialog(
                onDismiss = { showTriggerPicker = false },
                onTriggerSelected = { family, kind, title ->
                    selectedFamily = family
                    selectedKind = kind
                    triggerTitle = title
                    validationErrorMessage = null
                    if (family == TriggerFamily.LOCATION) {
                        branches.forEachIndexed { i, b ->
                            b.conditionKind = PredicateKind.LOCATION_IN_AREA
                            if (b.targetId == triggerDeviceId || b.targetId.contains("tws-selected")) {
                                b.targetId = ""
                            }
                        }
                    } else {
                        branches.forEach { b ->
                            b.conditionKind = PredicateKind.ALWAYS
                            b.targetId = triggerDeviceId
                        }
                    }
                }
            )
        }

        if (showBluetoothPicker) {
            BluetoothDevicePickerDialog(
                onDismiss = { showBluetoothPicker = false },
                onDeviceSelected = { name, address ->
                    triggerDeviceName = name
                    triggerDeviceId = address
                }
            )
        }

        if (actionPickerTargetBranchIndex != null || isAddingActionToElse) {
            ActionPickerDialog(
                onDismiss = {
                    actionPickerTargetBranchIndex = null
                    isAddingActionToElse = false
                },
                onActionSelected = { chosenKind ->
                    val newAction = EditableActionState(initialKind = chosenKind)
                    if (isAddingActionToElse) {
                        elseActions.add(newAction)
                    } else {
                        actionPickerTargetBranchIndex?.let { idx ->
                            if (idx in branches.indices) {
                                branches[idx].actions.add(newAction)
                            }
                        }
                    }
                }
            )
        }

        mapPickerTargetBranch?.let { targetBranch ->
            val lat = targetBranch.latitude.toDoubleOrNull() ?: -7.894303
            val lng = targetBranch.longitude.toDoubleOrNull() ?: 112.595458
            val rad = targetBranch.radiusM.toIntOrNull() ?: 150

            MapPickerDialog(
                initialLatitude = lat,
                initialLongitude = lng,
                initialRadiusM = rad,
                onDismiss = { mapPickerTargetBranch = null },
                onConfirm = { chosenLat, chosenLng, chosenRadius ->
                    targetBranch.latitude = String.format(java.util.Locale.US, "%.6f", chosenLat)
                    targetBranch.longitude = String.format(java.util.Locale.US, "%.6f", chosenLng)
                    targetBranch.radiusM = chosenRadius.toString()
                }
            )
        }

        if (showLocationPermissionDialog) {
            AlertDialog(
                onDismissRequest = {
                    showLocationPermissionDialog = false
                    pendingRuleToSave = null
                },
                title = {
                    Text(
                        text = "Izin Lokasi Presisi Diperlukan",
                        color = KronoOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Otomasi berbasis area memerlukan izin Lokasi Tepat (Presisi) agar Krono dapat mendeteksi batas area dan menjalankan pergantian wallpaper di latar belakang.",
                        color = KronoOnSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLocationPermissionDialog = false
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KronoPrimary)
                    ) {
                        Text("Berikan Izin", color = KronoOnPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showLocationPermissionDialog = false
                            pendingRuleToSave?.let { rule ->
                                viewModel.saveRule(rule, active = false)
                                pendingRuleToSave = null
                                onNavigateBack()
                            }
                        }
                    ) {
                        Text("Simpan Tanpa Aktif", color = KronoPrimary)
                    }
                },
                containerColor = KronoSurfaceContainer
            )
        }
    }
}

@Composable
fun ActionEditorRow(
    actionState: EditableActionState,
    viewModel: KronoViewModel,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isImporting by remember { mutableStateOf(false) }
    var selectedImageUriForCrop by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUriForCrop = uri
        }
    }

    selectedImageUriForCrop?.let { uri ->
        WallpaperCropDialog(
            imageUri = uri,
            onDismiss = { selectedImageUriForCrop = null },
            onCropConfirmed = { croppedBitmap ->
                selectedImageUriForCrop = null
                scope.launch {
                    isImporting = true
                    val newAssetId = viewModel.importCroppedWallpaper(croppedBitmap)
                    if (newAssetId != null) {
                        actionState.assetId = newAssetId
                    }
                    isImporting = false
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = KronoSurfaceHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (actionState.kind == ActionKind.SET_MEDIA_VOLUME) "Atur Volume Media" else "Ganti Wallpaper Layar",
                    color = KronoTertiary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus Aksi", tint = KronoError)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (actionState.kind) {
                ActionKind.SET_MEDIA_VOLUME -> {
                    Text(
                        text = "Volume Target: ${actionState.volumePercent.toInt()}%",
                        color = KronoOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = actionState.volumePercent,
                        onValueChange = { actionState.volumePercent = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = KronoTertiary,
                            activeTrackColor = KronoTertiary
                        )
                    )
                }
                ActionKind.SET_WALLPAPER -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Target Layar:", color = KronoOnSurface, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                actionState.wallpaperTarget = when (actionState.wallpaperTarget) {
                                    WallpaperTarget.SYSTEM -> WallpaperTarget.LOCK
                                    WallpaperTarget.LOCK -> WallpaperTarget.BOTH
                                    WallpaperTarget.BOTH -> WallpaperTarget.SYSTEM
                                }
                            }
                        ) {
                            Text(actionState.wallpaperTarget.name, fontSize = 11.sp, color = KronoTertiary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val assetFile = remember(actionState.assetId, isImporting) {
                        viewModel.getAssetFile(actionState.assetId)
                    }
                    val bitmap = remember(assetFile, isImporting) {
                        if (assetFile.exists() && assetFile.canRead()) {
                            try {
                                BitmapFactory.decodeFile(assetFile.absolutePath)
                            } catch (_: Exception) {
                                null
                            }
                        } else null
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Pratinjau Gambar",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, KronoOutline, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = KronoTertiary.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isImporting) "Menyimpan..." else "Pilih dari Galeri / Berkas",
                                    color = KronoTertiary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ID: ${actionState.assetId}",
                                color = KronoOnSurfaceVariant,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
