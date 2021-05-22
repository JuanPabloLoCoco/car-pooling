package it.polito.mad.car_pooling.services

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.models.Trip.Companion.toTrip
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirebaseTripService {
    private const val TAG = "FirebaseTripService"
    private const val TRIP_COLLECTION: String = "Trips"

    @ExperimentalCoroutinesApi
    suspend fun getMyTrips(userId: String): Flow<List<Trip>> {
        /*
        val db = FirebaseFirestore.getInstance()
        try {
            return db.collection(TRIP_COLLECTION)
                .whereEqualTo("owner", userId)
                .get()
                .await()
                .documents
                .mapNotNull{
                    it.toTrip()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting my Trips", e)
            return emptyList()
        }*/

        val db = FirebaseFirestore.getInstance()

        return callbackFlow {
            val listenerRegistration = db.collection(TRIP_COLLECTION)
                .whereEqualTo("owner", userId)
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null || querySnapshot == null) {
                        cancel(message = "Error fetching other trips", cause = firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    val map = querySnapshot?.documents
                        .mapNotNull { it.toTrip() }
                    offer(map)
                }
            awaitClose{
                Log.d(TAG, "Cancelling Others trips listener")
                listenerRegistration.remove()
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getOthersTrips(userId: String): Flow<List<Trip>> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(TRIP_COLLECTION)
                .whereNotEqualTo("owner", userId)
                .limit(20L)
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null || querySnapshot == null) {
                        cancel(message = "Error fetching other trips", cause = firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    val map = querySnapshot?.documents
                        .mapNotNull { it.toTrip() }
                    offer(map)


                }
            awaitClose{
                Log.d(TAG, "Cancelling Others trips listener")
                listenerRegistration.remove()
            }
        }
    }
}