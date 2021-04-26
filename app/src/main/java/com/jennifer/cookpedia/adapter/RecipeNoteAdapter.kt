package com.jennifer.cookpedia.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.jennifer.cookpedia.*
import com.jennifer.cookpedia.entity.RecipeNote
import com.jennifer.cookpedia.ui.upload.UploadFragment


class RecipeNoteAdapter(private val activity: Activity, private val fragment: UploadFragment) : RecyclerView.Adapter<RecipeNoteAdapter.NoteViewHolder>() {
    var listNotes = ArrayList<RecipeNote>()
        set(listNotes) {
            if (listNotes.size > 0) {
                this.listNotes.clear()
            }
            this.listNotes.addAll(listNotes)
            notifyDataSetChanged()
        }

    fun addItem(note: RecipeNote) {
        this.listNotes.add(note)
        notifyItemInserted(this.listNotes.size - 1)
    }

    fun updateItem(position: Int, note: RecipeNote) {
        this.listNotes[position] = note
        notifyItemChanged(position, note)
    }

    fun removeItem(position: Int) {
        this.listNotes.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.listNotes.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_recipe_note,
            parent,
            false
        )
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(listNotes[position])
    }

    override fun getItemCount(): Int = this.listNotes.size

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv_item_title: TextView = itemView.findViewById(R.id.tv_item_title)
        private val tv_item_description: TextView = itemView.findViewById(R.id.tv_item_description)
        private val cv_item_note: CardView = itemView.findViewById(R.id.cv_item_note)

        fun bind(note: RecipeNote) {
            with(itemView){
                tv_item_title.text = note.name
                tv_item_description.text = note.description

                cv_item_note.setOnClickListener(
                    CustomOnItemClickListener(
                        bindingAdapterPosition,
                        object : CustomOnItemClickListener.OnItemClickCallback {
                            override fun onItemClicked(view: View, position: Int) {
                                val intent =
                                    Intent(activity, DetailUploadRecipe::class.java)
                                intent.putExtra(
                                    DetailUploadRecipe.EXTRA_POSITION,
                                    position
                                )
                                intent.putExtra(DetailUploadRecipe.EXTRA_RECIPE_NOTE, note)
                                fragment.moveIntentToUpdate(
                                    intent,
                                    RecipeNoteAddUpdateActivity.REQUEST_UPDATE
                                )
                            }
                        })
                )
            }
        }
    }
}