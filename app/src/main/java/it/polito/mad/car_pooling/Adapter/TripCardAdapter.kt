package it.polito.mad.car_pooling.Adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.R
import it.polito.mad.car_pooling.models.Trip
import java.io.File

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
            val bundle = bundleOf( "tripId" to selectedTrip.id)
            navController.navigate(R.id.action_nav_list_trip_to_tripEditFragment, bundle)
            //Log.d("nav_list_trip", "${selectedTrip.id} yessssssss")
        }
    }

    override fun getItemCount(): Int {
        return tripList.size
    }
}