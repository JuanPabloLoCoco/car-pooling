package it.polito.mad.car_pooling.models

import com.google.firebase.Timestamp
import java.util.*


data class TripRequest(var requester: String, var tripOwner: String, var tripId: String) {
    companion object {
        val PENDING: String = "PENDING"
        val ACCEPTED: String = "ACCEPTED"
        val REJECTED: String = "REJECTED"
        val DATA_COLLECTION: String = "TripsRequests"
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
}