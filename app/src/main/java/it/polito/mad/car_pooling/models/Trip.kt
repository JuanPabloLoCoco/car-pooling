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
                new_trip.depLocation = getString("depLocation")?: ""
                new_trip.additional = getString("additional")?:""
                new_trip.ariLocation = getString("ariLocation")?:""
                new_trip.avaSeats = (getLong("avaSeats")?: 0).toInt()
                new_trip.depDate = getString("depDate")?:""
                new_trip.depTime = getString("depTime")?:""
                new_trip.estDuration = getString("estDuration")?:""
                new_trip.optional = getString("optional")?:""
                new_trip.plate = getString("plate")?:""
                new_trip.price = (getDouble("price")?: 0.0).toDouble()
                new_trip.owner = getString("owner")?:""
                new_trip.hasImage = getBoolean("hasImage")?: false
                // new_trip.imageUri = getString("image_uri")!!
                return new_trip
            } catch (e: Exception) {
                Log.d(TAG, "Error converting trip with id ${new_trip.id}", e)
                return null
            }
        }

        fun NewTrip(): Trip {
            var newTrip = Trip("0")
            newTrip.depLocation = ""
            newTrip.ariLocation = ""
            newTrip.depTime = ""
            newTrip.depDate = ""
            newTrip.estDuration = ""
            newTrip.avaSeats = 0
            newTrip.price = 10.0
            newTrip.additional = ""
            newTrip.optional = ""
            newTrip.plate = ""
            newTrip.owner = ""
            newTrip.status = OPEN
            newTrip.hasImage = false
            return newTrip
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
        this.status = OPEN

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
    var hasImage: Boolean = false

    fun toMap(): Map<String, Any> {
        return mapOf<String, Any>(
                "depLocation" to depLocation,
                "additional" to additional,
                "ariLocation" to ariLocation,
                "avaSeats" to avaSeats,
                "depDate" to depDate,
                "depTime" to depTime,
                "estDuration" to estDuration,
                "optional" to optional,
                "plate" to plate,
                "price" to price,
                "owner" to owner,
                "status" to status,
                "hasImage" to hasImage
        )
    }
}