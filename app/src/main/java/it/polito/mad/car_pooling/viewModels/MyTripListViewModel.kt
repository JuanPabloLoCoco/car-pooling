package it.polito.mad.car_pooling.viewModels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.repositories.TripRepository
import it.polito.mad.car_pooling.services.FirebaseTripService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class MyTripListViewModel(userId: String): ViewModel() {
    private val _myTrips = MutableLiveData<List<Trip>>()
    val myTrips: LiveData<List<Trip>> = _myTrips

    init {
        viewModelScope.launch {
            FirebaseTripService.getMyTrips(userId).collect{ _myTrips.value = it}
        }
    }
}