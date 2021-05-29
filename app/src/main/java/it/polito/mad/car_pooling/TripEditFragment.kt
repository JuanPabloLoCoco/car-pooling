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
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.Utils.TimeUtilFunctions
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.models.TripRequest
import it.polito.mad.car_pooling.viewModels.ProfileViewModel
import it.polito.mad.car_pooling.viewModels.ProfileViewModelFactory
import it.polito.mad.car_pooling.viewModels.TripViewModel
import it.polito.mad.car_pooling.viewModels.TripViewModelFactory
import kotlinx.coroutines.launch
import java.io.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private val REQUEST_IMAGE_CAPTURE = 1
private val REQUEST_OPEN_GALLERY = 2

private var imageUri: Uri? = null
private var photoFile: File? = null

class TripEditFragment : Fragment() {
    val args: TripEditFragmentArgs by navArgs()

    private lateinit var selectedTrip: Trip

    private lateinit var viewModel: TripViewModel
    private lateinit var viewModelFactory: TripViewModelFactory

    private lateinit var tripId: String
    private val NEW_TRIP: String = "NEW_TRIP"
    lateinit var check_status: String
    lateinit var adapter: OptionalIntermediatesCardAdapter
    private val TAG = "TripEditFragment"

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Get the arguments
        tripId = if (args.tripId == null) NEW_TRIP else args.tripId!!

        viewModelFactory = TripViewModelFactory(tripId)
        viewModel = viewModelFactory.create(TripViewModel::class.java)

        return inflater!!.inflate(R.layout.fragment_trip_edit, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editimageView = view.findViewById<ImageView>(R.id.imageEditCar)
        val blockTripButton = view.findViewById<Button>(R.id.blockTripButton)

        val default_str_car = "android.resource://it.polito.mad.car_pooling/drawable/car_default"
        editimageView.setImageURI(Uri.parse(default_str_car))

        viewModel.trip.observe(viewLifecycleOwner, {

            if (it != null) {
                Log.d("POLITO", "${it.toMap()}")
                selectedTrip = it
                if (selectedTrip.status == Trip.BLOCKED) {
                    blockTripButton.isEnabled = false
                }
                loadDataInFields(selectedTrip, view)
                if (selectedTrip.hasImage == true) {
                    val storage = Firebase.storage
                    val imageRef = storage.reference.child("trips/$tripId.jpg")
                    imageRef.downloadUrl.addOnSuccessListener { Uri ->
                        val image_uri = Uri.toString()
                        Glide.with(this).load(image_uri).into(editimageView)
                    }
                }
            } else {
                selectedTrip = Trip.NewTrip()
                loadDataInFields(selectedTrip, view)
                //Snackbar.make(requireView(), "An error happen getting the Trip", Snackbar.LENGTH_SHORT)
                //        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                //        .show()
                findNavController().popBackStack()
            }

        })
        blockTripButton.setOnClickListener {
            // Change status of trip to BLOCK
            // Change all the request that have status PENDING -> REJECTED
            // Do not display the requestFAB on the button
            blockTripButton.isEnabled = false
            viewModel.viewModelScope.launch {
                viewModel.updateTripStatus(Trip.BLOCKED)
                        .addOnSuccessListener {
                            Snackbar.make(view, "The trip was succesfully blocked", Snackbar.LENGTH_SHORT)
                                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                                    .show()
                        }
                        .addOnFailureListener {
                            blockTripButton.isEnabled = true
                            Snackbar.make(view, "An error happen while updating the trip", Snackbar.LENGTH_SHORT)
                                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.design_default_color_error))
                                    .show()
                        }
            }

            /*
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
             */
        }

        if (tripId == NEW_TRIP) {
            // Create data for newTrip
            (activity as AppCompatActivity).supportActionBar?.title = "Create new trip"
            blockTripButton.visibility = View.GONE

        }


        val editDepDate = view.findViewById<TextView>(R.id.textEditDepDate)
        val editDepTime = view.findViewById<TextView>(R.id.textEditDepTime)

        val imageButton = view.findViewById<ImageButton>(R.id.imageButton2)
        registerForContextMenu(imageButton)
        setHasOptionsMenu(true)

        val imageButtonMapDep = view.findViewById<ImageButton>(R.id.mapDepImageButton)
        imageButtonMapDep.setOnClickListener{
            val action = TripEditFragmentDirections.actionTripEditFragmentToMapFragment("departure")
            findNavController().navigate(action)
            //findNavController().navigate(R.id.action_tripEditFragment_to_mapFragment)
        }
        val imageButtonMapArr = view.findViewById<ImageButton>(R.id.mapArrImageButton)
        imageButtonMapArr.setOnClickListener{
            val action = TripEditFragmentDirections.actionTripEditFragmentToMapFragment("arrival")
            findNavController().navigate(action)
            //findNavController().navigate(R.id.action_tripEditFragment_to_mapFragment)
        }
        val imageButtonMapAddInter = view.findViewById<ImageButton>(R.id.mapAddInterImageButton)
        imageButtonMapAddInter.setOnClickListener{
            val action = TripEditFragmentDirections.actionTripEditFragmentToMapFragment("addInter")
            findNavController().navigate(action)
            //findNavController().navigate(R.id.action_tripEditFragment_to_mapFragment)
        }

        val optionalInterRV = view.findViewById<RecyclerView>(R.id.optional_intermediates_RV)
        optionalInterRV.layoutManager = LinearLayoutManager(requireContext())
        val testList = mutableListOf<String>()
        val noOpInterView = view.findViewById<TextView>(R.id.noLocationMessageTextView)
        if (testList.size == 0) {
            optionalInterRV.visibility = View.GONE
            noOpInterView.visibility = View.VISIBLE
        }
        val addOptionalIntermediatesButton = view.findViewById<ImageView>(R.id.mapAddInterImageButtonTest)
        addOptionalIntermediatesButton.setOnClickListener {
            //val action = TripEditFragmentDirections.actionTripEditFragmentToMapFragment("addInter")
            //findNavController().navigate(action)
            val randomNum = Random().nextInt(100)
            Log.d("Trip!!!!!!!!!!!", "$randomNum")
            testList.add(randomNum.toString())
            if (testList.size == 0) {
                optionalInterRV.visibility = View.GONE
                noOpInterView.visibility = View.VISIBLE
            } else {
                optionalInterRV.visibility = View.VISIBLE
                noOpInterView.visibility = View.GONE
            }
            val adapter = OptionalIntermediatesCardAdapter(testList, requireContext(), view)
            optionalInterRV.adapter = adapter
        }

        // return view
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadDataInFields(trip: Trip, view: View) {

        view.findViewById<TextInputLayout>(R.id.textEditDepLocation).editText?.setText(trip.depLocation)
        view.findViewById<TextInputLayout>(R.id.textEditAriLocation).editText?.setText(trip.ariLocation)

        val tripDepartureTimestamp = trip.departureDateTime

        val editDepDate = view.findViewById<TextView>(R.id.textEditDepDate)
        // editDepDate.text = trip.depDate
        editDepDate.text = TimeUtilFunctions.getDateFromTimestamp(tripDepartureTimestamp)
        editDepDate.setTextColor(Color.parseColor("#9E150808"))

        val editDepTime = view.findViewById<TextView>(R.id.textEditDepTime)
        //editDepTime.text = trip.depTime
        editDepTime.text = TimeUtilFunctions.getTimeFromTimestamp(tripDepartureTimestamp)
        editDepTime.setTextColor(Color.parseColor("#9E150808"))

        val cal = Calendar.getInstance()
        cal.time = tripDepartureTimestamp.toDate()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            editDepDate.text = SimpleDateFormat("dd.MM.yyyy").format(cal.time)
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            // view = Date().time
            editDepTime.text = SimpleDateFormat("HH:mm").format(cal.time)
        }


        editDepDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.datePicker.minDate = Date().time
            datePickerDialog.show()
        }
        editDepTime.setOnClickListener {
            val timePickerDialog = TimePickerDialog(requireContext(), timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
            timePickerDialog.show()
        }

        view.findViewById<TextInputLayout>(R.id.textEditEstDuration).editText?.setText(trip.estDuration)
        view.findViewById<TextInputLayout>(R.id.textEditAvaSeat).editText?.setText(trip.avaSeats.toString())
        view.findViewById<TextInputLayout>(R.id.textEditPrice).editText?.setText(trip.price.toString())
        view.findViewById<TextInputLayout>(R.id.textEditAdditional).editText?.setText(trip.additional)

        view.findViewById<TextInputLayout>(R.id.textEditOptional).editText?.setText(trip.optional)
        view.findViewById<TextInputLayout>(R.id.textEditPlate).editText?.setText(trip.plate)

        /*
        val TripImageUri = trip.imageUri //sharedPreferences.getString(getString(R.string.KeyImageTrip), "android.resource://it.polito.mad.car_pooling/drawable/car_default")
        val uri_input = if (TripImageUri.toString() == "android.resource://it.polito.mad.car_pooling/drawable/car_default"
                || TripImageUri.toString().isEmpty()) "android.resource://it.polito.mad.car_pooling/drawable/car_default" else TripImageUri
        imageUri = Uri.parse(uri_input)
        editimageView.setImageURI(imageUri)
         */
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun saveDataInTrip (): Trip {
        val tripData = selectedTrip.copy()

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

        val departureDateAsStr = editDepDate.text.toString()
        val departureTimeAsStr = editDepTime.text.toString()
        val newTimestamp = TimeUtilFunctions.getTimestampFromDateAndTime(departureDateAsStr, departureTimeAsStr)

        val acc_email = ModelPreferencesManager.get<String>(getString(R.string.keyCurrentAccount)) //sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email")
        tripData.depLocation = editDepLocation.editText?.text.toString()
        tripData.ariLocation = editAriLocation.editText?.text.toString()
        tripData.estDuration = editEstDuration.editText?.text.toString()
        tripData.avaSeats = editAvaSeat.editText?.text.toString().toInt()
        tripData.price = editPrice.editText?.text.toString().toDouble()
        tripData.additional = editAdditional.editText?.text.toString()
        tripData.optional = editOptional.editText?.text.toString()
        tripData.plate = editPlate.editText?.text.toString()
        tripData.depDate = editDepDate.text.toString()
        tripData.depTime = editDepTime.text.toString()
        tripData.imageUri = "yes"
        tripData.owner = acc_email.toString()
        tripData.status = selectedTrip.status
        tripData.hasImage = true
        tripData.departureDateTime = newTimestamp
        return tripData
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
                val newTrip = saveDataInTrip() // Trip(tripId)

                val message: String = if (tripId == NEW_TRIP) getString(R.string.tripCreatedSucces) else getString(
                                R.string.tripEditedSucces
                        )
                if (tripId == NEW_TRIP) {
                    viewModel.createTrip(newTrip)
                        .addOnSuccessListener { documentReference ->
                            saveImageToFirebaseStorage(documentReference.id)
                            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
                                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                                    .show()
                            findNavController().popBackStack()
                        }
                } else {
                    newTrip.id = tripId.toString()
                    viewModel.updateTrip(newTrip)
                            .addOnSuccessListener {
                                saveImageToFirebaseStorage(tripId)
                            }
                            .addOnSuccessListener {
                                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
                                        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                                        .show()
                                findNavController().popBackStack()
                            }
                }
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

class OptionalIntermediatesCardAdapter (val optionalIntermediatesList: MutableList<String>,
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
        val selectedRequest: String = optionalIntermediatesList[position]
        holder.optionalInterText.text = selectedRequest
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