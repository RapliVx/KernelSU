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
import androidx.compose.material3.TonalCard
import androidx.core.content.edit
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

// ================================
// FUNGSI UTILITAS BANNER
// ================================

/**
 * Fungsi untuk membaca banner dari module.prop
 */
fun parseBannerFromModuleProp(moduleId: String): String? = try {
    val prop = File("/data/adb/ksu/modules/$moduleId/module.prop")
    prop.takeIf { it.exists() }
        ?.readLines()
        ?.firstOrNull { it.startsWith("banner=") }
        ?.substringAfter("=")
        ?.trim()
} catch (e: Exception) {
    Log.e("ModuleBanner", "parse error", e)
    null
}

fun getModuleBannerPath(moduleId: String, banner: String?): String? {
    if (banner.isNullOrBlank()) return null
    return when {
        banner.startsWith("http") -> banner
        banner.startsWith("/") ->
            File("/data/adb/ksu/modules/$moduleId/${banner.drop(1)}")
                .takeIf { it.exists() }?.absolutePath
        else ->
            File(banner).takeIf { it.exists() }?.absolutePath
    }
}

// ================================
// KOMPONEN LABEL CHIP
// ================================

@Composable
fun LabelChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(backgroundColor, MaterialTheme.shapes.small)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
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

    val bannerPath = remember(module.id) {
        module.bannerPath ?: parseBannerFromModuleProp(module.id)
    }

    val resolvedBanner = remember(bannerPath) {
        getModuleBannerPath(module.id, bannerPath)
    }

    val textDecoration =
        if (module.remove) TextDecoration.LineThrough else null

    TonalCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                .padding(16.dp)
        ) {

            /* ---------- BANNER ---------- */

            resolvedBanner?.let { path ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(path)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(0.4f))
                                )
                            )
                    )

                    Text(
                        text = module.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))
            }

            /* ---------- HEADER ---------- */

            if (resolvedBanner == null) {
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = textDecoration
                )
            }

            Text(
                text = "v${module.version} • ${module.author}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = textDecoration
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = module.description,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.outline,
                textDecoration = textDecoration
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = Dp.Hairline)
            Spacer(Modifier.height(8.dp))

            /* ---------- ACTION ROW ---------- */

            Row(verticalAlignment = Alignment.CenterVertically) {

                Switch(
                    checked = module.enabled,
                    enabled = !module.remove,
                    onCheckedChange = onCheckChanged
                )

                Spacer(Modifier.weight(1f))

                AnimatedVisibility(updateUrl.isNotEmpty()) {
                    Button(onClick = { onUpdate(module) }) {
                        Icon(Icons.Outlined.Download, null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.module_update))
                    }
                }

                Spacer(Modifier.width(8.dp))

                FilledTonalButton(onClick = { onUninstallClicked(module) }) {
                    Icon(
                        if (module.remove) Icons.Outlined.Refresh else Icons.Outlined.Delete,
                        null,
                        modifier = if (module.remove) Modifier.rotate(180f) else Modifier
                    )
                }
            }

            AnimatedVisibility(module.hasActionScript || module.hasWebUi) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    if (module.hasActionScript) {
                        FilledTonalButton(
                            onClick = {
                                navigator.navigate(
                                    ExecuteModuleActionScreenDestination(module.id)
                                )
                            }
                        ) {
                            Icon(Icons.Outlined.PlayArrow, null)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.action))
                        }
                    }

                    if (module.hasWebUi) {
                        FilledTonalButton(onClick = { onClick(module) }) {
                            Icon(Icons.Outlined.Code, null)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.open))
                        }
                    }
                }
            }
        }
    }
}

// Komponen Label yang sederhana
@Composable
fun SimpleLabelChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                backgroundColor,
                androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
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