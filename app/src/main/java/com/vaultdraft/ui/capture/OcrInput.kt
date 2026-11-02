package com.vaultdraft.ui.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object OcrInput {

    fun extractText(context: Context, uri: Uri, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val bitmap: Bitmap = try {
            if (Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            onError("Could not load image: ${e.message}")
            return
        }
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { visionText -> onResult(visionText.text) }
            .addOnFailureListener { onError(it.message ?: "OCR failed") }
    }
}

@Composable
fun OcrButton(onResult: (String) -> Unit, onError: (String) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) OcrInput.extractText(context, uri, onResult, onError)
    }
    IconButton(onClick = { launcher.launch("image/*") }) {
        Icon(imageVector = Icons.Filled.DocumentScanner, contentDescription = "Scan text from image")
    }
}
