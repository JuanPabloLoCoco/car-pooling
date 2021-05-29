package it.polito.mad.car_pooling.services

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import it.polito.mad.car_pooling.models.Profile.Companion.toUser
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
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(TRIP_COLLECTION)
                .whereEqualTo("owner", userId)
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null || querySnapshot == null) {
                        cancel(message = "Error fetching other trips", cause = firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    val map = querySnapshot.documents
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
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null || querySnapshot == null) {
                        cancel(message = "Error fetching other trips", cause = firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    val map = querySnapshot.documents
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
    suspend fun getTripById(tripId: String): Flow<Trip?> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(TRIP_COLLECTION)
                    .document(tripId)
                    .addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                        if (firebaseFirestoreException != null || documentSnapshot == null) {
                            cancel(message = "Error fetching trip with id = $tripId", cause = firebaseFirestoreException)
                            return@addSnapshotListener
                        }
                        val user = documentSnapshot.toTrip()
                        offer(user)
                    }
            awaitClose {
                Log.d(TAG, "Cancelling trip with id $tripId listener")
                listenerRegistration.remove()
            }
        }
    }

    fun updateTrip(trip: Trip): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val tripId = trip.id
        return db.collection(TRIP_COLLECTION)
                .document(tripId)
                .update(trip.toMap())
    }

    fun createTrip(trip: Trip): Task<DocumentReference> {
        val db = FirebaseFirestore.getInstance()
        return db.collection(TRIP_COLLECTION)
                .add(trip.toMap())
    }




}