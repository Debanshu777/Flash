package com.debanshu777.huggingfacemanager.download

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

class IosStoragePathProvider : StoragePathProvider {
    @OptIn(ExperimentalForeignApi::class)
    override fun getModelsStorageDirectory(modelId: String): String {
        val docs = NSFileManager.defaultManager
            .URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, false, null)!!.path!!
        val dir = "$docs/models/$modelId"
        NSFileManager.defaultManager.createDirectoryAtPath(dir, true, null, null)
        return dir
    }
    
    @OptIn(ExperimentalForeignApi::class)
    override fun getDatabasePath(): String {
        val docs = NSFileManager.defaultManager
            .URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, false, null)!!.path!!
        val dbDir = "$docs/databases"
        NSFileManager.defaultManager.createDirectoryAtPath(dbDir, true, null, null)
        return "$dbDir/flash.db"
    }
}
