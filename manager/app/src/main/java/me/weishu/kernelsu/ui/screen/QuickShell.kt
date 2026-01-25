@file:OptIn(ExperimentalMaterial3Api::class)

package me.weishu.kernelsu.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import com.ramcosta.composedestinations.annotation.Destination
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// libsu
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell

@Destination<RootGraph>
@Composable
fun QuickShellScreen() {
    val context = LocalContext.current
    var cmd by rememberSaveable { mutableStateOf("") }
    val logs = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // --- [BARU] LOGIC BACKGROUND ---
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    val hasBackground = remember { prefs.getString("background_uri", null) != null }
    val backgroundAlpha = remember { prefs.getFloat("background_alpha", 0.5f) }
    val cardAlpha = remember { prefs.getFloat("card_alpha", 0.8f) }

    fun append(line: String) {
        logs.add(line)
    }

    fun runCommand() {
        val script = cmd.trim()
        if (script.isEmpty()) return

        append("[start] ---- TIME: ${System.currentTimeMillis()} ----")

        val stdoutCb = object : CallbackList<String>() {
            override fun onAddElement(e: String) {
                scope.launch(Dispatchers.Main) { append(e) }
            }
        }
        val stderrCb = object : CallbackList<String>() {
            override fun onAddElement(e: String) {
                scope.launch(Dispatchers.Main) { append("E: $e") }
            }
        }

        Shell.cmd(script)
            .to(stdoutCb, stderrCb)
            .submit { result ->
                scope.launch(Dispatchers.Main) {
                    append("[exit] code=${result.code}")
                }
            }
    }

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) listState.animateScrollToItem(logs.lastIndex)
    }

    Scaffold(
        // --- [BARU] Container Transparan ---
        containerColor = if (hasBackground) {
            MaterialTheme.colorScheme.background.copy(alpha = backgroundAlpha)
        } else {
            MaterialTheme.colorScheme.background
        },
        topBar = {
            // [BARU] Hitung warna TopBar
            val topBarColor = if (hasBackground) {
                MaterialTheme.colorScheme.surface.copy(alpha = backgroundAlpha)
            } else {
                MaterialTheme.colorScheme.surface
            }
            val contentColor = MaterialTheme.colorScheme.onSurface

            TopAppBar(
                title = { Text("QuickShell") },
                // [BARU] Apply Colors
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarColor,
                    titleContentColor = contentColor,
                    actionIconContentColor = contentColor
                ),
                actions = {
                    IconButton(onClick = { logs.clear() }) {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // [BARU] Gunakan TonalCard private dengan alpha
            TonalCard(
                modifier = Modifier.fillMaxWidth(),
                alpha = cardAlpha
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Commands",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cmd,
                        onValueChange = { cmd = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 1,
                        maxLines = 6,
                        placeholder = { Text("Input Command/Script…") },
                        trailingIcon = {
                            IconButton(
                                onClick = { runCommand() },
                                enabled = cmd.isNotBlank()
                            ) {
                                Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                            }
                        }
                    )

                }
            }

            Spacer(Modifier.height(10.dp))

            // [BARU] Gunakan TonalCard private dengan alpha
            TonalCard(
                modifier = Modifier.fillMaxSize(),
                alpha = cardAlpha
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    itemsIndexed(logs) { _, line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            // [BARU] Gunakan onSurface agar kontras terjaga
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

// --- [BARU] Definisi TonalCard Lokal (Private) ---
// Agar mendukung transparansi (alpha) dan teks kontras
@Composable
private fun TonalCard(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    containerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
    content: @Composable () -> Unit
) {
    val adjustedContainerColor = containerColor.copy(alpha = alpha)
    // Warna konten mengikuti containerColor asli (sebelum transparan) agar kontrasnya terjaga
    val contrastContentColor = MaterialTheme.colorScheme.contentColorFor(containerColor)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp), // Sesuaikan shape jika perlu
        colors = CardDefaults.cardColors(
            containerColor = adjustedContainerColor,
            contentColor = contrastContentColor
        )
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contrastContentColor
        ) {
            content()
        }
    }
}