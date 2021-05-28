package it.polito.mad.car_pooling.services

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
    }

    fun saveUser(user: Profile): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        return db.collection(USERS_COLLECTION)
                .document(user.email)
                .set(user.toMap())

        // TODO: Change how to return true or false depending on the result.
        // TODO: Take look at Brandan Jones Github
    }
}