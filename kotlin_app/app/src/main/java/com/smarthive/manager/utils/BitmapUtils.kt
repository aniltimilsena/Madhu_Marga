package com.smarthive.manager.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object BitmapUtils {
    /**
     * Compresses an image from a Uri and returns it as a ByteArray.
     * Useful for reducing size before uploading to Supabase or saving to Room.
     */
    fun compressImage(context: Context, uri: Uri, quality: Int = 70): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
