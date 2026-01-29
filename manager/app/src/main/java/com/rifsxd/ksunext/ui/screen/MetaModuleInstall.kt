package com.rifsxd.ksunext.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.FlashScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.rifsxd.ksunext.BuildConfig
import com.rifsxd.ksunext.Natives
import com.rifsxd.ksunext.R
import com.rifsxd.ksunext.ui.util.LocalSnackbarHost
import com.rifsxd.ksunext.ui.screen.FlashIt
import com.rifsxd.ksunext.ui.util.download
import com.rifsxd.ksunext.ui.util.DownloadListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.LabelItemDefaults
import com.rifsxd.ksunext.ui.viewmodel.ModuleViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import android.content.Intent

data class MetaModule(
    val id: String,
    val name: String,
    val description: String,
    val author: String,
    val repoUrl: String,
    val latestVersion: String = "Loading...",
    val downloadUrl: String = "",
    val isLoading: Boolean = true
)

sealed class MetaModuleState {
    object Loading : MetaModuleState()
    data class Success(val modules: List<MetaModule>) : MetaModuleState()
    data class Error(val message: String) : MetaModuleState()
}

/**
 * @author rifsxd
 * @date 2026/1/29.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun MetaModuleInstallScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val snackBarHost = LocalSnackbarHost.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isManager = Natives.isManager
    val ksuVersion = if (isManager) Natives.version else null
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // Hardcoded meta modules list (show immediately). Versions will show "Loading..." until fetched.
    val metaModules = listOf(
        MetaModule(
            id = "hybrid_mount",
            name = "Hybrid Mount",
            description = "Next Generation Mounting Matemodule with OverlayFS and Magic Mount",
            author = "Hybrid Mount Developers",
            repoUrl = "https://github.com/Hybrid-Mount/meta-hybrid_mount"
        ),
        MetaModule(
            id = "mountify",
            name = "Mountify",
            description = "Globally mounted modules via OverlayFS.",
            author = "backslashxx, KOWX712",
            repoUrl = "https://github.com/backslashxx/mountify"
        ),
        MetaModule(
            id = "meta_magic_mount",
            name = "Meta Magic Mount",
            description = "An implementation of a metamodule using Magic Mount",
            author = "7a72",
            repoUrl = "https://github.com/KernelSU-Modules-Repo/meta-mm"
        ),
        MetaModule(
            id = "meta_magic_mount_rs",
            name = "Meta Magic Mount RS",
            description = "An implementation of a metamodule using Magic Mount (Rust)",
            author = "7a72, Tools-cx-app, YuzakiKokuban",
            repoUrl = "https://github.com/Tools-cx-app/meta-magic_mount"
        ),
        MetaModule(
            id = "meta_overlayfs",
            name = "Meta OverlayFS",
            description = "An implementation of a metamodule using OverlayFS",
            author = "KernelSU Developers",
            repoUrl = "https://github.com/KernelSU-Modules-Repo/meta-overlayfs"
        )
    )

    var moduleState by remember { mutableStateOf<MetaModuleState>(MetaModuleState.Success(metaModules)) }
    var selectedModule by remember { mutableStateOf<MetaModule?>(null) }
    var downloadingModuleId by remember { mutableStateOf<String?>(null) }
    var downloadedUri by remember { mutableStateOf<Uri?>(null) }

    DownloadListener(context) { uri ->
        downloadedUri = uri
        downloadingModuleId = null
        navigator.navigate(
            FlashScreenDestination(FlashIt.FlashModules(listOf(uri)))
        )
    }

    val moduleViewModel = viewModel<ModuleViewModel>()

    val loadModules = suspend {
        try {
            val modules = metaModules.map { baseModule ->
                val releaseInfo = withContext(Dispatchers.IO) {
                    fetchLatestReleaseInfo(baseModule.repoUrl)
                }
                baseModule.copy(
                    latestVersion = releaseInfo?.version ?: "Unknown",
                    downloadUrl = releaseInfo?.zipUrl ?: "",
                    isLoading = false
                )
            }
            moduleState = MetaModuleState.Success(modules)
        } catch (e: Exception) {
            moduleState = MetaModuleState.Error(e.message ?: "Unknown error")
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            loadModules()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                onBack = { navigator.popBackStack() },
                onRefresh = {
                    scope.launch {
                        moduleState = MetaModuleState.Success(metaModules)
                        loadModules()
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackBarHost, modifier = Modifier.padding(bottom = navBarPadding)) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { paddingValues ->
        when (val state = moduleState) {
            is MetaModuleState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is MetaModuleState.Success -> {
                val installedIds = moduleViewModel.moduleList.map { it.id }
                
                val hasInstalledMetaModule = state.modules.any { installedIds.contains(it.id) }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp + 16.dp + navBarPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.modules) { module ->
                        val isInstalled = installedIds.contains(module.id)
                        val isThisModuleDownloading = downloadingModuleId == module.id
                        
                        val isInstallEnabled = when {
                            downloadingModuleId != null && !isThisModuleDownloading -> false
                            isInstalled -> true
                            hasInstalledMetaModule -> false
                            else -> true
                        }

                        MetaModuleCard(
                            module = module,
                            isInstalled = isInstalled,
                            isInstallEnabled = isInstallEnabled,
                            isDownloading = isThisModuleDownloading,
                            onCardClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(module.repoUrl))
                                context.startActivity(intent)
                            },
                            onDownload = { selectedModule ->
                                downloadingModuleId = selectedModule.id
                                scope.launch {
                                    try {
                                        val fileName = "${selectedModule.name.replace(" ", "_")}_${selectedModule.latestVersion.replace("/", "_")}.zip"
                                        download(
                                            context,
                                            selectedModule.downloadUrl,
                                            fileName,
                                            "Downloading ${selectedModule.name}"
                                        )
                                    } catch (e: Exception) {
                                        snackBarHost.showSnackbar("Error downloading module: ${e.message}")
                                        downloadingModuleId = null
                                    }
                                }
                            }
                        )
                    }
                }
            }
            is MetaModuleState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Error loading modules",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    moduleState = MetaModuleState.Success(metaModules)
                                    loadModules()
                                }
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaModuleCard(
    module: MetaModule,
    isInstalled: Boolean = false,
    isInstallEnabled: Boolean = true,
    isDownloading: Boolean = false,
    onCardClick: () -> Unit,
    onDownload: (MetaModule) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (isInstalled) {
                    LabelItem(
                        text = stringResource(R.string.installed),
                        style = LabelItemDefaults.style.copy(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = module.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "by ${module.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = module.description,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.latest_version),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = module.latestVersion,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { onDownload(module) },
                    modifier = Modifier.height(40.dp),
                    enabled = module.downloadUrl.isNotEmpty() && isInstallEnabled,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Download",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isInstalled) stringResource(R.string.reinstall) else stringResource(R.string.install))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.meta_module_screen),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        },
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}

private suspend fun fetchLatestReleaseInfo(repoUrl: String): ReleaseInfo? {
    return withContext(Dispatchers.IO) {
        try {
            val latestUrl = "$repoUrl/releases/latest"
            val connection = URL(latestUrl).openConnection() as java.net.HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("User-Agent", "KernelSU/${BuildConfig.VERSION_CODE}")
            
            val redirectUrl = connection.getHeaderField("Location")
            connection.disconnect()
            
            if (redirectUrl == null) return@withContext null
            
            val tagMatch = """/tag/([^/?\s]+)""".toRegex()
                .find(redirectUrl)?.groupValues?.get(1)
            
            if (tagMatch == null) return@withContext null
            
            val releasePageUrl = "$repoUrl/releases/expanded_assets/$tagMatch"
            val pageConnection = URL(releasePageUrl).openConnection()
            pageConnection.setRequestProperty("User-Agent", "KernelSU/${BuildConfig.VERSION_CODE}")
            
            val pageHtml = BufferedReader(InputStreamReader(pageConnection.getInputStream())).use {
                it.readText()
            }
        
            val zipUrlMatch = """href="(/[^"]+/releases/download/[^"]+\.zip)"""".toRegex()
                .find(pageHtml)?.groupValues?.get(1)
            
            if (zipUrlMatch != null) {
                return@withContext ReleaseInfo(
                    version = tagMatch,
                    zipUrl = "https://github.com$zipUrlMatch"
                )
            }
            
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

data class ReleaseInfo(
    val version: String,
    val zipUrl: String
)

@Preview
@Composable
private fun MetaModuleInstallScreenPreview() {
    MetaModuleInstallScreen(EmptyDestinationsNavigator)
}