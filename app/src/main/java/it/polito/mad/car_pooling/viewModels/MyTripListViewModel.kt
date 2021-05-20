package it.polito.mad.car_pooling.viewModels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.car_pooling.models.Trip

class MyTripListViewModel(): ViewModel() {
    private lateinit var firestore : FirebaseFirestore
    private var _trips: MutableLiveData<ArrayList<Trip>> = MutableLiveData<ArrayList<Trip>>()

    init {
        firestore = FirebaseFirestore.getInstance()
        listenToMyTrips()
    }

    private fun listenToMyTrips() {
        /*trips.whereEqualTo("owner", acc_email)
            .get()
            .addOnSuccessListener { documents ->
                trip_count = 0
                for (document in documents) {
                    trip_count += 1
                    val new_trip = Trip(document.id)
                    new_trip.depLocation = document.data["depLocation"].toString()
                    new_trip.additional = document.data["additional"].toString()
                    new_trip.ariLocation = document.data["ariLocation"].toString()
                    new_trip.avaSeats = (document.data["avaSeats"] as Long).toInt()
                    new_trip.depDate = document.data["depDate"].toString()
                    new_trip.depTime = document.data["depTime"].toString()
                    new_trip.estDuration = document.data["estDuration"].toString()
                    new_trip.optional = document.data["optional"].toString()
                    new_trip.plate = document.data["plate"].toString()
                    new_trip.price = (document.data["price"] as Double)
                    new_trip.imageUri = document.data["image_uri"].toString()
                    tripList.add(new_trip)
                }
                if (trip_count == 0){
                    super.onViewCreated(view, savedInstanceState)
                } else {
                    val rvAdapter = TripCardAdapter(tripList, requireContext(), findNavController())
                    reciclerView.adapter = rvAdapter
                    requireView().findViewById<TextView>(R.id.empty_triplist).visibility= View.INVISIBLE
                }
            }.addOnFailureListener { exception ->
                Log.d("nav_list_trip", "Error getting documents: ", exception)
            }*/
        firestore.collection(Trip.DATA_COLLECTION).addSnapshotListener {
                snapshot, e ->
            // if there is an exception we want to skip.
            if (e != null) {
                Log.w(TAG, "Listen Failed", e)
                return@addSnapshotListener
            }
            // if we are here, we did not encounter an exception
            if (snapshot != null) {
                // now, we have a populated shapshot
                val allTrips = ArrayList<Trip>()
                val documents = snapshot.documents
                documents.forEach {
                    val new_trip = documentToTrip(it)
                    allTrips.add(new_trip)
                }
                _trips.value = allTrips
            }
        }
    }

    private fun documentToTrip(document:DocumentSnapshot): Trip {
        val new_trip = Trip(document.id)
        new_trip.depLocation = document.data?.get("depLocation").toString()
        new_trip.additional = document.data?.get("additional").toString()
        new_trip.ariLocation = document.data?.get("ariLocation").toString()
        new_trip.avaSeats = (document.data?.get("avaSeats") as Long).toInt()
        new_trip.depDate = document.data!!["depDate"].toString()
        new_trip.depTime = document.data!!["depTime"].toString()
        new_trip.estDuration = document.data!!["estDuration"].toString()
        new_trip.optional = document.data!!["optional"].toString()
        new_trip.plate = document.data!!["plate"].toString()
        new_trip.price = (document.data!!["price"] as Double)
        new_trip.imageUri = document.data!!["image_uri"].toString()
        return new_trip
    }

    internal var trips:MutableLiveData<ArrayList<Trip>>
        get() { return _trips}
        set(value) {_trips = value}
}