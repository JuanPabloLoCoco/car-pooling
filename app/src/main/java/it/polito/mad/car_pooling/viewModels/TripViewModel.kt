package it.polito.mad.car_pooling.viewModels

import androidx.lifecycle.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.models.TripRequest
import it.polito.mad.car_pooling.models.TripRequestRating
import it.polito.mad.car_pooling.services.FirebaseTripRequestService
import it.polito.mad.car_pooling.services.FirebaseTripService
import it.polito.mad.car_pooling.services.FirebaseUserService
import it.polito.mad.car_pooling.services.LocalDataService
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


    fun getMyRequestWithTrip (requester: String): LiveData<TripRequestRating?> = liveData {
        FirebaseTripRequestService.findRequestsByRequesterAndTrip(requester,tripId).collect { tripRequest ->
            if (tripRequest == null) {
                emit(null)
            } else {
                var tmpTripRequestRating = TripRequestRating(tripRequest)
                // emit(tmpTripRequestRating)
                if (tripRequest.status == TripRequest.ACCEPTED) {
                    FirebaseUserService.getRatingByRequesterAndOwnerAndTrip(requester, tripRequest.tripOwner, tripId).collect { rating ->
                        tmpTripRequestRating.rating = rating
                        emit(tmpTripRequestRating)
                    }
                }
            }
        }
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
                        /*
                        FirebaseTripService.updateTrip(tripToUpdate!!)
                                .addOnSuccessListener {
                                    FirebaseTripRequestService.cancellAllPendingRequest(tripId)
                                }

                         */
                        updateTripStatus(Trip.FULL)
                    }

                }
        }
        return FirebaseTripRequestService.updateTripRequest(tripRequest)
    }

    suspend fun updateTripStatus(status: String): Task<Void> {
        val tripToUpdate = trip.value
        tripToUpdate!!.status = status
        return FirebaseTripService.updateTrip(tripToUpdate)
                .addOnSuccessListener {
                    if (status == Trip.FULL || status == Trip.BLOCKED) {
                        FirebaseTripRequestService.cancellAllPendingRequest(tripId)
                    }
                    return@addOnSuccessListener
                }
    }

    fun createTrip(trip: Trip): Task<DocumentReference> {
        return FirebaseTripService.createTrip(trip)
    }

    fun updateTrip(trip: Trip): Task<Void> {
        return FirebaseTripService.updateTrip(trip)
    }

    fun addInterestedTrip(userId: String) {
        return LocalDataService.addInterestedTrip(userId, tripId)
    }

    fun getInterestedTrips(userId: String): List<String> {
        return LocalDataService.getInterestedTrips(userId)
    }

    fun removeInterestedTrip (userId: String) {
        return LocalDataService.removeInterestedTrip(userId, tripId)
    }
}