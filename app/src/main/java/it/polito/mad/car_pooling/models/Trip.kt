package it.polito.mad.car_pooling.models

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.lang.Exception
import java.util.*

// data class  Model(val name: String= "", val count: Int = 0)
data class Trip (var id: String) {

    companion object {
        private val DEP_LOCATION = "depLocation"
        private val ADDITIONAL = "additional"
        private val ARI_LOCATION = "ariLocation"
        private val AVA_SEATS = "avaSeats"
        private val DEP_DATE = "depDate"
        private val DEP_TIME = "depTime"
        private val EST_DURATION = "estDuration"
        private val OPTIONAL = "optional"
        private val PLATE = "plate"
        private val PRICE = "price"
        private val OWNER = "owner"
        private val HAS_IMAGE = "hasImage"
        private val STATUS = "status"
        private val DEPARTURE_DATETIME = "departureDateTime"
        private val ARRIVAL_DATETIME = "arrivalDateTime"

        val EDIT_TRIP: String = "edit"
        val CREATE_TRIP: String = "create"
        val NEW_TRIP_ID: Int = -1
        val OPEN = "OPEN"
        val BLOCKED = "BLOCKED"
        val FULL = "FULL"
        val ENDED = "ENDED"
        val DATA_COLLECTION = "Trips"
        val FIELD_STATUS = "status"

        private const val TAG = "TRIP"

        fun DocumentSnapshot.toTrip(): Trip? {
            val new_trip = Trip("0")
            try {
                new_trip.id = id
                new_trip.depLocation = getString(DEP_LOCATION)?: ""
                new_trip.additional = getString(ADDITIONAL)?:""
                new_trip.ariLocation = getString(ARI_LOCATION)?:""
                new_trip.avaSeats = (getLong(AVA_SEATS)?: 0).toInt()
                new_trip.depDate = getString(DEP_DATE)?:""
                new_trip.depTime = getString(DEP_TIME)?:""
                new_trip.estDuration = getString(EST_DURATION)?:""
                new_trip.optional = getString(OPTIONAL)?:""
                new_trip.plate = getString(PLATE)?:""
                new_trip.price = (getDouble(PRICE)?: 0.0).toDouble()
                new_trip.owner = getString(OWNER)?:""
                new_trip.hasImage = getBoolean(HAS_IMAGE)?: false
                new_trip.status = getString(STATUS)?: OPEN
                new_trip.departureDateTime = getTimestamp(DEPARTURE_DATETIME) ?: Timestamp.now()
                new_trip.arrivalDateTime = getTimestamp(ARRIVAL_DATETIME) ?: new_trip.departureDateTime
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
            newTrip.departureDateTime = Timestamp.now()
            newTrip.arrivalDateTime = Timestamp.now()
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
        this.departureDateTime = Timestamp.now()
        this.arrivalDateTime = Timestamp.now()
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
    var departureDateTime: Timestamp = Timestamp(Date())
    var arrivalDateTime: Timestamp = Timestamp(Date())

    fun toMap(): Map<String, Any> {
        return mapOf<String, Any>(
                DEP_LOCATION to depLocation,
                ADDITIONAL to additional,
                ARI_LOCATION to ariLocation,
                AVA_SEATS to avaSeats,
                DEP_DATE to depDate,
                DEP_TIME to depTime,
                EST_DURATION to estDuration,
                OPTIONAL to optional,
                PLATE to plate,
                PRICE to price,
                OWNER to owner,
                STATUS to status,
                HAS_IMAGE to hasImage,
                DEPARTURE_DATETIME to departureDateTime,
                ARRIVAL_DATETIME to arrivalDateTime
        )
    }
}