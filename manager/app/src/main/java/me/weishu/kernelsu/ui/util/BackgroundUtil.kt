package me.weishu.kernelsu.ui.util

import android.content.Context

private const val PREFS_NAME = "settings"
private const val KEY_BACKGROUND_IMAGE = "background_image_uri"

/**
 * @return uri string of custom background image, or null if using default
 */
fun Context.getBackgroundImage(): String? {
    return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_BACKGROUND_IMAGE, null)
}

/**
 * Save custom background image uri.
 * Default background should NOT be saved.
 */
fun Context.saveBackgroundImage(uri: String) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_BACKGROUND_IMAGE, uri)
        .apply()
}

/**
 * Clear custom background image and fallback to default.
 */
fun Context.clearBackgroundImage() {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .remove(KEY_BACKGROUND_IMAGE)
        .apply()
}