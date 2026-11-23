package com.example.mediastoreexampleapp.data.audio

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mediastoreexampleapp.data.mapper.toTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class EmbeddedArtworkTest {
    @Test
    fun normalizesLargeArtworkToBoundedJpeg() {
        val source = Bitmap.createBitmap(2200, 1100, Bitmap.Config.ARGB_8888)
        val bytes = try {
            source.eraseColor(Color.RED)
            ByteArrayOutputStream().apply {
                source.compress(Bitmap.CompressFormat.PNG, 100, this)
            }.toByteArray()
        } finally {
            source.recycle()
        }
        val jpeg = ArtworkDownloader().normalize(bytes)
        assertEquals(0xFF, jpeg[0].toInt() and 0xFF)
        assertEquals(0xD8, jpeg[1].toInt() and 0xFF)
        val decoded = requireNotNull(BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size))
        try {
            assertEquals(1024, decoded.width)
            assertEquals(512, decoded.height)
        } finally {
            decoded.recycle()
        }
    }

    @Test(expected = IOException::class)
    fun rejectsNonImageResponse() {
        ArtworkDownloader().normalize("not an image".toByteArray())
    }

    @Test
    fun missingArtworkDoesNotRequireNetwork() = runBlocking {
        assertEquals(null, ArtworkDownloader().download(null))
        assertEquals(null, ArtworkDownloader().download(" "))
    }

    @Test
    fun publishedTracksExposeIndependentArtworkThroughMapperAlbumUris() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        val created = mutableListOf<android.net.Uri>()
        val albumIds = mutableListOf<Long>()
        try {
            listOf(Color.RED, Color.BLUE).forEachIndexed { index, color ->
                val album = "Artwork test ${System.nanoTime()} $index"
                val title = "Тест обложки $index"
                val artist = "Исполнитель"
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, "$album.mp3")
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.Audio.Media.TITLE, title)
                    put(MediaStore.Audio.Media.ARTIST, artist)
                    put(MediaStore.Audio.Media.ALBUM, album)
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/MediaStoreExampleAppTests")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
                val uri = requireNotNull(resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values))
                created.add(uri)
                val artwork = picture(color)
                requireNotNull(resolver.openOutputStream(uri)).use { output ->
                    Mp3MetadataWriter.write(audio().inputStream(), output, title, artist, album, artwork)
                }
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    assertEquals(title, retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE))
                    assertEquals(artist, retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST))
                    assertEquals(album, retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM))
                    val bytes = requireNotNull(retriever.embeddedPicture)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    assertNotNull(bitmap)
                    assertColor(color, bitmap.getPixel(16, 16))
                    bitmap.recycle()
                } finally {
                    retriever.release()
                }
                resolver.update(uri, ContentValues().apply {
                    put(MediaStore.Audio.Media.IS_PENDING, 0)
                }, null, null)
                val track = requireNotNull(resolver.query(uri, null, null, null, null)).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    albumIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)))
                    cursor.toTrack()
                }
                assertEquals(title, track.title)
                val thumbnail = resolver.loadThumbnail(android.net.Uri.parse(track.smallImageUrl), Size(64, 64), null)
                assertColor(color, thumbnail.getPixel(thumbnail.width / 2, thumbnail.height / 2))
                thumbnail.recycle()
            }
            assertNotEquals(albumIds[0], albumIds[1])
        } finally {
            created.forEach { resolver.delete(it, null, null) }
        }
    }

    private fun picture(color: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        return try {
            bitmap.eraseColor(color)
            ByteArrayOutputStream().apply {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, this))
            }.toByteArray()
        } finally {
            bitmap.recycle()
        }
    }

    // MPEG-1 Layer III, 128 kbps, 44.1 kHz: complete empty frames for metadata parsing.
    private fun audio(): ByteArray = ByteArrayOutputStream().apply {
        repeat(40) {
            write(byteArrayOf(0xFF.toByte(), 0xFB.toByte(), 0x90.toByte(), 0x64))
            write(ByteArray(413))
        }
    }.toByteArray()

    private fun assertColor(expected: Int, actual: Int) {
        assertTrue(kotlin.math.abs(Color.red(expected) - Color.red(actual)) < 15)
        assertTrue(kotlin.math.abs(Color.blue(expected) - Color.blue(actual)) < 15)
        assertTrue(kotlin.math.abs(Color.green(expected) - Color.green(actual)) < 15)
    }
}