package it.polito.mad.car_pooling

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.models.Trip
import java.io.File

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
                        new_trip.owner = document.data["owner"].toString()
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
        if (tripImageUri == "yes") {
            val localFile = File.createTempFile("trip_${selectedTrip.id}", "jpg")
            val storage = Firebase.storage
            storage.reference.child("trips/${selectedTrip.id}.jpg").getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                holder.tripImageView.setImageBitmap(bitmap)
            }
        } else {
            holder.tripImageView.setImageURI(Uri.parse("android.resource://it.polito.mad.car_pooling/drawable/car_default"))
        }
        /*val uri_input = if (tripImageUri.toString() == "android.resource://it.polito.mad.car_pooling/drawable/car_default"
                || tripImageUri.toString().isEmpty()) "android.resource://it.polito.mad.car_pooling/drawable/car_default" else tripImageUri
        holder.tripImageView.setImageURI(Uri.parse(uri_input))*/

        holder.tripCardView.setOnClickListener {
            //val tripDetailArguments = TripListFragmentDirections.actionNavListTripToNavTrip(selectedTrip.id)
            //navController.navigate(tripDetailArguments)
            val bundle = bundleOf( "tripId" to selectedTrip.id)
            navController.navigate(R.id.action_nav_list_trip_to_nav_trip, bundle)
        }

        holder.tripCardView.findViewById<MaterialButton>(R.id.tripCardEditTripButton).setOnClickListener{
            // Handle navigation to edit trip detail
            // Toast.makeText(context, "Go to edit trip ${selectedTrip.id}", Toast.LENGTH_SHORT).show()
            //val action = TripListFragmentDirections.actionNavListTripToTripEditFragment(selectedTrip.id)
            //navController.navigate(action)

           // val action = TripListFragmentDirections.
            val action = OthersTripListFragmentDirections.actionNavOtherListTripToNavProfile(userId=selectedTrip.owner,isOwner = false)
            val bundle = bundleOf( "owner" to selectedTrip.id)
            navController.navigate(action)
            //Log.d("nav_list_trip", "${selectedTrip.id} yessssssss")
        }
    }

    override fun getItemCount(): Int {
        return tripList.size
    }
}