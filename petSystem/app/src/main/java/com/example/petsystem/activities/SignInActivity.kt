package com.example.petsystem.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.petsystem.R
import com.example.petsystem.firebase.FirestoreClass
import com.example.petsystem.models.User
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setUpActionBar()

        auth = FirebaseAuth.getInstance()

        findViewById<EditText>(R.id.sign_in_email).setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Input has focus, change label color
                findViewById<TextInputLayout>(R.id.sign_in_email_text_input).defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondaryColor))
            } else {
                // Input lost focus, change label color back to the default color
                findViewById<TextInputLayout>(R.id.sign_in_email_text_input).defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primaryColor))
            }
        }

        findViewById<EditText>(R.id.sign_in_password).setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Input has focus, change label color
                findViewById<TextInputLayout>(R.id.sign_in_password_text_input).defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondaryColor))
            } else {
                // Input lost focus, change label color back to the default color
                findViewById<TextInputLayout>(R.id.sign_in_password_text_input).defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primaryColor))
            }
        }

        val btnSignIn = findViewById<Button>(R.id.btn_sign_in)
        btnSignIn.setOnClickListener {
            signInUser()
            startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        }
    }

    private fun setUpActionBar(){

        val signInToolbar = findViewById<Toolbar>(R.id.toolbar_sign_in_activity)
        setSupportActionBar(signInToolbar)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
        }

        signInToolbar.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    private fun signInUser(){
        val email: String = findViewById<EditText>(R.id.sign_in_email).text.toString().trim{ it <= ' '}
        val password: String = findViewById<EditText>(R.id.sign_in_password).text.toString().trim{ it <= ' '}

        if(validateForm(email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@SignInActivity) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Sign-in", "signInWithEmail:success")
                        FirestoreClass().loadUserData(this@SignInActivity)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign-in", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(email) ->{
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password) ->{
                showErrorSnackBar("Please enter a password")
                false
            }else ->{
                true
            }
        }
    }

    fun signInSuccess(user: User){
        hideProgressDialog()
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        Toast.makeText(
                this@SignInActivity,
        "Signed in successfully!",
        Toast.LENGTH_SHORT,
        ).show()
        finish()
    }
}