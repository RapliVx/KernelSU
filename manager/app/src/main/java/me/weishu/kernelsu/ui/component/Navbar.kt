@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class, androidx.compose.material.ExperimentalMaterialApi::class, androidx.compose.animation.ExperimentalAnimationApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
package me.weishu.kernelsu.ui.component

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.layout.onPlaced
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    val fullFeatured = Natives.isFullFeatured()
    val bottomBarRoutes = remember {
        BottomBarDestination.entries.map { it.direction.route }.toSet()
    }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val visibleTabs = remember(fullFeatured) {
        BottomBarDestination.entries.filter { fullFeatured || !it.rootRequired }
    }

    val insets = if (isFloating) {
        WindowInsets(0, 0, 0, 0)
    } else {
        WindowInsets.systemBars.union(WindowInsets.displayCutout).only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isFloating) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (isFloating) {
            Surface(
                modifier = Modifier.padding(bottom = 16.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                tonalElevation = 0.dp
            ) {
                val enableGesture = remember { prefs.getBoolean("floating_navbar_gesture", true) }
                val indicatorBounds = remember { mutableStateMapOf<Int, Rect>() }
                var selectedIndex by remember { mutableIntStateOf(0) }
                
                var isGestureActive by remember { mutableStateOf(false) }
                var dragPositionX by remember { mutableStateOf(0f) }
                var hoveredIndex by remember { mutableIntStateOf(-1) }
                
                visibleTabs.forEachIndexed { index, destination ->
                    val isCurrent by navController.isRouteOnBackStackAsState(destination.direction)
                    if (isCurrent) {
                        selectedIndex = index
                    }
                }
                
                val radiusPx = with(androidx.compose.ui.platform.LocalDensity.current) { 24.dp.toPx() }
                val activeRect = indicatorBounds[selectedIndex] ?: Rect.Zero
                
                val targetLeft = if (isGestureActive) dragPositionX - radiusPx else activeRect.left
                val targetRight = if (isGestureActive) dragPositionX + radiusPx else activeRect.right

                val animatedLeft by animateFloatAsState(
                    targetValue = targetLeft, 
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
                    label = "indicatorLeft"
                )
                val animatedRight by animateFloatAsState(
                    targetValue = targetRight, 
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
                    label = "indicatorRight"
                )
                val pillColor = MaterialTheme.colorScheme.primaryContainer

                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .height(48.dp)
                        .drawBehind {
                            if (animatedRight > animatedLeft) {
                                drawRoundRect(
                                    color = pillColor,
                                    topLeft = Offset(animatedLeft, 0f),
                                    size = Size(animatedRight - animatedLeft, size.height),
                                    cornerRadius = CornerRadius(size.height / 2, size.height / 2)
                                )
                            }
                        }
                        .pointerInput(Unit) {
                            if (enableGesture) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        isGestureActive = true
                                        dragPositionX = offset.x
                                        hoveredIndex = selectedIndex
                                    },
                                    onDragEnd = {
                                        isGestureActive = false
                                        if (hoveredIndex != -1 && hoveredIndex != selectedIndex) {
                                            val dest = visibleTabs[hoveredIndex]
                                            val isFromNonBottom = currentRoute !in bottomBarRoutes
                                            navigator.navigate(dest.direction) {
                                                if (isFromNonBottom) {
                                                    popUpTo(NavGraphs.root) { inclusive = true }
                                                } else {
                                                    popUpTo(NavGraphs.root) { saveState = true }
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                        hoveredIndex = -1
                                    },
                                    onDragCancel = {
                                        isGestureActive = false
                                        hoveredIndex = -1
                                    },
                                    onDrag = { change, _ ->
                                        val minX = indicatorBounds[0]?.left ?: 0f
                                        val maxX = indicatorBounds[visibleTabs.size - 1]?.right ?: size.width.toFloat()
                                        dragPositionX = change.position.x.coerceIn(minX, maxX)
                                        
                                        val newTarget = indicatorBounds.entries.find { it.value.left <= dragPositionX && it.value.right >= dragPositionX }?.key
                                        if (newTarget != null) {
                                            hoveredIndex = newTarget
                                        }
                                    }
                                )
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visibleTabs.forEachIndexed { index, destination ->
                        val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(destination.direction)

                        val isHovered = isGestureActive && hoveredIndex == index
                        val isActive = (!isGestureActive && isCurrentDestOnBackStack) || isHovered

                        val animationSpec = tween<Color>(durationMillis = 300, easing = FastOutSlowInEasing)

                        val contentColor by animateColorAsState(
                            targetValue = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = animationSpec,
                            label = "contentColor"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .onPlaced { coords ->
                                    val x = coords.positionInParent().x
                                    val width = coords.size.width.toFloat()
                                    indicatorBounds[index] = Rect(x, 0f, x + width, 0f)
                                }
                                .clip(RoundedCornerShape(50))
                                .clickable {
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
                                }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isActive) destination.iconSelected else destination.iconNotSelected,
                                contentDescription = stringResource(destination.label),
                                tint = contentColor
                            )

                            AnimatedVisibility(
                                visible = isCurrentDestOnBackStack && !isGestureActive,
                                enter = expandHorizontally(
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                    expandFrom = Alignment.Start
                                ) + fadeIn(animationSpec = tween(300)),
                                exit = shrinkHorizontally(
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                    shrinkTowards = Alignment.Start
                                ) + fadeOut(animationSpec = tween(300))
                            ) {
                                Text(
                                    text = stringResource(destination.label),
                                    color = contentColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            NavigationBar(
                windowInsets = insets,
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                tonalElevation = 0.dp
            ) {
                visibleTabs.forEach { destination ->
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
                        label = { 
                            Text(
                                text = stringResource(destination.label),
                                fontWeight = if (isCurrentDestOnBackStack) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        alwaysShowLabel = false
                    )
                }
            }
        }
    }
}