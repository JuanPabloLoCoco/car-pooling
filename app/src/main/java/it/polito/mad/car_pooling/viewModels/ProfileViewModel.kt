package it.polito.mad.car_pooling.viewModels

import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.tasks.Task
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.models.Rating
import it.polito.mad.car_pooling.models.RatingProfile
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

    val profileRatingList: LiveData<List<RatingProfile>> = liveData {
        FirebaseRatingService.getUserRatings(userId).collect { ratingList ->
            if (ratingList == null || ratingList.isEmpty()) {
                // Log.d(TAG, "Requests accepted to user ${userId} is empty")
                emit(emptyList<RatingProfile>())
            } else {
                var ratingMap = mutableMapOf<String, MutableList<Rating>>()
                var returnList = mutableListOf<RatingProfile>()
                for (ratingIterator in ratingList) {
                    var writerList = ratingMap.get(ratingIterator.writer)
                    if (writerList == null) {
                        writerList = mutableListOf(ratingIterator)
                    } else {
                        writerList.add(ratingIterator)
                    }
                    ratingMap.put(ratingIterator.writer, writerList)
                }
                FirebaseUserService.getUserByIdInList(ratingMap.keys.toList()).collect { writerProfileList ->
                    for (writerProfileIterator in writerProfileList) {
                        val profileRatingList = ratingMap.get(writerProfileIterator.email) ?: emptyList<Rating>()
                        for (ratingIterator in profileRatingList) {
                            returnList.add(RatingProfile(ratingIterator, writerProfileIterator))
                        }
                    }
                    emit(returnList)
                }
            }
        }
    }
}