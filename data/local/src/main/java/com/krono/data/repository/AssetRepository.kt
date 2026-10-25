package com.krono.data.repository

import android.content.Context
import com.krono.data.database.KronoDao
import com.krono.data.entity.AssetEntity
import java.io.File
import java.io.InputStream
import java.util.UUID

class AssetRepository(
    private val context: Context,
    private val dao: KronoDao
) {

    private val assetsDir: File by lazy {
        File(context.filesDir, "krono_wallpapers").apply {
            if (!exists()) mkdirs()
        }
    }

    suspend fun importAsset(
        inputStream: InputStream,
        mimeType: String = "image/png"
    ): AssetEntity? {
        val assetId = "asset-" + UUID.randomUUID().toString()
        val fileName = "$assetId.bin"
        val destinationFile = File(assetsDir, fileName)

        val now = System.currentTimeMillis()
        val initialEntity = AssetEntity(
            assetId = assetId,
            state = "IMPORTING",
            relativePath = fileName,
            mimeType = mimeType,
            createdAtUtcMs = now,
            updatedAtUtcMs = now
        )

        try {
            destinationFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            val finalEntity = initialEntity.copy(
                state = "READY",
                byteCount = destinationFile.length(),
                updatedAtUtcMs = System.currentTimeMillis()
            )
            return finalEntity
        } catch (e: Exception) {
            destinationFile.delete()
            return null
        }
    }

    fun getAssetFile(relativePath: String): File {
        return File(assetsDir, relativePath)
    }
}
