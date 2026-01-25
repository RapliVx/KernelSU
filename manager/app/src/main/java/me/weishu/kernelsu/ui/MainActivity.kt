package me.weishu.kernelsu.ui

import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.FlashScreenDestination
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.ui.screen.BottomBarDestination
import me.weishu.kernelsu.ui.screen.FlashIt
import me.weishu.kernelsu.ui.theme.AppSettings
import me.weishu.kernelsu.ui.theme.ColorMode
import me.weishu.kernelsu.ui.theme.KernelSUTheme
import me.weishu.kernelsu.ui.theme.ThemeController
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.install
import me.weishu.kernelsu.ui.util.rootAvailable

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)

        val isManager = Natives.isManager
        if (isManager && !Natives.requireNewKernel()) install()

        val isAnyKernel = intent.component?.className?.endsWith("FlashAnyKernel") == true

        val zipUri: ArrayList<Uri>? = if (intent.data != null) {
            arrayListOf(intent.data!!)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra("uris", Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra("uris")
            }
        }

        val appSettingsSaver = Saver<AppSettings, IntArray>(
            save = { intArrayOf(it.colorMode.value, it.keyColor) },
            restore = { AppSettings(ColorMode.fromValue(it[0]), it[1]) }
        )

        setContent {
            val appSettingsState = rememberSaveable(stateSaver = appSettingsSaver) {
                mutableStateOf(ThemeController.getAppSettings(this@MainActivity))
            }

            val prefs = remember { getSharedPreferences("settings", MODE_PRIVATE) }

            // --- CUSTOMIZATION STATE START ---
            var backgroundUri by remember { mutableStateOf(prefs.getString("background_uri", null)) }
            var fillScreen by remember { mutableStateOf(prefs.getBoolean("background_fill_screen", false)) }
            // [BARU] State untuk Opacity (Transparansi)
            var backgroundAlpha by remember { mutableFloatStateOf(prefs.getFloat("background_alpha", 0.5f)) }
            // --- CUSTOMIZATION STATE END ---

            val prefsListener = remember {
                SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
                    if (key == "color_mode" || key == "key_color") {
                        appSettingsState.value = ThemeController.getAppSettings(this@MainActivity)
                    }
                    // --- CUSTOMIZATION LISTENER START ---
                    else if (key == "background_uri") {
                        backgroundUri = sharedPrefs.getString("background_uri", null)
                    } else if (key == "background_fill_screen") {
                        fillScreen = sharedPrefs.getBoolean("background_fill_screen", false)
                    } else if (key == "background_alpha") {
                        // [BARU] Update alpha realtime
                        backgroundAlpha = sharedPrefs.getFloat("background_alpha", 0.5f)
                    }
                    // --- CUSTOMIZATION LISTENER END ---
                }
            }

            DisposableEffect(Unit) {
                prefs.registerOnSharedPreferenceChangeListener(prefsListener)
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
                }
            }

            KernelSUTheme(
                appSettings = appSettingsState.value
            ) {
                // --- BACKGROUND BOX WRAPPER ---
                Box(modifier = Modifier.fillMaxSize()) {

                    // 1. Render Background Image
                    if (backgroundUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(backgroundUri)
                                .build(),
                            contentDescription = "Background",
                            contentScale = if (fillScreen) ContentScale.Crop else ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 2. Render UI Aplikasi
                    val navController = rememberNavController()
                    val snackBarHostState = remember { SnackbarHostState() }

                    val bottomBarRoutes = remember {
                        BottomBarDestination.entries.map { it.direction.route }.toSet()
                    }

                    val navigator = navController.rememberDestinationsNavigator()
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStackEntry?.destination?.route

                    val homeDestination = BottomBarDestination.entries.firstOrNull()
                    val startRoute = homeDestination?.direction?.route

                    if (homeDestination != null && startRoute != null) {
                        BackHandler(enabled = currentRoute != startRoute && currentRoute in bottomBarRoutes) {
                            navigator.navigate(homeDestination.direction) {
                                popUpTo(NavGraphs.root) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }

                    LaunchedEffect(zipUri) {
                        if (!zipUri.isNullOrEmpty()) {
                            val flashIt = if (isAnyKernel) {
                                FlashIt.FlashAnyKernel(zipUri.first())
                            } else {
                                FlashIt.FlashModules(zipUri)
                            }
                            navigator.navigate(
                                FlashScreenDestination(flashIt)
                            )
                        }
                    }

                    val configuration = LocalConfiguration.current
                    val defaultTransitions = object : NavHostAnimatedDestinationStyle() {
                        override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                            {
                                if (targetState.destination.route !in bottomBarRoutes) {
                                    slideInHorizontally(initialOffsetX = { it })
                                } else {
                                    fadeIn(animationSpec = tween(340))
                                }
                            }

                        override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                            {
                                if (initialState.destination.route in bottomBarRoutes && targetState.destination.route !in bottomBarRoutes) {
                                    slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut()
                                } else {
                                    fadeOut(animationSpec = tween(340))
                                }
                            }

                        override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                            {
                                if (targetState.destination.route in bottomBarRoutes) {
                                    slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()
                                } else {
                                    fadeIn(animationSpec = tween(340))
                                }
                            }

                        override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                            {
                                if (initialState.destination.route !in bottomBarRoutes) {
                                    scaleOut(targetScale = 0.9f) + fadeOut()
                                } else {
                                    fadeOut(animationSpec = tween(340))
                                }
                            }
                    }

                    Scaffold(
                        // Container transparan agar background terlihat
                        containerColor = Color.Transparent,
                        bottomBar = {
                            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                                // [BARU] Kirim nilai alpha ke BottomBar
                                BottomBar(navController, backgroundAlpha)
                            }
                        },
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { innerPadding ->
                        CompositionLocalProvider(
                            LocalSnackbarHost provides snackBarHostState,
                        ) {
                            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                Row(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))) {
                                    // [BARU] Kirim nilai alpha ke SideBar
                                    SideBar(navController = navController, alpha = backgroundAlpha, modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)))
                                    DestinationsNavHost(
                                        modifier = Modifier.weight(1f),
                                        navGraph = NavGraphs.root,
                                        navController = navController,
                                        defaultTransitions = defaultTransitions
                                    )
                                }
                            } else {
                                DestinationsNavHost(
                                    modifier = Modifier.padding(innerPadding),
                                    navGraph = NavGraphs.root,
                                    navController = navController,
                                    defaultTransitions = defaultTransitions
                                )
                            }
                        }
                    }
                } // --- BOX END ---
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController, alpha: Float) {
    val navigator = navController.rememberDestinationsNavigator()
    val isManager = Natives.isManager
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    val bottomBarRoutes = remember {
        BottomBarDestination.entries.map { it.direction.route }.toSet()
    }
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    // [BARU] Logic pewarnaan BottomBar transparan
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE) }
    val hasBackground = remember(prefs.getString("background_uri", null)) { prefs.getString("background_uri", null) != null }

    val navContainerColor = if (hasBackground) {
        // Gunakan warna surface dengan alpha dari slider
        MaterialTheme.colorScheme.surface.copy(alpha = alpha)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) // Default semi-transparent
    }

    NavigationBar(
        containerColor = navContainerColor,
        // Hapus elevasi tonal jika pakai background image agar lebih menyatu
        tonalElevation = if (hasBackground) 0.dp else NavigationBarDefaults.Elevation,
        windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        )
    ) {
        BottomBarDestination.entries.forEach { destination ->
            if (!fullFeatured && destination.rootRequired) return@forEach
            val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(destination.direction)
            NavigationBarItem(
                selected = isCurrentDestOnBackStack,
                onClick = {
                    if (isCurrentDestOnBackStack) {
                        navigator.popBackStack(destination.direction, false)
                    } else {
                        val isFromNonBottom = currentRoute !in bottomBarRoutes
                        navigator.navigate(destination.direction) {
                            if (isFromNonBottom) {
                                popUpTo(NavGraphs.root) { inclusive = true }
                            } else {
                                popUpTo(NavGraphs.root) { saveState = true }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        if (isCurrentDestOnBackStack) destination.iconSelected else destination.iconNotSelected,
                        stringResource(destination.label)
                    )
                },
                label = { Text(stringResource(destination.label)) },
                alwaysShowLabel = false
            )
        }
    }
}

@Composable
private fun SideBar(navController: NavHostController, alpha: Float, modifier: Modifier = Modifier) {
    val navigator = navController.rememberDestinationsNavigator()
    val isManager = Natives.isManager
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    val bottomBarRoutes = remember {
        BottomBarDestination.entries.map { it.direction.route }.toSet()
    }
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    // [BARU] Logic pewarnaan SideBar transparan
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE) }
    val hasBackground = remember(prefs.getString("background_uri", null)) { prefs.getString("background_uri", null) != null }

    val navContainerColor = if (hasBackground) {
        MaterialTheme.colorScheme.surface.copy(alpha = alpha)
    } else {
        MaterialTheme.colorScheme.surface
    }

    NavigationRail(
        modifier = modifier,
        containerColor = navContainerColor,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            BottomBarDestination.entries.forEach { destination ->
                if (!fullFeatured && destination.rootRequired) return@forEach
                val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(destination.direction)
                NavigationRailItem(
                    selected = isCurrentDestOnBackStack,
                    onClick = {
                        if (isCurrentDestOnBackStack) {
                            navigator.popBackStack(destination.direction, false)
                        } else {
                            val isFromNonBottom = currentRoute !in bottomBarRoutes
                            navigator.navigate(destination.direction) {
                                if (isFromNonBottom) {
                                    popUpTo(NavGraphs.root) { inclusive = true }
                                } else {
                                    popUpTo(NavGraphs.root) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            if (isCurrentDestOnBackStack) destination.iconSelected else destination.iconNotSelected,
                            stringResource(destination.label)
                        )
                    },
                    label = { Text(stringResource(destination.label)) },
                    alwaysShowLabel = false,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}