package com.jennifer.mynotesapp.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
internal class DatabaseContract {

    internal class RecipeLikeColumns : BaseColumns {
        companion object {
            const val TABLE_NAME = "recipelike"
            const val ID = "idRecipe"
            const val USER = "idUser"
        }
    }

    internal class RecipeColumns : BaseColumns {
        companion object {
            const val TABLE_NAME = "recipe"
            const val _ID = "_id"
            const val TITLE = "title"
            const val DESCRIPTION = "description"
            const val INGREDIENTS = "ingredients"
            const val STEPS = "STEPS"
        }
    }
}