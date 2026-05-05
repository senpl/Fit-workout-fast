package com.easyfitness.DAO

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.core.content.ContentProviderCompat.requireContext

open class DAOBase(context: Context) {
    private var database: SQLiteDatabase? = null
    private val dbHelper: DatabaseHelper = DatabaseHelper.Companion.getInstance(context)

    fun open(): SQLiteDatabase? {
        return dbHelper.writableDatabase.also { database = it }
    }

    val writableDatabase: SQLiteDatabase
        get() = dbHelper.writableDatabase.also { database = it }

    val readableDatabase: SQLiteDatabase
        get() = dbHelper.readableDatabase.also { database = it }

    fun close() {
        dbHelper.close()
    }
}
