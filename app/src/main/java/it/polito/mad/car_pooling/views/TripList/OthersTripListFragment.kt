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
import java.lang.Double.parseDouble

class OthersTripListFragment : Fragment() {

    lateinit var adapter: OthersTripCardAdapter
    private lateinit var viewModel: TripListViewModel
    private lateinit var viewModelFactory: TripListViewModelFactory
    private lateinit var userId: String
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        userId = ModelPreferencesManager.get(getString(R.string.keyCurrentAccount))?: "no email"
        viewModelFactory = TripListViewModelFactory(userId)
        viewModel = viewModelFactory.create(TripListViewModel::class.java)

        return inflater.inflate(R.layout.fragment_others_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val reciclerView = view.findViewById<RecyclerView>(R.id.rv)
        reciclerView.layoutManager = LinearLayoutManager(requireContext())
        val tripList = mutableListOf<Trip>()
        adapter = OthersTripCardAdapter(tripList, requireContext(), findNavController(), viewModel)
        reciclerView.adapter = adapter

        viewModel.othersTrips.observe(viewLifecycleOwner, {
            adapter.tripList = it
            adapter.updateTripList(it)
            Log.d("POLITO", "Others Trips size ${it.size}")
            if (it.isNotEmpty()) {
                view.findViewById<TextView>(R.id.empty_triplist).visibility=View.INVISIBLE
            } else {
                view.findViewById<TextView>(R.id.empty_triplist).visibility=View.VISIBLE
            }
        })

        val refreshOtherTripList = view.findViewById<SwipeRefreshLayout>(R.id.refreshOtherTripList)
        refreshOtherTripList.setOnRefreshListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fragmentManager?.beginTransaction()?.detach(this)?.commitNow()
                fragmentManager?.beginTransaction()?.attach(this)?.commitNow()
            } else {
                fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
            }
            refreshOtherTripList.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        val toolbar : Toolbar = (activity as AppCompatActivity).findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = (activity as AppCompatActivity).findViewById(R.id.drawer_layout)
        val navView: NavigationView = (activity as AppCompatActivity).findViewById(R.id.nav_view)
        val navController = (activity as AppCompatActivity).findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_other_list_trip, R.id.nav_list_trip, R.id.nav_profile), drawerLayout)
        (activity as AppCompatActivity).setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)
        val searchMenuItem = menu.findItem(R.id.search_button)

        Log.d("POLITO", "search menu item $searchMenuItem")
        val searchView =searchMenuItem.actionView as SearchView

        searchView.queryHint = "search view hint"

        Log.d("POLITO", "Is null $searchView")
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("POLITO", "$newText")
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

class OthersTripCardAdapter(
        var tripList: List<Trip>,
        val context: Context,
        private val navController: NavController,
        val viewModel: TripListViewModel): RecyclerView.Adapter<OthersTripCardAdapter.TripCardViewHolder>(), Filterable {
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripCardViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return TripCardViewHolder(v)
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



        if (selectedTrip.hasImage == true) {
            val localFile = File.createTempFile("trip_${selectedTrip.id}", "jpg")
            val storage = Firebase.storage
            storage.reference
                .child("trips/${selectedTrip.id}.jpg")
                .getFile(localFile)
                .addOnSuccessListener {
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
            // Handle navigation to Trip
            val action = OthersTripListFragmentDirections.actionOthersTripListFragmentToNavTrip(tripId = selectedTrip.id, isOwner = false, "otherTrips")
            navController.navigate(action)
        }
        holder.tripCardView.findViewById<MaterialButton>(R.id.tripCardEditTripButton).text = "Profile"

        holder.tripCardView.findViewById<MaterialButton>(R.id.tripCardEditTripButton).setOnClickListener {
            // Handle navigation to Owner Profile

            val action = OthersTripListFragmentDirections.actionNavOtherListTripToNavProfile(userId = selectedTrip.owner, isOwner = false)
            navController.navigate(action)
        }


    }

    override fun getItemCount(): Int {
        return tripFilterList.size
    }

    override fun getFilter(): Filter {
        return object :Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                var charSearch = constraint.toString().toLowerCase()
                tripFilterList = tripList
                if(charSearch.isEmpty()){

                }else{
                    tripFilterList=tripList.filter { it ->
                        // Write more filterign conditions here
                        var numeric =true
                        try{
                            val string = parseDouble(charSearch)}
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
                var filterResults = FilterResults()
                filterResults.values = tripFilterList
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
