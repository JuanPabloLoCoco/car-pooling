package it.polito.mad.car_pooling.services

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.car_pooling.models.Rating
import it.polito.mad.car_pooling.models.Rating.Companion.toRating
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirebaseRatingService {
    private const val TAG = "FirebaseRatingService"
    private const val RATINGS_COLLECTION: String = "Ratings"

    fun saveRating(rating: Rating): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        return db.collection(RATINGS_COLLECTION)
                .document()
                .set(rating.toMap())
    }

    suspend fun getUserRatings(userId: String): Flow<List<Rating>> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(RATINGS_COLLECTION)
                    .whereEqualTo("ratedId", userId)
                    .addSnapshotListener { querySnapShot: QuerySnapshot?, error: FirebaseFirestoreException? ->
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
            val listenerRegistration = db.collection(RATINGS_COLLECTION)
                    .whereEqualTo("tripId", tripId)
                    .whereEqualTo("writerId", requester)
                    .whereEqualTo("ratedId", tripOwner)
                    .addSnapshotListener { querySnapShot: QuerySnapshot?, error: FirebaseFirestoreException? ->
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

    fun getRatingsWrittenByDriver(tripOwner: String, tripId: String): Flow<List<Rating>> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(RATINGS_COLLECTION)
                    .whereEqualTo("writerId", tripOwner)
                    .whereEqualTo("tripId", tripId)
                    .addSnapshotListener { querySnapShot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                        if (error != null || querySnapShot == null) {
                            cancel(message = "Error fetching rating to user ${tripOwner} for trip with id ${tripId}", cause = error)
                            return@addSnapshotListener
                        }
                        val userRatingList = querySnapShot?.documents
                                .mapNotNull { it.toRating() }
                        offer(userRatingList)
                    }
            awaitClose {
                Log.d(TAG, "Cancelling listener for rating to user ${tripOwner} for trip with id ${tripId}")
                listenerRegistration.remove()
            }
        }
    }
}