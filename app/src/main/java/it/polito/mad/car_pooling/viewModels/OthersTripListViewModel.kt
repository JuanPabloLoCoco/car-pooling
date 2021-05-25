package it.polito.mad.car_pooling.viewModels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.services.FirebaseTripService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OthersTripListViewModel(userId: String): ViewModel() {
    private val _othersTrips = MutableLiveData<List<Trip>>()
    val othersTrips: LiveData<List<Trip>> = _othersTrips

    init {
        viewModelScope.launch {
            FirebaseTripService.getOthersTrips(userId).collect { _othersTrips.value = it }
        }
    }
}