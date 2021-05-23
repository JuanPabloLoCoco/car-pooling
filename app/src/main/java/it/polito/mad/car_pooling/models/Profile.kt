package it.polito.mad.car_pooling.models

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot

data class Profile (var fullName: String){
    var nickName: String = ""
    var email: String = ""
    var location: String = ""
    var phoneNumber: String = ""
    var birthday: String = ""
    var imageUri: String = ""


    companion object {
        private const val TAG = "USER"

        fun DocumentSnapshot.toUser(): Profile? {
            val newUser = Profile("")
            try {
                newUser.email = id
                newUser.birthday = getString("birthday")!!
                newUser.nickName = getString("nick_name")!!
                newUser.fullName = getString("full_name")!!
                newUser.location = getString("location")!!
                newUser.phoneNumber = getString("phone_number")!!
                return newUser
            } catch (e: Exception) {
                Log.e(TAG, "Error converting user with email ${newUser.email}", e)
                return null
            }
        }
    }


}