package it.polito.mad.car_pooling.viewModels

import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.tasks.Task
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.models.Rating
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.services.FirebaseRatingService
import it.polito.mad.car_pooling.services.FirebaseTripService
import it.polito.mad.car_pooling.services.FirebaseUserService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProfileViewModel(private val userId: String): ViewModel() {
    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile> = _profile
    private val TAG = "ProfileViewModel"


    init {
        viewModelScope.launch {
            FirebaseUserService.getUserById(userId).collect { _profile.value = it }
        }
    }

    fun saveUser(profileToSave: Profile): Task<Void> {
        return FirebaseUserService.saveUser(profileToSave)
    }

    val profileRatingList: LiveData<List<Rating>> = liveData {
        FirebaseRatingService.getUserRatings(userId).collect {
            if (it == null || it.isEmpty()) {
                Log.d(TAG, "Requests accepted to user ${userId} is empty")
                emit(emptyList<Rating>())
            } else {
                emit(it)
            }
        }
    }
}