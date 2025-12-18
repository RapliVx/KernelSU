package me.weishu.kernelsu.ui.component

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ModuleBanner(
    uri: Uri,
    height: Dp = 120.dp
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        contentScale = ContentScale.Crop
    )
}