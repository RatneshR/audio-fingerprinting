package com.example.watersupplierdev

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/*  This Activity will check weather the user is logged in or not
If the User is logged in then move him to the Main Activity
If the User is not logged in then Create an account of the user
The User can be of 2 types :
1. Supplier
2. Transporter */
class SplashScreenActivity : AppCompatActivity() {
    //Declare an instance of FirebaseAuth
    private lateinit var auth: FirebaseAuth

    //In the onCreate() method, initialize the FirebaseAuth instance.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth
        auth = Firebase.auth

        val currentUser = auth.currentUser
        Log.d(TAG, "currentUser" + currentUser);
        if(currentUser == null){//Intent Function will have 2 Parameters : 1st - SplashScreenActivity , 2nd - MainActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }else{
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}