package me.weishu.kernelsu.ui.screen

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.*
import coil.compose.AsyncImage
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ExecuteModuleActionScreenDestination
import com.ramcosta.composedestinations.generated.destinations.FlashScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ModuleRepoScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.component.ConfirmResult
import me.weishu.kernelsu.ui.component.RebootListPopup
import me.weishu.kernelsu.ui.component.SearchAppBar
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.rememberLoadingDialog
import me.weishu.kernelsu.ui.util.DownloadListener
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.download
import me.weishu.kernelsu.ui.util.hasMagisk
import me.weishu.kernelsu.ui.util.module.fetchReleaseDescriptionHtml
import me.weishu.kernelsu.ui.util.reboot
import me.weishu.kernelsu.ui.util.toggleModule
import me.weishu.kernelsu.ui.util.undoUninstallModule
import me.weishu.kernelsu.ui.util.uninstallModule
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel
import me.weishu.kernelsu.ui.webui.WebUIActivity

// ================================
// IMPORT UNTUK FITUR BANNER
// ================================
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.request.ImageRequest
import java.io.File

/// ================================
// FUNGSI UTILITAS BANNER YANG DIPERBAIKI
// ================================

private const val TAG = "ModuleBanner"

/**
 * Fungsi utama untuk mendapatkan path banner dari module
 * Mencoba beberapa lokasi kemungkinan module directory
 */
fun getModuleBannerPath(
    moduleId: String,
    bannerPropValue: String?
): String? {
    if (bannerPropValue.isNullOrBlank()) {
        Log.d(TAG, "No banner property for module: $moduleId")
        return null
    }

    return try {
        when {
            // URL langsung - return as is
            bannerPropValue.startsWith("http://") || bannerPropValue.startsWith("https://") -> {
                Log.d(TAG, "Using HTTP banner for $moduleId: $bannerPropValue")
                bannerPropValue
            }

            // Path relatif - cari di beberapa lokasi module
            bannerPropValue.startsWith("/") -> {
                val relativePath = bannerPropValue.substring(1)
                findBannerInModulePaths(moduleId, relativePath)
            }

            // Path absolut - coba langsung
            else -> {
                val bannerFile = File(bannerPropValue)
                if (bannerFile.exists()) {
                    bannerFile.absolutePath
                } else {
                    // Coba sebagai path relatif juga
                    findBannerInModulePaths(moduleId, bannerPropValue)
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting banner path for $moduleId: $bannerPropValue", e)
        null
    }
}

/**
 * Cari banner di beberapa kemungkinan lokasi module directory
 */
private fun findBannerInModulePaths(moduleId: String, bannerFileName: String): String? {
    val possiblePaths = listOf(
        // Multi Path
        "/data/adb/ksu/modules/$moduleId",
        "/data/adb/modules/$moduleId",
        "/data/adb/kernelsu/modules/$moduleId"
    )

    for (basePath in possiblePaths) {
        val bannerFile = File(basePath, bannerFileName)
        if (bannerFile.exists() && bannerFile.isFile) {
            Log.d(TAG, "Found banner at: ${bannerFile.absolutePath}")
            return bannerFile.absolutePath
        }
    }

    Log.w(TAG, "Banner not found in any path: $bannerFileName for module: $moduleId")
    Log.d(TAG, "Searched paths: $possiblePaths")
    return null
}

/**
 * Baca module.prop dari beberapa kemungkinan lokasi
 */
fun readModuleProp(moduleId: String): String? {
    val possiblePaths = listOf(
        "/data/adb/ksu/modules/$moduleId/module.prop",
        "/data/adb/modules/$moduleId/module.prop",
        "/data/adb/kernelsu/modules/$moduleId/module.prop"
    )

    for (path in possiblePaths) {
        val file = File(path)
        if (file.exists() && file.isFile) {
            return try {
                Log.d(TAG, "Reading module.prop from: $path")
                file.readText()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read module.prop from $path", e)
                null
            }
        }
    }

    Log.w(TAG, "module.prop not found for module: $moduleId")
    Log.d(TAG, "Checked paths: $possiblePaths")
    return null
}

/**
 * Parse banner value dari konten module.prop
 */
fun parseBannerFromModuleProp(propContent: String?): String? {
    if (propContent.isNullOrBlank()) return null

    return try {
        propContent.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .firstOrNull { it.startsWith("banner=") }
            ?.substringAfter("banner=")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.also { banner ->
                Log.d(TAG, "Parsed banner value: $banner")
            }
    } catch (e: Exception) {
        Log.e(TAG, "Error parsing banner from module.prop", e)
        null
    }
}

// ================================
// MODULE SCREEN (SAMA DENGAN STOCK)
// ================================

@SuppressLint("StringFormatInvalid")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Destination<RootGraph>
@Composable
fun ModuleScreen(navigator: DestinationsNavigator) {
    val viewModel = viewModel<ModuleViewModel>()
    val scope = rememberCoroutineScope()
    val snackBarHost = LocalSnackbarHost.current

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val modules = viewModel.moduleList

    LaunchedEffect(Unit) {
        viewModel.checkModuleUpdate = prefs.getBoolean("module_check_update", true)
        viewModel.sortEnabledFirst = prefs.getBoolean("module_sort_enabled_first", false)
        viewModel.sortActionFirst = prefs.getBoolean("module_sort_action_first", false)

        when {
            viewModel.moduleList.isEmpty() || viewModel.isNeedRefresh -> {
                viewModel.fetchModuleList()
                scope.launch { viewModel.syncModuleUpdateInfo(viewModel.moduleList) }
            }
        }
    }

    LaunchedEffect(modules) {
        viewModel.syncModuleUpdateInfo(modules)
    }

    val webUILauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.fetchModuleList() }

    val isSafeMode = Natives.isSafeMode
    val magiskInstalled by produceState(initialValue = false) {
        value = withContext(Dispatchers.IO) { hasMagisk() }
    }
    val hideInstallButton = isSafeMode || magiskInstalled

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val pullToRefreshState = rememberPullToRefreshState()

    val onRefresh: () -> Unit = {
        scope.launch {
            viewModel.fetchModuleList()
            scope.launch { viewModel.syncModuleUpdateInfo(viewModel.moduleList) }
        }
    }

    val scaleFraction = remember(pullToRefreshState.distanceFraction) {
        if (viewModel.isRefreshing) 1f
        else LinearOutSlowInEasing.transform(pullToRefreshState.distanceFraction).coerceIn(0f, 1f)
    }

    Scaffold(
        modifier = Modifier.pullToRefresh(
            state = pullToRefreshState,
            isRefreshing = viewModel.isRefreshing,
            onRefresh = onRefresh,
        ),
        topBar = {
            SearchAppBar(
                title = { Text(stringResource(R.string.module)) },
                searchText = viewModel.search,
                onSearchTextChange = { viewModel.search = it },
                onClearClick = { viewModel.search = TextFieldValue("") },
                actionsContent = {
                    IconButton(
                        onClick = { navigator.navigate(ModuleRepoScreenDestination) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudDownload,
                            contentDescription = stringResource(id = R.string.module_repos)
                        )
                    }
                    RebootListPopup()
                },
                dropdownContent = {
                    var showDropdown by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { showDropdown = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(id = R.string.settings)
                        )

                        DropdownMenu(expanded = showDropdown, onDismissRequest = {
                            showDropdown = false
                        }) {
                            DropdownMenuItem(text = {
                                Text(stringResource(R.string.module_sort_action_first))
                            }, trailingIcon = {
                                Checkbox(viewModel.sortActionFirst, null)
                            }, onClick = {
                                viewModel.sortActionFirst = !viewModel.sortActionFirst
                                prefs.edit {
                                    putBoolean("module_sort_action_first", viewModel.sortActionFirst)
                                }
                                scope.launch { viewModel.fetchModuleList() }
                            })
                            DropdownMenuItem(text = {
                                Text(stringResource(R.string.module_sort_enabled_first))
                            }, trailingIcon = {
                                Checkbox(viewModel.sortEnabledFirst, null)
                            }, onClick = {
                                viewModel.sortEnabledFirst = !viewModel.sortEnabledFirst
                                prefs.edit {
                                    putBoolean("module_sort_enabled_first", viewModel.sortEnabledFirst)
                                }
                                scope.launch { viewModel.fetchModuleList() }
                            })
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (!hideInstallButton) {
                val moduleInstall = stringResource(id = R.string.module_install)
                val selectZipLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    if (it.resultCode != RESULT_OK) {
                        return@rememberLauncherForActivityResult
                    }
                    val data = it.data ?: return@rememberLauncherForActivityResult
                    val clipData = data.clipData

                    val uris = mutableListOf<Uri>()
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            clipData.getItemAt(i)?.uri?.let { uri -> uris.add(uri) }
                        }
                    } else {
                        data.data?.let { uri -> uris.add(uri) }
                    }

                    navigator.navigate(FlashScreenDestination(flashIt = FlashIt.FlashModules(uris), skipConfirmation = uris.size == 1))
                    viewModel.markNeedRefresh()
                }

                ExtendedFloatingActionButton(
                    onClick = {
                        // Select the zip files to install
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        }
                        selectZipLauncher.launch(intent)
                    },
                    icon = { Icon(Icons.Filled.Add, moduleInstall) },
                    text = { Text(text = moduleInstall) },
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        snackbarHost = { SnackbarHost(hostState = snackBarHost) }
    ) { innerPadding ->

        when {
            magiskInstalled -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.module_magisk_conflict),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                ModuleList(
                    navigator = navigator,
                    viewModel = viewModel,
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    boxModifier = Modifier.padding(innerPadding),
                    onInstallModule = { uri ->
                        navigator.navigate(FlashScreenDestination(flashIt = FlashIt.FlashModules(listOf(uri)), skipConfirmation = true))
                        viewModel.markNeedRefresh()
                    },
                    onClickModule = { id, name, hasWebUi ->
                        if (hasWebUi) {
                            webUILauncher.launch(
                                Intent(context, WebUIActivity::class.java)
                                    .setData(Uri.parse("kernelsu://webui/$id"))
                                    .putExtra("id", id)
                                    .putExtra("name", name)
                            )
                        }
                    },
                    context = context,
                    snackBarHost = snackBarHost,
                    pullToRefreshState = pullToRefreshState,
                    isRefreshing = viewModel.isRefreshing,
                    scaleFraction = scaleFraction
                )
            }
        }
    }
}

// ================================
// MODULE ITEM DENGAN BANNER SUPPORT
// ================================

@Composable
fun ModuleItem(
    navigator: DestinationsNavigator,
    module: ModuleViewModel.ModuleInfo,
    updateUrl: String,
    onUninstallClicked: (ModuleViewModel.ModuleInfo) -> Unit,
    onCheckChanged: (Boolean) -> Unit,
    onUpdate: (ModuleViewModel.ModuleInfo) -> Unit,
    onClick: (ModuleViewModel.ModuleInfo) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current

    // Baca dan parse module.prop secara async
    val (moduleProp, bannerValue, resolvedBanner) = produceBannerState(module.id)

    val textDecoration = if (module.remove) TextDecoration.LineThrough else null

    TonalCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            // Banner Section (jika ada)
            resolvedBanner?.let { bannerPath ->
                ModuleBannerSection(
                    bannerPath = bannerPath,
                    moduleName = module.name,
                    moduleAuthor = module.author
                )
            }

            // Content Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .then(
                        if (module.hasWebUi) {
                            Modifier.toggleable(
                                value = module.enabled,
                                enabled = !module.remove,
                                role = Role.Button,
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                onValueChange = { onClick(module) }
                            )
                        } else Modifier
                    )
            ) {
                // Nama module (jika tidak ada banner)
                if (resolvedBanner == null) {
                    Text(
                        text = module.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = textDecoration
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Info version dan author
                Text(
                    text = "${module.version} • ${module.author}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = textDecoration
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Deskripsi
                Text(
                    text = module.description,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textDecoration = textDecoration
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = Dp.Hairline)
                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons row
                ModuleActionRow(
                    module = module,
                    updateUrl = updateUrl,
                    onCheckChanged = onCheckChanged,
                    onUpdate = onUpdate,
                    onUninstallClicked = onUninstallClicked,
                    onClick = onClick,
                    navigator = navigator
                )
            }
        }
    }
}

/**
 * State untuk banner (dipisah untuk cleaner code)
 */
@Composable
private fun produceBannerState(moduleId: String): Triple<String?, String?, String?> {
    val moduleProp by produceState<String?>(initialValue = null, moduleId) {
        value = withContext(Dispatchers.IO) {
            readModuleProp(moduleId)
        }
    }

    val bannerValue = remember(moduleProp) {
        parseBannerFromModuleProp(moduleProp)
    }

    val resolvedBanner = remember(moduleId, bannerValue) {
        getModuleBannerPath(moduleId, bannerValue)
    }

    // Debug logging
    LaunchedEffect(moduleId, moduleProp, bannerValue, resolvedBanner) {
        Log.d(TAG, "Banner state for $moduleId:")
        Log.d(TAG, "  - Module prop loaded: ${moduleProp != null}")
        Log.d(TAG, "  - Banner value: $bannerValue")
        Log.d(TAG, "  - Resolved path: $resolvedBanner")
    }

    return Triple(moduleProp, bannerValue, resolvedBanner)
}

/**
 * Komponen banner yang reusable
 */
@Composable
private fun ModuleBannerSection(
    bannerPath: String,
    moduleName: String,
    moduleAuthor: String
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(
                MaterialTheme.shapes.large.copy(
                    bottomStart = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp)
                )
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(bannerPath)
                .crossfade(true)
                .build(),
            contentDescription = "Banner for $moduleName",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            onError = { error ->
                Log.e(TAG, "Failed to load banner: $bannerPath", error.result.throwable)
            }
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (isDarkTheme) Color.Black.copy(alpha = 0.6f)
                            else Color.Black.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        // Module info overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = moduleName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = moduleAuthor,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Komponen action row yang reusable
 */
@Composable
private fun ModuleActionRow(
    module: ModuleViewModel.ModuleInfo,
    updateUrl: String,
    onCheckChanged: (Boolean) -> Unit,
    onUpdate: (ModuleViewModel.ModuleInfo) -> Unit,
    onUninstallClicked: (ModuleViewModel.ModuleInfo) -> Unit,
    onClick: (ModuleViewModel.ModuleInfo) -> Unit,
    navigator: DestinationsNavigator
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Switch enable/disable
        Switch(
            checked = module.enabled,
            enabled = !module.remove,
            onCheckedChange = onCheckChanged
        )

        Spacer(modifier = Modifier.weight(1f))

        // Update button (jika ada update)
        AnimatedVisibility(updateUrl.isNotEmpty() && !module.remove) {
            FilledTonalButton(
                onClick = { onUpdate(module) },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Outlined.Download, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.module_update))
            }
        }

        // Uninstall/Restore button
        FilledTonalIconButton(
            onClick = { onUninstallClicked(module) },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (module.remove)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer,
                contentColor = if (module.remove)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(
                imageVector = if (module.remove)
                    Icons.Outlined.Refresh
                else
                    Icons.Outlined.Delete,
                contentDescription = if (module.remove) "Restore" else "Uninstall"
            )
        }
    }

    // Action script dan WebUI buttons
    AnimatedVisibility(module.hasActionScript || module.hasWebUi) {
        Column {
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (module.hasActionScript && !module.remove) {
                    FilledTonalButton(
                        onClick = {
                            navigator.navigate(ExecuteModuleActionScreenDestination(module.id))
                        }
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.action))
                    }
                }

                if (module.hasWebUi && !module.remove && module.enabled) {
                    FilledTonalButton(onClick = { onClick(module) }) {
                        Icon(Icons.Outlined.Code, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.open))
                    }
                }
            }
        }
    }
}

// ================================
// MODULE LIST (SAMA DENGAN STOCK)
// ================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModuleList(
    navigator: DestinationsNavigator,
    viewModel: ModuleViewModel,
    modifier: Modifier = Modifier,
    boxModifier: Modifier = Modifier,
    onInstallModule: (Uri) -> Unit,
    onClickModule: (id: String, name: String, hasWebUi: Boolean) -> Unit,
    context: Context,
    snackBarHost: SnackbarHostState,
    pullToRefreshState: PullToRefreshState,
    isRefreshing: Boolean,
    scaleFraction: Float
) {
    val failedEnable = stringResource(R.string.module_failed_to_enable)
    val failedDisable = stringResource(R.string.module_failed_to_disable)
    val failedUninstall = stringResource(R.string.module_uninstall_failed)
    val successUninstall = stringResource(R.string.module_uninstall_success)
    val reboot = stringResource(R.string.reboot)
    val rebootToApply = stringResource(R.string.reboot_to_apply)
    val moduleStr = stringResource(R.string.module)
    val uninstall = stringResource(R.string.uninstall)
    val cancel = stringResource(android.R.string.cancel)
    val moduleUninstallConfirm = stringResource(R.string.module_uninstall_confirm)
    val metaModuleUninstallConfirm = stringResource(R.string.metamodule_uninstall_confirm)
    val updateText = stringResource(R.string.module_update)
    val changelogText = stringResource(R.string.module_changelog)
    val downloadingText = stringResource(R.string.module_downloading)
    val startDownloadingText = stringResource(R.string.module_start_downloading)

    val scope = rememberCoroutineScope()
    val loadingDialog = rememberLoadingDialog()
    val confirmDialog = rememberConfirmDialog()

    suspend fun onModuleUpdate(
        module: ModuleViewModel.ModuleInfo,
        changelogUrl: String,
        downloadUrl: String,
        fileName: String
    ) {
        val changelogResult = if (changelogUrl.isNotEmpty()) {
            loadingDialog.withLoading {
                withContext(Dispatchers.IO) {
                    var url = changelogUrl
                    var isHtml = false
                    if (url.startsWith("#") && url.contains('@')) {
                        val parts = url.substring(1).split('@', limit = 2)
                        val moduleId = parts[0]
                        val tagName = parts[1]
                        fetchReleaseDescriptionHtml(moduleId, tagName)?.let {
                            url = it
                            isHtml = true
                        }
                    } else {
                        // old update json changelog
                        url = runCatching {
                            ksuApp.okhttpClient.newCall(
                                okhttp3.Request.Builder().url(url).build()
                            ).execute().body!!.string()
                        }.getOrDefault("")
                    }
                    url to isHtml
                }
            }
        } else {
            null
        }

        val changelog = changelogResult?.first ?: ""
        val isHtml = changelogResult?.second ?: false

        val confirmResult = confirmDialog.awaitConfirm(
            if (changelog.isNotEmpty()) changelogText else updateText,
            content = changelog.ifBlank { startDownloadingText.format(module.name) },
            html = isHtml,
            markdown = !isHtml && changelog.isNotEmpty(),
            confirm = updateText,
        )

        if (confirmResult != ConfirmResult.Confirmed) {
            return
        }

        withContext(Dispatchers.IO) {
            download(
                url = downloadUrl,
                fileName = fileName,
                onDownloaded = onInstallModule,
                onDownloading = {
                    scope.launch(Dispatchers.Main) {
                        Toast.makeText(context, downloadingText.format(module.name), Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    suspend fun onModuleUninstallClicked(module: ModuleViewModel.ModuleInfo) {
        val isUninstall = !module.remove
        if (isUninstall) {
            val formatter = if (module.metamodule) metaModuleUninstallConfirm else moduleUninstallConfirm
            val confirmResult = confirmDialog.awaitConfirm(
                moduleStr,
                content = formatter.format(module.name),
                confirm = uninstall,
                dismiss = cancel
            )
            if (confirmResult != ConfirmResult.Confirmed) {
                return
            }
        }

        val success = withContext(Dispatchers.IO) {
            if (isUninstall) {
                uninstallModule(module.id)
            } else {
                undoUninstallModule(module.id)
            }
        }

        if (success) {
            viewModel.fetchModuleList()
        }
        if (!isUninstall) return
        val message = if (success) {
            successUninstall.format(module.name)
        } else {
            failedUninstall.format(module.name)
        }
        val actionLabel = if (success) {
            reboot
        } else {
            null
        }
        val result = snackBarHost.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            reboot()
        }
    }

    Box(modifier = boxModifier) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = remember {
                PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp + 56.dp + 16.dp + 48.dp + 6.dp /* Scaffold Fab Spacing + Fab container height + SnackBar height */
                )
            },
        ) {
            when {
                viewModel.moduleList.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.module_empty),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    items(viewModel.moduleList) { module ->
                        val scope = rememberCoroutineScope()
                        val moduleUpdateInfo = viewModel.updateInfo[module.id] ?: ModuleViewModel.ModuleUpdateInfo.Empty

                        ModuleItem(
                            navigator = navigator,
                            module = module,
                            updateUrl = moduleUpdateInfo.downloadUrl,
                            onUninstallClicked = {
                                scope.launch { onModuleUninstallClicked(module) }
                            },
                            onCheckChanged = {
                                scope.launch {
                                    val success = withContext(Dispatchers.IO) {
                                        toggleModule(module.id, !module.enabled)
                                    }
                                    if (success) {
                                        viewModel.fetchModuleList()

                                        val result = snackBarHost.showSnackbar(
                                            message = rebootToApply,
                                            actionLabel = reboot,
                                            duration = SnackbarDuration.Long
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            reboot()
                                        }
                                    } else {
                                        val message = if (module.enabled) failedDisable else failedEnable
                                        snackBarHost.showSnackbar(message.format(module.name))
                                    }
                                }
                            },
                            onUpdate = {
                                scope.launch {
                                    onModuleUpdate(
                                        module,
                                        moduleUpdateInfo.changelog,
                                        moduleUpdateInfo.downloadUrl,
                                        "${module.name}-${moduleUpdateInfo.version}.zip"
                                    )
                                }
                            },
                            onClick = {
                                onClickModule(it.id, it.name, it.hasWebUi)
                            }
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    scaleX = scaleFraction
                    scaleY = scaleFraction
                }
        ) {
            PullToRefreshDefaults.LoadingIndicator(state = pullToRefreshState, isRefreshing = isRefreshing)
        }
        DownloadListener(context, onInstallModule)
    }
}

// ================================
// PREVIEW FUNCTION
// ================================

@Preview
@Composable
fun ModuleItemPreview() {
    val module = ModuleViewModel.ModuleInfo(
        id = "id",
        name = "name",
        version = "version",
        versionCode = 1,
        author = "author",
        description = "I am a test module and i do nothing but show a very long description",
        enabled = true,
        update = true,
        remove = false,
        updateJson = "",
        hasWebUi = false,
        hasActionScript = false,
        metamodule = true,
    )
    ModuleItem(EmptyDestinationsNavigator, module, "", {}, {}, {}, {})
}