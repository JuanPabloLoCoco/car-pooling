package it.polito.mad.car_pooling.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.models.Rating
import it.polito.mad.car_pooling.models.TripRequest
import it.polito.mad.car_pooling.models.TripRequestResponse
import it.polito.mad.car_pooling.services.FirebaseRatingService
import it.polito.mad.car_pooling.services.FirebaseTripRequestService
import it.polito.mad.car_pooling.services.FirebaseUserService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RatingViewModel(private val tripRequestId: String): ViewModel() {
    private val _tripRequest = MutableLiveData<TripRequest>()
    private val _driver = MutableLiveData<Profile>()
    private val _passenger = MutableLiveData<Profile>()
    private val _tripRequestResponse = MutableLiveData<TripRequestResponse>()
    val tripRequestResponse: LiveData<TripRequestResponse> = _tripRequestResponse


    init {
        viewModelScope.launch {
            FirebaseTripRequestService.findRequestById(tripRequestId).collect {
                _tripRequest.value = it
                if (it != null) {
                    FirebaseUserService.getUserByIdInList(listOf(it.requester, it.tripOwner)).collect{ profiles ->
                        var tmpPassenger = Profile("passenger")
                        var tmpDriver = Profile("driver")
                        for (profile in profiles) {
                            if (it.requester == profile.email) {
                                _passenger.value = profile
                                tmpPassenger = profile
                            }
                            if (it.tripOwner == profile.email) {
                                _driver.value = profile
                                tmpDriver = profile
                            }
                        }
                        _tripRequestResponse.value = TripRequestResponse(it, tmpPassenger, tmpDriver)
                    }
                }
            }
        }
    }

    fun saveRating(rating: Rating): Task<Void> {
        return FirebaseRatingService.saveRating(rating)
    }
}