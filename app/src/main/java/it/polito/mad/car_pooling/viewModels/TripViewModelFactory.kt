package it.polito.mad.car_pooling.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TripViewModelFactory(private val tripId: String): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            return TripViewModel(tripId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}