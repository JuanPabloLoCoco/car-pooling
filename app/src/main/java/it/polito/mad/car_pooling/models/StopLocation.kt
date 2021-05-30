package it.polito.mad.car_pooling.models

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.lang.Exception

data class StopLocation(var fullAddress : String) {
    var address : String = ""
    var city: String = ""
    var country : String = ""
    var latitude : String = ""
    var longitude : String = ""

    companion object {
        private const val TAG = "StopLocation"
        private val ADDRESS = "address"
        private val FULL_ADDRESS = "fullAddress"
        private val CITY = "city"
        private val COUNTRY = "country"
        private val LATITUDE = "latitude"
        private val LONGITUDE = "longitude"

        fun DocumentSnapshot.toTrip(): StopLocation? {
            val newStopLocation = StopLocation("")
            try {
                newStopLocation.fullAddress = getString(FULL_ADDRESS)?: ""
                newStopLocation.address = getString(ADDRESS)?: ""
                newStopLocation.city = getString(CITY) ?: ""
                newStopLocation.country = getString(COUNTRY)?: ""
                newStopLocation.latitude = getString(LATITUDE)?: ""
                newStopLocation.longitude = getString(LONGITUDE) ?: ""
                return newStopLocation
            } catch (e: Exception) {
                Log.d(TAG, "Error converting the stopLocation", e)
                return null
            }
        }

        fun parseStopLocation (stopLocationMap: Map<String, Any>): StopLocation? {
            val newStopLocation = StopLocation("")
            try {
                newStopLocation.fullAddress = (stopLocationMap.get(FULL_ADDRESS)?: "") as String
                newStopLocation.address = (stopLocationMap.get(ADDRESS)?: "") as String
                newStopLocation.city = (stopLocationMap.get(CITY) ?: "") as String
                newStopLocation.country = (stopLocationMap.get(COUNTRY)?: "") as String
                newStopLocation.latitude = (stopLocationMap.get(LATITUDE)?: "") as String
                newStopLocation.longitude = (stopLocationMap.get(LONGITUDE) ?: "") as String
                return newStopLocation
            } catch (e: Exception) {
                Log.d(TAG, "Error converting the stopLocation", e)
                return null
            }
        }

        fun newLocation(): StopLocation? {
            var newLocation = StopLocation("")
            newLocation.fullAddress = ""
            newLocation.address = ""
            newLocation.longitude = ""
            newLocation.latitude = ""
            newLocation.city = ""
            newLocation.country = ""
            return newLocation
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf<String, Any>(
            FULL_ADDRESS to fullAddress,
            LATITUDE to latitude,
            LONGITUDE to longitude,
            ADDRESS to address,
            CITY to city,
            COUNTRY to country,
        )
    }
}