package it.polito.mad.car_pooling.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.services.FirebaseTripRequestService
import it.polito.mad.car_pooling.services.FirebaseTripService
import it.polito.mad.car_pooling.services.LocalDataService
import kotlinx.coroutines.flow.collect

class TripListViewModel (userId: String) : ViewModel () {

    private var TAG = "TripListViewModel"

    val interestTrips: LiveData<List<Trip>> = liveData {
        val listOfTrip = LocalDataService.getInterestedTrips(userId)
        if (listOfTrip == null || listOfTrip.isEmpty()) {
            emit(emptyList<Trip>())
        } else {
            FirebaseTripService.findTripsByIdInList(listOfTrip).collect { emit(it) }
        }
    }

    val boughtTrips: LiveData<List<Trip>> = liveData {
        FirebaseTripRequestService.findPassangerAcceptedRequest(userId).collect {
            if (it == null || it.isEmpty()) {
                Log.d(TAG, "Requests accepted to user ${userId} is empty")
                emit(emptyList<Trip>())
            } else {
                val listOfIds = it.map { tripRequest -> tripRequest.tripId }
                FirebaseTripService.findTripsByIdInList(listOfIds).collect { trips -> emit(trips) }
            }
        }
    }
}