package it.polito.mad.car_pooling.models

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.lang.Exception
import java.util.*


data class TripRequest(var requester: String, var tripOwner: String, var tripId: String) {

    companion object {
        val ENDED: String = "ENDED"
        val PENDING: String = "PENDING"
        val ACCEPTED: String = "ACCEPTED"
        val REJECTED: String = "REJECTED"
        val DATA_COLLECTION: String = "TripsRequests"
        private const val TAG = "TRIP_REQUEST"
        fun DocumentSnapshot.toTripRequest(): TripRequest? {
            val id = getId()
            try {
                val requester = getString("requester")!!
                val tripOwner = getString("tripOwner")!!
                val tripId = getString("tripId")!!
                val creationTS = getTimestamp("creationTimestamp")!!
                val updatedTS = getTimestamp("updateTimestamp")!!
                val status = getString("status")!!
                return TripRequest(requester, tripOwner, tripId, creationTS, updatedTS, status, id)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting trip request with id ${id}", e)
                return null
            }
        }
    }

    constructor( requester: String,
                 tripOwner: String,
                 tripId: String,
                 creationTimestamp: Timestamp,
                 updateTimestamp: Timestamp,
                 status: String,
                 id: String
    ): this(requester, tripOwner, tripId) {
        this@TripRequest.creationTimestamp = creationTimestamp
        this@TripRequest.updateTimestamp = updateTimestamp
        this@TripRequest.status = status
        this.id = id
    }

    var id : String = ""
    var creationTimestamp: Timestamp = Timestamp(Date())
    var updateTimestamp: Timestamp = Timestamp(Date())
    var status: String = PENDING

    fun toMap (): Map<String, Any> {
        val tripRequestAsMap = mapOf(
                "requester" to requester,
                "tripOwner" to tripOwner,
                "tripId" to tripId,
                "creationTimestamp" to creationTimestamp,
                "updateTimestamp" to updateTimestamp,
                "status" to status)

        return tripRequestAsMap
    }
}