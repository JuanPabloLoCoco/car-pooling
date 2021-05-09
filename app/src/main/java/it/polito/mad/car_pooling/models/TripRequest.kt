package it.polito.mad.car_pooling.models

import com.google.firebase.Timestamp
import java.util.*


data class TripRequest(var requester: String, var tripOwner: String, var tripId: String) {
    companion object {
        val PENDING: Int = 1
        val ACCEPTED: Int = 2
        val REJECTED: Int = 3
    }
    var creationTimestamp: Timestamp = Timestamp(Date())
    var updateTimestamp: Timestamp = Timestamp(Date())
    var status: Int = PENDING
}