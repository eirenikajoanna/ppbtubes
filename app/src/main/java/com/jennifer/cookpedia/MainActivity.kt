package com.jennifer.cookpedia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.jennifer.cookpedia.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
//    private lateinit var adapter: ArrayAdapter<*>
    companion object {
        const val RC_SIGN_IN = 123
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginGoogle.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

//        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.))

    }

    public override fun onStart() {
        super.onStart()
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            moveToNavActivity()
        }
    }


    override fun onClick(v: View){
        when(v.id){
            R.id.loginGoogle -> {
                val signInIntent = mGoogleSignInClient.getSignInIntent()
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
               moveToNavActivity()
            }
            // Signed in successfully, show authenticated UI.
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("FAILED_SIGNIN", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private fun moveToNavActivity(){
        val moveWithObjectIntent = Intent(this@MainActivity, NavActivity::class.java)
        startActivity(moveWithObjectIntent)
        finish()
    }

    fun setActionBarTitle(title: String?) {
        supportActionBar!!.title = title
    }
}