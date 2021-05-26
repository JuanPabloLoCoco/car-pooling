package it.polito.mad.car_pooling.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.services.FirebaseUserService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProfileViewModel(private val userId: String): ViewModel() {
    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile> = _profile

    init {
        viewModelScope.launch {
            FirebaseUserService.getUserById(userId).collect { _profile.value = it }
        }
    }
}