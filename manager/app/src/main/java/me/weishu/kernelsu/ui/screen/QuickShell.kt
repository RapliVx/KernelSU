@file:OptIn(ExperimentalMaterial3Api::class)

package me.weishu.kernelsu.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import com.ramcosta.composedestinations.annotation.Destination
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.weishu.kernelsu.ui.component.BackgroundImage
import me.weishu.kernelsu.ui.util.getBoxOpacity
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// libsu
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell

@Destination<RootGraph>
@Composable
fun QuickShellScreen() {
    var cmd by rememberSaveable { mutableStateOf("") }
    val logs = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val context = LocalContext.current

    // REACTIVE OPACITY STATE
    var boxOpacity by remember {
        mutableFloatStateOf(context.getBoxOpacity())
    }

    // listen when coming back from settings
    LaunchedEffect(Unit) {
        boxOpacity = context.getBoxOpacity()
    }

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

    BackgroundImage { containerColor ->
        Scaffold(
            containerColor = containerColor,
            topBar = {
                TopAppBar(
                    title = { Text("QuickShell") },
                    actions = {
                        IconButton(onClick = { logs.clear() }) {
                            Icon(Icons.Outlined.Delete, contentDescription = null)
                        }
                    },
                    // FIX BUG: TopBar transparan agar background image menyatu sampai atas
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            },
            // FIX BUG: Mengatur insets agar konten digambar di area aman tapi background full
            contentWindowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
            )
        ) { padding ->

            // SEMI-TRANSPARENT SURFACE CONTAINER DI ATAS BACKGROUND
            Surface(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = boxOpacity),
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {

                    // KARTU 1 (INPUT)
                    // FIX BUG: Tambahkan colors transparan di TonalCard agar tidak solid menutupi background
                    TonalCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.filledTonalCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        )
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
                                minLines = 1,
                                maxLines = 6,
                                placeholder = { Text("Input Command") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { runCommand() },
                                        enabled = cmd.isNotBlank()
                                    ) {
                                        Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                                    }
                                },
                                // Opsional: Textfield juga dibuat transparan agar elegan
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // KARTU 2 (LOGS)
                    // FIX BUG: Tambahkan colors transparan di TonalCard
                    TonalCard(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.filledTonalCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        )
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
                                    color = MaterialTheme.colorScheme
                                        .onSurface
                                        .copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}