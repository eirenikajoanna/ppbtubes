package com.jennifer.cookpedia.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jennifer.mynotesapp.db.DatabaseContract

internal class DatabaseRecipeHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "cookpedia"
        private const val DATABASE_VERSION = 3
        private val SQL_CREATE_TABLE_NOTE = "CREATE TABLE ${DatabaseContract.RecipeLikeColumns.TABLE_NAME}"+ " (${DatabaseContract.RecipeLikeColumns.ID} INTEGER NOT NULL," +
                " ${DatabaseContract.RecipeLikeColumns.USER} TEXT NOT NULL,"+
                "PRIMARY KEY (${DatabaseContract.RecipeLikeColumns.ID}, ${DatabaseContract.RecipeLikeColumns.USER}))"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_TABLE_NOTE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.RecipeLikeColumns.TABLE_NAME}")
        onCreate(db)
    }
}