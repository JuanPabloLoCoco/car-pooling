package it.polito.mad.car_pooling

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.models.TripList
import android.content.Intent as Intent1


class TripDetailsFragment : Fragment() {

    private lateinit var imageTripUri: String
    val args: TripEditFragmentArgs by navArgs()
    private lateinit var selectedTrip: Trip

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        imageTripUri = ""
        return inflater.inflate(R.layout.fragment_trip_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tripId = args.tripId
        var storedTripList = ModelPreferencesManager.get<TripList>(getString(R.string.KeyTripList))
        if (storedTripList == null) {
            // An imposible case
            Log.e("POLITO_ERRORS", "You are accesing an invalid id")
        } else {
            val tripList = storedTripList.tripList
            selectedTrip = tripList.get(tripId)
        }
        loadTripInFields(selectedTrip, view)

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


        requireView().findViewById<TextView>(R.id.textDepLocation).text = if (depLocation == null || depLocation.isEmpty() || depLocation.isBlank()) "Departure Location" else depLocation
        requireView().findViewById<TextView>(R.id.textAriLocation).text = if (ariLocation == null || ariLocation.isEmpty() || ariLocation.isBlank()) "Arrival Location" else ariLocation
        requireView().findViewById<TextView>(R.id.textDepDate).text = if (depDate == null || depDate.isEmpty() || depDate.isBlank()) "Departure Date&Time" else depDate
        requireView().findViewById<TextView>(R.id.textDepTime).text = if (depTime == null || depTime.isEmpty() || depTime.isBlank()) "Departure Date&Time" else depTime
        requireView().findViewById<TextView>(R.id.textEstDuration).text = if (EstDuration == null || EstDuration.isEmpty() || EstDuration.isBlank()) "Estimated trip duration" else EstDuration
        requireView().findViewById<TextView>(R.id.textAvaSeat).text = if (AvaSeat == null || AvaSeat.isEmpty() || AvaSeat.isBlank()) "Available Seats" else AvaSeat
        requireView().findViewById<TextView>(R.id.textPrice).text = if (Price == null || Price.isEmpty() || Price.isBlank()) "Price" else Price
        requireView().findViewById<TextView>(R.id.textAdditional).text = if (Additional == null || Additional.isEmpty() || Additional.isBlank()) "Additional Information" else Additional
        requireView().findViewById<TextView>(R.id.textOptional).text = if (Optional == null || Optional.isEmpty() || Optional.isBlank()) "Optional Intermediates" else Optional
        requireView().findViewById<TextView>(R.id.textPlate).text = if (Plate == null || Plate.isEmpty() || Plate.isBlank()) "Plate Number" else Plate
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
                val editTripFragmentArguments = TripDetailsFragmentDirections.actionTripDetailsFragmentToTripEditFragment(selectedTrip.id)
                findNavController().navigate(editTripFragmentArguments)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}