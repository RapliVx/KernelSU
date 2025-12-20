package me.weishu.kernelsu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme

import coil.compose.AsyncImage

import me.weishu.kernelsu.ui.util.getBackgroundImage

@Composable
fun BackgroundImage(
    content: @Composable (containerColor: Color) -> Unit
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    // Ambil URI dari SharedPreferences
    val imageUri = remember {
        context.getBackgroundImage()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Default background theme (selalu ada)
            .background(cs.background)
    ) {

        // Custom background image (jika ada)
        if (!imageUri.isNullOrEmpty()) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Overlay supaya UI kebaca & nyatu theme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.background.copy(alpha = 0.60f))
            )
        }

        // Kirim containerColor ke Scaffold
        content(
            if (imageUri.isNullOrEmpty())
                cs.background
            else
                Color.Transparent
        )
    }
}
