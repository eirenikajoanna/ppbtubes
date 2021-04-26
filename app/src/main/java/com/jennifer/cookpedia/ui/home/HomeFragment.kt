package com.jennifer.cookpedia.ui.home

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jennifer.cookpedia.R
import com.jennifer.cookpedia.adapter.GridRecipeAdapter
import com.jennifer.cookpedia.db.RecipeHelper
import com.jennifer.cookpedia.entity.Person
import com.jennifer.cookpedia.entity.Recipe
import com.jennifer.cookpedia.helper.MappingHelper
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.coroutines.Dispatchers
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.jennifer.cookpedia.DetailRecipeActivity
import com.jennifer.cookpedia.ui.search.SearchFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var user : Person
    private lateinit var progressbar : ProgressBar
    private lateinit var rv_recipe : RecyclerView
    private lateinit var recipeHelper: RecipeHelper
    private var list = ArrayList<Recipe>()
    private lateinit var adapter: GridRecipeAdapter
    private lateinit var search: EditText
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    companion object{
        private const val STATE_USER = "state_user"
        private const val STATE_LIST = "state_list"
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        if (arguments != null) {
            user = arguments?.getParcelable<Person>("user")!!
        }else{
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
            val acct = GoogleSignIn.getLastSignedInAccount(activity)
            val person = Person(
                acct?.displayName,
                acct?.id,
                acct?.email,
                acct?.photoUrl.toString()
            )
            user = person
        }
        homeViewModel.setSelectedNews(user)

        recipeHelper = RecipeHelper.getInstance(requireActivity().applicationContext)
        recipeHelper.open()

        progressbar = root.findViewById(R.id.progressbar)
        rv_recipe = root.findViewById(R.id.rv_recipe) as RecyclerView
        rv_recipe.setHasFixedSize(true)

        rv_recipe.layoutManager = GridLayoutManager(activity, 2)
        adapter = GridRecipeAdapter(requireActivity())
        adapter.idUser = user.ID.toString()
        adapter.mode = 0
        rv_recipe.adapter = adapter

        progressbar.visibility = View.INVISIBLE

        search = root.findViewById(R.id.search_bar)
        search.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (event != null && event.keyCode === KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                var searchText = search.text.toString()
                var isEmptyFields = false
                if(searchText.isEmpty()){
                    isEmptyFields = true
                    search.error = "Field ini tidak boleh kosong"
                }
                if(!isEmptyFields){
                    val navController = findNavController()
                    val bundle = bundleOf(
                        "user" to this.user,
                        "searchText" to searchText
                    )
                    navController.navigate(R.id.navigation_search, bundle)
                }
            }
            false
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(savedInstanceState != null) {
            val user_state = savedInstanceState.getParcelable<Person>(HomeFragment.STATE_USER)
            val recipe = savedInstanceState.getParcelableArrayList<Recipe>(HomeFragment.STATE_LIST)
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
    }

    fun getListRecipe() {
        progressbar.visibility = View.VISIBLE

        val client = AsyncHttpClient()
        client.addHeader("x-rapidapi-key", "b8c8173233mshad9b42b38beb82dp11be63jsnba3b8a6d02eb")
        client.addHeader("x-rapidapi-host", "tasty.p.rapidapi.com")
        val url = "https://tasty.p.rapidapi.com/recipes/list?from=0&size=20&tags=under_30_minutes"

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
                Log.d("FragmentHome", result)
                try {
                    val jsonResult = JSONObject(result)
                    val jsonArray = jsonResult.getJSONArray("results")

                    for (i in 0 until jsonArray.length()) {
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
                    Log.d("FragmentHome", list.toString())
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
        search.setText("")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(HomeFragment.STATE_LIST, list)
        outState.putParcelable(HomeFragment.STATE_USER, user)
    }
}