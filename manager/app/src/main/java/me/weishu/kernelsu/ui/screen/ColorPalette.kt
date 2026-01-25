package me.weishu.kernelsu.ui.screen

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Brightness1
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.compose.dropUnlessResumed
import com.materialkolor.rememberDynamicColorScheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.result.ResultBackNavigator
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.theme.ColorMode
import me.weishu.kernelsu.ui.theme.ThemeController
import me.weishu.kernelsu.ui.util.clearHeaderImage
import me.weishu.kernelsu.ui.util.getHeaderImage
import me.weishu.kernelsu.ui.util.getLayoutStyle
import me.weishu.kernelsu.ui.util.saveHeaderImage
import me.weishu.kernelsu.ui.util.setLayoutStyle

private val keyColorOptions = listOf(
    Color(0xFF1A73E8).toArgb(),
    Color(0xFFEA4335).toArgb(),
    Color(0xFF34A853).toArgb(),
    Color(0xFF9333EA).toArgb(),
    Color(0xFFFB8C00).toArgb(),
    Color(0xFF009688).toArgb(),
    Color(0xFFE91E63).toArgb(),
    Color(0xFF795548).toArgb(),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Destination<RootGraph>
@Composable
fun ColorPaletteScreen(resultNavigator: ResultBackNavigator<Boolean>) {
    val context = LocalContext.current
    var hasCustomHeader by rememberSaveable {
        mutableStateOf(context.getHeaderImage() != null)
    }
    val imagePicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri == null) {
                // User cancel → revert toggle
                hasCustomHeader = false
            } else {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                context.saveHeaderImage(uri.toString())

                hasCustomHeader = true
            }
        }

    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // === START: VARIABLE & LAUNCHER BACKGROUND ===
    var backgroundUri by remember { mutableStateOf(prefs.getString("background_uri", null)) }
    var backgroundFill by remember { mutableStateOf(prefs.getBoolean("background_fill_screen", false)) }

    // Move Alpha State to Top Scope for Scaffold usage
    var bgAlpha by remember { mutableFloatStateOf(prefs.getFloat("background_alpha", 0.5f)) }

    val backgroundImagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val uriStr = uri.toString()
            prefs.edit().putString("background_uri", uriStr).apply()
            backgroundUri = uriStr
        }
    }
    // === END: VARIABLE & LAUNCHER BACKGROUND ===

    var appSettings by remember { mutableStateOf(ThemeController.getAppSettings(context)) }
    var currentColorMode by remember { mutableStateOf(appSettings.colorMode) }
    var currentKeyColor by remember { mutableIntStateOf(appSettings.keyColor) }
    var currentLauncherIcon by remember { mutableStateOf(prefs.getBoolean("enable_official_launcher", false)) }

    // Logic Warna Transparan untuk Scaffold
    val hasBg = backgroundUri != null
    val scaffoldContainerColor = if (hasBg) {
        MaterialTheme.colorScheme.background.copy(alpha = bgAlpha)
    } else {
        MaterialTheme.colorScheme.background
    }
    val topBarContainerColor = if (hasBg) {
        MaterialTheme.colorScheme.surface.copy(alpha = bgAlpha)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Scaffold(
        containerColor = scaffoldContainerColor, // [BARU] Terapkan transparansi
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarContainerColor, // [BARU] Terapkan transparansi
                    scrolledContainerColor = topBarContainerColor
                ),
                navigationIcon = {
                    IconButton(
                        onClick = dropUnlessResumed {
                            resultNavigator.navigateBack(result = true)
                        }
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                },
                title = { Text(stringResource(R.string.settings_theme)) },
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isDark = currentColorMode.getDarkThemeValue(isSystemInDarkTheme())
            ThemePreviewCard(keyColor = currentKeyColor, isDark = isDark, currentLauncherIcon = currentLauncherIcon)

            Spacer(modifier = Modifier.height(8.dp))

            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(modifier = Modifier.width(4.dp))
                // Dynamic
                ColorButton(
                    color = Color.Unspecified,
                    isSelected = currentKeyColor == 0,
                    isDark = isDark,
                    onClick = {
                        currentKeyColor = 0
                        prefs.edit { putInt("key_color", 0) }
                    }
                )

                // Color options
                keyColorOptions.forEachIndexed { _, color ->
                    ColorButton(
                        color = Color(color),
                        isSelected = currentKeyColor == color,
                        isDark = isDark,
                        onClick = {
                            currentKeyColor = color
                            prefs.edit { putInt("key_color", color) }
                        }
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val options = listOf(
                    ColorMode.SYSTEM to stringResource(R.string.settings_theme_mode_system),
                    ColorMode.LIGHT to stringResource(R.string.settings_theme_mode_light),
                    ColorMode.DARK to stringResource(R.string.settings_theme_mode_dark),
                    ColorMode.DARKAMOLED to stringResource(R.string.settings_theme_mode_dark)
                )

                options.chunked(4).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                    ) {
                        rowOptions.forEachIndexed { index, (mode, label) ->
                            ToggleButton(
                                checked = currentColorMode == mode,
                                onCheckedChange = {
                                    if (it) {
                                        currentColorMode = mode
                                        prefs.edit { putInt("color_mode", mode.value) }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { role = Role.RadioButton },
                                shapes = when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    rowOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                            ) {
                                Icon(
                                    imageVector = when (mode) {
                                        ColorMode.SYSTEM -> Icons.Filled.Brightness4
                                        ColorMode.LIGHT -> Icons.Filled.Brightness7
                                        ColorMode.DARK -> Icons.Filled.Brightness3
                                        ColorMode.DARKAMOLED -> Icons.Filled.Brightness1
                                    },
                                    contentDescription = label
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    val launcherOptions = listOf(false, true)
                    launcherOptions.forEachIndexed { index, isOfficial ->
                        ToggleButton(
                            checked = currentLauncherIcon == isOfficial,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    currentLauncherIcon = isOfficial
                                    prefs.edit { putBoolean("enable_official_launcher", isOfficial) }
                                    val pm = context.packageManager
                                    val pkg = context.packageName
                                    val mainComponent   = ComponentName(pkg, "$pkg.ui.MainActivity")
                                    val aliasComponent  = ComponentName(pkg, "$pkg.MainActivityOfficial")
                                    val (enableComp, disableComp) = if (isOfficial) aliasComponent to mainComponent else mainComponent to aliasComponent

                                    pm.setComponentEnabledSetting(enableComp, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
                                    pm.setComponentEnabledSetting(disableComp, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = if (isOfficial) R.drawable.ic_launcher_monochrome else R.drawable.ic_launcher_mambo_monochrome),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .wrapContentSize(unbounded = true)
                                        .requiredSize(48.dp)
                                )
                                Text(if (isOfficial) stringResource(R.string.app_name) else stringResource(R.string.app_name_mambo))
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Surface(
                        modifier = Modifier.padding(4.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            text = stringResource(R.string.header_image),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        ButtonGroupDefaults.ConnectedSpaceBetween
                    )
                ) {
                    val headerOptions = listOf(false, true) // false = default, true = custom

                    headerOptions.forEachIndexed { index, isCustom ->
                        ToggleButton(
                            checked = hasCustomHeader == isCustom,
                            onCheckedChange = { checked ->
                                if (!checked) return@ToggleButton

                                if (isCustom) {
                                    hasCustomHeader = true
                                    imagePicker.launch(arrayOf("image/*"))
                                } else {
                                    context.clearHeaderImage()
                                    hasCustomHeader = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                headerOptions.lastIndex ->
                                    ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isCustom)
                                        Icons.Filled.Image
                                    else
                                        Icons.Filled.Restore,
                                    contentDescription = null
                                )
                                Text(
                                    text = if (isCustom)
                                        stringResource(R.string.header_custom)
                                    else
                                        stringResource(R.string.header_default)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp)) // Jarak antar section

            // === LAYOUT SWITCH (DESAIN BARU) ===
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Padding disamakan
            ) {
                // Label Pill (Desain Kapsul)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Surface(
                        modifier = Modifier.padding(4.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            text = "Card Layout",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Toggle Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        ButtonGroupDefaults.ConnectedSpaceBetween
                    )
                ) {
                    val context = LocalContext.current
                    var useClassicLayout by remember { mutableStateOf(context.getLayoutStyle()) }
                    val layoutOptions = listOf(true, false) // True = Classic, False = Modern

                    layoutOptions.forEachIndexed { index, isClassic ->
                        ToggleButton(
                            checked = useClassicLayout == isClassic,
                            onCheckedChange = { checked ->
                                if (!checked) return@ToggleButton
                                useClassicLayout = isClassic
                                context.setLayoutStyle(isClassic)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                layoutOptions.lastIndex ->
                                    ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isClassic)
                                        Icons.AutoMirrored.Filled.ViewList
                                    else
                                        Icons.Filled.ViewColumn,
                                    contentDescription = null
                                )
                                Text(
                                    text = if (isClassic) "Classic" else "Modern"
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ================================================
            // === CONTROLLER APP BACKGROUND ===
            // ================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Label Section
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Surface(
                        modifier = Modifier.padding(4.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            text = "App Background",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Button: Default vs Select Image
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        ButtonGroupDefaults.ConnectedSpaceBetween
                    )
                ) {
                    val bgOptions = listOf(false, true) // false = Default, true = Custom

                    bgOptions.forEachIndexed { index, isCustom ->
                        ToggleButton(
                            checked = if (isCustom) hasBg else !hasBg,
                            onCheckedChange = { checked ->
                                if (!checked) return@ToggleButton
                                if (isCustom) {
                                    backgroundImagePicker.launch(arrayOf("image/*"))
                                } else {
                                    prefs.edit().remove("background_uri").apply()
                                    backgroundUri = null
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isCustom) Icons.Filled.Image else Icons.Filled.Close,
                                    contentDescription = null
                                )
                                Text(if (isCustom) "Select Image" else "Default")
                            }
                        }
                    }
                }

                // Opsi Lanjutan (Scale & Opacity) - Hanya muncul jika ada background
                if (backgroundUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Scale Toggle (Fill/Fit)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            ButtonGroupDefaults.ConnectedSpaceBetween
                        )
                    ) {
                        val scaleOptions = listOf(false, true) // false = Fit, true = Fill

                        scaleOptions.forEachIndexed { index, isFill ->
                            ToggleButton(
                                checked = backgroundFill == isFill,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        backgroundFill = isFill
                                        prefs.edit().putBoolean("background_fill_screen", isFill).apply()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { role = Role.RadioButton },
                                shapes = when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                }
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isFill) Icons.Filled.Crop else Icons.Filled.ViewInAr,
                                        contentDescription = null
                                    )
                                    Text(if (isFill) "Fill Screen" else "Fit Screen")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Opacity Sliders (Background & Cards)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // --- SLIDER 1: Background Opacity ---
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Background Opacity",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(bgAlpha * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = bgAlpha,
                                onValueChange = { newValue ->
                                    bgAlpha = newValue
                                    prefs.edit().putFloat("background_alpha", newValue).apply()
                                },
                                valueRange = 0f..1f,
                                steps = 19,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // --- SLIDER 2: Card Opacity ---
                        var cardAlpha by remember { mutableFloatStateOf(prefs.getFloat("card_alpha", 0.8f)) }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Card Opacity",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(cardAlpha * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = cardAlpha,
                                onValueChange = { newValue ->
                                    cardAlpha = newValue
                                    prefs.edit().putFloat("card_alpha", newValue).apply()
                                },
                                valueRange = 0f..1f,
                                steps = 19,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ================================================
            // === [BARU] START: HOME CONTENT VISIBILITY ===
            // ================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Label
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Surface(
                        modifier = Modifier.padding(4.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            text = "Home Content Visibility",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Toggle 1: Info Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    val infoOptions = listOf(false, true) // false = Visible, true = Hidden
                    val hideInfo = prefs.getBoolean("hide_info", false)

                    infoOptions.forEachIndexed { index, isHidden ->
                        ToggleButton(
                            checked = hideInfo == isHidden,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    prefs.edit().putBoolean("hide_info", isHidden).apply()
                                    // Trigger recomposition might be needed if state is not hoisted,
                                    // but usually prefs listener handles main/home screen update.
                                    // For local switch visual update, we rely on reading prefs again or state variable.
                                    // (Simplified here assuming direct interaction updates UI)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                                Text(if (isHidden) "Hide Info" else "Show Info")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Toggle 2: Learn More
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    val learnMoreOptions = listOf(false, true) // false = Visible, true = Hidden
                    val hideLearnMore = prefs.getBoolean("hide_learn_more", false)

                    learnMoreOptions.forEachIndexed { index, isHidden ->
                        ToggleButton(
                            checked = hideLearnMore == isHidden,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    prefs.edit().putBoolean("hide_learn_more", isHidden).apply()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isHidden) Icons.Filled.VisibilityOff else Icons.Filled.School,
                                    contentDescription = null
                                )
                                Text(if (isHidden) "Hide Learn More" else "Show Learn More")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Toggle 3: Donate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    val donateOptions = listOf(false, true) // false = Visible, true = Hidden
                    val hideDonate = prefs.getBoolean("hide_donate", false)

                    donateOptions.forEachIndexed { index, isHidden ->
                        ToggleButton(
                            checked = hideDonate == isHidden,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    prefs.edit().putBoolean("hide_donate", isHidden).apply()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isHidden) Icons.Filled.VisibilityOff else Icons.Filled.VolunteerActivism,
                                    contentDescription = null
                                )
                                Text(if (isHidden) "Hide Donate" else "Show Donate")
                            }
                        }
                    }
                }
            }
            // === END: HOME CONTENT VISIBILITY ===

            Spacer(Modifier.height(32.dp))
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemePreviewCard(keyColor: Int, isDark: Boolean, currentLauncherIcon: Boolean = false) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    val screenRatio = screenWidth / screenHeight
    val colorScheme = when {
        keyColor == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        keyColor == 0 -> {
            if (isDark) darkColorScheme() else expressiveLightColorScheme()
        }
        else -> rememberDynamicColorScheme(seedColor = Color(keyColor), isDark = isDark)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.5f).aspectRatio(screenRatio),
            color = colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                // top bar
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 12.dp, top = 16.dp, end = 12.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentLauncherIcon) stringResource(R.string.app_name) else stringResource(R.string.app_name_mambo),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurface
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TonalCard(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(12.dp),
                            content = { }
                        )
                        TonalCard(
                            modifier = Modifier.fillMaxWidth().height(128.dp),
                            shape = RoundedCornerShape(12.dp),
                            content = { }
                        )
                    }
                }

                // bottom bar
                Surface(
                    color = colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Home, null, tint = colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorButton(color: Color, isSelected: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val colorScheme =
        if (color == Color.Unspecified) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDark) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            } else {
                MaterialTheme.colorScheme
            }
        } else {
            rememberDynamicColorScheme(seedColor = color, isDark = isDark)
        }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surfaceContainer,
        modifier = Modifier.size(72.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(48.dp)) {
                // Top
                drawArc(
                    color = colorScheme.primaryContainer,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true
                )
                // Bottom
                drawArc(
                    color = colorScheme.tertiaryContainer,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true
                )
            }

            val scale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1.0f)
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(2.dp, colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.align(Alignment.Center).size(16.dp)
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = !isSelected,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(colorScheme.primary, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun TonalCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = containerColor),
        shape = shape
    ) {
        content()
    }
}