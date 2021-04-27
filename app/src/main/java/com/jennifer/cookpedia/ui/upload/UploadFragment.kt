package com.jennifer.cookpedia.ui.upload

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jennifer.cookpedia.R
import com.jennifer.cookpedia.RecipeNoteAddUpdateActivity
import com.jennifer.cookpedia.adapter.RecipeNoteAdapter
import com.jennifer.cookpedia.db.RecipeNoteHelper
import com.jennifer.cookpedia.entity.RecipeNote
import com.jennifer.cookpedia.helper.MappingHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class UploadFragment : Fragment() {

    private lateinit var uploadViewModel: UploadViewModel
    private lateinit var adapter: RecipeNoteAdapter
    private lateinit var noteHelper: RecipeNoteHelper
    private lateinit var rv_notes : RecyclerView
    private lateinit var fab_add : FloatingActionButton
    private lateinit var progressbar : ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uploadViewModel =
            ViewModelProvider(this).get(UploadViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_upload, container, false)

        rv_notes = root.findViewById(R.id.rv_notes)
        rv_notes.layoutManager = LinearLayoutManager(context)
        rv_notes.setHasFixedSize(true)
        adapter = RecipeNoteAdapter(requireActivity(), this@UploadFragment)
        rv_notes.adapter = adapter

        noteHelper = RecipeNoteHelper.getInstance(requireActivity().applicationContext)!!
        noteHelper.open()
        fab_add = root.findViewById(R.id.fab_add)

        progressbar = root.findViewById(R.id.progressbar)
        fab_add.setOnClickListener {
            val intent = Intent(activity, RecipeNoteAddUpdateActivity::class.java)
            startActivityForResult(intent, RecipeNoteAddUpdateActivity.REQUEST_ADD)
        }

        if (savedInstanceState == null) {
            loadNotesAsync()
        } else {
            val list = savedInstanceState.getParcelableArrayList<RecipeNote>(BluetoothAdapter.EXTRA_STATE)
            if (list != null) {
                adapter.listNotes = list
            }
        }
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            when (requestCode) {
                RecipeNoteAddUpdateActivity.REQUEST_ADD -> if (resultCode == RecipeNoteAddUpdateActivity.RESULT_ADD) {
                    val note =
                        data.getParcelableExtra<RecipeNote>(RecipeNoteAddUpdateActivity.EXTRA_NOTE)
                    if (note != null) {
                        adapter.addItem(note)
                    }
                    rv_notes.smoothScrollToPosition(adapter.itemCount - 1)

                    showSnackbarMessage("Item successfully created")
                }
                RecipeNoteAddUpdateActivity.REQUEST_UPDATE ->
                    when (resultCode) {
                        RecipeNoteAddUpdateActivity.RESULT_UPDATE -> {

                            val note = data.getParcelableExtra<RecipeNote>(
                                RecipeNoteAddUpdateActivity.EXTRA_NOTE
                            )
                            val position = data.getIntExtra(
                                RecipeNoteAddUpdateActivity.EXTRA_POSITION,
                                0
                            )
                            adapter.updateItem(position, note!!)
                            rv_notes.smoothScrollToPosition(position)

                            showSnackbarMessage("Item successfully updated")
                        }
                        RecipeNoteAddUpdateActivity.RESULT_DELETE -> {
                            val position = data.getIntExtra(
                                RecipeNoteAddUpdateActivity.EXTRA_POSITION,
                                0
                            )

                            adapter.removeItem(position)

                            showSnackbarMessage("item successfully deleted")
                        }
                    }
            }
        }
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(rv_notes, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        noteHelper.close()
    }
    private fun loadNotesAsync() {
        GlobalScope.launch(Dispatchers.Main) {
            progressbar.visibility = View.VISIBLE
            val deferredNotes = async(Dispatchers.IO) {
                val cursor = noteHelper.queryAll()
                MappingHelper.mapCursorToArrayListForRN(cursor)
            }
            progressbar.visibility = View.INVISIBLE
            val notes = deferredNotes.await()
            if (notes.size > 0) {
                adapter.listNotes = notes
            } else {
                adapter.listNotes = ArrayList()
                showSnackbarMessage("No data")
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(BluetoothAdapter.EXTRA_STATE, adapter.listNotes)
    }

    fun moveIntentToUpdate(intent: Intent, requestUpdate: Int) {
        startActivityForResult(intent, requestUpdate)
    }
}