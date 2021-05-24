package it.polito.mad.car_pooling.models

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import java.lang.Exception

// data class  Model(val name: String= "", val count: Int = 0)
data class Trip (var id: String) {
    companion object {
        val EDIT_TRIP: String = "edit"
        val CREATE_TRIP: String = "create"
        val NEW_TRIP_ID: Int = -1
        val OPEN = "OPEN"
        val BLOCKED = "BLOCKED"
        val FULL = "FULL"
        val DATA_COLLECTION = "Trips"
        val FIELD_STATUS = "status"
        private const val TAG = "TRIP"

        fun DocumentSnapshot.toTrip(): Trip? {
            val new_trip = Trip("0")
            try {
                new_trip.id = id
                new_trip.depLocation = getString("depLocation")!!
                new_trip.additional = getString("additional")!!
                new_trip.ariLocation = getString("ariLocation")!!
                new_trip.avaSeats = (getLong("avaSeats")!!).toInt()
                new_trip.depDate = getString("depDate")!!
                new_trip.depTime = getString("depTime")!!
                new_trip.estDuration = getString("estDuration")!!
                new_trip.optional = getString("optional")!!
                new_trip.plate = getString("plate")!!
                new_trip.price = (getDouble("price")!!)
                // new_trip.imageUri = getString("image_uri")!!
                return new_trip
            } catch (e: Exception) {
                Log.e(TAG, "Error converting trip with id ${new_trip.id}", e)
                return null
            }
        }
    }

    constructor(depLocation: String,
        ariLocation: String,
        depDate: String,
        depTime: String,
        estDuration: String,
        avaSeats: Int,
        price: Double,
        additional: String,
        optional: String,
        plate: String,
        imageUri: String,
        owner: String
    ): this("0") {
        this.depLocation = depLocation
        this.ariLocation = ariLocation
        this.depDate = depDate
        this.depTime = depTime
        this.estDuration = estDuration
        this.avaSeats = avaSeats
        this.price = price
        this.additional = additional
        this.optional = optional
        this.plate = plate
        this.imageUri = imageUri
        this.owner = owner
    }

    var depLocation: String = ""
    var ariLocation: String=""
    var depDate: String = ""
    var depTime: String = ""
    var estDuration: String = ""
    var avaSeats: Int = 0
    var price: Double = 1.0
    var additional: String = ""
    var optional: String = ""
    var plate: String = ""
    var imageUri: String = ""
    var owner: String=""
    var status: String = OPEN
}