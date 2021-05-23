package it.polito.mad.car_pooling.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.services.FirebaseUserService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class EditProfileViewModel(userId: String): ViewModel() {
    fun saveUser(user: Profile? = null) {
        if (user != null) {
            FirebaseUserService.saveUser(user)
        } else {
            Log.d("POLITO", "Updating user in the viewModel")
        }
    }

    private val _user = MutableLiveData<Profile>()
    val user: LiveData<Profile> = _user

    init {
        viewModelScope.launch {
            FirebaseUserService.getUserById(userId).collect { _user.value = it }
        }
    }
}