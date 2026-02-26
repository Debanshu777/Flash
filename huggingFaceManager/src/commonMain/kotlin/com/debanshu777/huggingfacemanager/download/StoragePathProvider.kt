package com.debanshu777.huggingfacemanager.download

interface StoragePathProvider {
    fun getModelsStorageDirectory(modelId: String): String
    fun getDatabasePath(): String
    fun fileExists(path: String): Boolean

    /**
     * Returns true if the path points to a readable model file (exists, is a file, and can be read).
     */
    fun isModelFileReadable(path: String): Boolean
}