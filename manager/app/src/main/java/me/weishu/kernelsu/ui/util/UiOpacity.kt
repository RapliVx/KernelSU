package me.weishu.kernelsu.ui.util

import android.content.Context

private const val PREFS_NAME = "settings"
private const val KEY_BOX_OPACITY = "box_opacity"

/**
 * Default opacity = 1f (SOLID)
 */
fun Context.getBoxOpacity(): Float {
    return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_BOX_OPACITY, 1f) // 🔥 SOLID DEFAULT
}

/**
 * Save opacity (0.1f .. 1f)
 */
fun Context.saveBoxOpacity(value: Float) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putFloat(KEY_BOX_OPACITY, value.coerceIn(0.1f, 1f))
        .apply()
}