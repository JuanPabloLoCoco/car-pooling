package it.polito.mad.car_pooling

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.viewModels.TripListViewModel
import it.polito.mad.car_pooling.viewModels.TripListViewModelFactory
import java.io.File

class TripsOfInterestListFragment : Fragment() {

    private lateinit var userId: String
    private lateinit var viewModel: TripListViewModel
    private lateinit var viewModelFactory: TripListViewModelFactory
    private var TAG = "TripsOfInterestListFragment"
    private lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var adapter: InterestTripCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        userId = ModelPreferencesManager.get(getString(R.string.keyCurrentAccount))?: "no email"
        viewModelFactory = TripListViewModelFactory(userId)
        viewModel = viewModelFactory.create(TripListViewModel::class.java)
        return inflater.inflate(R.layout.fragment_trips_of_interest_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val reciclerView = view.findViewById<RecyclerView>(R.id.rv)
        reciclerView.layoutManager = LinearLayoutManager(requireContext())

        val tripList = mutableListOf<Trip>()
        adapter = InterestTripCardAdapter(tripList, requireContext(), findNavController())
        reciclerView.adapter = adapter

        viewModel.interestTrips.observe(viewLifecycleOwner, {
            adapter.tripList = it
            adapter.updateTripList(it)
            if (it.size > 0) {
                view.findViewById<TextView>(R.id.empty_tripInterestList).visibility=View.INVISIBLE
            } else {
                view.findViewById<TextView>(R.id.empty_tripInterestList).visibility=View.VISIBLE
            }
            Log.d(TAG, "Trip list by View Model size = ${it.size}")
        })

        val refreshInterestTripList = view.findViewById<SwipeRefreshLayout>(R.id.refreshInterestTripList)
        refreshInterestTripList.setOnRefreshListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fragmentManager?.beginTransaction()?.detach(this)?.commitNow()
                fragmentManager?.beginTransaction()?.attach(this)?.commitNow()
            } else {
                fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
            }
            refreshInterestTripList.isRefreshing = false
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

    override fun onResume() {
        super.onResume()
        val toolbar : Toolbar = (activity as AppCompatActivity).findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = (activity as AppCompatActivity).findViewById(R.id.drawer_layout)
        val navView: NavigationView = (activity as AppCompatActivity).findViewById(R.id.nav_view)
        val navController = (activity as AppCompatActivity).findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_other_list_trip, R.id.nav_list_trip, R.id.nav_profile, R.id.nav_list_interest_trip, R.id.nav_list_bought_trip, R.id.nav_setting), drawerLayout)
        (activity as AppCompatActivity).setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_dehaze_24)
    }
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
        holder.tripCardView.findViewById<MaterialButton>(R.id.tripCardEditTripButton).visibility = View.GONE

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

    fun updateTripList (tripList: List<Trip>) {
        this.tripList = tripList
        tripFilterList = tripList
        notifyDataSetChanged()
    }
}

