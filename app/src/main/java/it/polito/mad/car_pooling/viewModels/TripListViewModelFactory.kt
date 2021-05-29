package it.polito.mad.car_pooling.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TripListViewModelFactory (private val userId: String): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripListViewModel::class.java)) {
            return TripListViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}