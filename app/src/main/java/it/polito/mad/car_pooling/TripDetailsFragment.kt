package it.polito.mad.car_pooling

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.models.Profile
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
        val tripId = args.tripId
        val db = FirebaseFirestore.getInstance()

        val sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        acc_email = sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email")!!

        val requestFabView = view.findViewById<FloatingActionButton>(R.id.requestTripFAB)
        val requestTitleView = view.findViewById<TextView>(R.id.requestTextView)
        val requestListRV = view.findViewById<RecyclerView>(R.id.requestRV)
        val noTripsMessageView = view.findViewById<TextView>(R.id.noTripsMessageTextView)
        val statusMessageView = view.findViewById<TextView>(R.id.requestStatusTextView)
        val tripRequestList = mutableListOf<TripRequest>()

        requestListRV.layoutManager = LinearLayoutManager(requireContext())
        if (args.isOwner) {
            // If i am the owner i cant request my own trip
            requestFabView.visibility = View.GONE
            statusMessageView.visibility = View.GONE
            // requestListRV.visibility = View.GONE
            db.collection(TripRequest.DATA_COLLECTION)
                .whereEqualTo("tripId", tripId)
                .whereIn("status", mutableListOf(TripRequest.ACCEPTED, TripRequest.PENDING))
                .addSnapshotListener { documents, error ->
                    if (error != null) {
                        Log.w("ERRORS", "Listen failed.", error)
                        throw error
                        return@addSnapshotListener
                    }
                    if (documents == null || documents.isEmpty) {
                        // If there are no information or the requests are empty i make invisible the RV
                        requestListRV.visibility = View.GONE
                    } else {
                        noTripsMessageView.visibility = View.GONE
                        // Limpiamos la lista
                        tripRequestList.clear()

                        for (document in documents) {
                            val requester = document.data["requester"].toString()
                            val tripOwner = document.data["tripOwner"].toString()
                            val tripId = document.data["tripId"].toString()
                            val creationTS = document.get("creationTimestamp") as Timestamp //.data["creationTimestamp"]
                            val updatedTS = document.get("updateTimestamp") as Timestamp //.toString()
                            val status = document.data["status"].toString()
                            val tripRequestId = document.id
                            val tripRequestToAdd = TripRequest(requester, tripOwner, tripId,  creationTS, updatedTS, status, tripRequestId)
                            Log.d("POLITO", "TripRequest id: ${tripRequestId}")
                            tripRequestList.add(tripRequestToAdd)
                        }
                        Log.d("POLITO", "Item 0: ${tripRequestList.get(0)}")
                        val tripRequestListAdapter = TripRequestsCardAdapter(tripRequestList, requireContext(), findNavController(), db, view)
                        requestListRV.adapter = tripRequestListAdapter
                    }

                }
        } else {
            // If I am not the owner I can't see the
            view.findViewById<TextView>(R.id.textPlate).visibility = View.GONE
            requestTitleView.visibility = View.GONE
            requestListRV.visibility = View.GONE
            noTripsMessageView.visibility = View.GONE
            db.collection(TripRequest.DATA_COLLECTION)
                .whereEqualTo("tripId", tripId)
                .whereEqualTo("requester", acc_email)
                .addSnapshotListener { documents, error ->
                    if (error != null) {
                        Log.w("ERRORS", "Listen failed.", error)
                        throw error
                        return@addSnapshotListener
                    }
                    if (documents == null || documents.isEmpty()) {
                        // If there are no information or the requests are empty i make invisible the RV

                    } else {
                        requestFabView.visibility = View.GONE
                        val status = documents.documents.get(0)["status"].toString()
                        statusMessageView.visibility = View.VISIBLE
                        val message = when (status) {
                            TripRequest.ACCEPTED -> "Your request was accepted"
                            TripRequest.REJECTED -> "Your request was rejected"
                            TripRequest.PENDING -> "Your request is in pending revision"
                            else -> "Your request is in pending revision"
                        }
                        statusMessageView.text = message
                    }
                }
        }
        db.collection("Trips")
            .document(tripId.toString())
            .addSnapshotListener { value, error ->
                if (error != null) throw error
                if (value != null) {
                    selectedTrip = Trip(tripId)
                    selectedTrip.depLocation = value["depLocation"].toString()
                    selectedTrip.ariLocation = value["ariLocation"].toString()
                    selectedTrip.estDuration = value["estDuration"].toString()
                    selectedTrip.avaSeats = (value["avaSeats"] as Long).toInt()
                    selectedTrip.price = value["price"] as Double
                    selectedTrip.additional = value["additional"].toString()
                    selectedTrip.optional = value["optional"].toString()
                    selectedTrip.plate = value["plate"].toString()
                    selectedTrip.depDate = value["depDate"].toString()
                    selectedTrip.depTime = value["depTime"].toString()
                    selectedTrip.owner = value["owner"].toString()
                    selectedTrip.status = value[Trip.FIELD_STATUS].toString()


                    if (selectedTrip.status == Trip.BLOCKED || selectedTrip.status == Trip.FULL) {
                        if (!args.isOwner) {
                            requestFabView.visibility = View.GONE
                            statusMessageView.visibility = View.GONE
                        }
                        statusMessageView.visibility = View.VISIBLE
                        statusMessageView.setTextColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_error))
                        statusMessageView.text = if (selectedTrip.status == Trip.BLOCKED) "The trip is blocked" else "The trip is full"
                    }
                    
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
                        val storage = Firebase.storage
                        val imageRef = storage.reference.child("trips/$tripId.jpg")
                        imageRef.downloadUrl.addOnSuccessListener { Uri ->
                            val image_uri = Uri.toString()
                            Glide.with(this).load(image_uri).into(imageView)
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



        requestFabView.setOnClickListener {
            // A new Request from the current user to the owner of the user,should be done
            var tripRequest = TripRequest(acc_email, selectedTrip.owner, selectedTrip.id)

            val newTripRequest = mapOf(
                    "requester" to tripRequest.requester,
                    "tripOwner" to tripRequest.requester,
                    "tripId" to tripRequest.tripId,
                    "creationTimestamp" to tripRequest.creationTimestamp,
                    "updateTimestamp" to tripRequest.updateTimestamp,
                    "status" to tripRequest.status)
            db.collection("TripsRequests").add(newTripRequest)
                    .addOnSuccessListener { documentReference ->
                        // Show the message
                        // Return to main menu
                        val message = "Trip requested!"
                        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
                                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                                .show()

                    }
                    .addOnFailureListener { e ->
                        // Show error
                        // Return to main menu
                        val message = "A problem occurs while creating the request"
                        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
                                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                                .setBackgroundTint(Color.RED)
                                .show()
                        Log.w("POLITO", "Error adding document", e)
                    }
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
        val AvaSeat = trip.avaSeats // sharedPreferences.getString(getString(R.string.KeyAvaSeat), "Available Seats")
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
        requireView().findViewById<TextView>(R.id.textAvaSeat).text = AvaSeat.toString()
        requireView().findViewById<TextView>(R.id.textPrice).text = Price.toString()
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

class TripRequestsCardAdapter (val tripRequestList: List<TripRequest>,
val context: Context,
val navController: NavController,
val dbInstance: FirebaseFirestore,
val generalView: View): RecyclerView.Adapter<TripRequestsCardAdapter.TripRequestsViewHolder>() {
    class TripRequestsViewHolder(v: View): RecyclerView.ViewHolder (v) {
        val requesterCard = v.findViewById<CardView>(R.id.requesterTripCard)
        val requesterAvatar = v.findViewById<ImageView>(R.id.image_request_user)
        val requesterUser = v.findViewById<TextView>(R.id.request_user)
        val actionMenuView = v.findViewById<ImageButton>(R.id.imageButton_3dots)
        fun bind(t: TripRequest) {}
        fun unbind() {}
    }

    override fun getItemCount(): Int {
        return tripRequestList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripRequestsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.request_user_profile_card, parent, false)
        return TripRequestsViewHolder(v)
    }

    override fun onViewRecycled(holder: TripRequestsViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun onBindViewHolder(holder: TripRequestsViewHolder, position: Int) {
        val selectedRequest : TripRequest = tripRequestList[position]
        // We have to add to the requestTripObject the name of the requester
        holder.requesterUser.text = selectedRequest.requester;

        // holder.actionMenuView.visibility = View.GONE
        holder.requesterCard.setOnClickListener {

            // Probably is better to have something here
            val action = TripDetailsFragmentDirections.actionNavTripToNavProfile(selectedRequest.requester, false)
            navController.navigate(action)
        }

        if (selectedRequest.status == TripRequest.ACCEPTED) {
            // I will not show the menu
            holder.actionMenuView.visibility = View.GONE
        } else {
            holder.actionMenuView.setOnClickListener{
                val popup = PopupMenu(context, holder.actionMenuView)
                popup.setOnMenuItemClickListener {
                    onMenuItemClick(it, selectedRequest)
                }
                val inflater: MenuInflater = popup.menuInflater
                inflater.inflate(R.menu.request_trip_options_menu, popup.menu)
                popup.show()
            }
        }


        // Add the menu for Accept or Deny the request
        // Set onClickListener on button to display the menu

    }

    private fun onMenuItemClick(item: MenuItem, selectedRequest: TripRequest): Boolean {
        return when (item.itemId) {
            R.id.accept_request -> {
                selectedRequest.status = TripRequest.ACCEPTED
                updateTripRequest(selectedRequest)
                //Snackbar.make(generalView, "Request accepted ${selectedRequest.tripId}", Snackbar.LENGTH_SHORT)
                //        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                //        .show()
                return true
            }
            R.id.reject_request -> {
                selectedRequest.status = TripRequest.REJECTED
                updateTripRequest(selectedRequest)
                //Snackbar.make(generalView, "Request Rejected", Snackbar.LENGTH_SHORT)
                //        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                //        .show()
                true
            }
            else -> false
        }
    }

    private fun updateTripRequest(tripRequest: TripRequest): Unit {
        dbInstance.collection(TripRequest.DATA_COLLECTION).document(tripRequest.id)
            .update(mapOf(
                "status" to tripRequest.status,
                "updateTimestamp" to Timestamp(Date())
            ))
            .addOnSuccessListener {
                if (tripRequest.status == TripRequest.ACCEPTED){
                    Snackbar.make(generalView, "Request accepted", Snackbar.LENGTH_SHORT)
                            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                            .show()
                }
            }
            .addOnFailureListener {
                Snackbar.make(generalView, "An error happen while updating the request", Snackbar.LENGTH_SHORT)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                    .show()
            }
    }
}