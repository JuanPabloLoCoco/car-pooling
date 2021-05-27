package it.polito.mad.car_pooling.viewModels

import androidx.lifecycle.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.models.TripRequest
import it.polito.mad.car_pooling.services.FirebaseTripRequestService
import it.polito.mad.car_pooling.services.FirebaseTripService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TripViewModel (private val tripId: String): ViewModel() {
    private val _trip = MutableLiveData<Trip>()
    val trip: LiveData<Trip> = _trip

    init {
        viewModelScope.launch {
            FirebaseTripService.getTripById(tripId).collect { _trip.value = it}
        }
    }
    val tripRequests: LiveData<List<TripRequest>>  = liveData {
        FirebaseTripRequestService.findRequestsByTrip(tripId).collect { emit(it) }
    }


    fun getMyRequestWithTrip (requester: String) = liveData<TripRequest?> {
        FirebaseTripRequestService.findRequestsByRequesterAndTrip(requester,tripId).collect { emit(it) }
    }

    fun createTripRequest(tripRequest: TripRequest): Task<Void> {
        return FirebaseTripRequestService.saveTripRequest(tripRequest)
    }

    fun updateTripRequest(tripRequest: TripRequest): Task<Void> {
        var seatsFull: Int? = tripRequests.value?.fold(0, { acc, thisTrip ->
            acc + (if (thisTrip.status == TripRequest.ACCEPTED) 1 else 0)
        })
        var seatsUsed: Int = seatsFull ?: 0
        var freeSeats: Int = (trip.value?.avaSeats ?: 0) - seatsUsed

        // A trip request can be accepted or rejected
        if (tripRequest.status == TripRequest.REJECTED) {
            // If I reject.... Only reject the funtion
            return FirebaseTripRequestService.updateTripRequest(tripRequest)
        }
        // If I accept....
        if (freeSeats <= 1) {
            return FirebaseTripRequestService.updateTripRequest(tripRequest)
                .addOnSuccessListener {
                    val tripToUpdate = trip.value
                    tripToUpdate!!.status = Trip.FULL
                    viewModelScope.launch {
                        FirebaseTripService.updateTrip(tripToUpdate!!)
                                .addOnSuccessListener {
                                    FirebaseTripRequestService.cancellAllPendingRequest(tripId)
                                }
                    }

                }
        }
        return FirebaseTripRequestService.updateTripRequest(tripRequest)

    }
}