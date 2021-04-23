package it.polito.mad.car_pooling

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

private val REQUEST_IMAGE_CAPTURE = 1
private val REQUEST_OPEN_GALLERY = 2

private var imageUri: Uri? = null
private var photoFile: File? = null

class TripEditFragment : Fragment() {

    lateinit var storeDepAriLocation : String
    lateinit var storeDepDateTime : String
    lateinit var storeEstDuration : String
    lateinit var storeAvaSeat : String
    lateinit var storePrice : String
    lateinit var storeAdditional : String
    lateinit var storeOptional : String
    lateinit var storePlate : String
    lateinit var save_date : String

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater!!.inflate(R.layout.fragment_trip_edit, container, false)

        /*val editDepAriLocation = view.findViewById<TextView>(R.id.textEditDepAriLocation)
        val editDepDateTime = view.findViewById<TextView>(R.id.textEditDepDateTime)
        val editEstDuration = view.findViewById<TextView>(R.id.textEditEstDuration)
        val editAvaSeat = view.findViewById<TextView>(R.id.textEditAvaSeat)
        val editPrice = view.findViewById<TextView>(R.id.textEditPrice)
        val editAdditional = view.findViewById<TextView>(R.id.textEditAdditional)
        val editOptional = view.findViewById<TextView>(R.id.textEditOptional)
        val editPlate = view.findViewById<TextView>(R.id.textEditPlate)
        val editimageView = view.findViewById<ImageView>(R.id.imageEditCar)*/

        val editDepAriLocation = view.findViewById<TextInputLayout>(R.id.textEditDepAriLocation)
        val editDepDateTime = view.findViewById<TextView>(R.id.textEditDepDateTime)
        val editEstDuration = view.findViewById<TextInputLayout>(R.id.textEditEstDuration)
        val editAvaSeat = view.findViewById<TextInputLayout>(R.id.textEditAvaSeat)
        val editPrice = view.findViewById<TextInputLayout>(R.id.textEditPrice)
        val editAdditional = view.findViewById<TextInputLayout>(R.id.textEditAdditional)
        val editOptional = view.findViewById<TextInputLayout>(R.id.textEditOptional)
        val editPlate = view.findViewById<TextInputLayout>(R.id.textEditPlate)
        val editimageView = view.findViewById<ImageView>(R.id.imageEditCar)

        val sharedPreferences = this.requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        storeDepAriLocation = sharedPreferences.getString(getString(R.string.KeyDepAriLocation), "Departure&Arrival Location")!!
        storeDepDateTime = sharedPreferences.getString(getString(R.string.KeyDepDateTime), "Departure Date&Time")!!
        storeEstDuration = sharedPreferences.getString(getString(R.string.KeyEstDuration), "Estimated Duration")!!
        storeAvaSeat = sharedPreferences.getString(getString(R.string.KeyAvaSeat), "Available Seats")!!
        storePrice = sharedPreferences.getString(getString(R.string.KeyPrice), "Price")!!
        storeAdditional = sharedPreferences.getString(getString(R.string.KeyAdditional), "Additional Information")!!
        storeOptional = sharedPreferences.getString(getString(R.string.KeyOptional), "Optional Intermediates")!!
        storePlate = sharedPreferences.getString(getString(R.string.KeyPlate), "Plate Number")!!

        val depAriLocationInput = if (storeDepAriLocation == "Departure&Arrival Location" || storeDepAriLocation.isEmpty()) "" else storeDepAriLocation
        val depDateTimeInput = if (storeDepDateTime == "Departure Date&Time" || storeDepDateTime.isEmpty()) "Departure Date&Time" else storeDepDateTime
        val estDurationInput = if (storeEstDuration == "Estimated Duration" || storeEstDuration.isEmpty()) "" else storeEstDuration
        val avaSeatInput = if (storeAvaSeat == "Available Seats" || storeAvaSeat.isEmpty()) "" else storeAvaSeat
        val priceInput = if (storePrice == "Price" || storePrice.isEmpty()) "" else storePrice
        val additionalInput = if (storeAdditional == "Additional Information" || storeAdditional.isEmpty()) "" else storeAdditional
        val optionalInput = if (storeOptional == "Optional Intermediates" || storeOptional.isEmpty()) "" else storeOptional
        val plateInput = if (storePlate == "Plate Number" || storePlate.isEmpty()) "" else storePlate

        editDepAriLocation.editText?.setText(depAriLocationInput)
        editDepDateTime.hint= depDateTimeInput
        editEstDuration.editText?.setText(estDurationInput)
        editAvaSeat.editText?.setText(avaSeatInput)
        editPrice.editText?.setText(priceInput)
        editAdditional.editText?.setText(additionalInput)
        editOptional.editText?.setText(optionalInput)
        editPlate.editText?.setText(plateInput)

        val TripImageUri = sharedPreferences.getString(getString(R.string.KeyImageTrip), "android.resource://it.polito.mad.car_pooling/drawable/car_default")
        val uri_input = if (TripImageUri.toString() == "android.resource://it.polito.mad.car_pooling/drawable/car_default"
                || TripImageUri.toString().isEmpty()) "android.resource://it.polito.mad.car_pooling/drawable/car_default" else TripImageUri
        imageUri = Uri.parse(uri_input)
        editimageView.setImageURI(imageUri)

        val imageButton = view.findViewById<ImageButton>(R.id.imageButton2)
        registerForContextMenu(imageButton)
        setHasOptionsMenu(true)

        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            save_date = SimpleDateFormat("dd.MM.yyyy").format(cal.time)
        }
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            editDepDateTime.text = save_date + " " + SimpleDateFormat("HH:mm").format(cal.time)
        }
        editDepDateTime.setOnClickListener {
            TimePickerDialog(requireContext(), timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        return view
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
                writeSharedPreferences()
                findNavController().navigate(R.id.nav_trip)
                //Toast.makeText(requireContext(), "Saving success", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun writeSharedPreferences() {
        val sharedPreferences = this.requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

            val editDepAriLocation = requireView().findViewById<TextInputLayout>(R.id.textEditDepAriLocation).editText?.text.toString()
            val editDepDateTime = requireView().findViewById<TextView>(R.id.textEditDepDateTime).text.toString()
            val editEstDuration = requireView().findViewById<TextInputLayout>(R.id.textEditEstDuration).editText?.text.toString()
            val editAvaSeat = requireView().findViewById<TextInputLayout>(R.id.textEditAvaSeat).editText?.text.toString()
            val editPrice = requireView().findViewById<TextInputLayout>(R.id.textEditPrice).editText?.text.toString()
            val editAdditional = requireView().findViewById<TextInputLayout>(R.id.textEditAdditional).editText?.text.toString()
            val editOptional = requireView().findViewById<TextInputLayout>(R.id.textEditOptional).editText?.text.toString()
            val editPlate = requireView().findViewById<TextInputLayout>(R.id.textEditPlate).editText?.text.toString()

            val input1 = if (editDepAriLocation == storeDepAriLocation || editDepAriLocation.isEmpty()) storeDepAriLocation else editDepAriLocation
            val input2 = if (editDepDateTime == storeDepDateTime || editDepDateTime.isEmpty()) storeDepDateTime else editDepDateTime
            val input3 = if (editEstDuration == storeEstDuration || editEstDuration.isEmpty()) storeEstDuration else editEstDuration
            val input4 = if (editAvaSeat == storeAvaSeat || editAvaSeat.isEmpty()) storeAvaSeat else editAvaSeat
            val input5 = if (editPrice == storePrice || editPrice.isEmpty()) storePrice else editPrice
            val input6 = if (editAdditional == storeAdditional || editAdditional.isEmpty()) storeAdditional else editAdditional
            val input7 = if (editOptional == storeOptional || editOptional.isEmpty()) storeOptional else editOptional
            val input8 = if (editPlate == storePlate || editPlate.isEmpty()) storePlate else editPlate

        with(sharedPreferences.edit()) {
            putString( getString(R.string.KeyDepAriLocation), input1)
            putString( getString(R.string.KeyDepDateTime), input2)
            putString( getString(R.string.KeyEstDuration), input3)
            putString( getString(R.string.KeyAvaSeat), input4)
            putString( getString(R.string.KeyPrice), input5)
            putString( getString(R.string.KeyAdditional), input6)
            putString( getString(R.string.KeyOptional), input7)
            putString( getString(R.string.KeyPlate), input8)
            putString( getString(R.string.KeyImageTrip), imageUri.toString())
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
            if (ActivityCompat.checkSelfPermission(requireContext(),android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissionsGallery = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissionsGallery, REQUEST_OPEN_GALLERY)
            } else {
                openGallery()
            }
        } else {
            openGallery()
        }

    }

    private fun openGallery () {
        Log.d("POLITO_ERRORS", "Open gallery")
        val openGalleryIntent = Intent(Intent.ACTION_PICK)
        openGalleryIntent.type = "image/*"
        startActivityForResult(openGalleryIntent, REQUEST_OPEN_GALLERY)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("WrongConstant")
    private fun openCameraClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                val permissionsCamera = arrayOf(android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

}