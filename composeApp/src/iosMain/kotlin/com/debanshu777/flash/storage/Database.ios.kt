package com.debanshu777.flash.storage

import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(dbPath: String): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(
        name = dbPath
    )
}
