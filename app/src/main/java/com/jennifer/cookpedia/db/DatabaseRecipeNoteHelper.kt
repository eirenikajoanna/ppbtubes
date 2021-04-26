package com.jennifer.cookpedia.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jennifer.mynotesapp.db.DatabaseContract

class DatabaseRecipeNoteHelper (context: Context) : SQLiteOpenHelper(context, DatabaseRecipeNoteHelper.DATABASE_NAME, null, DatabaseRecipeNoteHelper.DATABASE_VERSION){
    companion object {
        private const val DATABASE_NAME = "cookpedia"
        private const val DATABASE_VERSION = 3
        private val SQL_CREATE_TABLE_RECIPE = "CREATE TABLE ${DatabaseContract.RecipeColumns.TABLE_NAME}"+ " (${DatabaseContract.RecipeColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                " ${DatabaseContract.RecipeColumns.TITLE} TEXT NOT NULL," +
                " ${DatabaseContract.RecipeColumns.DESCRIPTION} TEXT NOT NULL,"+
                " ${DatabaseContract.RecipeColumns.INGREDIENTS} TEXT NOT NULL,"+
                " ${DatabaseContract.RecipeColumns.STEPS} TEXT NOT NULL)"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_TABLE_RECIPE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.RecipeColumns.TABLE_NAME}")
        onCreate(db)
    }
}