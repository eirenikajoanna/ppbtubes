package com.jennifer.cookpedia.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.jennifer.mynotesapp.db.DatabaseContract.RecipeLikeColumns.Companion.TABLE_NAME
import com.jennifer.mynotesapp.db.DatabaseContract.RecipeLikeColumns.Companion.ID
import com.jennifer.mynotesapp.db.DatabaseContract.RecipeLikeColumns.Companion.USER
import java.sql.SQLException

class RecipeHelper(context: Context) {
    companion object {
        private const val DATABASE_TABLE = TABLE_NAME
        private lateinit var dataBaseHelper: DatabaseRecipeHelper
        private var INSTANCE: RecipeHelper? = null
        private lateinit var database: SQLiteDatabase
        fun getInstance(context: Context): RecipeHelper =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: RecipeHelper(context)
                }
    }
    init {
        dataBaseHelper = DatabaseRecipeHelper(context)
    }
    @Throws(SQLException::class)
    fun open() {
        database = dataBaseHelper.writableDatabase
    }
    fun close() {
        dataBaseHelper.close()

        if (database.isOpen)
            database.close()
    }
    fun queryAll(): Cursor {
        return database.query(
                DATABASE_TABLE,
                null,
                null,
                null,
                null,
                null,
                "$ID ASC")
    }
    fun queryByUserId(id: String): Cursor {
        return database.query(
            DATABASE_TABLE,
            null,
            "$USER = ?",
            arrayOf(id),
            null,
            null,
            null,
            null)
    }
    fun insert(values: ContentValues?): Long {
        return database.insert(DATABASE_TABLE, null, values)
    }

    fun deleteById(id: String, userid: String): Int {
        return database.delete(DATABASE_TABLE, "$ID = '$id' AND $USER = '$userid'", null)
    }
}
