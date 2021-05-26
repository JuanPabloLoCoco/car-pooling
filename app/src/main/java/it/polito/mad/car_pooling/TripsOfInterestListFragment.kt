package it.polito.mad.car_pooling

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
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

class TripsOfInterestListFragment : Fragment() {

    var trip_count : Int = 0
    lateinit var  tripList :List<Trip>
    lateinit var adapter: InterestTripCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_trips_of_interest_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val reciclerView = view.findViewById<RecyclerView>(R.id.rv)


        reciclerView.layoutManager = LinearLayoutManager(requireContext())


        val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val acc_email = sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email")
        val db = FirebaseFirestore.getInstance()
        val trips = db.collection("Trips")
        val tripListdb = mutableListOf<Trip>()

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
                    new_trip.avaSeats = (document.data["avaSeats"] as Long).toInt()
                    new_trip.depDate = document.data["depDate"].toString()
                    new_trip.depTime = document.data["depTime"].toString()
                    new_trip.estDuration = document.data["estDuration"].toString()
                    new_trip.optional = document.data["optional"].toString()
                    new_trip.plate = document.data["plate"].toString()
                    new_trip.price = document.data["price"] as Double
                    new_trip.imageUri = document.data["image_uri"].toString()
                    new_trip.owner = document.data["owner"].toString()
                    tripListdb.add(new_trip)

                }
                tripList=tripListdb.toList()
                if (trip_count == 0){
                    super.onViewCreated(view, savedInstanceState)
                } else {
                    adapter = InterestTripCardAdapter(tripList, requireContext(), findNavController())
                    reciclerView.adapter = adapter
                    view.findViewById<TextView>(R.id.empty_tripInterestList).visibility=View.INVISIBLE
                }
            }.addOnFailureListener { exception ->
                Log.d("nav_list_trip", "Error getting documents: ", exception)
            }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)
        val searchMenuItem = menu.findItem(R.id.search_button)
        val searchView =searchMenuItem.actionView as SearchView
        searchView.setQueryHint("search view hint")
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
            override fun onQueryTextSubmit(charString: String?): Boolean {
                return false
            }
        })
        return super.onCreateOptionsMenu(menu, inflater)

    }

    class InterestTripCardAdapter(
        var tripList: List<Trip>,
        val context: Context,
        val navController: NavController
    ): RecyclerView.Adapter<InterestTripCardAdapter.TripCardViewHolder>(), Filterable {
        var tripFilterList: List<Trip> = tripList

        init {
            tripFilterList = tripList
        }

        class TripCardViewHolder(v: View) : RecyclerView.ViewHolder(v) {
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
            val selectedTrip: Trip = tripFilterList[position]

            holder.departureLocationView.text = getStringFromField(selectedTrip.depLocation)
            holder.arriveLocationView.text = getStringFromField(selectedTrip.ariLocation)
            holder.departureTimeView.text = getStringFromField(selectedTrip.depDate + " " + selectedTrip.depTime)
            holder.priceView.text = getStringFromField(selectedTrip.price.toString())
            holder.availableSeatsView.text = getStringFromField(selectedTrip.avaSeats.toString())


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

            holder.tripCardView.setOnClickListener {
                // Handle navigation to Trip
                val action = TripsOfInterestListFragmentDirections.actionNavListInterestTripToNavTrip(tripId = selectedTrip.id, isOwner = false, sourceFragment = "interestTrips")
                navController.navigate(action)
            }
            holder.tripCardView.findViewById<MaterialButton>(R.id.tripCardEditTripButton).text = "Profile"

            holder.tripCardView.findViewById<MaterialButton>(R.id.tripCardEditTripButton).setOnClickListener {
                // Handle navigation to Owner Profile
                val action = TripsOfInterestListFragmentDirections.actionNavListInterestTripToNavProfile(userId = selectedTrip.owner, isOwner = false)
                navController.navigate(action)
            }
        }


        override fun getItemCount(): Int {
            return tripFilterList.size
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    var charSearch = constraint.toString().toLowerCase()
                    tripFilterList =tripList
                    if(charSearch.isEmpty()){

                    }else{
                        tripFilterList=tripList.filter { it ->
                            // Write more filterign conditions here
                            var numeric =true
                            try{
                                val string = java.lang.Double.parseDouble(charSearch)
                            }
                            catch (e : NumberFormatException){
                                numeric =false
                            }
                            if (numeric){
                                charSearch.toDouble() >=it.price.toDouble()

                            }else{
                                charSearch in it.depLocation.toLowerCase() ||
                                        charSearch in it.ariLocation.toLowerCase() ||
                                        charSearch in it.depTime.toLowerCase()
                            }
                        }
                    }
                    var filterResults= FilterResults()
                    filterResults.values =tripFilterList
                    return filterResults
                }
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    tripFilterList=results?.values as List<Trip>
                    notifyDataSetChanged()
                }
            }
        }
    }

    class  SearchResultsActivity : Activity(){
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            handleIntent(intent)
        }

        override fun onNewIntent(intent: Intent?) {
            handleIntent(intent)
        }

        private fun handleIntent(intent: Intent?) {
            if (intent != null) {
                if (Intent.ACTION_SEARCH == intent.action){
                    val query =intent.getStringExtra(SearchManager.QUERY)
                }
            }
        }
    }
}
