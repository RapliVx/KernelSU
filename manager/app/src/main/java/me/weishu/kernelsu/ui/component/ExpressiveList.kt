package me.weishu.kernelsu.ui.component

import android.annotation.SuppressLint
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

// Types and Interfaces

internal val LocalExpressiveListScope = staticCompositionLocalOf<ExpressiveListScope> {
    error("No ExpressiveListScope provided")
}

@LayoutScopeMarker
@Immutable
interface ExpressiveListScope {
    val shape: Shape
}

private class ExpressiveListScopeImpl(
    override val shape: Shape
) : ExpressiveListScope

@LayoutScopeMarker
interface ExpressiveListContentScope {
    fun item(content: @Composable () -> Unit)
}

private class ExpressiveListContentScopeImpl : ExpressiveListContentScope {
    val items = mutableListOf<@Composable () -> Unit>()
    override fun item(content: @Composable () -> Unit) {
        items.add(content)
    }
}

private object ExpressiveListShapes {
    val Single = RoundedCornerShape(24.dp)
    val Top = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
    val Bottom = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    val Middle = RoundedCornerShape(8.dp)
}

private fun getExpressiveListItemShape(
    index: Int,
    totalCount: Int
): Shape {
    return when {
        totalCount == 1 -> ExpressiveListShapes.Single
        index == 0 -> ExpressiveListShapes.Top
        index == totalCount - 1 -> ExpressiveListShapes.Bottom
        else -> ExpressiveListShapes.Middle
    }
}

@Composable
private fun ProvideExpressiveListScope(
    shape: Shape,
    content: @Composable ExpressiveListScope.() -> Unit
) {
    val listScope = remember(shape) { ExpressiveListScopeImpl(shape) }
    CompositionLocalProvider(LocalExpressiveListScope provides listScope) {
        content(listScope)
    }
}

// List Composable

@Composable
fun ExpressiveList(
    modifier: Modifier = Modifier,
    content: @Composable ExpressiveListContentScope.() -> Unit
) {
    val scope = ExpressiveListContentScopeImpl()
    scope.content()

    Column(
        modifier = modifier.padding(bottom = 16.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val numItems = scope.items.size
        scope.items.forEachIndexed { index, item ->
            val shape = getExpressiveListItemShape(index, numItems)
            ProvideExpressiveListScope(shape) {
                item()
            }
        }
    }
}

@Composable
fun ExpressiveLazyList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(bottom = 16.dp),
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        content()
    }
}

// Item Composables

@Composable
fun ExpressiveListItem(
    onClick: () -> Unit,
    headlineContent: @Composable () -> Unit,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ElevatedCard(
        onClick = { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = LocalExpressiveListScope.current.shape
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingContent != null) {
                Box(
                    modifier = Modifier.padding(end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    leadingContent()
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                headlineContent()
                if (supportingContent != null) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.outline
                    ) {
                        ProvideTextStyle(value = MaterialTheme.typography.bodySmall) {
                            supportingContent()
                        }
                    }
                }
            }
            if (trailingContent != null) {
                Box(
                    modifier = Modifier.padding(start = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProvideTextStyle(value = MaterialTheme.typography.bodySmall) {
                        trailingContent()
                    }
                }
            }
        }
    }
}

@Composable
fun rememberExpressiveListItemShape(
    index: Int,
    totalCount: Int
): Shape {
    return remember(index, totalCount) {
        getExpressiveListItemShape(index, totalCount)
    }
}

@Composable
fun ExpressiveListItemScope(
    index: Int,
    totalCount: Int,
    content: @Composable ExpressiveListScope.() -> Unit
) {
    val shape = rememberExpressiveListItemShape(index, totalCount)
    ProvideExpressiveListScope(shape, content)
}

@Composable
fun ExpressiveSwitchItem(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    ExpressiveListItem(
        onClick = { onCheckedChange(!checked) },
        modifier = Modifier.toggleable(
            value = checked,
            interactionSource = interactionSource,
            role = Role.Switch,
            enabled = enabled,
            indication = LocalIndication.current,
            onValueChange = onCheckedChange
        ),
        headlineContent = { Text(title) },
        leadingContent = icon?.let { { Icon(it, title) } },
        trailingContent = {
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
                interactionSource = interactionSource
            )
        },
        supportingContent = summary?.let { { Text(it) } }
    )
}

@Composable
fun ExpressiveDropdownItem(
    icon: ImageVector,
    title: String,
    summary: String? = null,
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val hasItems = items.isNotEmpty()
    val safeIndex = if (hasItems) {
        selectedIndex.coerceIn(0, items.lastIndex)
    } else {
        -1
    }

    ExpressiveListItem(
        onClick = { if (hasItems) expanded = true },
        modifier = Modifier.clickable { if (hasItems) expanded = true },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        headlineContent = { Text(text = title) },
        supportingContent = summary?.let { { Text(it) } },
        trailingContent = {
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                Text(
                    text = if (hasItems && safeIndex >= 0) items[safeIndex] else "",
                    color = MaterialTheme.colorScheme.primary
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEachIndexed { index, text ->
                        DropdownMenuItem(
                            text = { Text(text) },
                            onClick = {
                                if (index in items.indices) {
                                    onItemSelected(index)
                                }
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ExpressiveRadioItem(
    title: String,
    summary: String? = null,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    ExpressiveListItem(
        onClick = onClick,
        modifier = Modifier.toggleable(
            value = selected,
            onValueChange = { onClick() },
            enabled = enabled,
            role = Role.RadioButton
        ),
        headlineContent = { Text(title) },
        leadingContent = {
            RadioButton(
                selected = selected,
                onClick = null,
                enabled = enabled
            )
        },
        supportingContent = summary?.let { { Text(it) } }
    )
}
