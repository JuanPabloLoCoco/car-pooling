package it.polito.mad.car_pooling

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.StopLocation
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.models.TripRequest
import it.polito.mad.car_pooling.models.TripRequestRating
import it.polito.mad.car_pooling.viewModels.TripViewModel
import it.polito.mad.car_pooling.viewModels.TripViewModelFactory


@Suppress("UNREACHABLE_CODE")
class TripDetailsFragment : Fragment() {

    private lateinit var imageTripUri: String
    val args: TripDetailsFragmentArgs by navArgs()
    private lateinit var selectedTrip: Trip
    private lateinit var acc_email: String
    var realAvaiableSeats: Int = 0
    var tripRequestList = listOf<TripRequestRating>()

    private lateinit var viewModel: TripViewModel
    private lateinit var viewModelFactory: TripViewModelFactory

    private lateinit var tripRequestListAdapter: TripRequestsCardAdapter
    private lateinit var interestedTrips : List<String>
    var isInterestedTrip : Boolean = false
    private val TAG = "TripDetailsFragment"

    private lateinit var optionalStopsAdapter: TripOptionalIntermediatesCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val isOwner = args.isOwner
        val tripId = args.tripId

        setHasOptionsMenu(isOwner)
        imageTripUri = ""

        viewModelFactory = TripViewModelFactory(tripId)
        viewModel = viewModelFactory.create(TripViewModel::class.java)

        return inflater.inflate(R.layout.fragment_trip_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isOwner = args.isOwner
        val tripId = args.tripId
        val sourceFragment = args.sourceFragment
        val db = FirebaseFirestore.getInstance()

        // val sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        // acc_email = sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email")!!
        acc_email = ModelPreferencesManager.get<String>(getString(R.string.keyCurrentAccount))?: "no email"
        val ratingButton = view.findViewById<Button>(R.id.ratingTripButton)
        if (sourceFragment == "interestTrips") {

            ratingButton.visibility=View.INVISIBLE
            val params: ViewGroup.LayoutParams = ratingButton.layoutParams
            params.height = 0
            ratingButton.layoutParams = params
        }
        val requestFabView = view.findViewById<FloatingActionButton>(R.id.requestTripFAB)
        val requestTitleView = view.findViewById<TextView>(R.id.requestTextView)
        val requestListRV = view.findViewById<RecyclerView>(R.id.requestRV)
        val noTripsMessageView = view.findViewById<TextView>(R.id.noTripsMessageTextView)
        val statusMessageView = view.findViewById<TextView>(R.id.requestStatusTextView)
        val driverProfileButton = view.findViewById<Button>(R.id.viewDriverProfileButton)
        val alreadyRatedTextView = view.findViewById<TextView>(R.id.alreadyRatedTextView)

        val rateTripButton = view.findViewById<Button>(R.id.ratingTripButton)

        // The rate button should only be visible if I have make the trip and the trip is Done!
        // I know that I have make the trip because my request was accepted
        // I know that the trip is Done because the Current TimeStamp is greater than the arrivalDateTime
        ratingButton.visibility = View.GONE

        requestListRV.layoutManager = LinearLayoutManager(requireContext())

        val optionalInterRV = view.findViewById<RecyclerView>(R.id.trip_optional_intermediates_RV)
        optionalInterRV.layoutManager = LinearLayoutManager(activity)
        val testList = listOf<StopLocation>()

        val noOpInterView = view.findViewById<TextView>(R.id.tripNoLocationMessageTextView)

        optionalStopsAdapter = TripOptionalIntermediatesCardAdapter(testList, requireContext())
        optionalInterRV.adapter = optionalStopsAdapter


        requestListRV.layoutManager = LinearLayoutManager(requireContext())

        viewModel.trip.observe(viewLifecycleOwner, {
            selectedTrip = it
            if (it == null) {
                Log.d("ERROR", "Trip with id ${tripId} is null")
            } else {
                // The trip is not null
                loadTripInFields(selectedTrip, view)
                val default_str_car = "android.resource://it.polito.mad.car_pooling/drawable/car_default"
                val imageView = view.findViewById<ImageView>(R.id.imageviewCar)

                val optionalStops = it.optionalStops
                if (optionalStops.isEmpty()) {
                    optionalInterRV.visibility = View.GONE
                    noOpInterView.visibility = View.VISIBLE
                } else {
                    optionalInterRV.visibility = View.VISIBLE
                    noOpInterView.visibility = View.GONE
                    optionalStopsAdapter.updateCollection(optionalStops)
                }

                tripRequestListAdapter = TripRequestsCardAdapter(tripRequestList, requireContext(), findNavController(), db, view, selectedTrip, viewModel)
                requestListRV.adapter = tripRequestListAdapter

                if (selectedTrip.hasImage != true) {
                    imageTripUri = default_str_car
                    imageView.setImageURI(Uri.parse(imageTripUri))
                } else {
                    val storage = Firebase.storage
                    val imageRef = storage.reference.child("trips/$tripId.jpg")
                    imageRef.downloadUrl.addOnSuccessListener { Uri ->
                        val image_uri = Uri.toString()
                        Glide.with(this)
                            .load(image_uri).into(imageView)
                    }
                }

                if (isOwner) {
                    // Is owner
                    // Get the request for the trip
                    viewModel.tripRequests.observe(viewLifecycleOwner, {
                        Log.d("POLITO", "Trip request list size =  ${it.size} ")
                        tripRequestList = it
                        if (tripRequestList.isEmpty()) {
                            requestListRV.visibility = View.GONE
                        }
                        tripRequestListAdapter.updateTripRequestList(it)
                        // tripRequestListAdapter.tripRequestList = tripRequestList
                        // tripRequestListAdapter.notifyDataSetChanged()

                        ratingButton.visibility=View.INVISIBLE
                        val params: ViewGroup.LayoutParams = ratingButton.layoutParams
                        params.height = 0
                        ratingButton.layoutParams = params
                    })

                    // I dont want to request the trip
                    requestFabView.visibility = View.GONE

                    // I dont want to see the status of my Request
                    statusMessageView.visibility = View.GONE

                    // I dont show the profile button
                    driverProfileButton.visibility = View.GONE

                    if (selectedTrip.arrivalDateTime < Timestamp.now()) {
                        selectedTrip.status = Trip.ENDED
                    }

                    if (selectedTrip.status == Trip.BLOCKED || selectedTrip.status == Trip.FULL || selectedTrip.status == Trip.ENDED) {
                        statusMessageView.visibility = View.VISIBLE
                        statusMessageView.setTextColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_error))
                        statusMessageView.text = when (selectedTrip.status) {
                            Trip.BLOCKED -> "The trip is blocked"
                            Trip.FULL -> "The trip is full"
                            Trip.ENDED -> "The trip is ended"
                            else -> ""
                        }
                    }

                } else {
                    // Is not owner
                    if (selectedTrip.status == Trip.BLOCKED || selectedTrip.status == Trip.FULL || selectedTrip.avaSeats == 0) {
                        requestFabView.visibility = View.GONE
                    }

                    // I show a button for going to the profile view of the driver
                    driverProfileButton.visibility = View.VISIBLE
                    driverProfileButton.setOnClickListener {
                        val action = TripDetailsFragmentDirections.actionNavTripToNavProfile(selectedTrip.owner, false)
                        findNavController().navigate(action)
                    }


                    // I can't see owners messages
                    requestTitleView.visibility = View.GONE
                    requestListRV.visibility = View.GONE
                    noTripsMessageView.visibility = View.GONE

                    // I want to get my request.
                    // If i have it, i want my status
                    // If i don't, don't show status
                    viewModel.getMyRequestWithTrip(acc_email).observe(viewLifecycleOwner, {
                        if (it == null) {
                            // If there are no information or the requests are empty i make invisible the RV
                        } else {
                            val tripRequestRating = it
                            requestFabView.visibility = View.GONE
                            val tripRequestDB = tripRequestRating.tripRequest
                            val ratingDB = tripRequestRating.rating
                            Log.d(TAG, "Request getted: ${it.tripRequest.toMap()}")
                            Log.d(TAG, "Rating from that Request ${it.rating?.toMap()}")
                            var statusCopy = tripRequestDB.status
                            var status = tripRequestDB.status
                            if (selectedTrip.arrivalDateTime < Timestamp.now()) {
                                statusCopy = TripRequest.ENDED
                                if (ratingDB == null) {
                                    ratingButton.visibility = View.VISIBLE
                                    ratingButton.setOnClickListener {
                                        val action1 = TripDetailsFragmentDirections.actionNavTripToRating(tripRequestDB.id)
                                        findNavController().navigate(action1)
                                    }
                                }
                            }
                            val message = when (statusCopy) {
                                TripRequest.ACCEPTED -> "Your request was accepted"
                                TripRequest.REJECTED -> "Your request was rejected"
                                TripRequest.PENDING -> "Your request is in pending revision"
                                TripRequest.ENDED -> "This trip has ended"
                                else -> "Your request is in pending revision"
                            }

                            statusMessageView.text = message
                            statusMessageView.visibility = View.VISIBLE
                        }
                    })

                    val trips = db.collection("Trips")
                    val trip = trips.document(tripId)
                    trip.get().addOnSuccessListener { document ->
                        if (document != null) {
                            val target_email = document.get("owner").toString()
                            val users = db.collection("Users")
                            val profile = users.document(target_email)
                            profile.addSnapshotListener { snapshot, e ->
                                if (e != null) {
                                    Log.w("tripDetailFragment", "Listen failed.", e)
                                    return@addSnapshotListener
                                }
                                if (snapshot != null && snapshot.exists()) {
                                    val hidePlate = snapshot["hidePlate"].toString().toBoolean()
                                    if (hidePlate && sourceFragment != "boughtTrips") {
                                        val view = view.findViewById<TextView>(R.id.textPlate)
                                        view.visibility = View.INVISIBLE
                                        val params: ViewGroup.LayoutParams = view.layoutParams
                                        params.height = 0
                                        view.layoutParams = params
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })

        requestFabView.setOnClickListener {
            // A new Request from the current user to the owner of the user,should be done
            var tripRequest = TripRequest(acc_email, selectedTrip.owner, selectedTrip.id)

            viewModel.createTripRequest(tripRequest)
                    .addOnSuccessListener { documentReference ->
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

        val buttonCheckLocationMap = view.findViewById<Button>(R.id.buttonCheckLocationMap)
        buttonCheckLocationMap.setOnClickListener {
            val tripStopList = mutableListOf<StopLocation>()
            if (selectedTrip.departureLocation != null) {
                tripStopList.add(selectedTrip.departureLocation!!)
            }
            tripStopList.addAll(selectedTrip.optionalStops)
            if(selectedTrip.arrivalLocation != null) {
                tripStopList.add(selectedTrip.arrivalLocation!!)
            }
            Log.d(TAG, "Stops = ${tripStopList}")
            val action = TripDetailsFragmentDirections.actionNavTripToMapFragment("checkLocation", Gson().toJson(tripStopList))
            findNavController().navigate(action)
        }


        val likeButton = activity?.findViewById<ImageButton>(R.id.likeButton)

        if (sourceFragment == "otherTrips") {
            likeButton?.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24)
            //likeButton.setBackgroundResource(R.drawable.ic_baseline_favorite_24)
        }

        interestedTrips = viewModel.getInterestedTrips(acc_email)
        Log.d("POLITO", "Interested Trips $interestedTrips . Size = ${interestedTrips.size}")
        isInterestedTrip = interestedTrips.filter { it == tripId }.isNotEmpty()
        if (isInterestedTrip) {
            if (likeButton != null) {
                likeButton.setBackgroundResource(R.drawable.ic_baseline_favorite_24)
            }
        }
        likeButton?.setOnClickListener{
            if (isInterestedTrip) {
                likeButton.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24)
                Log.d("TRIP_DETAIL_FRAGMENT", "Remove Interested")
                viewModel.removeInterestedTrip(acc_email)
                isInterestedTrip = false
            } else {
                likeButton.setBackgroundResource(R.drawable.ic_baseline_favorite_24)
                Log.d("TRIP_DETAIL_FRAGMENT", "Add Interested")
                viewModel.addInterestedTrip(acc_email)
                isInterestedTrip = true
            }
        }


    }

    override fun onPause() {
        super.onPause()
        val likeButton = requireActivity().findViewById<ImageButton>(R.id.likeButton)
        likeButton.setBackgroundDrawable(null)
    }

    private fun loadTripInFields (trip: Trip, view: View) {
        view.findViewById<TextView>(R.id.textDepLocation).text = trip.depLocation //value["depLocation"].toString()
        view.findViewById<TextView>(R.id.textAriLocation).text = trip.ariLocation //value["ariLocation"].toString()
        view.findViewById<TextView>(R.id.textEstDuration).text = trip.estDuration // ["estDuration"].toString()
        view.findViewById<TextView>(R.id.textAvaSeat).text = trip.avaSeats.toString() // ["avaSeats"].toString()
        view.findViewById<TextView>(R.id.textPrice).text = trip.price.toString() //value["price"].toString()
        view.findViewById<TextView>(R.id.textAdditional).text = trip.additional //value["additional"].toString()
        //view.findViewById<TextView>(R.id.textOptional).text = trip.optional //value["optional"].toString()
        view.findViewById<TextView>(R.id.textPlate).text = trip.plate //value["plate"].toString()
        view.findViewById<TextView>(R.id.textDepDate).text = trip.depDate //value["depDate"].toString()
        view.findViewById<TextView>(R.id.textDepTime).text = trip.depTime //value["depTime"].toString()
        //view.findViewById<TextView>(R.id.textDepDate).setTextColor(Color.parseColor("#54150808"))
        //view.findViewById<TextView>(R.id.textDepTime).setTextColor(Color.parseColor("#54150808"))

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_trip_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.edit_trip -> {
                val tripId = args.tripId
                //val editTripFragmentArguments = TripDetailsFragmentDirections.actionTripDetailsFragmentToTripEditFragment(tripId)
                //findNavController().navigate(editTripFragmentArguments)
                // val bundle = bundleOf( "tripId" to tripId)
                val action = TripDetailsFragmentDirections.actionTripDetailsFragmentToTripEditFragment(tripId!!)
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

class TripRequestsCardAdapter (var tripRequestList: List<TripRequestRating>,
val context: Context,
val navController: NavController,
val dbInstance: FirebaseFirestore,
val generalView: View,
val tripSelected: Trip,
val viewModel: TripViewModel): RecyclerView.Adapter<TripRequestsCardAdapter.TripRequestsViewHolder>() {
    private val TAG = "TripRequestsCardAdapter"
    class TripRequestsViewHolder(v: View): RecyclerView.ViewHolder (v) {
        val requesterCard = v.findViewById<CardView>(R.id.requesterTripCard)
        val requesterAvatar = v.findViewById<ImageView>(R.id.image_request_user)
        val requesterUser = v.findViewById<TextView>(R.id.request_user)
        val actionMenuView = v.findViewById<ImageButton>(R.id.imageButton_3dots)
        val ratingButton = v.findViewById<Button>(R.id.rateUserButton)
        fun unbind() {}
    }

    fun updateTripRequestList (newTripRequestRating: List<TripRequestRating>) {
        tripRequestList = newTripRequestRating
        notifyDataSetChanged()
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
        val tripRequestRating = tripRequestList[position]
        Log.d(TAG, "Trip Request Rating: Rating: ${tripRequestRating.rating}, Passenger:${tripRequestRating.passenger}")

        val selectedRequest : TripRequest = tripRequestRating.tripRequest

        // We have to add to the requestTripObject the name of the requester
        holder.requesterUser.text = tripRequestRating.passenger?.fullName ?: tripRequestRating.tripRequest.requester;

        // holder.actionMenuView.visibility = View.GONE
        holder.requesterCard.setOnClickListener {
            // Probably is better to have something here
            val action = TripDetailsFragmentDirections.actionNavTripToNavProfile(selectedRequest.requester, false)
            navController.navigate(action)

        }
        val availableSeats = tripSelected.avaSeats - tripRequestList.filter { it.tripRequest.status == TripRequest.ACCEPTED }.size

        if (selectedRequest.status == TripRequest.ACCEPTED) {
            // I will not show the menu
            holder.actionMenuView.visibility = View.GONE

            if (tripRequestRating.rating == null && tripSelected.arrivalDateTime < Timestamp.now()) {
                holder.ratingButton.visibility = View.VISIBLE
                holder.ratingButton.setOnClickListener {
                    val action = TripDetailsFragmentDirections.actionNavTripToRating(selectedRequest.id)
                    navController.navigate(action)
                }
            } else {
                holder.ratingButton.visibility = View.INVISIBLE
            }
            // You have to add a menu here for rating the users!!!

        } else {
            holder.ratingButton.visibility = View.INVISIBLE
            holder.actionMenuView.setOnClickListener{
                val popup = PopupMenu(context, holder.actionMenuView)
                // holder.actionMenuView.findViewById<Button>(R.id.ratingTripButton).visibility=View.INVISIBLE
                popup.setOnMenuItemClickListener {
                    onMenuItemClick(it, selectedRequest, availableSeats)
                }
                val inflater: MenuInflater = popup.menuInflater
                inflater.inflate(R.menu.request_trip_options_menu, popup.menu)
                popup.show()
            }
        }


        // Add the menu for Accept or Deny the request
        // Set onClickListener on button to display the menu

    }

    private fun onMenuItemClick(item: MenuItem, selectedRequest: TripRequest, trueAvaiableSeats: Int): Boolean {
        return when (item.itemId) {
            R.id.accept_request -> {

                selectedRequest.status = TripRequest.ACCEPTED
                updateTripRequest(selectedRequest, trueAvaiableSeats)
                return true
            }
            R.id.reject_request -> {
                selectedRequest.status = TripRequest.REJECTED
                updateTripRequest(selectedRequest, trueAvaiableSeats)
                true
            }
            else -> false
        }
    }

    private fun updateTripRequest(tripRequest: TripRequest, trueAvaiableSeats: Int): Unit {
        viewModel.updateTripRequest(tripRequest)
                .addOnSuccessListener {
                    // Update the trip if it's full
                }
                .addOnFailureListener {
                    Snackbar.make(generalView, "An error happen while updating the request", Snackbar.LENGTH_SHORT)
                        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                        .show()
                }
    }
}

class TripOptionalIntermediatesCardAdapter (var tripOptionalIntermediatesList: List<StopLocation>,
                                            val context: Context) :
    RecyclerView.Adapter<TripOptionalIntermediatesCardAdapter.TripOptionalIntermediatesViewHolder>() {
    class TripOptionalIntermediatesViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val optionalInterText = v.findViewById<TextView>(R.id.optional_intermediates_text)
        val deleteCardImageButton = v.findViewById<ImageButton>(R.id.imageButton_delete_card)
        fun bind(t: String) {}
        fun unbind() {}
    }

    fun updateCollection (newStopsLocationList: List<StopLocation>) {
        tripOptionalIntermediatesList = newStopsLocationList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return tripOptionalIntermediatesList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripOptionalIntermediatesViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.optional_intermediates_card, parent, false)
        return TripOptionalIntermediatesViewHolder(v)
    }

    override fun onViewRecycled(holder: TripOptionalIntermediatesViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun onBindViewHolder(holder: TripOptionalIntermediatesViewHolder, position: Int) {
        val selectedRequest: StopLocation = tripOptionalIntermediatesList[position]
        holder.optionalInterText.text = selectedRequest.fullAddress
        holder.deleteCardImageButton.setVisibility(View.GONE)
    }
}