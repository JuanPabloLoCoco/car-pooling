package it.polito.mad.car_pooling

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.StopLocation
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.models.TripRequest
import java.io.*
import java.util.*

private val REQUEST_IMAGE_CAPTURE = 1
private val REQUEST_OPEN_GALLERY = 2

private var imageUri: Uri? = null
private var photoFile: File? = null

class TripEditFragment : Fragment() {
    val args: TripEditFragmentArgs by navArgs()

    lateinit var selectedTrip: Trip
    lateinit var check_status: String
    lateinit var adapter: OptionalIntermediatesCardAdapter
    lateinit var locationList : MutableList<StopLocation>

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater!!.inflate(R.layout.fragment_trip_edit, container, false)

        // Get the arguments
        val tripId = arguments?.getString("tripId")
        val tripNewOrNot = arguments?.getString("newOrOld")

        // Instanciate the database
        val db = FirebaseFirestore.getInstance()

        // Get the views
        val editDepLocation = view.findViewById<TextInputLayout>(R.id.textEditDepLocation)
        val editAriLocation = view.findViewById<TextInputLayout>(R.id.textEditAriLocation)
        val editEstDuration = view.findViewById<TextInputLayout>(R.id.textEditEstDuration)
        val editAvaSeat = view.findViewById<TextInputLayout>(R.id.textEditAvaSeat)
        val editPrice = view.findViewById<TextInputLayout>(R.id.textEditPrice)
        val editAdditional = view.findViewById<TextInputLayout>(R.id.textEditAdditional)
        val editOptional = view.findViewById<TextInputLayout>(R.id.textEditOptional)
        val editPlate = view.findViewById<TextInputLayout>(R.id.textEditPlate)
        val editimageView = view.findViewById<ImageView>(R.id.imageEditCar)
        val editDepDate = view.findViewById<TextView>(R.id.textEditDepDate)
        val editDepTime = view.findViewById<TextView>(R.id.textEditDepTime)
        val blockTripButton = view.findViewById<Button>(R.id.blockTripButton)


        var input_idx : String
        if (tripNewOrNot == "new") {
            (activity as AppCompatActivity).supportActionBar?.title = "Create new trip"
            check_status = "new"
            input_idx = "default_trip"
            blockTripButton.visibility = View.GONE
            locationList = emptyList<StopLocation>().toMutableList()
        } else {
            check_status = "old"
            input_idx = tripId.toString()
            blockTripButton.setOnClickListener {
                // Change status of trip to BLOCK
                // Change all the request that have status PENDING -> REJECTED
                // Do not display the requestFAB on the button
                blockTripButton.isEnabled = false
                db.collection(Trip.DATA_COLLECTION)
                    .document(tripId!!)
                    .update(
                            mapOf(Trip.FIELD_STATUS to Trip.BLOCKED)
                    )
                    .addOnSuccessListener {
                        // Change all the request that have status PENDING -> REJECT
                        db.collection(TripRequest.DATA_COLLECTION)
                            .whereEqualTo("status", TripRequest.PENDING)
                            .whereEqualTo("tripId", tripId)
                            .get()
                            .addOnSuccessListener {documents ->
                                for (document in documents) {
                                    val tripRequestId = document.id
                                    db.collection(TripRequest.DATA_COLLECTION)
                                        .document(tripRequestId)
                                        .update(mapOf("status" to TripRequest.REJECTED))
                                }
                                Snackbar.make(view, "The trip was succesfully blocked", Snackbar.LENGTH_SHORT)
                                        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                                        .show()
                            }

                    }
                    .addOnFailureListener {
                        blockTripButton.isEnabled = true
                        Snackbar.make(view, "An error happen while updating the trip", Snackbar.LENGTH_SHORT)
                                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.design_default_color_error))
                                .show()
                    }
                Log.d("POLITO", "We need to block this trip")
            }
        }

        /*if (tripId == Trip.NEW_TRIP_ID) {
            selectedTrip = Trip(Trip.NEW_TRIP_ID)
            (activity as AppCompatActivity).supportActionBar?.title = "Create new trip"
        } else {
            var storedTripList = ModelPreferencesManager.get<TripList>(getString(R.string.KeyTripList))
            if (storedTripList == null) {
                // An imposible case
                Log.e("POLITO_ERRORS", "You are accesing an invalid id")
            } else {
                val tripList = storedTripList.tripList
                selectedTrip = tripList.get(tripId)
            }
        } */


        // Get the trip data
        val trips = db.collection("Trips")
        val default_str_car = "android.resource://it.polito.mad.car_pooling/drawable/car_default"
        if (input_idx == "default_trip"){
            /*editDepLocation.editText?.setText("Departure Location")
            editAriLocation.editText?.setText("Arrival Location")
            editEstDuration.editText?.setText("Estimated Duration")
            editAdditional.editText?.setText("Additional Information")
            editOptional.editText?.setText("Optional Intermediates")
            editPlate.editText?.setText("Plate Number")*/
            editAvaSeat.editText?.setText("0")
            editPrice.editText?.setText("0")
            editDepDate.text = "Departure Date"
            editDepTime.text = "Time"
            //editDepDate.setTextColor(Color.parseColor("#54150808"))
            //editDepTime.setTextColor(Color.parseColor("#54150808"))
            editimageView.setImageURI(Uri.parse(default_str_car))
        } else {
            trips.document(input_idx).addSnapshotListener { value, error ->
                if (error != null) throw error
                if (value != null) {
                    editDepLocation.editText?.setText(value["depLocation"].toString())
                    editAriLocation.editText?.setText(value["ariLocation"].toString())
                    editEstDuration.editText?.setText(value["estDuration"].toString())
                    editAvaSeat.editText?.setText(value["avaSeats"].toString())
                    editPrice.editText?.setText(value["price"].toString())
                    editAdditional.editText?.setText(value["additional"].toString())
                    editOptional.editText?.setText(value["optional"].toString())
                    editPlate.editText?.setText(value["plate"].toString())
                    editDepDate.text = value["depDate"].toString()
                    editDepTime.text = value["depTime"].toString()
                    editDepDate.setTextColor(Color.parseColor("#54150808"))
                    editDepTime.setTextColor(Color.parseColor("#54150808"))

                    val storage = Firebase.storage
                    /*val localFile = File.createTempFile("my_trip", "jpg")
                    storage.reference.child("trips/$input_idx.jpg").getFile(localFile).addOnSuccessListener {
                        val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                        editimageView.setImageBitmap(bitmap)
                    }*/
                    val imageRef = storage.reference.child("trips/$input_idx.jpg")
                    imageRef.downloadUrl.addOnSuccessListener { Uri ->
                        val image_uri = Uri.toString()
                        Glide.with(this).load(image_uri).into(editimageView)

                    }
                }
            }
        }

        val imageButton = view.findViewById<ImageButton>(R.id.imageButton2)
        registerForContextMenu(imageButton)
        setHasOptionsMenu(true)

        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            editDepDate.text = SimpleDateFormat("dd.MM.yyyy").format(cal.time)
        }
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            editDepTime.text = SimpleDateFormat("HH:mm").format(cal.time)
        }
        editDepDate.setOnClickListener {
            DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        editDepTime.setOnClickListener {
            TimePickerDialog(requireContext(), timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        /*val imageButtonMapDep = view.findViewById<ImageButton>(R.id.mapDepImageButton)
        imageButtonMapDep.setOnClickListener{
            val emptyList : List<String> = emptyList()
            val action = TripEditFragmentDirections.actionTripEditFragmentToMapFragment("departure")
            findNavController().navigate(action)
            //findNavController().navigate(R.id.action_tripEditFragment_to_mapFragment)
        }
        val imageButtonMapArr = view.findViewById<ImageButton>(R.id.mapArrImageButton)
        imageButtonMapArr.setOnClickListener{
            val action = TripEditFragmentDirections.actionTripEditFragmentToMapFragment("arrival")
            findNavController().navigate(action)
            //findNavController().navigate(R.id.action_tripEditFragment_to_mapFragment)
        }*/
        /*val imageButtonMapAddInter = view.findViewById<ImageButton>(R.id.mapAddInterImageButton)
        imageButtonMapAddInter.setOnClickListener{
            val action = TripEditFragmentDirections.actionTripEditFragmentToMapFragment("addInter")
            findNavController().navigate(action)
            //findNavController().navigate(R.id.action_tripEditFragment_to_mapFragment)
        }*/

        val optionalInterRV = view.findViewById<RecyclerView>(R.id.optional_intermediates_RV)
        optionalInterRV.layoutManager = LinearLayoutManager(requireContext())
        //val testList = mutableListOf<String>()
        val noOpInterView = view.findViewById<TextView>(R.id.noLocationMessageTextView)
        if (locationList.size == 0) {
            optionalInterRV.visibility = View.GONE
            noOpInterView.visibility = View.VISIBLE
        } else {
            optionalInterRV.visibility = View.VISIBLE
            noOpInterView.visibility = View.GONE
        }

        val adapter = OptionalIntermediatesCardAdapter(locationList, requireContext(), view)
        optionalInterRV.adapter = adapter
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("location")?.observe(
            viewLifecycleOwner) { result ->
            Log.d("trip!!!!!!!!", "${locationList.size}")
            val randomNum = Random().nextInt(100)
            val type = object: TypeToken<MutableList<StopLocation>>(){}.type
            val jsonList = Gson().fromJson<MutableList<StopLocation>>(result, type)
            Log.d("trip!!!!!!!!", "${jsonList}")
            Log.d("trip!!!!!!!!", "${jsonList[0]}")
            Log.d("trip!!!!!!!!", "${jsonList[0].address}")
            for (i in 0..jsonList.size - 1) {
                val insertLocation = StopLocation(jsonList[i].fullAddress)
                insertLocation.address = jsonList[i].address
                insertLocation.city = jsonList[i].city
                insertLocation.country = jsonList[i].country
                insertLocation.latitude = jsonList[i].latitude
                insertLocation.longitude = jsonList[i].longitude
                locationList.add(insertLocation)
            }
            Log.d("trip!!!!!!!!", "${locationList}")
            Log.d("trip!!!!!!!!", "${locationList.size}")
            val adapter = OptionalIntermediatesCardAdapter(locationList, requireContext(), view)
            optionalInterRV.adapter = adapter
            if (locationList.size == 0) {
                optionalInterRV.visibility = View.GONE
                noOpInterView.visibility = View.VISIBLE
            } else {
                optionalInterRV.visibility = View.VISIBLE
                noOpInterView.visibility = View.GONE
            }
        }

        val addOptionalIntermediatesButton = view.findViewById<ImageView>(R.id.mapAddInterImageButtonTest)
        addOptionalIntermediatesButton.setOnClickListener {
            val action = TripEditFragmentDirections.actionTripEditFragmentToMapFragment("addInter", Gson().toJson(locationList))
            Log.d("tripEdit!!!!!!!!", "${Gson().toJson(locationList)}")
            findNavController().navigate(action)
            //val randomNum = Random().nextInt(100)
            //testList.add(randomNum.toString())
        }

        return view
    }

    private fun loadDataInFields(trip: Trip, view: View) {
        /*
        val editDepLocation = view.findViewById<TextInputLayout>(R.id.textEditDepLocation)
        val editAriLocation = view.findViewById<TextInputLayout>(R.id.textEditAriLocation)
        val editDepDate = view.findViewById<TextView>(R.id.textEditDepDate)
        val editDepTime = view.findViewById<TextView>(R.id.textEditDepTime)
        val editEstDuration = view.findViewById<TextInputLayout>(R.id.textEditEstDuration)
        val editAvaSeat = view.findViewById<TextInputLayout>(R.id.textEditAvaSeat)
        val editPrice = view.findViewById<TextInputLayout>(R.id.textEditPrice)
        val editAdditional = view.findViewById<TextInputLayout>(R.id.textEditAdditional)
        val editOptional = view.findViewById<TextInputLayout>(R.id.textEditOptional)
        val editPlate = view.findViewById<TextInputLayout>(R.id.textEditPlate)
        val editimageView = view.findViewById<ImageView>(R.id.imageEditCar)

        editDepLocation.editText?.setText(trip.depLocation)
        editAriLocation.editText?.setText(trip.ariLocation)
        //editDepDateTime.hint= trip.depDateTime
        editDepDate.text = if (editDepDate.text == "Departure Date") "Departure Date" else trip.depDate
        editDepDate.setTextColor(Color.parseColor("#9E150808"))
        editDepTime.text = if (editDepTime.text == "Time") "Time" else trip.depTime
        editDepTime.setTextColor(Color.parseColor("#9E150808"))
        editEstDuration.editText?.setText(trip.estDuration)
        editAvaSeat.editText?.setText(trip.avaSeat)
        editPrice.editText?.setText(trip.price)
        editAdditional.editText?.setText(trip.additional)
        editOptional.editText?.setText(trip.optional)
        editPlate.editText?.setText(trip.plate)
        val TripImageUri = trip.imageUri //sharedPreferences.getString(getString(R.string.KeyImageTrip), "android.resource://it.polito.mad.car_pooling/drawable/car_default")

        val uri_input = if (TripImageUri.toString() == "android.resource://it.polito.mad.car_pooling/drawable/car_default"
                || TripImageUri.toString().isEmpty()) "android.resource://it.polito.mad.car_pooling/drawable/car_default" else TripImageUri
        imageUri = Uri.parse(uri_input)
        editimageView.setImageURI(imageUri)
         */
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("IMAGE_URI", imageUri.toString())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val savedImageUri = savedInstanceState?.getString("IMAGE_URI")
        if (savedImageUri != null) {
            imageUri = Uri.parse(savedImageUri)
            requireView().findViewById<ImageView>(R.id.imageEditCar).setImageURI(imageUri)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save_trip_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_Trip -> {
                val editDepLocation = requireView().findViewById<TextInputLayout>(R.id.textEditDepLocation)
                val editAriLocation = requireView().findViewById<TextInputLayout>(R.id.textEditAriLocation)
                val editEstDuration = requireView().findViewById<TextInputLayout>(R.id.textEditEstDuration)
                val editAvaSeat = requireView().findViewById<TextInputLayout>(R.id.textEditAvaSeat)
                val editPrice = requireView().findViewById<TextInputLayout>(R.id.textEditPrice)
                val editAdditional = requireView().findViewById<TextInputLayout>(R.id.textEditAdditional)
                val editOptional = requireView().findViewById<TextInputLayout>(R.id.textEditOptional)
                val editPlate = requireView().findViewById<TextInputLayout>(R.id.textEditPlate)
                val editDepDate = requireView().findViewById<TextView>(R.id.textEditDepDate)
                val editDepTime = requireView().findViewById<TextView>(R.id.textEditDepTime)
                val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                val acc_email = sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email")

                val db = FirebaseFirestore.getInstance()
                val trips = db.collection("Trips")
                val tripId = arguments?.getString("tripId")
                val tripNewOrNot = arguments?.getString("newOrOld")

                val newTrip = Trip("0")
                newTrip.depLocation = editDepLocation.editText?.text.toString()
                newTrip.ariLocation = editAriLocation.editText?.text.toString()
                newTrip.estDuration = editEstDuration.editText?.text.toString()
                newTrip.avaSeats = editAvaSeat.editText?.text.toString().toInt()
                newTrip.price = editPrice.editText?.text.toString().toDouble()
                newTrip.additional = editAdditional.editText?.text.toString()
                newTrip.optional = editOptional.editText?.text.toString()
                newTrip.plate = editPlate.editText?.text.toString()
                newTrip.depDate = editDepDate.text.toString()
                newTrip.depTime = editDepTime.text.toString()
                newTrip.imageUri = "yes"
                newTrip.owner = acc_email.toString()

                /*
                val input_data = hashMapOf(
                    "depLocation" to editDepLocation.editText?.text.toString(),
                    "ariLocation" to editAriLocation.editText?.text.toString(),
                    "estDuration" to editEstDuration.editText?.text.toString(),
                    "avaSeats" to editAvaSeat.editText?.text.toString(),
                    "price" to ,
                    "additional" to editAdditional.editText?.text.toString(),
                    "optional" to editOptional.editText?.text.toString(),
                    "plate" to editPlate.editText?.text.toString(),
                    "depDate" to editDepDate.text.toString(),
                    "depTime" to editDepTime.text.toString(),
                        //"image_uri" to imageUri.toString(),
                    "image_uri" to "yes",
                    "owner" to acc_email.toString()
                )
                 */

                if (tripNewOrNot == "new") {
                    trips.add(newTrip)
                        .addOnSuccessListener { documentReference ->
                            saveImageToFirebaseStorage(documentReference.id)
                        }
                } else {
                    trips.document(tripId.toString())
                            .set(newTrip)
                    saveImageToFirebaseStorage(tripId.toString())
                }
                /*
                db.collection("Trips")
                  .document(tripId.toString())
                  .set(mapOf(
                        "depLocation" to editDepLocation.editText?.text.toString(),
                        "ariLocation" to editAriLocation.editText?.text.toString(),
                        "estDuration" to editEstDuration.editText?.text.toString(),
                        "avaSeats" to editAvaSeat.editText?.text.toString(),
                        "price" to editPrice.editText?.text.toString(),
                        "additional" to editAdditional.editText?.text.toString(),
                        "optional" to editOptional.editText?.text.toString(),
                        "plate" to editPlate.editText?.text.toString(),
                        "depDate" to editDepDate.text.toString(),
                        "depTime" to editDepTime.text.toString(),
                        "image_uri" to imageUri.toString(),
                        "owner" to acc_email.toString()
                ))*/

                //saveDataInTrip()
                /*var storedTripList = ModelPreferencesManager.get<TripList>(getString(R.string.KeyTripList))
                var tripList: List<Trip> = mutableListOf()
                var mutableTripList : MutableList<Trip>
                //var tripList = ModelPreferencesManager.get<TripList>(getString(R.string.KeyTripList))
                if (storedTripList != null) {
                    tripList = storedTripList.tripList;
                }
                var tripCreated : Boolean = false
                mutableTripList = tripList.toMutableList()
                if (selectedTrip.id == Trip.NEW_TRIP_ID) {
                    selectedTrip.id = tripList.size
                    tripCreated = true
                    mutableTripList.add(selectedTrip)
                } else {
                    mutableTripList[selectedTrip.id] = selectedTrip
                    // Toast.makeText(requireContext(), "Save edited trip. Still not implemented", Toast.LENGTH_LONG).show()
                }

                ModelPreferencesManager.put(TripList(mutableTripList.toList()), getString(R.string.KeyTripList))*/

                val message: String =
                    if (check_status == "new") getString(R.string.tripCreatedSucces) else getString(
                        R.string.tripEditedSucces
                    )
                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                    .show()

                // val tripDetailArguments = bundleOf(getString(R.string.KeyDetailTripId) to selectedTrip.id)
                // val args = TripEditFragmentDirections.actionTripEditFragmentToTripDetailsFragment(selectedTrip.id)
                /*if ((activity as AppCompatActivity).supportActionBar?.title == "Create new trip") {
                    val bundle = bundleOf("tripCount" to num_input)
                    findNavController().navigate(R.id.action_tripEditFragment_to_nav_list_trip, bundle)
                } else {
                    findNavController().popBackStack()
                }*/
                findNavController().popBackStack()
                /*val bundle = bundleOf("tripId" to num_input)
                if (check_status == "new") {
                    findNavController().navigate(R.id.action_tripEditFragment_to_nav_list_trip, bundle)
                } else {
                    findNavController().navigate(R.id.action_tripEditFragment_to_tripDetailsFragment, bundle)
                    //findNavController().popBackStack()
                }*/
                //.navigate(R.id.nav_trip, tripDetailArguments)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun saveImageToFirebaseStorage(imageId: String){
        val imageView = requireView().findViewById<ImageView>(R.id.imageEditCar)
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val storage = Firebase.storage
        val storageRef = storage.reference
        storageRef.child("trips/$imageId.jpg").putBytes(data)
    }

    fun saveNewTrip(){
        val tripList = ModelPreferencesManager.get<ArrayList<Trip>>(getString(R.string.KeyTripList))

    }

    fun saveDataInTrip () {
        /*
        var editDepLocation = requireView().findViewById<TextInputLayout>(R.id.textEditDepLocation).editText?.text.toString()
        var editAriLocation = requireView().findViewById<TextInputLayout>(R.id.textEditAriLocation).editText?.text.toString()
        var editDepDate = requireView().findViewById<TextView>(R.id.textEditDepDate).text.toString()
        var editDepTime = requireView().findViewById<TextView>(R.id.textEditDepTime).text.toString()
        var editEstDuration = requireView().findViewById<TextInputLayout>(R.id.textEditEstDuration).editText?.text.toString()
        var editAvaSeat = requireView().findViewById<TextInputLayout>(R.id.textEditAvaSeat).editText?.text.toString()
        var editPrice = requireView().findViewById<TextInputLayout>(R.id.textEditPrice).editText?.text.toString()
        var editAdditional = requireView().findViewById<TextInputLayout>(R.id.textEditAdditional).editText?.text.toString()
        var editOptional = requireView().findViewById<TextInputLayout>(R.id.textEditOptional).editText?.text.toString()
        var editPlate = requireView().findViewById<TextInputLayout>(R.id.textEditPlate).editText?.text.toString()

        selectedTrip.depLocation = editDepLocation//if (editDepAriLocation == storeDepAriLocation || editDepAriLocation.isEmpty()) storeDepAriLocation else editDepAriLocation
        selectedTrip.ariLocation = editAriLocation
        selectedTrip.depDate = editDepDate//if (editDepDateTime == storeDepDateTime || editDepDateTime.isEmpty()) storeDepDateTime else editDepDateTime
        selectedTrip.depTime = editDepTime
        selectedTrip.estDuration = editEstDuration//if (editEstDuration == storeEstDuration || editEstDuration.isEmpty()) storeEstDuration else editEstDuration
        selectedTrip.avaSeat = editAvaSeat//if (editAvaSeat == storeAvaSeat || editAvaSeat.isEmpty()) storeAvaSeat else editAvaSeat
        selectedTrip.price = editPrice//if (editPrice == storePrice || editPrice.isEmpty()) storePrice else editPrice
        selectedTrip.additional =editAdditional //if (editAdditional == storeAdditional || editAdditional.isEmpty()) storeAdditional else editAdditional
        selectedTrip.optional = editOptional//if (editOptional == storeOptional || editOptional.isEmpty()) storeOptional else editOptional
        selectedTrip.plate = editPlate//if (editPlate == storePlate || editPlate.isEmpty()) storePlate else editPlate
        selectedTrip.imageUri = imageUri.toString()
         */
    }

    private fun writeSharedPreferences() {
        val sharedPreferences = this.requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        with(sharedPreferences.edit()) {

            commit()
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(Menu.NONE, R.id.select_image_trip, Menu.NONE, "select an image")
        menu.add(Menu.NONE, R.id.take_photo_trip, Menu.NONE, "take a picture")
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageView = requireView().findViewById<ImageView>(R.id.imageEditCar)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            if (photoFile != null) {
                imageUri = Uri.fromFile(photoFile)
                imageView.setImageURI(imageUri)
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_OPEN_GALLERY) {
            imageUri = data?.data
            val source = imageUri?.let { ImageDecoder.createSource(this.requireContext().contentResolver, it) }
            imageUri = bitmapToFile(ImageDecoder.decodeBitmap(source!!))
            imageView.setImageURI(imageUri)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.select_image_trip -> {
                openGalleryClick()
                true
            }
            R.id.take_photo_trip -> {
                openCameraClick()
                true
            }
            else -> {
                true
            }
        }
    }

    private fun openGalleryClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissionsGallery = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissionsGallery, REQUEST_OPEN_GALLERY)
            } else {
                openGallery()
            }
        } else {
            openGallery()
        }

    }

    private fun openGallery () {
        val openGalleryIntent = Intent(Intent.ACTION_PICK)
        openGalleryIntent.type = "image/*"
        startActivityForResult(openGalleryIntent, REQUEST_OPEN_GALLERY)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("WrongConstant")
    private fun openCameraClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                val permissionsCamera = arrayOf(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permissionsCamera, REQUEST_IMAGE_CAPTURE)
            } else {
                openCamera()
            }
        } else {
            openCamera()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun openCamera () {
        val takenPictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = createImageFile()
        val fileProvider = FileProvider.getUriForFile(requireContext(), "it.polito.mad.car_pooling.fileprovider", photoFile!!)
        takenPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

        startActivityForResult(takenPictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun bitmapToFile(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(requireContext())

        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        file = File(file,"JPEG_${timeStamp}.jpg")

        try{
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Snackbar.make(requireView(), getString(R.string.cannotOpenCamera) , Snackbar.LENGTH_SHORT)
                            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                            .show()
                }
            }

            REQUEST_OPEN_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Snackbar.make(requireView(), getString(R.string.cannotOpenGallery) , Snackbar.LENGTH_SHORT)
                            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                            .show()
                }
            }
            else -> {
                // Nothing
            }
        }
    }


}

class OptionalIntermediatesCardAdapter (val optionalIntermediatesList: MutableList<StopLocation>,
                                        val context: Context,
                                        val view: View) :
    RecyclerView.Adapter<OptionalIntermediatesCardAdapter.OptionalIntermediatesViewHolder>() {
    class OptionalIntermediatesViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val optionalInterCard = v.findViewById<CardView>(R.id.optionalInterTripCard)
        val optionalInterText = v.findViewById<TextView>(R.id.optional_intermediates_text)
        val deleteCardImageButton = v.findViewById<ImageButton>(R.id.imageButton_delete_card)
        fun bind(t: String) {}
        fun unbind() {}
    }

    override fun getItemCount(): Int {
        return optionalIntermediatesList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionalIntermediatesViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.optional_intermediates_card, parent, false)
        return OptionalIntermediatesViewHolder(v)
    }

    override fun onViewRecycled(holder: OptionalIntermediatesViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun onBindViewHolder(holder: OptionalIntermediatesViewHolder, position: Int) {
        val address: String = optionalIntermediatesList[position].address
        holder.optionalInterText.text = address
        holder.deleteCardImageButton.setOnClickListener {
            optionalIntermediatesList.removeAt(position)
            notifyDataSetChanged()
            val noOpInterView = view.findViewById<TextView>(R.id.noLocationMessageTextView)
            if (getItemCount() == 0) {
                noOpInterView.visibility = View.VISIBLE
            }
        }
    }
}