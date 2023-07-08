package com.example.petsystem.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.petsystem.R
import com.example.petsystem.firebase.FirestoreDatabase
import com.example.petsystem.models.User
import com.example.petsystem.utils.Constants
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var selectedImageFileUri: Uri? = null
    private var downloadedImageFileUri: String = ""
    private lateinit var userDetails: User

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()

        FirestoreDatabase().loadUserData(this@MyProfileActivity)

        findViewById<ImageView>(R.id.my_profile_user_image).setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED
            ) {
                showImageChooser()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        findViewById<EditText>(R.id.my_profile_username).setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Input has focus, change label color
                findViewById<TextInputLayout>(R.id.my_profile_username_text_input).defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondaryColor))
            } else {
                // Input lost focus, change label color back to the default color
                findViewById<TextInputLayout>(R.id.my_profile_username_text_input).defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primaryColor))
            }
        }

        findViewById<EditText>(R.id.my_profile_email).setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Input has focus, change label color
                findViewById<TextInputLayout>(R.id.my_profile_email_text_input).defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondaryColor))
            } else {
                // Input lost focus, change label color back to the default color
                findViewById<TextInputLayout>(R.id.my_profile_email_text_input).defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primaryColor))
            }
        }

        findViewById<EditText>(R.id.my_profile_mobile).setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Input has focus, change label color
                findViewById<TextInputLayout>(R.id.my_profile_mobile_text_input).defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondaryColor))
            } else {
                // Input lost focus, change label color back to the default color
                findViewById<TextInputLayout>(R.id.my_profile_mobile_text_input).defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primaryColor))
            }
        }

        findViewById<Button>(R.id.btn_update).setOnClickListener {
            if(selectedImageFileUri != null){
                uploadUserImage()
            } else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            selectedImageFileUri = data.data

            try {

                Glide
                    .with(this@MyProfileActivity)
                    .load(Uri.parse(selectedImageFileUri.toString()))
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(findViewById(R.id.my_profile_user_image))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            } else {
                Toast.makeText(
                    this,
                    "You need to give permission to local storage.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupActionBar() {

        setSupportActionBar(findViewById(R.id.toolbar_my_profile_activity))

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.title = resources.getString(R.string.nav_my_profile)
        }

        findViewById<Toolbar>(R.id.toolbar_my_profile_activity).setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataInMyProfile(user: User) {
        userDetails = user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_placeholder)
            .into(findViewById(R.id.my_profile_user_image))

        findViewById<TextView>(R.id.my_profile_username).text = user.username
        findViewById<TextView>(R.id.my_profile_email).text = user.email
        if (user.mobile != 0L) {
            findViewById<TextView>(R.id.my_profile_mobile).text = user.mobile.toString()
        }
    }

    private fun showImageChooser() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, Constants.PICK_IMAGE_REQUEST_CODE)
    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if(selectedImageFileUri != null){
            val storage: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" +
                        System.currentTimeMillis() +
                        "." +
                        getFileExtension(selectedImageFileUri))

            storage.putFile(selectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.e("FirebaseImageURL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.e("DownloadableImageURL", uri.toString())
                    downloadedImageFileUri = uri.toString()
                    updateUserProfileData()
                }
            }.addOnFailureListener {
                exception ->
                Toast.makeText(this@MyProfileActivity, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }
    private fun getFileExtension(uri: Uri?): String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()
        if(downloadedImageFileUri.isNotEmpty() && downloadedImageFileUri != userDetails.image){
            userHashMap[Constants.IMAGE] = downloadedImageFileUri
        }

        if(findViewById<TextView>(R.id.my_profile_username).text.toString() != userDetails.username){
            userHashMap[Constants.USERNAME] = findViewById<TextView>(R.id.my_profile_username).text.toString()
        }

        if(findViewById<TextView>(R.id.my_profile_mobile).text.toString() != userDetails.mobile.toString() && findViewById<TextView>(R.id.my_profile_mobile).text.toString() != ""){
            userHashMap[Constants.MOBILE] = findViewById<TextView>(R.id.my_profile_mobile).text.toString().toLong()
        }

        FirestoreDatabase().updateUserProfileData(this@MyProfileActivity, userHashMap)
    }
}