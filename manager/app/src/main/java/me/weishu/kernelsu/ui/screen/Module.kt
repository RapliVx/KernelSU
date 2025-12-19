package me.weishu.kernelsu.ui.screen

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.layout.ContentScale
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

import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import com.topjohnwu.superuser.io.SuFile

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

    val scaleFraction = {
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
                            clipData.getItemAt(i)?.uri?.let { uris.add(it) }
                        }
                    } else {
                        data.data?.let { uris.add(it) }
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
                    navigator,
                    viewModel = viewModel,
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    boxModifier = Modifier.padding(innerPadding),
                    onInstallModule = {
                        navigator.navigate(FlashScreenDestination(flashIt = FlashIt.FlashModules(listOf(it)), skipConfirmation = true))
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
                    scaleFraction = scaleFraction()
                )
            }
        }
    }
}

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
    TonalCard(modifier = Modifier.fillMaxWidth()) {

        val textDecoration = if (!module.remove) null else TextDecoration.LineThrough
        val interactionSource = remember { MutableInteractionSource() }
        val indication = LocalIndication.current
        val viewModel = viewModel<ModuleViewModel>()

        var expanded by rememberSaveable(module.id) { mutableStateOf(false) }

        val cardHeight by animateDpAsState(
            targetValue = if (expanded) 280.dp else 170.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "cardHeight"
        )

        // Banner lebih kelihatan
        val bannerAlpha by animateFloatAsState(
            targetValue = if (expanded) 0.18f else 0.12f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "bannerAlpha"
        )

        val cs = MaterialTheme.colorScheme
        val isDark = isSystemInDarkTheme()

        val scrim = remember(cs, isDark) {
            Brush.verticalGradient(
                0f to cs.surface.copy(
                    alpha = if (isDark) 0.08f else 0.18f
                ),
                1f to cs.surface.copy(
                    alpha = if (isDark) 0.40f else 0.55f
                )
            )
        }

        val context = LocalContext.current
        val bannerData by produceState<ByteArray?>(
            initialValue = null,
            module.id,
            module.banner
        ) {
            value = withContext(Dispatchers.IO) {
                try {
                    val b = module.banner?.trim().orEmpty()
                    if (b.isEmpty()) return@withContext null

                    val rel = b.removePrefix("/")
                    val p1 = "/data/adb/modules/${module.id}/$rel"
                    val p2 = b

                    val file = SuFile(if (SuFile(p1).exists()) p1 else p2)
                    file.newInputStream().use { it.readBytes() }
                } catch (_: Exception) {
                    null
                }
            }
        }

        val shape = RoundedCornerShape(14.dp)
        val moduleVersion = stringResource(id = R.string.module_version)
        val moduleAuthor = stringResource(id = R.string.module_author)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = cardHeight)
                .clip(shape)
        ) {

            // Banner
            if (!module.banner.isNullOrEmpty() && bannerData != null) {
                val req = remember(bannerData, module.id, module.banner) {
                    ImageRequest.Builder(context)
                        .data(bannerData)
                        .memoryCacheKey("module-banner:${module.id}:${module.banner}")
                        .crossfade(true)
                        .build()
                }

                AsyncImage(
                    model = req,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    alpha = bannerAlpha
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(cs.surfaceContainerLow)
                )
            }

            // Fade overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(scrim)
            )

            // Clickable Content
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .run {
                        if (module.hasWebUi) {
                            toggleable(
                                value = module.enabled,
                                enabled = !module.remove && module.enabled,
                                interactionSource = interactionSource,
                                role = Role.Button,
                                indication = indication,
                                onValueChange = { onClick(module) }
                            )
                        } else this
                    }
                    .clickable { expanded = !expanded }
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    .padding(22.dp, 18.dp, 22.dp, 12.dp)
            ) {

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(end = 64.dp)
                ) {
                    Text(
                        text = module.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = textDecoration
                    )

                    Text(
                        text = "$moduleVersion: ${module.version}",
                        color = cs.onSurface.copy(alpha = 0.78f),
                        textDecoration = textDecoration,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )

                    Text(
                        text = "$moduleAuthor: ${module.author}",
                        color = cs.onSurface.copy(alpha = 0.78f),
                        textDecoration = textDecoration,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )

                    val descTop by animateDpAsState(
                        targetValue = if (expanded) 12.dp else 6.dp,
                        label = "descTop"
                    )

                    val descMaxLines by animateIntAsState(
                        targetValue = if (expanded) 3 else 1,
                        label = "descMaxLines"
                    )

                    val descText = remember(module.description) {
                        if (module.description.length > 300)
                            module.description.take(300) + "…"
                        else module.description
                    }

                    Text(
                        modifier = Modifier.padding(top = descTop),
                        text = descText,
                        color = cs.onSurface.copy(alpha = 0.80f),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        maxLines = descMaxLines,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = textDecoration
                    )
                }
            }

            Switch(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 6.dp)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                enabled = !module.update,
                checked = module.enabled,
                onCheckedChange = onCheckChanged,
                interactionSource = if (!module.hasWebUi) interactionSource else null
            )

            // Action Buttons
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hasUpdate by remember(updateUrl) {
                        derivedStateOf { updateUrl.isNotEmpty() }
                    }

                    val actionButtonsEnabled = !module.remove && module.enabled

                    if (actionButtonsEnabled && module.hasWebUi) {
                        FilledTonalButton(
                            onClick = { onClick(module) }
                        ) {
                            Icon(Icons.Outlined.Code, null)
                        }
                    }

                    if (hasUpdate) {
                        Button(
                            enabled = !module.remove,
                            onClick = { onUpdate(module) }
                        ) {
                            Icon(Icons.Outlined.Download, null)
                        }
                    }

                    FilledTonalButton(
                        onClick = { onUninstallClicked(module) }
                    ) {
                        Icon(
                            imageVector = if (!module.remove)
                                Icons.Outlined.Delete
                            else Icons.Outlined.Refresh,
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.padding(start = 6.dp),
                            text = stringResource(
                                if (module.remove) R.string.undo else R.string.uninstall
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ModuleItemPreview() {
    val module = ModuleViewModel.ModuleInfo(
        id = "id",
        name = "name",
        version = "version",
        versionCode = 1,
        author = "author",
        description = "I am a test module and I do nothing but show a very long description",
        enabled = true,
        update = true,
        remove = false,
        updateJson = "",
        hasWebUi = false,
        hasActionScript = false,
        metamodule = true,
        banner = null
    )
    ModuleItem(EmptyDestinationsNavigator, module, "", {}, {}, {}, {})
}