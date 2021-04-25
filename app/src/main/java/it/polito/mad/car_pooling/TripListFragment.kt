package it.polito.mad.car_pooling

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.models.TripList


class TripListFragment : Fragment() {

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

        fabView.setOnClickListener {
            Toast.makeText(context, "A click on FAB", Toast.LENGTH_SHORT).show()

            val action = TripListFragmentDirections.actionNavListTripToTripEditFragment(Trip.NEW_TRIP_ID)
            findNavController().navigate(action)
        }

        val reciclerView = view.findViewById<RecyclerView>(R.id.rv)
        reciclerView.layoutManager = LinearLayoutManager(requireContext())


        if(dataList.isNotEmpty()){
            val rvAdapter = TripCardAdapter(dataList, requireContext(), findNavController())
            reciclerView.adapter = rvAdapter
            requireView().findViewById<TextView>(R.id.empty_triplist).visibility=View.INVISIBLE
        }else {
            // display some message ,that message will on the trip detailfragment
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


        holder.departureLocationView.text = getStringFromField(selectedTrip.depAriLocation)
        holder.arriveLocationView.text = getStringFromField(selectedTrip.arrLocation)
        holder.departureTimeView.text = getStringFromField(selectedTrip.depDateTime)
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
            Toast.makeText(context, "Go to edit trip ${selectedTrip.id}", Toast.LENGTH_SHORT).show()
            val action = TripListFragmentDirections.actionNavListTripToTripEditFragment(selectedTrip.id)
            navController.navigate(action)
        }

    }

    override fun getItemCount(): Int {
        return tripList.size
    }
}
