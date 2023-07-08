package com.example.petsystem.activities

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.petsystem.R
import com.example.petsystem.firebase.FirestoreDatabase
import com.example.petsystem.models.User
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setUpActionBar()

        findViewById<EditText>(R.id.sign_up_username).setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Input has focus, change label color
                findViewById<TextInputLayout>(R.id.sign_up_username_text_input).defaultHintTextColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.secondaryColor))
            } else {
                // Input lost focus, change label color back to the default color
                findViewById<TextInputLayout>(R.id.sign_up_username_text_input).defaultHintTextColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.primaryColor))
            }
        }

        findViewById<EditText>(R.id.sign_up_email).setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Input has focus, change label color
                findViewById<TextInputLayout>(R.id.sign_up_email_text_input).defaultHintTextColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.secondaryColor))
            } else {
                // Input lost focus, change label color back to the default color
                findViewById<TextInputLayout>(R.id.sign_up_email_text_input).defaultHintTextColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.primaryColor))
            }
        }

        findViewById<EditText>(R.id.sign_up_password).setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Input has focus, change label color
                findViewById<TextInputLayout>(R.id.sign_up_password_text_input).defaultHintTextColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.secondaryColor))
            } else {
                // Input lost focus, change label color back to the default color
                findViewById<TextInputLayout>(R.id.sign_up_password_text_input).defaultHintTextColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.primaryColor))
            }
        }

        val btnSignUp = findViewById<Button>(R.id.btn_sign_up)
        btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun setUpActionBar(){

        val signUpToolbar = findViewById<Toolbar>(R.id.toolbar_sign_up_activity)
        setSupportActionBar(signUpToolbar)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
        }

        signUpToolbar.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    private fun registerUser(){
        val username: String = findViewById<EditText>(R.id.sign_up_username).text.toString().trim{ it <= ' '}
        val email: String = findViewById<EditText>(R.id.sign_up_email).text.toString().trim{ it <= ' '}
        val password: String = findViewById<EditText>(R.id.sign_up_password).text.toString().trim{ it <= ' '}

        if(validateForm(username, email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val regEmail = firebaseUser.email!!
                        val user =  User(firebaseUser.uid, username, regEmail)
                        FirestoreDatabase().registerUser(this, user)
                    } else {
                        Toast.makeText(
                            this,
                            task.exception!!.message, Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    private fun validateForm(username: String, email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(username) ->{
                showErrorSnackBar("Please enter a username")
                false
            }
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

    fun userRegisteredSuccess(){
        Toast.makeText(
            this,
            "You have successfully registered!",
            Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}