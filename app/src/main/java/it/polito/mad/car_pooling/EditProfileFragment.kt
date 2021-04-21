package it.polito.mad.car_pooling

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.icu.text.SimpleDateFormat
import android.media.ExifInterface
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
import androidx.navigation.fragment.findNavController
import java.util.*
import androidx.appcompat.app.AppCompatActivity

import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class EditProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_OPEN_GALLERY = 2

    private val IMAGE_URI_STATE_KEY = "IMAGE_URI"

    private var imageUri: Uri? = null
    private var photoFile: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    // ---------------------------- Life Cycle -----------------------
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.edit_profile_layout, container, false)


        val etFullName: TextView = view.findViewById(R.id.editViewFullName) as TextView
        val etNickname: TextView = view.findViewById(R.id.editViewNickName) as TextView
        val etEmail: TextView = view.findViewById(R.id.editViewEmail) as TextView
        val etLocation: TextView = view.findViewById(R.id.editViewLocation) as TextView
        val etBirthday: TextView = view.findViewById(R.id.editViewBirthday) as TextView
        val etPhoneNumber: TextView = view.findViewById(R.id.editViewPhoneNumber) as TextView
        val editPhotoView = view.findViewById<ImageView>(R.id.imageViewEditPhoto)

        val sharedPreferences = this.requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        // Get stored data
        val fullName = sharedPreferences.getString(getString(R.string.KeyFullName), getString(R.string.fullName))
        val nickName = sharedPreferences.getString(getString(R.string.KeyNickName), getString(R.string.nickName))
        val email = sharedPreferences.getString(getString(R.string.KeyEmail), getString(R.string.email))
        val location = sharedPreferences.getString(getString(R.string.KeyLocation), getString(R.string.location))
        val phoneNumber = sharedPreferences.getString(getString(R.string.KeyPhoneNumber), getString(R.string.phoneNumber))
        val birthday = sharedPreferences.getString(getString(R.string.KeyBirthday), getString(R.string.birthday))
        val storedImageUri =  sharedPreferences.getString(getString(R.string.KeyImage), getUriFromResource(R.drawable.default_image).toString())

        // Set Text
        etFullName.text = if (fullName == getString(R.string.fullName)) "" else fullName
        etNickname.text = if (nickName == getString(R.string.nickName)) "" else nickName
        etEmail.text = if (email == getString(R.string.email)) "" else email
        etLocation.text = if (location == getString(R.string.location)) "" else location
        etBirthday.text = if (birthday == getString(R.string.birthday)) "" else birthday
        etPhoneNumber.text = if (phoneNumber == getString(R.string.phoneNumber)) "" else phoneNumber

        if (storedImageUri != null && storedImageUri.isNotEmpty()) {
            imageUri = Uri.parse(storedImageUri)
            editPhotoView.setImageURI(imageUri);
        }
        // Load data
        // readProfileData(view)

        // Register photo menu
        val imageButton = view.findViewById<ImageButton>(R.id.imageButton1)
        registerForContextMenu(imageButton)
        setHasOptionsMenu(true)

        return view
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(IMAGE_URI_STATE_KEY, imageUri.toString())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val savedImageUri = savedInstanceState?.getString(IMAGE_URI_STATE_KEY)
        if (savedImageUri != null) {
            imageUri = Uri.parse(savedImageUri)
            requireView().findViewById<ImageView>(R.id.imageViewEditPhoto).setImageURI(imageUri)
        }
    }

    // ----------------------------- Manage Shared Preferences ---------------------
    // First Load
    private fun readProfileData (view: View) {
        // Instantiate each variable
        val etFullName: TextView = view.findViewById(R.id.editViewFullName) as TextView
        val etNickname: TextView = view.findViewById(R.id.editViewNickName) as TextView
        val etEmail: TextView = view.findViewById(R.id.editViewEmail) as TextView
        val etLocation: TextView = view.findViewById(R.id.editViewLocation) as TextView
        val etBirthday: TextView = view.findViewById(R.id.editViewBirthday) as TextView
        val etPhoneNumber: TextView = view.findViewById(R.id.editViewPhoneNumber) as TextView
        val editPhotoView = view.findViewById<ImageView>(R.id.imageViewEditPhoto)

        val sharedPreferences = this.requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        // Get stored data
        val fullName = sharedPreferences.getString(getString(R.string.KeyFullName), getString(R.string.fullName))
        val nickName = sharedPreferences.getString(getString(R.string.KeyNickName), getString(R.string.nickName))
        val email = sharedPreferences.getString(getString(R.string.KeyEmail), getString(R.string.email))
        val location = sharedPreferences.getString(getString(R.string.KeyLocation), getString(R.string.location))
        val phoneNumber = sharedPreferences.getString(getString(R.string.KeyPhoneNumber), getString(R.string.phoneNumber))
        val birthday = sharedPreferences.getString(getString(R.string.KeyBirthday), getString(R.string.birthday))
        val storedImageUri =  sharedPreferences.getString(getString(R.string.KeyImage), getUriFromResource(R.drawable.default_image).toString())

        // Set Text
        etFullName.text = if (fullName == getString(R.string.fullName)) "" else fullName
        etNickname.text = if (nickName == getString(R.string.nickName)) "" else nickName
        etEmail.text = if (email == getString(R.string.email)) "" else email
        etLocation.text = if (location == getString(R.string.location)) "" else location
        etBirthday.text = if (birthday == getString(R.string.birthday)) "" else birthday
        etPhoneNumber.text = if (phoneNumber == getString(R.string.phoneNumber)) "" else phoneNumber

        if (storedImageUri != null && storedImageUri.isNotEmpty()) {
            imageUri = Uri.parse(storedImageUri)
            editPhotoView.setImageURI(imageUri);
        }
    }

    private fun savedProfileData () {
        val sharedPreferences = this.requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        var newFullName = requireView().findViewById<TextView>(R.id.editViewFullName).text.toString()
        newFullName = if (newFullName == null || newFullName.isBlank() || newFullName.isEmpty()) getString(R.string.KeyFullName) else newFullName
        with(sharedPreferences.edit()) {

            putString( getString(R.string.KeyFullName), newFullName)
            putString( getString(R.string.KeyNickName), requireView().findViewById<TextView>(R.id.editViewNickName).text.toString())
            putString( getString(R.string.KeyEmail), requireView().findViewById<TextView>(R.id.editViewEmail).text.toString())
            putString( getString(R.string.KeyLocation), requireView().findViewById<TextView>(R.id.editViewLocation).text.toString())
            putString( getString(R.string.KeyPhoneNumber), requireView().findViewById<TextView>(R.id.editViewPhoneNumber).text.toString())
            putString( getString(R.string.KeyBirthday), requireView().findViewById<TextView>(R.id.editViewBirthday).text.toString())
            putString( getString(R.string.KeyImage), imageUri.toString())
            commit()
        }

        val nav_header_image = requireActivity().findViewById<ImageView>(R.id.nav_header_image)
        nav_header_image.setImageURI(imageUri)
        val nav_header_full_name = requireActivity().findViewById<TextView>(R.id.nav_header_full_name)
        nav_header_full_name.text = newFullName
    }

    // ----------------------------- Option Menu ---------------------
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_profile_option_menu, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.saveItem -> {
                savedProfileData()
                findNavController().navigate(R.id.nav_profile)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(Menu.NONE, R.id.select_image, Menu.NONE, "select an image")
        menu.add(Menu.NONE, R.id.take_photo, Menu.NONE, "take a picture")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.select_image -> {
                openGalleryClick()
                true
            }
            R.id.take_photo -> {
                openCameraClick()
                true
            }
            else -> {
                true
            }
        }
    }

    // ----------------------------- Take photo ----------------------------------------
    @RequiresApi(Build.VERSION_CODES.N)
    private fun openCameraClick () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                || requireContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
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

        // This doesn't work for API >= 24
        // takenPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile)
        val fileProvider = FileProvider.getUriForFile(requireContext(), "it.polito.mad.car_pooling.fileprovider", photoFile!!)
        takenPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

        startActivityForResult(takenPictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    // ----------------------------- Open Gallery --------------------------------------
    private fun openGalleryClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
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


    // ----------------------------- Activity Results ----------------------------------

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // super.onActivityResult(requestCode, resultCode, data)
        val imageView = requireView().findViewById<ImageView>(R.id.imageViewEditPhoto)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            //val takenImage = BitmapFactory.decodeFile(photoFile?.absolutePath)
            //imageView.setImageBitmap(takenImage)
            if (photoFile != null) {
                imageUri = Uri.fromFile(photoFile)
                val finalUri = imageUri
                if (finalUri != null) {
                    val bitmap = BitmapFactory.decodeFile(imageUri?.path).fixRotation(finalUri)
                    imageView.setImageBitmap(bitmap)
                } else {
                    //imageView.setImageBitmap(/*Bitmap().fixRotation(uri)*/)
                    imageView.setImageURI(imageUri)
                }
            } else {
                Toast.makeText(requireContext(), "There was a problem while taking the photo", Toast.LENGTH_SHORT).show()
            }
            //imageView.setImageURI(imageUri)
            //setPic(imageView, photoFile?.absolutePath)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_OPEN_GALLERY) {
            imageUri = data?.data
            // Log.d("POLITO_ERRORS", "Image uri: " + imageUri.toString())
            // imageView.setImageURI(data?.data)
            // setPic(imageView, imageUri.toString())

            val source = imageUri?.let { ImageDecoder.createSource(requireContext().contentResolver, it) }
            imageUri = bitmapToFile(ImageDecoder.decodeBitmap(source!!))
            imageView.setImageURI(imageUri)
            /*
            if (imageUri != null) {
                setPic(imageView, imageUri.toString())
                // imageView.setImageURI(imageUri);
            }
             */
        }
    }

    // ----------------------------- Util functions ----------------------------------
    private fun getUriFromResource (resourceId: Int): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(resourceId))
            .appendPath(resources.getResourceTypeName(resourceId))
            .appendPath(resources.getResourceEntryName(resourceId))
            .build()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
        // .apply {
        // currentPhotoPath = absolutePath
        // }
    }

    private fun setPic(imageView: ImageView, photoPath: String?) {
        // Get the dimensions of the View
        if (photoPath == null) {
            return
        }
        val targetW: Int = imageView.width
        val targetH: Int = imageView.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            // inPurgeable = true
        }
        BitmapFactory.decodeFile(photoPath, bmOptions)?.also { bitmap ->
            imageView.setImageBitmap(bitmap)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun bitmapToFile(bitmap: Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(requireContext())

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images",Context.MODE_PRIVATE)
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        file = File(file,"JPEG_${timeStamp}.jpg")

        try{
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
    }

    fun Bitmap.fixRotation(uri: Uri): Bitmap? {
        val pathString = uri.path
        lateinit var ei: ExifInterface;
        if (pathString != null) {
            ei = ExifInterface(pathString)
        } else {
            return null
        }

        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage( 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage( 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage( 270f)
            ExifInterface.ORIENTATION_NORMAL -> this
            else -> this
        }
    }

    fun Bitmap.rotateImage(angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            this, 0, 0, width, height,
            matrix, true
        )
    }
}