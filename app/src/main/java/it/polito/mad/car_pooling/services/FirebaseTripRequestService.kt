package it.polito.mad.car_pooling.services

import android.util.Log
import android.view.View
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.car_pooling.models.Profile.Companion.toUser
import it.polito.mad.car_pooling.models.TripRequest
import it.polito.mad.car_pooling.models.TripRequest.Companion.toTripRequest
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

object FirebaseTripRequestService {
    private val TAG = "FirebaseTripRequestService"
    private val TRIP_REQUESTS_COLLECTION = "TripsRequests"

    suspend fun findRequestsByTrip (tripId: String): Flow<List<TripRequest>> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(TRIP_REQUESTS_COLLECTION)
                    .whereEqualTo("tripId", tripId)
                    //.whereEqualTo("requester", acc_email)
                    .addSnapshotListener { querySnapShot: QuerySnapshot?, error:FirebaseFirestoreException? ->
                        if (error != null || querySnapShot == null) {
                            cancel(message = "Error fetching other trips", cause = error)
                            return@addSnapshotListener
                        }
                        val tripRequestList = querySnapShot?.documents
                                .mapNotNull { it.toTripRequest() }
                        offer(tripRequestList)
                    }
            awaitClose {
                Log.d(TAG, "Cancelling trip requests listener for trip with id $tripId")
                listenerRegistration.remove()
            }
        }
    }

    suspend fun findRequestsByRequesterAndTrip (requester: String, tripId: String): Flow<TripRequest?> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(TRIP_REQUESTS_COLLECTION)
                    .whereEqualTo("tripId", tripId)
                    .whereEqualTo("requester", requester)
                    //.whereEqualTo("requester", acc_email)
                    .addSnapshotListener { document: QuerySnapshot?, error:FirebaseFirestoreException? ->
                        if (error != null || document == null) {
                            cancel(message = "Error fetching other trips", cause = error)
                            return@addSnapshotListener
                        }
                        val tripRequestList = document.documents
                                .mapNotNull { it.toTripRequest() }
                        if (tripRequestList.isEmpty()) {
                            offer(null)
                        } else {
                            offer(tripRequestList.get(0))
                        }
                    }
            awaitClose {
                Log.d(TAG, "Cancelling trip requests listener for trip with id $tripId")
                listenerRegistration.remove()
            }
        }
    }

    suspend fun findPassengerAcceptedRequest(userId: String): Flow<List<TripRequest>> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(TRIP_REQUESTS_COLLECTION)
                    .whereEqualTo("requester", userId)
                    .whereEqualTo("status", TripRequest.ACCEPTED)
                    .addSnapshotListener { querySnapShot: QuerySnapshot?, error:FirebaseFirestoreException? ->
                        if (error != null || querySnapShot == null) {
                            cancel(message = "Error fetching passenger accepted Trip Requests by passenger ${userId}", cause = error)
                            return@addSnapshotListener
                        }
                        val tripRequestList = querySnapShot?.documents
                                .mapNotNull { it.toTripRequest() }
                        offer(tripRequestList)
                    }
            awaitClose {
                Log.d(TAG, "Cancelling trip requests listener for passenger accepted Trip Requests by passenger ${userId}")
                listenerRegistration.remove()
            }
        }
    }

    fun saveTripRequest (tripRequest: TripRequest): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        return db.collection(TRIP_REQUESTS_COLLECTION)
                .document()
                .set(tripRequest.toMap())
    }

    fun updateTripRequest (tripRequest: TripRequest): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        return db.collection(TRIP_REQUESTS_COLLECTION)
                .document(tripRequest.id)
                .update(tripRequest.toMap())
    }

    fun cancellAllPendingRequest(tripId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(TRIP_REQUESTS_COLLECTION)
            .whereEqualTo("status", TripRequest.PENDING)
            .whereEqualTo("tripId", tripId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val tripRequestId = document.id
                    db.collection(TripRequest.DATA_COLLECTION)
                            .document(tripRequestId)
                            .update(mapOf("status" to TripRequest.REJECTED))
                }

            }

    }

    suspend fun findRequestById(tripRequestId: String): Flow<TripRequest?> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(TRIP_REQUESTS_COLLECTION)
                .document(tripRequestId)
                .addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null || documentSnapshot == null) {
                        cancel(message = "Error fetching user with email = $tripRequestId", cause = firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    val tripRequest = documentSnapshot?.toTripRequest()
                    offer(tripRequest)
                }
            awaitClose {
                Log.d(TAG, "Cancelling trip request with id $tripRequestId listener")
                listenerRegistration.remove()
            }
        }
    }


}