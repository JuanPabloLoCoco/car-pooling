package it.polito.mad.car_pooling

import android.content.Context
import android.graphics.BitmapFactory
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
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.viewModels.MyTripListViewModel
import it.polito.mad.car_pooling.viewModels.MyTripListViewModelFactory
import java.io.File

class TripListFragment : Fragment() {

    var trip_count : Int = 0
    var trip_total : Int = 0

    private lateinit var userId: String

    private lateinit var viewModel: MyTripListViewModel
    private lateinit var myTripListViewModelFactory: MyTripListViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // val  rv= requireView().findViewById<RecyclerView>(R.id.rv)

        // val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        // var acc_email = sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email")

        userId = ModelPreferencesManager.get(getString(R.string.keyCurrentAccount))?: "no email"

        myTripListViewModelFactory = MyTripListViewModelFactory(userId)
        viewModel = myTripListViewModelFactory.create(MyTripListViewModel::class.java)

        return inflater.inflate(R.layout.fragment_trip_list, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reciclerView = view.findViewById<RecyclerView>(R.id.rv)
        reciclerView.layoutManager = LinearLayoutManager(requireContext())

        val tripList = mutableListOf<Trip>()

        val rvAdapter = TripCardAdapter(tripList, requireContext(), findNavController())
        reciclerView.adapter = rvAdapter
        viewModel.myTrips.observe(viewLifecycleOwner, Observer {
            rvAdapter.tripList = it
            if (it.size > 0) {
                requireView().findViewById<TextView>(R.id.empty_triplist).visibility = View.INVISIBLE
            } else {
                requireView().findViewById<TextView>(R.id.empty_triplist).visibility = View.VISIBLE
            }
            rvAdapter.notifyDataSetChanged()
            Log.d("POLITO", "TRIP LIST BY VIEW MODEL size: ${it.size}" )
        })

        val fabView = view.findViewById<FloatingActionButton>(R.id.addTripFAB)
        fabView.setOnClickListener {
            // val bundle = bundleOf( "newOrOld" to "new")
            val action = TripListFragmentDirections.actionNavListTripToTripEditFragment(tripId = null)
            findNavController().navigate(action)
        }
    }
}

class TripCardAdapter (var tripList: List<Trip>,
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripCardViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return TripCardViewHolder(v)
    }

    override fun onViewRecycled(holder: TripCardViewHolder) {
        super.onViewRecycled(holder)
    }

    fun getStringFromField(field: String?): String {
        return if (field == null) "" else field
    }

    override fun onBindViewHolder(holder: TripCardViewHolder, position: Int) {
        val selectedTrip: Trip = tripList[position]

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
            //val tripDetailArguments = TripListFragmentDirections.actionNavListTripToNavTrip(selectedTrip.id)
            val action = TripListFragmentDirections.actionNavListTripToNavTrip(selectedTrip.id, true, "myTrips")
            navController.navigate(action/*R.id.action_nav_list_trip_to_nav_trip, bundle*/)
        }

        holder.tripCardView.findViewById<MaterialButton>(R.id.tripCardEditTripButton).setOnClickListener{
            // Handle navigation to edit trip detail
            val action = TripListFragmentDirections.actionNavListTripToTripEditFragment(selectedTrip.id)
            navController.navigate(action/*R.id.action_nav_list_trip_to_tripEditFragment, bundle*/)
        }
    }

    override fun getItemCount(): Int {
        return tripList.size
    }
}