package me.weishu.kernelsu.ui.util

import android.content.Context
import android.net.Uri
import java.io.File

fun resolveModuleBanner(
    context: Context,
    moduleId: String,
    banner: String
): Uri? {
    if (banner.isBlank()) return null

    return when {
        banner.startsWith("http://") || banner.startsWith("https://") ->
            Uri.parse(banner)

        banner.startsWith("/") -> {
            val file = File("/data/adb/ksu/$moduleId$banner")
            if (file.exists()) Uri.fromFile(file) else null
        }

        else -> null
    }
}