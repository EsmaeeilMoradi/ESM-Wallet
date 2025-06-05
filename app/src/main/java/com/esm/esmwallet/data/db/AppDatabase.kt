package com.esm.esmwallet.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.esm.esmwallet.data.dao.CustomTokenDao
import com.esm.esmwallet.data.model.CustomToken

@Database(entities = [CustomToken::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customTokenDao(): CustomTokenDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esm_wallet_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}