package it.polito.mad.car_pooling.services


import android.util.Log
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager

object LocalDataService {
    private val USER_DATA :String  = "USER_DATA"
    private val INTERESTED_TRIPS :String = "INTERESTED_TRIPS"

    fun getInterestedTrips(userId: String): List<String> {
        var interestedTrips = ModelPreferencesManager.get<Map<String, List<String>>>(INTERESTED_TRIPS)
        if (interestedTrips == null) {
            return emptyList()
        }
        if (!interestedTrips.containsKey(userId)) {
            return emptyList()
        }
        return interestedTrips.get(userId)?: emptyList()
    }

    fun addInterestedTrip (userId: String, interestedTripId: String) {
        var interestedTrips = ModelPreferencesManager.get<MutableMap<String, MutableList<String>>>(INTERESTED_TRIPS)
        if (interestedTrips == null) {
            interestedTrips = mutableMapOf(userId to mutableListOf(interestedTripId))
            ModelPreferencesManager.put<Map<String, List<String>>>(interestedTrips , INTERESTED_TRIPS)
            return
        }
        if (interestedTrips.get(userId) == null || interestedTrips.get(userId)!!.size == 0) {
            interestedTrips.put(userId, mutableListOf(interestedTripId))
            ModelPreferencesManager.put<Map<String, List<String>>>(interestedTrips , INTERESTED_TRIPS)
            return
        }
        var itemFound = interestedTrips.get(userId)!!.filter { it == interestedTripId }
        if (itemFound.size == 0) {
            interestedTrips.get(userId)!!.add(interestedTripId)
            ModelPreferencesManager.put<Map<String, List<String>>>(interestedTrips , INTERESTED_TRIPS)
        }
        return
    }

    fun removeInterestedTrip (userId: String, interestedTripId: String) {
        var interestedTrips = ModelPreferencesManager.get<MutableMap<String, MutableList<String>>>(INTERESTED_TRIPS)

        if (interestedTrips == null) {
            return
        }
        if (interestedTrips.get(userId) == null || interestedTrips.get(userId)!!.size == 0) {
            return
        }
        var itemFound = interestedTrips.get(userId)!!.filter { it == interestedTripId }
        if (itemFound.size == 0) {
            return
        }
        var listWithoutItem = interestedTrips.get(userId)!!.filter { it != interestedTripId }
        interestedTrips.put(userId, listWithoutItem.toMutableList())
        ModelPreferencesManager.put<Map<String, List<String>>>(interestedTrips , INTERESTED_TRIPS)
    }
}