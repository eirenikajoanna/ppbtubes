package com.jennifer.cookpedia

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jennifer.cookpedia.adapter.ListDetailAdapter
import com.jennifer.cookpedia.entity.RecipeNote

class DetailUploadRecipe : AppCompatActivity() {
    private lateinit var resepNote: RecipeNote
    private lateinit var detailName : TextView
    private lateinit var listView : LinearLayout
    private lateinit var listViewIng : LinearLayout
    private var position: Int = 0
    private lateinit var btn_edit : LinearLayout
    private lateinit var description : TextView
    private lateinit var listIngridients : ArrayList<String>
    private lateinit var listSteps : ArrayList<String>

    companion object{
        private const val STATE_POSITION = "state_position"
        const val EXTRA_RECIPE_NOTE = "recipe_note"
        const val EXTRA_POSITION = "position"
        private const val STATE_RESULT = "state_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_upload_recipe)

        resepNote = intent.getParcelableExtra<RecipeNote>(EXTRA_RECIPE_NOTE)!!
        position = intent.getIntExtra(EXTRA_POSITION, 0)
        listView = findViewById(R.id.lv_steps)
        listViewIng = findViewById(R.id.lv_ingridients)
        description = findViewById(R.id.description)
        btn_edit = findViewById(R.id.btn_edit)

        try {
            listIngridients = resepNote.ingridients?.split("\n") as ArrayList<String>
            listSteps = resepNote.steps?.split("\n") as ArrayList<String>
        }catch (e: ClassCastException){
            val list = ArrayList<String>()
            list.add(resepNote.ingridients!!)
            listIngridients = list
            val list2 = ArrayList<String>()
            list2.add(resepNote.steps!!)
            listSteps = list2
        }

        detailName = findViewById(R.id.detail_name)
        if (savedInstanceState != null) {
            resepNote = savedInstanceState.getParcelable<RecipeNote>(DetailUploadRecipe.STATE_RESULT)!!
            position = savedInstanceState.getInt(DetailUploadRecipe.STATE_POSITION, 0)
        }

        detailName.text = resepNote.name
        description.text = resepNote.description
        addIngridients()
        addItem()
        btn_edit.setOnClickListener {
            val intent = Intent(this, RecipeNoteAddUpdateActivity::class.java)
            intent.putExtra(
                RecipeNoteAddUpdateActivity.EXTRA_POSITION,
                position
            )
            intent.putExtra(RecipeNoteAddUpdateActivity.EXTRA_NOTE, resepNote)
            startActivityForResult(intent, RecipeNoteAddUpdateActivity.REQUEST_UPDATE)
        }
    }

    private fun addItem() {
        val adapter = ListDetailAdapter(this)
        adapter.list = listSteps!!
        for (i in 0 until adapter.count) {
            val vi: View = adapter.getView(i, null, listView);
            listView.addView(vi)
        }
    }
    private fun addIngridients() {
        val adapter = ListDetailAdapter(this)
        adapter.list = listIngridients!!
        for (i in 0 until adapter.count) {
            val vi: View = adapter.getView(i, null, listViewIng);
            listViewIng.addView(vi)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(DetailUploadRecipe.STATE_RESULT, resepNote)
        outState.putInt(DetailUploadRecipe.STATE_POSITION, position)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setResult(resultCode, data)
        finish()
    }
}