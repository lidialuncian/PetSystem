package com.example.petsystem.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.petsystem.activities.FeederActivity
import com.example.petsystem.activities.MainActivity
import com.example.petsystem.activities.MyProfileActivity
import com.example.petsystem.activities.SignInActivity
import com.example.petsystem.activities.SignUpActivity
import com.example.petsystem.models.Feeder
import com.example.petsystem.models.User
import com.example.petsystem.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.UUID

class FirestoreDatabase {

    private val dbFireStore = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String?{
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid
    }

    fun registerUser(activity: SignUpActivity, userInfo: User){
        getCurrentUserId()?.let {
            dbFireStore.collection(Constants.USERS)
                .document(it)
                .set(userInfo,  SetOptions.merge())
                .addOnSuccessListener {
                    activity.userRegisteredSuccess()
                }
                .addOnFailureListener{
                    e-> Log.e(activity.javaClass.simpleName, "Error registering user in FireStore ${e.message}")
                }
            createThreeMeals(userInfo)
        }
    }

    private fun createThreeMeals(user: User) {
        val feederId: String = UUID.randomUUID().toString()
        Log.e("Feeder ID:", "$feederId")
        val feeder = Feeder(
            id = feederId,
            userId = user.id,
        )

        dbFireStore.collection(Constants.FEEDERS)
            .document(user.id)
            .set(feeder)
            .addOnSuccessListener {
                Log.d("FirestoreClass", "Feeder created successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreClass", "Error creating feeder", e)
            }
    }

    fun loadMeals(activity: FeederActivity){
        val loggedUserId = getCurrentUserId()
        dbFireStore.collection(Constants.FEEDERS)
            .whereEqualTo("userId", loggedUserId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.isEmpty) {
                    val feederDocument = document.documents[0]
                    val feeder = feederDocument.toObject(Feeder::class.java)
                    if (feeder != null) {
                        activity.setMealsDataInFeederPage(feeder)
                    }
                }
            }
            .addOnFailureListener { e->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting data",
                    e
                )
            }
    }

    // load data when user is already signed in
    fun loadUserData(activity: Activity) {
        getCurrentUserId()?.let {
            dbFireStore.collection(Constants.USERS)
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val loggedInUser = document.toObject(User::class.java)!!
                    when(activity){
                        is SignInActivity ->
                            activity.signInSuccess(loggedInUser)

                        is MainActivity ->
                            activity.updateNavigationUserDetails(loggedInUser)

                        is MyProfileActivity ->
                            activity.setUserDataInMyProfile(loggedInUser)
                    }
                }
                .addOnFailureListener { e ->
                    when(activity){
                        is SignInActivity ->
                            activity.hideProgressDialog()

                        is MainActivity ->
                            activity.hideProgressDialog()
                    }
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error while getting logged in",
                        e
                    )
                }
        }
    }

    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>){
        getCurrentUserId()?.let {
            dbFireStore.collection(Constants.USERS)
                .document(it)
                .update(userHashMap)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_LONG).show()
                    activity.profileUpdateSuccess()
                }.addOnFailureListener{
                    e ->
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error when updating profile",
                        e
                    )
                    Toast.makeText(activity, "Error!", Toast.LENGTH_LONG).show()
                }
        }
    }

    fun updateMeals(activity: FeederActivity, mealHashMap: HashMap<String, Any>){
        getCurrentUserId()?.let {
            dbFireStore.collection(Constants.FEEDERS)
                .document(it)
                .update(mealHashMap)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Meal updated successfully!", Toast.LENGTH_LONG).show()
                    activity.mealUpdateSuccess()
                }.addOnFailureListener{
                        e ->
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error when updating meal",
                        e
                    )
                    Toast.makeText(activity, "Error!", Toast.LENGTH_LONG).show()
                }
        }
    }
}