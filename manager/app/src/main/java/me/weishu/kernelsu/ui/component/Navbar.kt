package me.weishu.kernelsu.ui.component

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.ui.screen.BottomBarDestination
import me.weishu.kernelsu.ui.util.rootAvailable

@Composable
fun BottomBar(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    val isFloating by remember { mutableStateOf(prefs.getBoolean("enable_floating_navbar", false)) }

    val navigator = navController.rememberDestinationsNavigator()
    val isManager = Natives.isManager
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    val bottomBarRoutes = remember {
        BottomBarDestination.entries.map { it.direction.route }.toSet()
    }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val modifier = if (isFloating) {
        Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .widthIn(max = 400.dp)
            .clip(RoundedCornerShape(50.dp))
    } else {
        Modifier
    }

    val insets = if (isFloating) {
        WindowInsets(0, 0, 0, 0)
    } else {
        WindowInsets.systemBars.union(WindowInsets.displayCutout).only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar(
            modifier = modifier,
            windowInsets = insets,
            containerColor = if (isFloating) MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) else NavigationBarDefaults.containerColor,
            tonalElevation = if (isFloating) 0.dp else NavigationBarDefaults.Elevation
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
}