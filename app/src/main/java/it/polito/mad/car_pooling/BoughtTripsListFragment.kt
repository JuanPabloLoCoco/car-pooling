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
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.viewModels.TripListViewModel
import it.polito.mad.car_pooling.viewModels.TripListViewModelFactory
import java.io.File

class BoughtTripsListFragment : Fragment() {

    private lateinit var userId: String
    lateinit var adapter: BoughtTripCardAdapter
    private var TAG = "BoughtTripsListFragment"
    private lateinit var viewModel: TripListViewModel
    private lateinit var viewModelFactory: TripListViewModelFactory


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        userId = ModelPreferencesManager.get(getString(R.string.keyCurrentAccount)) ?: "no email"
        viewModelFactory = TripListViewModelFactory(userId)
        viewModel = viewModelFactory.create(TripListViewModel::class.java)
        return inflater.inflate(R.layout.fragment_bought_trips_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val reciclerView = view.findViewById<RecyclerView>(R.id.rv)
        reciclerView.layoutManager = LinearLayoutManager(requireContext())

        val tripList = mutableListOf<Trip>()
        adapter = BoughtTripCardAdapter(tripList, requireContext(), findNavController())
        reciclerView.adapter = adapter
        viewModel.boughtTrips.observe(viewLifecycleOwner, {
            adapter.tripList = it
            adapter.updateTripList(it)
            if (it.size > 0) {
                view.findViewById<TextView>(R.id.empty_tripBoughtList).visibility=View.INVISIBLE
            } else {
                view.findViewById<TextView>(R.id.empty_tripBoughtList).visibility=View.VISIBLE
            }
            Log.d(TAG, "Trips found size = ${it.size}")
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)
        val searchMenuItem = menu.findItem(R.id.search_button)
        val searchView = searchMenuItem.actionView as SearchView
        searchView.setQueryHint("search view hint")
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
}

class BoughtTripCardAdapter(
    var tripList: List<Trip>,
    val context: Context,
    val navController: NavController
): RecyclerView.Adapter<BoughtTripCardAdapter.TripCardViewHolder>(), Filterable {
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
            val action = BoughtTripsListFragmentDirections.actionNavListBoughtTripToNavTrip(tripId = selectedTrip.id, isOwner = false, sourceFragment = "boughtTrips")
            navController.navigate(action)
        }
        holder.tripCardView.findViewById<MaterialButton>(R.id.tripCardEditTripButton).text = "Profile"

        holder.tripCardView.findViewById<MaterialButton>(R.id.tripCardEditTripButton).setOnClickListener {
            // Handle navigation to Owner Profile
            val action = BoughtTripsListFragmentDirections.actionNavListBoughtTripToNavProfile(userId = selectedTrip.owner, isOwner = false)
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

    fun updateTripList (tripList: List<Trip>) {
        this.tripList = tripList
        tripFilterList = tripList
        notifyDataSetChanged()
    }
}
