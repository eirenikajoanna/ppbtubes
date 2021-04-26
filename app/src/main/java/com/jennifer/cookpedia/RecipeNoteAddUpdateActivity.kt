package com.jennifer.cookpedia

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.jennifer.cookpedia.db.RecipeNoteHelper
import com.jennifer.cookpedia.entity.RecipeNote
import com.jennifer.mynotesapp.db.DatabaseContract
import kotlinx.android.synthetic.main.activity_recipe_note_add_update.*
import java.text.SimpleDateFormat
import java.util.*

class RecipeNoteAddUpdateActivity : AppCompatActivity() {
    private var isEdit = false
    private var note: RecipeNote? = null
    private var position: Int = 0
    private lateinit var noteHelper: RecipeNoteHelper
    private lateinit var edt_name: EditText
    private lateinit var edt_desc: EditText
    private lateinit var edt_ingr: EditText
    private lateinit var edt_step: EditText
    private lateinit var btn_next: LinearLayout
    private lateinit var btn_text: TextView

    companion object {
        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_POSITION = "extra_position"
        const val REQUEST_ADD = 100
        const val RESULT_ADD = 101
        const val REQUEST_UPDATE = 200
        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_note_add_update)

        noteHelper = RecipeNoteHelper.getInstance(applicationContext)!!

        edt_name = findViewById(R.id.edt_name)
        edt_desc = findViewById(R.id.edt_desc)
        edt_ingr = findViewById(R.id.edt_ingr)
        edt_step = findViewById(R.id.edt_step)
        btn_next = findViewById(R.id.btn_next)

        note = intent.getParcelableExtra(EXTRA_NOTE)
        if (note != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            note = RecipeNote()
        }

        val actionBarTitle: String
        val btnTitle: String
        if (isEdit) {
            actionBarTitle = "Change"
            btnTitle = "Update"

            note?.let {
                edt_name.setText(it.name)
                edt_desc.setText(it.description)
                edt_ingr.setText(it.ingridients)
                edt_step.setText(it.steps)
            }

        } else {
            actionBarTitle = "Add"
            btnTitle = "Save"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btn_text = findViewById(R.id.btn_text)
        btn_text.text = btnTitle

        btn_next.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (view != null) {
                    if (view.id == R.id.btn_next) {
                        val name = edt_name.text.toString().trim()
                        val description = edt_desc.text.toString().trim()
                        val ingredients = edt_ingr.text.toString().trim()
                        val steps = edt_step.text.toString().trim()

                        if (name.isEmpty()) {
                            edt_name.error = "Field can not be blank"
                            return
                        }
                        if (description.isEmpty()) {
                            edt_name.error = "Field can not be blank"
                            return
                        }
                        if (ingredients.isEmpty()) {
                            edt_name.error = "Field can not be blank"
                            return
                        }
                        if (steps.isEmpty()) {
                            edt_name.error = "Field can not be blank"
                            return
                        }

                        note?.name = name
                        note?.description = description
                        note?.ingridients = ingredients
                        note?.steps = steps

                        val intent = Intent()
                        intent.putExtra(EXTRA_NOTE, note)
                        intent.putExtra(EXTRA_POSITION, position)

                        val values = ContentValues()
                        values.put(DatabaseContract.RecipeColumns.TITLE, name)
                        values.put(DatabaseContract.RecipeColumns.DESCRIPTION, description)
                        values.put(DatabaseContract.RecipeColumns.INGREDIENTS, ingredients)
                        values.put(DatabaseContract.RecipeColumns.STEPS, steps)

                        if (isEdit) {
                            val result = noteHelper.update(note?.id.toString(), values).toLong()
                            if (result > 0) {
                                setResult(RESULT_UPDATE, intent)
                                finish()
                            } else {
                                Toast.makeText(this@RecipeNoteAddUpdateActivity, "Update Failed", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val result = noteHelper.insert(values)

                            if (result > 0) {
                                note?.id = result.toInt()
                                setResult(RESULT_ADD, intent)
                                finish()
                            } else {
                                Toast.makeText(this@RecipeNoteAddUpdateActivity, "Insert Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String

        if (isDialogClose) {
            dialogTitle = "Cancel"
            dialogMessage = "Do you want to undo changes to the form?"
        } else {
            dialogMessage = "Are you sure you want to delete this item?"
            dialogTitle = "Delete Recipe Note"
        }

        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                if (isDialogClose) {
                    finish()
                } else {
                    val result = noteHelper.deleteById(note?.id.toString()).toLong()
                    if (result > 0) {
                        val intent = Intent()
                        intent.putExtra(EXTRA_POSITION, position)
                        setResult(RESULT_DELETE, intent)
                        finish()
                    } else {
                        Toast.makeText(this@RecipeNoteAddUpdateActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No") { dialog, id -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}