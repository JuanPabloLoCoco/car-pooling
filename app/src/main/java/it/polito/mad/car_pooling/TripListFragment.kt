package it.polito.mad.car_pooling

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.Trip


class TripListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // val  rv= requireView().findViewById<RecyclerView>(R.id.rv)
        return inflater.inflate(R.layout.fragment_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fabView = view.findViewById<FloatingActionButton>(R.id.addTripFAB)

        val list = ModelPreferencesManager.get<ArrayList<Trip>>(getString(R.string.KeyTripList))
        if (list == null || list.isEmpty()) {
            Toast.makeText(context, "Esta vacia la lista", Toast.LENGTH_LONG).show()
        }

        fabView.setOnClickListener {
            Toast.makeText(context, "A click on FAB", Toast.LENGTH_SHORT).show()
            ModelPreferencesManager.put(Trip.CREATE_TRIP, getString(R.string.KeyEditTripAction))
            findNavController().navigate(R.id.tripEditFragment)
        }

        val reciclerView = view.findViewById<RecyclerView>(R.id.rv)
        reciclerView.layoutManager = LinearLayoutManager(requireContext())

        val trip1 = Trip(1)
        trip1.price = "10 Euros"
        trip1.depAriLocation = "Torino"
        trip1.arrLocation="Milano"
        trip1.depDateTime = "10pm"
        trip1.avaSeat = "4 Seats"

        val trip2 = Trip(2)
        trip2.price = "80 Euros"
        trip2.depAriLocation = "Milano"
        trip2.arrLocation="Milano"
        trip2.depDateTime = "9pm"
        trip2.avaSeat = "5 Seats"

        val trip3 = Trip(3)
        trip3.price = "30 Euros"
        trip3.depAriLocation = "Rome"
        trip3.arrLocation="Milano"
        trip3.depDateTime = "1pm"
        trip3.avaSeat = "2 Seats"

        //val dataList = ArrayList<Trip>(trip1, trip2, trip3)
        val dataList = arrayListOf<Trip>(trip1, trip2, trip3)
        //val dataList = arrayListOf<Trip>()
        if(dataList.isNotEmpty()){

            val rvAdapter = TripCardAdapter(dataList.shuffled(), requireContext())
            reciclerView.adapter = rvAdapter
            requireView().findViewById<TextView>(R.id.empty_triplist).visibility=View.INVISIBLE
        }else {

            //display some message ,that message will on the trip detailfragment
        }

        // Log.d("POLITO_ERRORS", "Recicler view es null: " + (reciclerView == null).toString())
    }
}

class TripCardAdapter (val tripList: List<Trip>, val context: Context): RecyclerView.Adapter<TripCardAdapter.TripCardViewHolder>() {
    class TripCardViewHolder(v: View): RecyclerView.ViewHolder (v) {
        val departureLocationView = v.findViewById<TextView>(R.id.depatureview)
        val arriveLocationView = v.findViewById<TextView>(R.id.arriveview)
        val departureTimeView = v.findViewById<TextView>(R.id.timeview)
        val priceView = v.findViewById<TextView>(R.id.priceview)
        val availableSeatsView = v.findViewById<TextView>(R.id.tripAvailableSeatsField)


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

    override fun onBindViewHolder(holder: TripCardViewHolder, position: Int) {
        val selectedTrip: Trip = tripList[position]

        holder.departureLocationView.text = selectedTrip.depAriLocation
        holder.arriveLocationView.text= selectedTrip.arrLocation
        holder.departureTimeView.text = selectedTrip.depDateTime
        holder.priceView.text = selectedTrip.price
        holder.availableSeatsView.text = selectedTrip.avaSeat

        holder.tripCardView.setOnClickListener {
            // Handle navigation to show trip detail

            Toast.makeText(context, "A click on card ${selectedTrip.id}", Toast.LENGTH_SHORT).show()
        }

        holder.tripCardView.findViewById<MaterialButton>(R.id.tripCardEditTripButton).setOnClickListener{
            // Handle navigation to edit trip detail
            Toast.makeText(context, "Go to edit trip ${selectedTrip.id}", Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount(): Int {
        return tripList.size
    }
}
