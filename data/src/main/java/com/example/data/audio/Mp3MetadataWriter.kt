package com.example.data.audio

import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PushbackInputStream

/** Writes one ID3v2.3 tag, replacing leading ID3v2 tags without buffering the audio. */
internal object Mp3MetadataWriter {
    fun write(
        input: InputStream,
        output: OutputStream,
        title: String,
        artist: String,
        album: String,
        artwork: ByteArray?,
        checkCancellation: () -> Unit = {}
    ) {
        val source = PushbackInputStream(input, HEADER_SIZE)
        skipExistingTags(source, checkCancellation)
        output.write(buildTag(title, artist, album, artwork))
        val buffer = ByteArray(8 * 1024)
        while (true) {
            checkCancellation()
            val count = source.read(buffer)
            if (count == -1) break
            output.write(buffer, 0, count)
        }
    }

    private fun buildTag(title: String, artist: String, album: String, artwork: ByteArray?): ByteArray {
        val body = ByteArrayOutputStream().apply {
            write(textFrame("TIT2", title))
            write(textFrame("TPE1", artist))
            write(textFrame("TALB", album))
            if (artwork != null) {
                // ISO-8859-1 description, JPEG MIME, front cover, empty description.
                val content = byteArrayOf(0) + "image/jpeg".toByteArray(Charsets.ISO_8859_1) +
                    byteArrayOf(0, 3, 0) + artwork
                write(frame("APIC", content))
            }
        }.toByteArray()
        // ID3v2.3 unsynchronisation applies to the whole body, not individual frames.
        val encoded = ByteArrayOutputStream().apply {
            body.forEachIndexed { index, byte ->
                write(byte.toInt() and 0xFF)
                if ((byte.toInt() and 0xFF) == 0xFF) {
                    val next = body.getOrNull(index + 1)?.toInt()?.and(0xFF)
                    if (next == null || next == 0 || next >= 0xE0) write(0)
                }
            }
        }.toByteArray()
        val header = byteArrayOf(73, 68, 51, 3, 0, 0x80.toByte(), 0, 0, 0, 0)
        repeat(4) { index ->
            header[6 + index] = ((encoded.size ushr (21 - index * 7)) and 0x7F).toByte()
        }
        return header + encoded
    }

    private fun textFrame(id: String, value: String): ByteArray =
        frame(id, byteArrayOf(1) + value.toByteArray(Charsets.UTF_16))

    private fun frame(id: String, content: ByteArray): ByteArray {
        val header = ByteArray(HEADER_SIZE)
        id.toByteArray(Charsets.US_ASCII).copyInto(header)
        repeat(4) { index ->
            header[4 + index] = (content.size ushr (24 - index * 8)).toByte()
        }
        return header + content
    }

    private fun skipExistingTags(input: PushbackInputStream, checkCancellation: () -> Unit) {
        var totalSkipped = 0L
        while (true) {
            checkCancellation()
            val header = ByteArray(HEADER_SIZE)
            var count = 0
            while (count < header.size) {
                val read = input.read(header, count, header.size - count)
                if (read == -1) break
                count += read
            }
            if (count < 3 || header[0] != 73.toByte() || header[1] != 68.toByte() ||
                header[2] != 51.toByte()
            ) {
                input.unread(header, 0, count)
                return
            }
            if (count != HEADER_SIZE) throw EOFException("Truncated ID3 header")
            val version = header[3].toInt() and 0xFF
            if (version !in 2..4 || (6..9).any { header[it].toInt() < 0 }) {
                throw IOException("Invalid ID3 header")
            }
            var size = 0
            for (index in 6..9) size = (size shl 7) or header[index].toInt()
            val footerSize = if (version == 4 && (header[5].toInt() and 0x10) != 0) 10 else 0
            totalSkipped += HEADER_SIZE + size.toLong() + footerSize
            if (totalSkipped > MAX_EXISTING_TAG_BYTES) throw IOException("ID3 tags are too large")
            var remaining = size + footerSize
            val buffer = ByteArray(8 * 1024)
            while (remaining > 0) {
                checkCancellation()
                val read = input.read(buffer, 0, minOf(buffer.size, remaining))
                if (read == -1) throw EOFException("Truncated ID3 tag")
                remaining -= read
            }
        }
    }

    private const val HEADER_SIZE = 10
    private const val MAX_EXISTING_TAG_BYTES = 16 * 1024 * 1024
}