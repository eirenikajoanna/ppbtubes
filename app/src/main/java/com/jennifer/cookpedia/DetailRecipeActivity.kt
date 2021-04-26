package com.jennifer.cookpedia

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jennifer.cookpedia.adapter.ListDetailAdapter
import com.jennifer.cookpedia.db.RecipeHelper
import com.jennifer.cookpedia.entity.Recipe
import com.jennifer.cookpedia.helper.MappingHelper
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject

class DetailRecipeActivity : AppCompatActivity() {
    private lateinit var resep: Recipe
    private lateinit var id : String
    private lateinit var userid : String
    private lateinit var detailPhoto : ImageView
    private lateinit var detailName : TextView
    private lateinit var listView : LinearLayout
    private lateinit var listViewIng : LinearLayout
    private lateinit var description : TextView
    private lateinit var recipeHelper: RecipeHelper
    private lateinit var progressbar : ProgressBar

    companion object{
        const val EXTRA_ID = "recipe_id"
        const val EXTRA_USERID = "user_id"
        internal const val STATE_RESULT = "state_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_recipe)

        id = intent.getStringExtra(EXTRA_ID)!!
        userid = intent.getStringExtra(EXTRA_USERID)!!
        listView = findViewById(R.id.lv_steps)
        listViewIng = findViewById(R.id.lv_ingridients)
        description = findViewById(R.id.description)
        progressbar = findViewById(R.id.progressbar)
        recipeHelper = RecipeHelper.getInstance(applicationContext)
        recipeHelper.open()

        detailPhoto = findViewById(R.id.detail_photo)
        detailName = findViewById(R.id.detail_name)

        if (savedInstanceState == null) {
            prepare()
        } else {
            val recipe = savedInstanceState.getParcelable<Recipe>(STATE_RESULT)
            if (recipe != null){
                Glide.with(this@DetailRecipeActivity)
                        .load(recipe.photo)
                        .into(detailPhoto)
                detailName.text = recipe.name
                description.text = recipe.description
                addIngridients()
                addItem()
            }
        }
    }

    private fun prepare() {
        progressbar.visibility = View.VISIBLE

        val client = AsyncHttpClient()
        client.addHeader("x-rapidapi-key", "b8c8173233mshad9b42b38beb82dp11be63jsnba3b8a6d02eb")
        client.addHeader("x-rapidapi-host", "tasty.p.rapidapi.com")
        val url = "https://tasty.p.rapidapi.com/recipes/detail?id="+id

        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<Header>,
                responseBody: ByteArray
            ) {
                progressbar.visibility = View.INVISIBLE
                // Jika koneksi berhasil
                // Parsing JSON
                val listStep = ArrayList<String>()
                val listIngridients = ArrayList<String>()
                var descriptionGet = "-"
                val result = String(responseBody)
                Log.d("DetailRecipeActivity", result)
                try {
                    val jsonResult = JSONObject(result)
                    val jsonArray = jsonResult.getJSONArray("instructions")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val text = jsonObject.getString("display_text")
                        listStep.add(text)
                    }
                    val jsonArray2 = jsonResult.getJSONArray("sections")
                    val components = jsonArray2.getJSONObject(0).getJSONArray("components")
                    for (i in 0 until components.length()) {
                        val jsonObject = components.getJSONObject(i)
                        val text = jsonObject.getString("raw_text")
                        listIngridients.add(text)
                    }
                    val textDesc = jsonResult.getString("description")
                    if (textDesc != null && textDesc != ""){
                        descriptionGet = textDesc
                    }

                    var isLike = false
                    GlobalScope.launch(Dispatchers.Main) {
                        val deferredNotes = async(Dispatchers.IO) {
                            val cursor = recipeHelper.queryByUserId(userid)
                            MappingHelper.mapCursorToArrayList(cursor)
                        }
                        val idList = deferredNotes.await()
                        if (idList[0].contains(jsonResult.getInt("id").toString())) {
                            isLike = true
                        }
                    }
                    val recipe = Recipe(
                        jsonResult.getInt("id"),
                        jsonResult.getString("name"),
                        jsonResult.getString("thumbnail_url"),
                            descriptionGet,
                            listIngridients,
                        listStep,
                            isLike
                    )
                    resep = recipe
                    Glide.with(this@DetailRecipeActivity)
                        .load(recipe.photo)
                        .into(detailPhoto)
                    detailName.text = recipe.name
                    description.text = recipe.description
                    addIngridients()
                    addItem()
                    Log.d("FragmentProfile", resep.toString())
                } catch (e: Exception) {
                    Toast.makeText(this@DetailRecipeActivity, e.message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header>,
                responseBody: ByteArray,
                error: Throwable
            ) {
                // Jika koneksi gagal
                progressbar.visibility = View.INVISIBLE
                val errorMessage = when (statusCode) {
                    401 -> "$statusCode : Bad Request"
                    403 -> "$statusCode : Forbidden"
                    404 -> "$statusCode : Not Found"
                    else -> "$statusCode : ${error.message}"
                }
                Toast.makeText(this@DetailRecipeActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addItem() {
        val adapter = ListDetailAdapter(this)
        adapter.list = resep.steps!!
        for (i in 0 until adapter.count) {
            val vi: View = adapter.getView(i, null, listView);
            listView.addView(vi)
        }
    }
    private fun addIngridients() {
        val adapter = ListDetailAdapter(this)
        adapter.list = resep.ingridients!!
        for (i in 0 until adapter.count) {
            val vi: View = adapter.getView(i, null, listViewIng);
            listViewIng.addView(vi)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_RESULT, resep)
    }
}