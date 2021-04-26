package com.jennifer.cookpedia.helper

import android.database.Cursor
import com.jennifer.cookpedia.entity.RecipeNote
import com.jennifer.mynotesapp.db.DatabaseContract

object MappingHelper {
    fun mapCursorToArrayList(notesCursor: Cursor?): Array<ArrayList<String>> {
        val listId = ArrayList<String>()
        val listUser = ArrayList<String>()


        notesCursor?.apply {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.RecipeLikeColumns.ID))
                val user = getString(getColumnIndexOrThrow(DatabaseContract.RecipeLikeColumns.USER))
                listId.add(id.toString())
                listUser.add(user)
            }
        }

        val lala: Array<ArrayList<String>> = arrayOf(listId, listUser)
        return lala
    }
    fun mapCursorToArrayListForRN(notesCursor: Cursor?): ArrayList<RecipeNote> {
        val notesList = ArrayList<RecipeNote>()

        notesCursor?.apply {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.RecipeColumns._ID))
                val name = getString(getColumnIndexOrThrow(DatabaseContract.RecipeColumns.TITLE))
                val description = getString(getColumnIndexOrThrow(DatabaseContract.RecipeColumns.DESCRIPTION))
                val ingredients = getString(getColumnIndexOrThrow(DatabaseContract.RecipeColumns.INGREDIENTS))
                val steps = getString(getColumnIndexOrThrow(DatabaseContract.RecipeColumns.STEPS))
                notesList.add(RecipeNote(id, name, description, ingredients, steps))
            }
        }
        return notesList
    }
}