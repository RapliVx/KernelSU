package me.weishu.kernelsu.ui.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives

object AdbRootManager {
    private val _adbRootState = MutableStateFlow<Boolean?>(null)
    val adbRootState: StateFlow<Boolean?> = _adbRootState.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun fetchState() {
        CoroutineScope(Dispatchers.IO).launch {
            _adbRootState.value = Natives.isAdbRootEnabled()
        }
    }

    fun setAdbRoot(enabled: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            _isProcessing.value = true
            val success = Natives.setAdbRootEnabled(enabled)
            if (success) {
                _adbRootState.value = enabled
            }
            _isProcessing.value = false
        }
    }
}
