package me.weishu.kernelsu.ui.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives

object SelinuxHideManager {
    private val _selinuxHideState = MutableStateFlow<Boolean?>(null)
    val selinuxHideState: StateFlow<Boolean?> = _selinuxHideState.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Status code event for toasts: 0 (success), -EAGAIN (reboot required), or other error codes
    private val _statusEvent = MutableStateFlow<Int?>(null)
    val statusEvent: StateFlow<Int?> = _statusEvent.asStateFlow()

    fun fetchState() {
        CoroutineScope(Dispatchers.IO).launch {
            _selinuxHideState.value = Natives.isSelinuxHideEnabled()
        }
    }

    fun setSelinuxHide(enabled: Boolean, execKsud: (String, Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            _isProcessing.value = true
            val status = Natives.setSelinuxHideEnabled(enabled)
            
            // From the commit, we need to save the feature using ksud
            execKsud("feature save", true)
            
            if (status == 0) {
                _selinuxHideState.value = enabled
            }
            
            _statusEvent.value = status
            _isProcessing.value = false
        }
    }

    fun clearStatusEvent() {
        _statusEvent.value = null
    }
}
