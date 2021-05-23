package it.polito.mad.car_pooling.services

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.R
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.models.Profile.Companion.toUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirebaseUserService {
    private const val TAG = "FirebaseProfileService"
    private const val USERS_COLLECTION: String = "Users"

    @ExperimentalCoroutinesApi
    suspend fun getUserById(userId: String): Flow<Profile?> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(USERS_COLLECTION)
                    .document(userId)
                    .addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                        if (firebaseFirestoreException != null || documentSnapshot == null) {
                            cancel(message = "Error fetching user with email = $userId", cause = firebaseFirestoreException)
                            return@addSnapshotListener
                        }
                        val user = documentSnapshot?.toUser()
                        offer(user)
                    }
            awaitClose {
                Log.d(TAG, "Cancelling user with email $userId listener")
                listenerRegistration.remove()
            }
        }

        //val users = db.collection("Users")
        /*
        if (acc_email != "no email"){
            val my_profile = users.document(acc_email.toString())
            my_profile.addSnapshotListener { value, error ->
                if (error != null) throw error
                if (value != null) {
                    if (value.exists()) {
                        view.findViewById<TextInputLayout>(R.id.editViewFullName).editText?.setText(value["full_name"].toString())
                        view.findViewById<TextInputLayout>(R.id.editViewNickName).editText?.setText(value["nick_name"].toString())
                        view.findViewById<TextInputLayout>(R.id.editViewEmail).editText?.setText(value["email"].toString())
                        view.findViewById<TextInputLayout>(R.id.editViewLocation).editText?.setText(value["location"].toString())
                        view.findViewById<TextView>(R.id.editViewBirthday).text = value["birthday"].toString()
                        view.findViewById<TextInputLayout>(R.id.editViewPhoneNumber).editText?.setText(value["phone_number"].toString())
                        val default_str_profile = "android.resource://it.polito.mad.car_pooling/drawable/default_image"
                        val imageView = view.findViewById<ImageView>(R.id.imageViewEditPhoto)
                        if (value["image_uri"].toString() == "" || value["image_uri"].toString().isEmpty()) {
                            imageUri = Uri.parse(default_str_profile)
                            imageView.setImageURI(imageUri)
                        } else {
                            val storage = Firebase.storage
                            val imageRef = storage.reference.child("users/$acc_email.jpg")
                            imageRef.downloadUrl.addOnSuccessListener { Uri ->
                                val image_uri = Uri.toString()
                                Glide.with(this).load(image_uri).into(imageView)
                            }
                        }
                    } else {
                        writeTextView(view)
                    }
                }
            }
        }*/
    }

    fun saveUser(user: Profile) {

    }
}