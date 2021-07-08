package com.bugsnag.android.internal

import com.dslplatform.json.DslJson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class JsonHelper private constructor() {
    companion object {
        // Only one global DslJson is needed, and is thread-safe
        // Note: dsl-json adds about 150k to the final binary size.
        val dslJson = DslJson<MutableMap<String, Any>>()

        fun serialize(value: Any, stream: OutputStream) {
            dslJson.serialize(value, stream)
        }

        fun serialize(value: Any, file: File) {
            try {
                FileOutputStream(file).use { stream -> dslJson.serialize(value, stream) }
            } catch (ex: IOException) {
                throw IOException("Could not serialize JSON document to $file", ex)
            }
        }

        fun deserialize(bytes: ByteArray): MutableMap<String, Any> {
            val document = dslJson.deserialize(
                MutableMap::class.java,
                bytes,
                bytes.size
            )
            requireNotNull(document) { "JSON document is invalid" }
            @Suppress("UNCHECKED_CAST")
            return document as MutableMap<String, Any>
        }

        fun deserialize(stream: InputStream): MutableMap<String, Any> {
            val document = dslJson.deserialize(MutableMap::class.java, stream)
            requireNotNull(document) { "JSON document is invalid" }
            @Suppress("UNCHECKED_CAST")
            return document as MutableMap<String, Any>
        }

        fun deserialize(file: File): MutableMap<String, Any> {
            try {
                FileInputStream(file).use { stream -> return deserialize(stream) }
            } catch (ex: IOException) {
                throw IOException("Could not deserialize from $file", ex)
            }
        }
    }
}
