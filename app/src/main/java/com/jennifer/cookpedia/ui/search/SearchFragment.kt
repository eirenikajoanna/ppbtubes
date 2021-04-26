package com.jennifer.cookpedia.ui.search

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jennifer.cookpedia.R
import com.jennifer.cookpedia.adapter.GridRecipeAdapter
import com.jennifer.cookpedia.db.RecipeHelper
import com.jennifer.cookpedia.entity.Person
import com.jennifer.cookpedia.entity.Recipe
import com.jennifer.cookpedia.helper.MappingHelper
import com.jennifer.cookpedia.ui.home.HomeFragment
import com.jennifer.cookpedia.ui.profile.ProfileFragment
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject

class SearchFragment : Fragment(){

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var user : Person
    private var searchResult : String? = ""
    private lateinit var progressbar : ProgressBar
    private lateinit var rv_recipe : RecyclerView
    private lateinit var recipeHelper: RecipeHelper
    private var list = ArrayList<Recipe>()
    private lateinit var adapter: GridRecipeAdapter
    private lateinit var search: EditText

    companion object{
        private const val STATE_USER = "state_user"
        private const val STATE_LIST = "state_list"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        searchViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        if (arguments != null) {
            user = arguments?.getParcelable<Person>("user")!!
            searchResult = arguments?.getString("searchText")!!
        }
        searchViewModel.setSelectedNews(user)
        recipeHelper = RecipeHelper.getInstance(requireActivity().applicationContext)
        recipeHelper.open()
        Toast.makeText(activity, searchResult, Toast.LENGTH_SHORT).show()

        progressbar = root.findViewById(R.id.progressbar)
        rv_recipe = root.findViewById(R.id.rv_recipe) as RecyclerView
        rv_recipe.setHasFixedSize(true)

        rv_recipe.layoutManager = GridLayoutManager(activity, 2)
        adapter = GridRecipeAdapter(requireActivity())
        adapter.idUser = user.ID.toString()
        adapter.mode = 0
        rv_recipe.adapter = adapter

        progressbar.visibility = View.INVISIBLE
        if(savedInstanceState != null) {
            val user_state = savedInstanceState.getParcelable<Person>(SearchFragment.STATE_USER)
            val recipe = savedInstanceState.getParcelableArrayList<Recipe>(SearchFragment.STATE_LIST)
            if (recipe != null){
                for (item: Recipe in recipe) {
                    adapter.addItem(item)
                }
            }
            if(user_state != null){
                user = user_state
            }

        }else{
            getListRecipe()
        }

        search = root.findViewById(R.id.search_bar)
        search.setText(searchResult,TextView.BufferType.EDITABLE)
        search.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (event != null && event.keyCode === KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                var searchText = search.text.toString()
                var isEmptyFields = false
                if (searchText.isEmpty()) {
                    isEmptyFields = true
                    search.error = "Field ini tidak boleh kosong"
                }
                if (!isEmptyFields) {
                    Toast.makeText(activity, searchText, Toast.LENGTH_SHORT).show()
                    val navController = findNavController()
                    val bundle = bundleOf("searchText" to searchText)
                    navController.navigate(R.id.navigation_search, bundle)
                }
            }
            false
        })
        return root
    }
    fun getListRecipe() {
        progressbar.visibility = View.VISIBLE

        val client = AsyncHttpClient()
        client.addHeader("x-rapidapi-key", "b8c8173233mshad9b42b38beb82dp11be63jsnba3b8a6d02eb")
        client.addHeader("x-rapidapi-host", "tasty.p.rapidapi.com")
        val url = "https://tasty.p.rapidapi.com/recipes/list?from=0&size=20&q="+searchResult

        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<Header>,
                responseBody: ByteArray
            ) {
                // Jika koneksi berhasil
                progressbar.visibility = View.INVISIBLE
                // Parsing JSON
                val result = String(responseBody)
                Log.d("FragmentSearch", result)
                try {
                    val jsonResult = JSONObject(result)
                    val jsonArray = jsonResult.getJSONArray("results")

                    for (i in 10 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        var isLike = false
                        GlobalScope.launch(Dispatchers.Main) {
                            val deferredNotes = async(Dispatchers.IO) {
                                val cursor = recipeHelper.queryByUserId(user.ID.toString())
                                MappingHelper.mapCursorToArrayList(cursor)
                            }
                            val idList = deferredNotes.await()
                            if (idList[0].contains(jsonObject.getInt("id").toString())) {
                                isLike = true
                            }
                            progressbar.visibility = View.INVISIBLE
                            val recipe = Recipe(
                                jsonObject.getInt("id"),
                                jsonObject.getString("name"),
                                jsonObject.getString("thumbnail_url"),
                                "",
                                null,
                                null,
                                isLike
                            )
                            list.add(recipe)
                            adapter.addItem(recipe)
                        }
                    }
                    Log.d("FragmentSearch", list.toString())
                } catch (e: Exception) {
                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        search.setText(searchResult)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(SearchFragment.STATE_LIST, list)
        outState.putParcelable(SearchFragment.STATE_USER, user)
    }
}