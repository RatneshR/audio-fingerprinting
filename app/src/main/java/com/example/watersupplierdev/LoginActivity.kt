package com.example.watersupplierdev

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Login Button
        var login_button = findViewById(R.id.login_button) as Button
        //Redirect to Login Details Activity
        login_button.setOnClickListener {
//            startActivity(Intent(this, LoginDetailsActivity::class.java))
            startActivity(Intent(this, LandingMapsActivity::class.java))
        }

        //Sign Up Button
        var signup_button = findViewById(R.id.signup_button) as Button
        //Redirect to Sign Up Activity
        signup_button.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
