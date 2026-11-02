package com.example.mediastoreexampleapp.data.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.util.concurrent.CancellationException

class Mp3MetadataWriterTest {
    private val audio = byteArrayOf(0xFF.toByte(), 0xFB.toByte(), 0x90.toByte(), 0, 12, 34)

    @Test
    fun writesUnicodeMetadataAndPreservesAudio() {
        val result = write(audio)
        assertEquals("ID3", String(result, 0, 3, Charsets.US_ASCII))
        assertEquals(3, result[3].toInt())
        assertEquals(result.size - audio.size - 10, tagSize(result))
        val frames = frames(result)
        assertEquals("Песня", text(frames.getValue("TIT2")))
        assertEquals("Исполнитель", text(frames.getValue("TPE1")))
        assertEquals("Album 42", text(frames.getValue("TALB")))
        assertFalse(frames.containsKey("APIC"))
        assertArrayEquals(audio, result.copyOfRange(10 + tagSize(result), result.size))
    }

    @Test
    fun writesFrontCoverAndRestoresUnsynchronisedBytes() {
        val picture = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0, 0xFF.toByte())
        val result = write(audio, picture)
        val apic = frames(result).getValue("APIC")
        assertArrayEquals(
            byteArrayOf(0) + "image/jpeg".toByteArray() + byteArrayOf(0, 3, 0) + picture,
            apic
        )
        assertTrue(result[5].toInt() and 0x80 != 0)
    }

    @Test
    fun replacesExistingAndConsecutiveTags() {
        val oldTag = byteArrayOf(73, 68, 51, 3, 0, 0, 0, 0, 0, 3, 1, 2, 3)
        assertArrayEquals(write(audio), write(oldTag + oldTag + audio))
    }

    @Test
    fun skipsVersionFourFooter() {
        val oldTag = byteArrayOf(73, 68, 51, 4, 0, 16, 0, 0, 0, 0) + ByteArray(10)
        assertArrayEquals(write(audio), write(oldTag + audio))
    }

    @Test(expected = EOFException::class)
    fun rejectsTruncatedTag() {
        write(byteArrayOf(73, 68, 51, 3, 0, 0, 0, 0, 0, 5, 1))
    }

    @Test(expected = CancellationException::class)
    fun propagatesCancellation() {
        Mp3MetadataWriter.write(audio.inputStream(), ByteArrayOutputStream(), "", "", "", null) {
            throw CancellationException()
        }
    }

    private fun write(source: ByteArray, artwork: ByteArray? = null): ByteArray =
        ByteArrayOutputStream().apply {
            Mp3MetadataWriter.write(source.inputStream(), this, "Песня", "Исполнитель", "Album 42", artwork)
        }.toByteArray()

    private fun tagSize(bytes: ByteArray): Int =
        (6..9).fold(0) { size, index -> (size shl 7) or (bytes[index].toInt() and 0x7F) }

    private fun text(bytes: ByteArray): String {
        assertEquals(1, bytes[0].toInt())
        return String(bytes, 1, bytes.size - 1, Charsets.UTF_16)
    }

    private fun frames(tag: ByteArray): Map<String, ByteArray> {
        val body = ByteArrayOutputStream()
        var index = 10
        val end = 10 + tagSize(tag)
        while (index < end) {
            val value = tag[index++].toInt() and 0xFF
            body.write(value)
            if (value == 0xFF && index < end && tag[index] == 0.toByte()) index++
        }
        val bytes = body.toByteArray()
        val result = mutableMapOf<String, ByteArray>()
        index = 0
        while (index < bytes.size) {
            val id = String(bytes, index, 4, Charsets.US_ASCII)
            val size = (4..7).fold(0) { value, offset ->
                (value shl 8) or (bytes[index + offset].toInt() and 0xFF)
            }
            result[id] = bytes.copyOfRange(index + 10, index + 10 + size)
            index += 10 + size
        }
        return result
    }
}