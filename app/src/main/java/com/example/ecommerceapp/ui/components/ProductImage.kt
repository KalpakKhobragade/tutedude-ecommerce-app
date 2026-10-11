package com.example.ecommerceapp.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Shows a product image. Supports normal URLs (Firebase Storage / FakeStore API)
 * and "data:image/jpeg;base64,..." strings (images saved inside Firestore).
 */
@Composable
fun ProductImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (model != null && model.startsWith("data:image")) {
        val bitmap by produceState<ImageBitmap?>(initialValue = null, model) {
            value = withContext(Dispatchers.Default) { decodeDataUri(model) }
        }
        val loaded = bitmap
        if (loaded != null) {
            Image(
                bitmap = loaded,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        } else {
            Box(modifier = modifier.background(Color.LightGray))
        }
    } else {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = modifier.background(Color.LightGray),
            contentScale = contentScale
        )
    }
}

private fun decodeDataUri(dataUri: String): ImageBitmap? {
    return try {
        val bytes = Base64.decode(dataUri.substringAfter("base64,"), Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
