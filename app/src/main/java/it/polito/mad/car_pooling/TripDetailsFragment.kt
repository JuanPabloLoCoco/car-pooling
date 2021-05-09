package it.polito.mad.car_pooling

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.models.TripRequest
import java.io.File
import java.util.*


@Suppress("UNREACHABLE_CODE")
class TripDetailsFragment : Fragment() {

    private lateinit var imageTripUri: String
    val args: TripDetailsFragmentArgs by navArgs()
    private lateinit var selectedTrip: Trip
    private lateinit var acc_email: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val isOwner = args.isOwner
        setHasOptionsMenu(isOwner)
        imageTripUri = ""
        return inflater.inflate(R.layout.fragment_trip_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val requestFabView = view.findViewById<FloatingActionButton>(R.id.requestTripFAB)
        if (args.isOwner) {
            // If i am the owner i cant request my own trip
            requestFabView.visibility = View.INVISIBLE
        } else {
            // If I am not the owner I can't see the
            view.findViewById<TextView>(R.id.textPlate).visibility = View.INVISIBLE
        }

        val tripId = args.tripId
        val db = FirebaseFirestore.getInstance()
        db.collection("Trips").document(tripId.toString()).addSnapshotListener { value, error ->
            if (error != null) throw error
            if (value != null) {
                selectedTrip = Trip(tripId)
                selectedTrip.depLocation = value["depLocation"].toString()
                selectedTrip.ariLocation = value["ariLocation"].toString()
                selectedTrip.estDuration = value["estDuration"].toString()
                selectedTrip.avaSeat = value["avaSeats"].toString()
                selectedTrip.price = value["price"].toString()
                selectedTrip.additional = value["additional"].toString()
                selectedTrip.optional = value["optional"].toString()
                selectedTrip.plate = value["plate"].toString()
                selectedTrip.depDate = value["depDate"].toString()
                selectedTrip.depTime = value["depTime"].toString()
                selectedTrip.owner = value["owner"].toString()

                view.findViewById<TextView>(R.id.textDepLocation).text = value["depLocation"].toString()
                view.findViewById<TextView>(R.id.textAriLocation).text = value["ariLocation"].toString()
                view.findViewById<TextView>(R.id.textEstDuration).text = value["estDuration"].toString()
                view.findViewById<TextView>(R.id.textAvaSeat).text = value["avaSeats"].toString()
                view.findViewById<TextView>(R.id.textPrice).text = value["price"].toString()
                view.findViewById<TextView>(R.id.textAdditional).text = value["additional"].toString()
                view.findViewById<TextView>(R.id.textOptional).text = value["optional"].toString()
                view.findViewById<TextView>(R.id.textPlate).text = value["plate"].toString()
                view.findViewById<TextView>(R.id.textDepDate).text = value["depDate"].toString()
                view.findViewById<TextView>(R.id.textDepTime).text = value["depTime"].toString()
                view.findViewById<TextView>(R.id.textDepDate).setTextColor(Color.parseColor("#54150808"))
                view.findViewById<TextView>(R.id.textDepTime).setTextColor(Color.parseColor("#54150808"))
                val default_str_car = "android.resource://it.polito.mad.car_pooling/drawable/car_default"
                val imageView = view.findViewById<ImageView>(R.id.imageviewCar)
                if (value["image_uri"].toString() == "" || value["image_uri"].toString().isEmpty()) {
                    imageTripUri = default_str_car
                    imageView.setImageURI(Uri.parse(imageTripUri))
                } else {
                    val localFile = File.createTempFile("trip_$tripId", "jpg")
                    val storage = Firebase.storage
                    storage.reference.child("trips/$tripId.jpg").getFile(localFile).addOnSuccessListener {
                        val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                        imageView.setImageBitmap(bitmap)
                    }
                }
            }
        }

        /*
        var storedTripList = ModelPreferencesManager.get<TripList>(getString(R.string.KeyTripList))
        if (storedTripList == null) {
            // An imposible case
            Log.e("POLITO_ERRORS", "You are accesing an invalid id")
        } else {
            val tripList = storedTripList.tripList
            selectedTrip = tripList.get(tripId-1)
        }*/
        //loadTripInFields(selectedTrip, view)

        val sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        acc_email = sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email")!!

        requestFabView.setOnClickListener {
            // A new Request from the current user to the owner of the user,should be done
            var tripRequest = TripRequest(acc_email, selectedTrip.owner, selectedTrip.id)

            // Store the request in the database
            // Show a message
            // Return to the main page

            //Log.d("POLITO", "Trip ${tripRequest}")
            //Log.d("POLITO", "Trip data: Status: ${tripRequest.status} - creationTS: ${tripRequest.creationTimestamp}")

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*
        val sharedPreferences = this.requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val DepAriLocation = sharedPreferences.getString(getString(R.string.KeyDepAriLocation), "Departure&Arrival Location")
        val DepDateTime = sharedPreferences.getString(getString(R.string.KeyDepDateTime), "Departure Date&Time")
        val EstDuration = sharedPreferences.getString(getString(R.string.KeyEstDuration), "Estimated Trip Duration")
        val AvaSeat = sharedPreferences.getString(getString(R.string.KeyAvaSeat), "Available Seats")
        val Price = sharedPreferences.getString(getString(R.string.KeyPrice), "Price")
        val Additional = sharedPreferences.getString(getString(R.string.KeyAdditional), "Additional Information")
        val Optional = sharedPreferences.getString(getString(R.string.KeyOptional), "Optional intermediates")
        val Plate = sharedPreferences.getString(getString(R.string.KeyPlate), "Plate Number")
        val TripImageUri = sharedPreferences.getString(getString(R.string.KeyImageTrip), "android.resource://it.polito.mad.car_pooling/drawable/car_default")

        requireView().findViewById<TextView>(R.id.textDepAriLocation).text = if (DepAriLocation == null || DepAriLocation.isEmpty() || DepAriLocation.isBlank()) "Departure&Arrival Location" else DepAriLocation
        requireView().findViewById<TextView>(R.id.textDepDateTime).text = if (DepDateTime == null || DepDateTime.isEmpty() || DepDateTime.isBlank()) "Departure Date&Time" else DepDateTime
        requireView().findViewById<TextView>(R.id.textEstDuration).text = if (EstDuration == null || EstDuration.isEmpty() || EstDuration.isBlank()) "Estimated trip duration" else EstDuration
        requireView().findViewById<TextView>(R.id.textAvaSeat).text = if (AvaSeat == null || AvaSeat.isEmpty() || AvaSeat.isBlank()) "Available Seats" else AvaSeat
        requireView().findViewById<TextView>(R.id.textPrice).text = if (Price == null || Price.isEmpty() || Price.isBlank()) "Price" else Price
        requireView().findViewById<TextView>(R.id.textAdditional).text = if (Additional == null || Additional.isEmpty() || Additional.isBlank()) "Additional Information" else Additional
        requireView().findViewById<TextView>(R.id.textOptional).text = if (Optional == null || Optional.isEmpty() || Optional.isBlank()) "Optional Intermediates" else Optional
        requireView().findViewById<TextView>(R.id.textPlate).text = if (Plate == null || Plate.isEmpty() || Plate.isBlank()) "Plate Number" else Plate
        val uri_input = if (TripImageUri.toString() != "android.resource://it.polito.mad.car_pooling/drawable/car_default") TripImageUri else "android.resource://it.polito.mad.car_pooling/drawable/car_default"
        requireView().findViewById<ImageView>(R.id.imageviewCar).setImageURI(Uri.parse(uri_input))

        imageTripUri = TripImageUri.toString()
         */
    }

    private fun loadTripInFields (trip: Trip, view: View) {
        // val sharedPreferences = this.requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val depLocation = trip.depLocation // sharedPreferences.getString(getString(R.string.KeyDepAriLocation), "Departure&Arrival Location")
        val ariLocation = trip.ariLocation
        val depDate = trip.depDate // sharedPreferences.getString(getString(R.string.KeyDepDateTime), "Departure Date&Time")
        val depTime = trip.depTime
        val EstDuration = trip.estDuration // sharedPreferences.getString(getString(R.string.KeyEstDuration), "Estimated Trip Duration")
        val AvaSeat = trip.avaSeat // sharedPreferences.getString(getString(R.string.KeyAvaSeat), "Available Seats")
        val Price = trip.price // sharedPreferences.getString(getString(R.string.KeyPrice), "Price")
        val Additional = trip.additional //sharedPreferences.getString(getString(R.string.KeyAdditional), "Additional Information")
        val Optional = trip.optional // sharedPreferences.getString(getString(R.string.KeyOptional), "Optional intermediates")
        val Plate = trip.plate // sharedPreferences.getString(getString(R.string.KeyPlate), "Plate Number")
        val TripImageUri = trip.imageUri // sharedPreferences.getString(getString(R.string.KeyImageTrip), "android.resource://it.polito.mad.car_pooling/drawable/car_default")


        requireView().findViewById<TextView>(R.id.textDepLocation).text = if (depLocation.isEmpty() || depLocation.isBlank()) "Departure Location" else depLocation
        requireView().findViewById<TextView>(R.id.textAriLocation).text = if (ariLocation.isEmpty() || ariLocation.isBlank()) "Arrival Location" else ariLocation
        requireView().findViewById<TextView>(R.id.textDepDate).text = if (depDate.isEmpty() || depDate.isBlank()) "Departure Date&Time" else depDate
        requireView().findViewById<TextView>(R.id.textDepTime).text = if (depTime.isEmpty() || depTime.isBlank()) "Departure Date&Time" else depTime
        requireView().findViewById<TextView>(R.id.textEstDuration).text = if (EstDuration.isEmpty() || EstDuration.isBlank()) "Estimated trip duration" else EstDuration
        requireView().findViewById<TextView>(R.id.textAvaSeat).text = if (AvaSeat.isEmpty() || AvaSeat.isBlank()) "Available Seats" else AvaSeat
        requireView().findViewById<TextView>(R.id.textPrice).text = if (Price.isEmpty() || Price.isBlank()) "Price" else Price
        requireView().findViewById<TextView>(R.id.textAdditional).text = if (Additional.isEmpty() || Additional.isBlank()) "Additional Information" else Additional
        requireView().findViewById<TextView>(R.id.textOptional).text = if (Optional.isEmpty() || Optional.isBlank()) "Optional Intermediates" else Optional
        requireView().findViewById<TextView>(R.id.textPlate).text = if (Plate.isEmpty() || Plate.isBlank()) "Plate Number" else Plate

        requireView().findViewById<TextView>(R.id.textDepDate).setTextColor(Color.parseColor("#54150808"))
        requireView().findViewById<TextView>(R.id.textDepTime).setTextColor(Color.parseColor("#54150808"))

        val uri_input = if (TripImageUri != "android.resource://it.polito.mad.car_pooling/drawable/car_default") TripImageUri else "android.resource://it.polito.mad.car_pooling/drawable/car_default"
        requireView().findViewById<ImageView>(R.id.imageviewCar).setImageURI(Uri.parse(uri_input))

        imageTripUri = TripImageUri
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_trip_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.edit_trip -> {
                val tripId = arguments?.getString("tripId")
                //val editTripFragmentArguments = TripDetailsFragmentDirections.actionTripDetailsFragmentToTripEditFragment(tripId)
                //findNavController().navigate(editTripFragmentArguments)
                val bundle = bundleOf( "tripId" to tripId)
                findNavController().navigate(R.id.action_tripDetailsFragment_to_tripEditFragment, bundle)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}