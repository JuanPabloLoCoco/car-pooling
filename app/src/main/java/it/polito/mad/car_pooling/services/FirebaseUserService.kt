package it.polito.mad.car_pooling.services

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.models.Profile.Companion.toUser
import it.polito.mad.car_pooling.models.Rating
import it.polito.mad.car_pooling.models.Rating.Companion.toRating
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirebaseUserService {
    private const val TAG = "FirebaseProfileService"
    private const val USERS_COLLECTION: String = "Users"
    private const val RATINGS_COLLECTION: String = "Ratings"

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

    suspend fun getUserByIdInList(userIdList: List<String>): Flow<List<Profile>> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(USERS_COLLECTION)
                    .whereIn("__name__", userIdList)
                    .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                        if (firebaseFirestoreException != null || querySnapshot == null) {
                            cancel(message = "Error fetching users with emails = $userIdList", cause = firebaseFirestoreException)
                            return@addSnapshotListener
                        }
                        val usersList = querySnapshot.documents
                                .mapNotNull { it.toUser() }
                        offer(usersList)
                    }
            awaitClose {
                Log.d(TAG, "Cancelling users with emails $userIdList listener")
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

    fun saveRating(rating: Rating): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        return db.collection(USERS_COLLECTION)
                .document(rating.rated)
                .collection(RATINGS_COLLECTION)
                .document()
                .set(rating.toMap())
    }

    suspend fun getUserRatings(userId: String): Flow<List<Rating>> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(RATINGS_COLLECTION)
                    .addSnapshotListener { querySnapShot: QuerySnapshot?, error:FirebaseFirestoreException? ->
                        if (error != null || querySnapShot == null) {
                            cancel(message = "Error fetching ratings for user with id ${userId}", cause = error)
                            return@addSnapshotListener
                        }
                        val userRatingList = querySnapShot?.documents
                                .mapNotNull { it.toRating() }
                        offer(userRatingList)
                    }
            awaitClose {
                Log.d(TAG, "Cancelling rating list listener for user with id ${userId}")
                listenerRegistration.remove()
            }
        }

    }

    fun getRatingByRequesterAndOwnerAndTrip(requester: String, tripOwner: String, tripId: String): Flow<Rating?> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(USERS_COLLECTION)
                    .document(tripOwner)
                    .collection(RATINGS_COLLECTION)
                    .whereEqualTo("tripId", tripId)
                    .whereEqualTo("writerId", requester)
                    .addSnapshotListener { querySnapShot: QuerySnapshot?, error:FirebaseFirestoreException? ->
                        if (error != null || querySnapShot == null) {
                            cancel(message = "Error fetching rating from ${requester} to user ${tripOwner} for trip with id ${tripId}", cause = error)
                            return@addSnapshotListener
                        }
                        val userRatingList = querySnapShot?.documents
                                .map { it.toRating() }.firstOrNull()
                        offer(userRatingList)
                    }
            awaitClose {
                Log.d(TAG, "Cancelling listener for rating from ${requester} to user ${tripOwner} for trip with id ${tripId}")
                listenerRegistration.remove()
            }
        }
    }
}