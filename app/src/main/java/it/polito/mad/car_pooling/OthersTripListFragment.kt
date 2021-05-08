package it.polito.mad.car_pooling

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.car_pooling.Adapter.TripCardAdapter
import it.polito.mad.car_pooling.models.Trip

class OthersTripListFragment : Fragment() {

    var trip_count : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_others_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val reciclerView = view.findViewById<RecyclerView>(R.id.rv)
        reciclerView.layoutManager = LinearLayoutManager(requireContext())

        val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val acc_email = sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email")
        val db = FirebaseFirestore.getInstance()
        val trips = db.collection("Trips")
        val tripList = mutableListOf<Trip>()

        trips.whereNotEqualTo("owner", acc_email)
                .limit(20L)
                .get()
                .addOnSuccessListener { documents ->
                    trip_count = 0
                    for (document in documents) {
                        trip_count += 1
                        val new_trip = Trip(document.id)
                        new_trip.depLocation = document.data["depLocation"].toString()
                        new_trip.additional = document.data["additional"].toString()
                        new_trip.ariLocation = document.data["ariLocation"].toString()
                        new_trip.avaSeat = document.data["avaSeats"].toString()
                        new_trip.depDate = document.data["depDate"].toString()
                        new_trip.depTime = document.data["depTime"].toString()
                        new_trip.estDuration = document.data["estDuration"].toString()
                        new_trip.optional = document.data["optional"].toString()
                        new_trip.plate = document.data["plate"].toString()
                        new_trip.price = document.data["price"].toString()
                        new_trip.imageUri = document.data["image_uri"].toString()
                        tripList.add(new_trip)
                    }
            if (trip_count == 0){
                super.onViewCreated(view, savedInstanceState)
            } else {
                val rvAdapter = TripCardAdapter(tripList, requireContext(), findNavController())
                reciclerView.adapter = rvAdapter
                requireView().findViewById<TextView>(R.id.empty_triplist).visibility=View.INVISIBLE
            }
        }.addOnFailureListener { exception ->
            Log.d("nav_list_trip", "Error getting documents: ", exception)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.depLocationSearch -> {
                return true
            }
            R.id.ariLocationSearch -> {
                return true
            }
            R.id.depDateTimeSearch -> {
                return true
            }
            R.id.avaSeatsSearch -> {
                return true
            }
            R.id.priceSearch -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}