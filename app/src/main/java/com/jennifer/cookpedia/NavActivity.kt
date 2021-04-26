package com.jennifer.cookpedia

import android.R.attr
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jennifer.cookpedia.entity.Person


class NavActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        val navController = findNavController(R.id.nav_host_fragment)
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        val person = Person(
            acct?.displayName,
            acct?.id,
            acct?.email,
            acct?.photoUrl.toString()
        )
        val bundle = bundleOf("user" to person)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home, bundle)
                }
                R.id.navigation_upload -> {
                    navController.navigate(R.id.navigation_upload)
                }
                R.id.navigation_profile -> {
                    navController.navigate(R.id.navigation_profile, bundle)
                }
            }
            true
        }
    }
    public fun signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener{
            val intent= Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}