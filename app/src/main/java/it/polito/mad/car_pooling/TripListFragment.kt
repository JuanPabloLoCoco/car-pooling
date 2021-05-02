package it.polito.mad.car_pooling

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.models.TripList

class TripListFragment : Fragment() {

    var trip_count : Int = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // val  rv= requireView().findViewById<RecyclerView>(R.id.rv)
        return inflater.inflate(R.layout.fragment_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fabView = view.findViewById<FloatingActionButton>(R.id.addTripFAB)

        var storedTripList = ModelPreferencesManager.get<TripList>(getString(R.string.KeyTripList))
        var dataList: List<Trip>
        if (storedTripList == null) {
            dataList = listOf()
        } else {
            dataList = storedTripList.tripList
        }

        val reciclerView = view.findViewById<RecyclerView>(R.id.rv)
        reciclerView.layoutManager = LinearLayoutManager(requireContext())

        //var check : String = "yes"
        val db = FirebaseFirestore.getInstance()
        val my_trip = db.collection("my_trip")
        //var trip_count = 0
        my_trip.document("trip1").get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    Log.d("nav_list_trip", "Success")
                    //check = "yes"
                    val tripList = mutableListOf<Trip>()
                    val db = FirebaseFirestore.getInstance()
                    val my_trip = db.collection("my_trip")
                    my_trip.get()
                            .addOnSuccessListener { result ->
                                for (document in result) {
                                    Log.d("nav_list_trip", "${document.id} => ${document.data}")
                                    if (document.id != "default_trip") {
                                        var new_trip = Trip(document.id.last().toString().toInt())
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
                                        tripList.add(new_trip)
                                        trip_count += 1
                                    }
                                }
                                val rvAdapter = TripCardAdapter(tripList, requireContext(), findNavController())
                                reciclerView.adapter = rvAdapter
                                requireView().findViewById<TextView>(R.id.empty_triplist).visibility=View.INVISIBLE
                            }
                            .addOnFailureListener { exception ->
                                Log.d("nav_list_trip", "Error getting documents: ", exception)
                            }
                } else {
                    Log.d("nav_list_trip", "No such document")
                    //check = "no"
                    //Log.d("nav_list_trip", "${check} whyyyyyyyyy")
                    super.onViewCreated(view, savedInstanceState)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("nav_list_trip", "get failed with ", exception)
            }



        //val tripCount = arguments?.getInt("tripCount")!!.toInt()

        //if(dataList.isNotEmpty()){
        /*var trip_count = 0
        if(check == "no"){
           return super.onViewCreated(view, savedInstanceState)
        } else {
            val tripList = mutableListOf<Trip>()
            val db = FirebaseFirestore.getInstance()
            val my_trip = db.collection("my_trip")
            my_trip.get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        Log.d("nav_list_trip", "${document.id} => ${document.data}")
                        if (document.id != "default_trip") {
                            var new_trip = Trip(document.id.last().toString().toInt())
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
                            tripList.add(new_trip)
                            trip_count += 1
                        }
                    }
                    val rvAdapter = TripCardAdapter(tripList, requireContext(), findNavController())
                    reciclerView.adapter = rvAdapter
                    requireView().findViewById<TextView>(R.id.empty_triplist).visibility=View.INVISIBLE
                }
                .addOnFailureListener { exception ->
                    Log.d("nav_list_trip", "Error getting documents: ", exception)
                }
            // display some message ,that message will on the trip detailfragment
        }*/

        fabView.setOnClickListener {
            //Toast.makeText(context, "A click on FAB", Toast.LENGTH_SHORT).show()

            //val action = TripListFragmentDirections.actionNavListTripToTripEditFragment(Trip.NEW_TRIP_ID)
            //findNavController().navigate(action)
            val bundle = bundleOf("tripId" to Trip.NEW_TRIP_ID, "tripCount" to trip_count)
            findNavController().navigate(R.id.action_nav_list_trip_to_tripEditFragment, bundle)
        }
    }
}

class TripCardAdapter (val tripList: List<Trip>,
                       val context: Context,
                       val navController: NavController): RecyclerView.Adapter<TripCardAdapter.TripCardViewHolder>() {
    class TripCardViewHolder(v: View): RecyclerView.ViewHolder (v) {
        val departureLocationView = v.findViewById<TextView>(R.id.depatureview)
        val arriveLocationView = v.findViewById<TextView>(R.id.arriveview)
        val departureTimeView = v.findViewById<TextView>(R.id.timeview)
        val priceView = v.findViewById<TextView>(R.id.priceview)
        val availableSeatsView = v.findViewById<TextView>(R.id.tripAvailableSeatsField)
        val tripImageView = v.findViewById<ImageView>(R.id.imageview)

        val tripCardView = v.findViewById<CardView>(R.id.tripCard)
        fun bind(t: Trip) {

        }

        fun unbind() {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripCardViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return TripCardViewHolder(v)
    }

    override fun onViewRecycled(holder: TripCardViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    fun getStringFromField(field: String?): String {
        return if (field == null) "" else field
    }

    override fun onBindViewHolder(holder: TripCardViewHolder, position: Int) {
        val selectedTrip: Trip = tripList[position]

        holder.departureLocationView.text = getStringFromField(selectedTrip.depLocation)
        holder.arriveLocationView.text = getStringFromField(selectedTrip.ariLocation)
        holder.departureTimeView.text = getStringFromField(selectedTrip.depDate + " " + selectedTrip.depTime)
        holder.priceView.text = getStringFromField(selectedTrip.price)
        holder.availableSeatsView.text = getStringFromField(selectedTrip.avaSeat)

        val tripImageUri = selectedTrip.imageUri //sharedPreferences.getString(getString(R.string.KeyImageTrip), "android.resource://it.polito.mad.car_pooling/drawable/car_default")

        val uri_input = if (tripImageUri.toString() == "android.resource://it.polito.mad.car_pooling/drawable/car_default"
                || tripImageUri.toString().isEmpty()) "android.resource://it.polito.mad.car_pooling/drawable/car_default" else tripImageUri
        holder.tripImageView.setImageURI(Uri.parse(uri_input))

        holder.tripCardView.setOnClickListener {
            val tripDetailArguments = TripListFragmentDirections.actionNavListTripToNavTrip(selectedTrip.id)
            navController.navigate(tripDetailArguments)
        }

        holder.tripCardView.findViewById<MaterialButton>(R.id.tripCardEditTripButton).setOnClickListener{
            // Handle navigation to edit trip detail
            // Toast.makeText(context, "Go to edit trip ${selectedTrip.id}", Toast.LENGTH_SHORT).show()
            val action = TripListFragmentDirections.actionNavListTripToTripEditFragment(selectedTrip.id-1)
            navController.navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return tripList.size
    }
}
