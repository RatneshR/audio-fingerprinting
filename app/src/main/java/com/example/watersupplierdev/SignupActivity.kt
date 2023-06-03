package com.example.watersupplierdev

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {
    private lateinit var oneTapClient: SignInClient
    private lateinit var signUpRequest: BeginSignInRequest

    private val REQ_ONE_TAP = 2
    private var showOneTapUI = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        //Back to Login Activity Button
        var back_signup_button = findViewById(R.id.imageBackSignupButton) as ImageButton
        //Redirect to Sign Up Activity
        back_signup_button.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }


//    Integrate Google One Tap sign-in into your app by following the steps on the Sign users in with their saved credentials page. When you configure the BeginSignInRequest object, call setGoogleIdTokenRequestOptions:
        oneTapClient = Identity.getSignInClient(this)
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.app_firebase_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .setAutoSelectEnabled(true).build()
//    You must pass your "server" client ID to the setGoogleIdTokenRequestOptions method. To find the OAuth 2.0 client ID:
//    Open the Credentials page in the GCP Console.
//    The Web application type client ID is your backend server's OAuth 2.0 client ID.

        //Initiate Google Sign Up for the User
        var googleLoginButton = findViewById<ImageButton>(R.id.googleSignup)
        googleLoginButton.setOnClickListener {
            oneTapClient.beginSignIn(signUpRequest).addOnSuccessListener(this) { result ->
                try {
                    Log.e(TAG, "Could start One Tap UI with Google button Sign Up")
                    @Suppress("DEPRECATION")
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP, null, 0,0,0, null)
                } catch (e: IntentSender.SendIntentException){
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
                .addOnFailureListener(this) { e ->
                    // No saved credentials found. Launch the One Tap sign-up flow, or
                    // do nothing and continue presenting the signed-out UI.
                    Log.d(TAG, e.localizedMessage)
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            Log.d(TAG, "Got ID token. Now you can move to another Activity")
                            Log.d(TAG, credential.toString());
                            Log.d(TAG, idToken.toString());

                            // User successfully signed in.
                            Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")


                            startActivity(Intent(this, MainActivity::class.java))

                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token!")
                        }
                    }
                }catch (e: ApiException) {
                    // ...
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d(TAG, "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                            showOneTapUI = false
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d(TAG, "One-tap encountered a network error.")
                            // Try again or just ignore.
                        }
                        else -> {
                            Log.d(TAG, "Couldn't get credential from result." +
                                    " (${e.localizedMessage})")
                        }
                    }
                }
            }
        }
    }
}