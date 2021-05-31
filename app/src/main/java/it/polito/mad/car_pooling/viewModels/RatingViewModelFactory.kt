package it.polito.mad.car_pooling.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RatingViewModelFactory (private val tripRequestId: String): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RatingViewModel::class.java)) {
            return RatingViewModel(tripRequestId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}