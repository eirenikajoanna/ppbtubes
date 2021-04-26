package com.jennifer.cookpedia.adapter

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jennifer.cookpedia.DetailRecipeActivity
import com.jennifer.cookpedia.R
import com.jennifer.cookpedia.db.RecipeHelper
import com.jennifer.cookpedia.entity.Recipe
import com.jennifer.mynotesapp.db.DatabaseContract

class GridRecipeAdapter(private val activity: Activity) : RecyclerView.Adapter<GridRecipeAdapter.GridViewHolder>(){
    private lateinit var recipeHelper: RecipeHelper
    internal var mode = 0
    var listRecipe = ArrayList<Recipe>()
        set(listNotes) {
            if (listNotes.size > 0) {
                this.listRecipe.clear()
            }
            this.listRecipe.addAll(listNotes)
            notifyDataSetChanged()
        }
    internal var idUser = "0"
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GridRecipeAdapter.GridViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_grid_recipe,
            parent,
            false
        )
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridRecipeAdapter.GridViewHolder, position: Int) {
        holder.bind(listRecipe[position], position)
    }

    override fun getItemCount(): Int = listRecipe.size

    fun addItem(recipe: Recipe) {
        this.listRecipe.add(recipe)
        notifyItemInserted(this.listRecipe.size - 1)
    }

    fun updateItem(position: Int, note: Recipe) {
        this.listRecipe[position] = note
        notifyItemChanged(position, note)
    }
    fun removeItem(position: Int) {
        this.listRecipe.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.listRecipe.size)
    }

    inner class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgItemPhoto : ImageView = itemView.findViewById(R.id.img_item_photo)
        private val tvItemName : TextView  = itemView.findViewById(R.id.recipe_item_name)
        private val buttonLike : ImageView = itemView.findViewById(R.id.buttonLike)
        private val linearRecipeLayout : LinearLayout = itemView.findViewById(R.id.recipeGrid)
        fun bind(recipe: Recipe, position: Int) {
            Glide.with(itemView.context)
                .load(recipe.photo)
                .apply(RequestOptions().override(350, 350))
                .into(imgItemPhoto)
            tvItemName.text = recipe.name
            if (recipe.isLiked) buttonLike.setImageResource(R.drawable.heart_red)
            buttonLike.setOnClickListener{
                recipeHelper = RecipeHelper.getInstance(it.context.applicationContext)
                recipeHelper.open()
                if(mode == 0){
                    if (recipe.isLiked) {
                        val result = recipeHelper.deleteById(recipe.id.toString(),idUser).toLong()
                        buttonLike.setImageResource(R.drawable.heart_white)
                        recipe.isLiked = false
                        updateItem(position, recipe)
                    }else{
                        val values = ContentValues()
                        values.put(DatabaseContract.RecipeLikeColumns.ID, recipe.id)
                        values.put(DatabaseContract.RecipeLikeColumns.USER, idUser)
                        val result = recipeHelper.insert(values)
                        buttonLike.setImageResource(R.drawable.heart_red)
                        recipe.isLiked = true
                        updateItem(position, recipe)
                    }
                }else{
                    if (recipe.isLiked) {
                        val result = recipeHelper.deleteById(recipe.id.toString(),idUser).toLong()
                        removeItem(position)
                    }else{
                        val values = ContentValues()
                        values.put(DatabaseContract.RecipeLikeColumns.ID, recipe.id)
                        values.put(DatabaseContract.RecipeLikeColumns.USER, idUser)
                        val result = recipeHelper.insert(values)
                    }
                }
                recipeHelper.close()
            }
            linearRecipeLayout.setOnClickListener {
                val intent = Intent(activity, DetailRecipeActivity::class.java)
                intent.putExtra(DetailRecipeActivity.EXTRA_ID, recipe.id.toString())
                intent.putExtra(DetailRecipeActivity.EXTRA_USERID, idUser)
                it.getContext().startActivity(intent)
            }
        }
    }
}