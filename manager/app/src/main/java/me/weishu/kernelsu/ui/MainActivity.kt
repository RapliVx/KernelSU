package me.weishu.kernelsu.ui

import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.FlashScreenDestination
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.ui.screen.BottomBarDestination
import me.weishu.kernelsu.ui.screen.FlashIt
import me.weishu.kernelsu.ui.theme.*
import me.weishu.kernelsu.ui.util.*
import me.weishu.kernelsu.ui.component.*

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

        val zipUri: ArrayList<Uri>? = intent.data?.let {
            arrayListOf(it)
        } ?: run {
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
            val context = LocalContext.current

            val appSettingsState = rememberSaveable(stateSaver = appSettingsSaver) {
                mutableStateOf(ThemeController.getAppSettings(context))
            }

            val prefs = remember { context.getSharedPreferences("settings", MODE_PRIVATE) }
            DisposableEffect(Unit) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "color_mode" || key == "key_color") {
                        appSettingsState.value = ThemeController.getAppSettings(context)
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            KernelSUTheme(appSettings = appSettingsState.value) {

                val navController = rememberNavController()
                val navigator = navController.rememberDestinationsNavigator()
                val snackBarHostState = remember { SnackbarHostState() }
                val configuration = LocalConfiguration.current

                val bottomBarRoutes = remember {
                    BottomBarDestination.entries.map { it.direction.route }.toSet()
                }

                val boxOpacity = context.getBoxOpacity()

                LaunchedEffect(zipUri) {
                    if (!zipUri.isNullOrEmpty()) {
                        navigator.navigate(
                            FlashScreenDestination(FlashIt.FlashModules(zipUri))
                        )
                    }
                }

                val defaultTransitions = object : NavHostAnimatedDestinationStyle() {
                    override val enterTransition:
                            AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                        {
                            if (targetState.destination.route !in bottomBarRoutes) {
                                slideInHorizontally { it }
                            } else fadeIn(tween(340))
                        }

                    override val exitTransition:
                            AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                        {
                            if (initialState.destination.route in bottomBarRoutes &&
                                targetState.destination.route !in bottomBarRoutes
                            ) {
                                slideOutHorizontally { -it / 4 } + fadeOut()
                            } else fadeOut(tween(340))
                        }

                    override val popEnterTransition:
                            AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                        {
                            if (targetState.destination.route in bottomBarRoutes) {
                                slideInHorizontally { -it / 4 } + fadeIn()
                            } else fadeIn()
                        }

                    override val popExitTransition:
                            AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                        {
                            if (initialState.destination.route !in bottomBarRoutes) {
                                scaleOut(0.9f) + fadeOut()
                            } else fadeOut(tween(340))
                        }
                }

                BackgroundImage { containerColor ->

                    Scaffold(
                        containerColor = containerColor,
                        bottomBar = {
                            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                                BottomBar(navController, boxOpacity)
                            }
                        },
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { innerPadding ->

                        CompositionLocalProvider(
                            LocalSnackbarHost provides snackBarHostState
                        ) {
                            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .windowInsetsPadding(
                                            WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
                                        )
                                ) {
                                    SideBar(
                                        navController = navController,
                                        boxOpacity = boxOpacity,
                                        modifier = Modifier.windowInsetsPadding(
                                            WindowInsets.systemBars.only(WindowInsetsSides.Top)
                                        )
                                    )
                                    DestinationsNavHost(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(innerPadding),
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
                }
            }
        }
    }
}

/* ===========================
   Bottom Bar (Opacity Aware)
   =========================== */

@Composable
private fun BottomBar(
    navController: NavHostController,
    boxOpacity: Float
) {
    val navigator = navController.rememberDestinationsNavigator()
    val isManager = Natives.isManager
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()

    val bottomBarRoutes = remember {
        BottomBarDestination.entries.map { it.direction.route }.toSet()
    }

    val currentRoute = navController.currentBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = boxOpacity),
        windowInsets = WindowInsets.systemBars
            .union(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
    ) {
        BottomBarDestination.entries.forEach { destination ->
            if (!fullFeatured && destination.rootRequired) return@forEach

            val selected by navController.isRouteOnBackStackAsState(destination.direction)

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (selected) {
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
                        if (selected) destination.iconSelected else destination.iconNotSelected,
                        stringResource(destination.label)
                    )
                },
                label = { Text(stringResource(destination.label)) },
                alwaysShowLabel = false
            )
        }
    }
}

/* ===========================
   Side Bar (Opacity Aware)
   =========================== */

@Composable
private fun SideBar(
    navController: NavHostController,
    boxOpacity: Float,
    modifier: Modifier = Modifier
) {
    val navigator = navController.rememberDestinationsNavigator()
    val isManager = Natives.isManager
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()

    val bottomBarRoutes = remember {
        BottomBarDestination.entries.map { it.direction.route }.toSet()
    }

    val currentRoute = navController.currentBackStackEntry?.destination?.route

    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = boxOpacity)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            BottomBarDestination.entries.forEach { destination ->
                if (!fullFeatured && destination.rootRequired) return@forEach

                val selected by navController.isRouteOnBackStackAsState(destination.direction)

                NavigationRailItem(
                    selected = selected,
                    onClick = {
                        if (selected) {
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
                            if (selected) destination.iconSelected else destination.iconNotSelected,
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