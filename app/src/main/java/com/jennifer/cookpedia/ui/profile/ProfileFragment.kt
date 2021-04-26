package com.jennifer.cookpedia.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jennifer.cookpedia.NavActivity
import com.jennifer.cookpedia.R
import com.jennifer.cookpedia.adapter.GridRecipeAdapter
import com.jennifer.cookpedia.db.RecipeHelper
import com.jennifer.cookpedia.entity.Person
import com.jennifer.cookpedia.entity.Recipe
import com.jennifer.cookpedia.helper.MappingHelper
import com.jennifer.cookpedia.ui.search.SearchFragment
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject


class ProfileFragment : Fragment(), View.OnClickListener{
    private lateinit var adapter: GridRecipeAdapter
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var user : Person
    private lateinit var logout :LinearLayout
    private lateinit var imgPhoto : CircleImageView
    private lateinit var progressbar : ProgressBar
    private lateinit var recipeHelper: RecipeHelper
    private lateinit var rv_recipe : RecyclerView
    private var list = ArrayList<Recipe>()

    companion object{
        private const val STATE_USER = "state_user"
        private const val STATE_LIST = "state_list"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        if (arguments != null) {
            user = arguments?.getParcelable<Person>("user")!!
        }
        profileViewModel.setSelectedNews(user)
        val textView: TextView = root.findViewById(R.id.text_profile)
        profileViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it.name
        })
        logout = root.findViewById(R.id.logout)
        logout.setOnClickListener(this)

        recipeHelper = RecipeHelper.getInstance(requireActivity().applicationContext)
        recipeHelper.open()

        progressbar = root.findViewById(R.id.progressbar)
        rv_recipe = root.findViewById(R.id.rv_recipe) as RecyclerView
        rv_recipe.setHasFixedSize(true)
        rv_recipe.layoutManager = GridLayoutManager(activity, 2)
        adapter = GridRecipeAdapter(requireActivity())
        adapter.idUser = user.ID.toString()
        adapter.mode = 1
        rv_recipe.adapter = adapter
        imgPhoto = root.findViewById(R.id.img_photo)
        progressbar.visibility = View.VISIBLE
        Glide.with(this)
            .load(user.personPhoto)
            .error(
                Glide.with(this)
                    .load("https://pfpmaker.com/_nuxt/img/profile-3-1.3e702c5.png")
            )
            .into(imgPhoto)
        progressbar.visibility = View.INVISIBLE

        if(savedInstanceState != null) {
            val user_state = savedInstanceState.getParcelable<Person>(ProfileFragment.STATE_USER)
            val recipe = savedInstanceState.getParcelableArrayList<Recipe>(ProfileFragment.STATE_LIST)
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
        return root
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when(v.id){
                R.id.logout -> {
                    val builder = AlertDialog.Builder(activity)
                    //set title for alert dialog
                    builder.setTitle("Sign Out")
                    //set message for alert dialog
                    builder.setMessage("Are you sure want to sign out?")
                    builder.setIcon(android.R.drawable.ic_dialog_alert)

                    //performing positive action
                    builder.setPositiveButton("Yes"){dialogInterface, which ->
                        val activity: NavActivity? = activity as NavActivity?
                        activity?.signOut()
                    }
                    //performing negative action
                    builder.setNegativeButton("No"){dialogInterface, which ->
                        Toast.makeText(activity,"sign out canceled",Toast.LENGTH_LONG).show()
                    }
                    // Create the AlertDialog
                    val alertDialog: AlertDialog = builder.create()
                    // Set other dialog properties
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
            }
        }
    }


    fun getListRecipe() {
        GlobalScope.launch(Dispatchers.Main) {
            val deferredNotes = async(Dispatchers.IO) {
                val cursor = recipeHelper.queryByUserId(user.ID!!)
                MappingHelper.mapCursorToArrayList(cursor)
            }
            val idList = deferredNotes.await()
            if(idList[0].size > 0 ){
                progressbar.visibility = View.VISIBLE
                for (i in 0 until idList[0].size) {
                    val client = AsyncHttpClient()
                    client.addHeader("x-rapidapi-key", "b8c8173233mshad9b42b38beb82dp11be63jsnba3b8a6d02eb")
                    client.addHeader("x-rapidapi-host", "tasty.p.rapidapi.com")
                    val url = "https://tasty.p.rapidapi.com/recipes/detail?id="+idList[0][i]

                    client.get(url , object : AsyncHttpResponseHandler() {
                        override fun onSuccess(
                                statusCode: Int,
                                headers: Array<Header>,
                                responseBody: ByteArray
                        ) {
                            // Jika koneksi berhasil
                            progressbar.visibility = View.INVISIBLE
                            // Parsing JSON
                            val result = String(responseBody)
                            Log.d("FragmentProfile", result)
                            try {
                                val jsonResult = JSONObject(result)
                                val recipe = Recipe(
                                        jsonResult.getInt("id"),
                                        jsonResult.getString("name"),
                                        jsonResult.getString("thumbnail_url"),
                                        "",
                                        null,
                                        null,
                                        true
                                )
                                adapter.addItem(recipe)
                                list.add(recipe)
                                Log.d("FragmentProfile", list.toString())
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
                progressbar.visibility = View.INVISIBLE
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(ProfileFragment.STATE_LIST, list)
        outState.putParcelable(ProfileFragment.STATE_USER, user)
    }

}