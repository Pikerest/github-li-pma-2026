package com.example.myapp009notehub.data

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver

object NoteHubDatabaseInstance {
    @Volatile
    private var INSTANCE: NoteHubDatabase? = null

    fun getDatabase(context: Context): NoteHubDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                NoteHubDatabase::class.java,
                "notehub_database"
            )
                .setDriver(AndroidSQLiteDriver())
                .build()
            INSTANCE = instance
            instance
        }
    }
}
