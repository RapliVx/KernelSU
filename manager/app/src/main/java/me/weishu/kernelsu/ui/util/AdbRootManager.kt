package me.weishu.kernelsu.ui.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.topjohnwu.superuser.ShellUtils

object AdbRootManager {
    private val _adbRootState = MutableStateFlow<Boolean?>(null)
    val adbRootState: StateFlow<Boolean?> = _adbRootState.asStateFlow()

    private val _adbRootStatus = MutableStateFlow("unsupported")
    val adbRootStatus: StateFlow<String> = _adbRootStatus.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun fetchState() {
        CoroutineScope(Dispatchers.IO).launch {
            val shell = getRootShell()
            if (!shell.isRoot) return@launch

            val statusStr = ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} feature check adb_root").trim()
            _adbRootStatus.value = statusStr
            
            val getConfigOut = ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} feature get --config adb_root").trim()
            val isEnabled = getConfigOut.contains("Status: enabled")
            _adbRootState.value = isEnabled
        }
    }

    fun setAdbRoot(enabled: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            _isProcessing.value = true
            val success = execKsud("feature set adb_root ${if (enabled) 1 else 0}", true)
            if (success) {
                // Save feature config so it persists
                execKsud("feature save", true)
                // Restart adbd dynamically
                val shell = getRootShell()
                ShellUtils.fastCmd(shell, "setprop ctl.restart adbd")
                _adbRootState.value = enabled
            }
            _isProcessing.value = false
        }
    }
}
