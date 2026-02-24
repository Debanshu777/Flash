package com.debanshu777.huggingfacemanager.download

import android.content.Context
import android.os.Environment
import java.io.File

class AndroidStoragePathProvider(private val context: Context) : StoragePathProvider {
    override fun getModelsStorageDirectory(modelId: String): String {
        val base = when {
            Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED ->
                context.getExternalFilesDirs( null).firstOrNull()
            else -> null
        } ?: context.filesDir
        return File(base, "models/$modelId").apply { mkdirs() }.absolutePath
    }
    
    override fun getDatabasePath(): String =
        File(context.filesDir, "databases").apply { mkdirs() }.absolutePath + "/flash.db"
}
