package com.example.watersupplierdev

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    //Declare an instance of FirebaseAuth
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Firebase Auth
        auth = Firebase.auth

        val currentUser = auth.currentUser
        Log.d(ContentValues.TAG, "currentUser-" + currentUser);
        Log.d(ContentValues.TAG, "auth-" + auth);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}