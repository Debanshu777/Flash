package com.debanshu777.huggingfacemanager.download

interface StoragePathProvider {
    fun getModelsStorageDirectory(modelId: String): String
    fun getDatabasePath(): String
}