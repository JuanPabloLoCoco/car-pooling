package it.polito.mad.car_pooling.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.services.FirebaseTripService
import it.polito.mad.car_pooling.services.LocalDataService
import kotlinx.coroutines.flow.collect

class TripListViewModel (userId: String) : ViewModel () {

    val interestTrips: LiveData<List<Trip>> = liveData {
        val listOfTrip = LocalDataService.getInterestedTrips(userId)
        if (listOfTrip == null || listOfTrip.isEmpty()) {
            emit(emptyList<Trip>())
        } else {
            FirebaseTripService.findTripsByIdInList(listOfTrip).collect { emit(it) }
        }
    }
}