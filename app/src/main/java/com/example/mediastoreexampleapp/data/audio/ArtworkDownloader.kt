package com.example.mediastoreexampleapp.data.audio

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ArtworkDownloader @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun download(url: String?): ByteArray? {
        if (url.isNullOrBlank()) return null
        val coroutineContext = currentCoroutineContext()
        coroutineContext.ensureActive()
        val bytes = client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Artwork HTTP ${response.code}")
            val body = response.body
            if (body.contentLength() > MAX_DOWNLOAD_BYTES) throw IOException("Artwork is too large")
            body.byteStream().use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)
                while (true) {
                    coroutineContext.ensureActive()
                    val count = input.read(buffer)
                    if (count == -1) break
                    if (output.size() + count > MAX_DOWNLOAD_BYTES) {
                        throw IOException("Artwork is too large")
                    }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
        }
        coroutineContext.ensureActive()
        return normalize(bytes)
    }

    internal fun normalize(bytes: ByteArray): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0 ||
            bounds.outWidth.toLong() * bounds.outHeight > MAX_PIXELS
        ) throw IOException("Invalid or oversized artwork")

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_DIMENSION * 2) sample *= 2
        val bitmap = BitmapFactory.decodeByteArray(
            bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample }
        ) ?: throw IOException("Cannot decode artwork")
        var resized: Bitmap? = null
        try {
            val largest = maxOf(bitmap.width, bitmap.height)
            val image = if (largest > MAX_DIMENSION) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    maxOf(1, bitmap.width * MAX_DIMENSION / largest),
                    maxOf(1, bitmap.height * MAX_DIMENSION / largest),
                    true
                ).also { resized = it }
            } else bitmap
            return ByteArrayOutputStream().use { output ->
                if (!image.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
                    throw IOException("Cannot encode artwork")
                }
                output.toByteArray()
            }
        } finally {
            resized?.takeIf { it !== bitmap }?.recycle()
            bitmap.recycle()
        }
    }

    private companion object {
        const val MAX_DOWNLOAD_BYTES = 5 * 1024 * 1024
        const val MAX_DIMENSION = 1024
        const val MAX_PIXELS = 40_000_000L
    }
}