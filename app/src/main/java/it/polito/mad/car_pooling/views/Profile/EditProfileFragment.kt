package it.polito.mad.car_pooling.views.Profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.R
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.viewModels.ProfileViewModel
import it.polito.mad.car_pooling.viewModels.ProfileViewModelFactory
import java.io.*
import java.util.*

class EditProfileFragment : Fragment() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_OPEN_GALLERY = 2

    private val IMAGE_URI_STATE_KEY = "IMAGE_URI"

    private var imageUri: Uri? = null
    private var photoFile: File? = null

    private lateinit var profile: Profile

    private lateinit var acc_email: String

    private lateinit var viewModel: ProfileViewModel
    private lateinit var viewModelFactory: ProfileViewModelFactory

    // ---------------------------- Life Cycle -----------------------
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.edit_profile_layout, container, false)

        // Register photo menu
        val imageButton = view.findViewById<ImageButton>(R.id.imageButton1)
        registerForContextMenu(imageButton)
        setHasOptionsMenu(true)

        val editBithday = view.findViewById<TextView>(R.id.editViewBirthday)
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            editBithday.text = SimpleDateFormat("dd.MM.yyyy").format(cal.time)
        }
        editBithday.setOnClickListener {
            DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        // var acc_emailone = sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email")

        acc_email = ModelPreferencesManager.get(getString(R.string.keyCurrentAccount))?: "no email"

        viewModelFactory = ProfileViewModelFactory(acc_email)
        viewModel = viewModelFactory.create(ProfileViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*
        var storedProfile = ModelPreferencesManager.get<Profile>(getString(R.string.KeyProfileData))
        if (storedProfile === null) {
            storedProfile = Profile("")
        }
        profile = storedProfile
        loadProfileInFields(storedProfile, view)

        val etFullName = view.findViewById<TextInputLayout>(R.id.editViewFullName)
        val etNickname = view.findViewById<TextInputLayout>(R.id.editViewNickName)
        val etEmail = view.findViewById<TextInputLayout>(R.id.editViewEmail)
        val etLocation = view.findViewById<TextInputLayout>(R.id.editViewLocation)
        val etBirthday = view.findViewById<TextView>(R.id.editViewBirthday)
        val etPhoneNumber = view.findViewById<TextInputLayout>(R.id.editViewPhoneNumber)
        val editPhotoView = view.findViewById<ImageView>(R.id.imageViewEditPhoto)

        val db = FirebaseFirestore.getInstance()
        val user = db.collection("user")
        val my_profile = user.document("my_profile")

        db.collection("user").document("my_profile").get().addOnFailureListener {
            Toast.makeText(requireContext(), "Internet Connection Error", Toast.LENGTH_LONG).show()
        }

        my_profile.addSnapshotListener { value, error ->
            if (error != null) throw error
            if (value != null) {
                etFullName.editText?.setText(value["full_name"].toString())
                etNickname.editText?.setText(value["nick_name"].toString())
                etEmail.editText?.setText(value["email"].toString())
                etLocation.editText?.setText(value["location"].toString())
                etBirthday.text = value["birthday"].toString()
                etPhoneNumber.editText?.setText(value["phone_number"].toString())
                val default_str_car = "android.resource://it.polito.mad.car_pooling/drawable/default_image"
                imageUri = if (value["image_uri"].toString() == "" || value["image_uri"].toString().isEmpty()) Uri.parse(default_str_car)
                           else Uri.parse(value["image_uri"].toString())
                editPhotoView.setImageURI(imageUri)
            }
        }*/
        val default_str_profile = "android.resource://it.polito.mad.car_pooling/drawable/default_image"
        val imageView = view.findViewById<ImageView>(R.id.imageViewEditPhoto)
        val db = FirebaseFirestore.getInstance()
        val users = db.collection("Users")
        val my_profile = users.document(acc_email)
        my_profile.get().addOnSuccessListener {document ->
            if (document.data != null) {
                if (document.data!!["hasImage"] == true) {
                    val storage = Firebase.storage
                    val imageRef = storage.reference.child("users/$acc_email.jpg")
                    imageRef.downloadUrl.addOnSuccessListener { Uri ->
                        val image_uri = Uri.toString()
                        Glide.with(this).load(image_uri).into(imageView)
                    }
                } else {
                    imageUri = Uri.parse(default_str_profile)
                    imageView.setImageURI(imageUri)
                }
            }
        }

        viewModel.profile.observe( viewLifecycleOwner, {
            val thisUser = it
            if (thisUser != null) {
                loadProfileInFields(it, view)
                Log.d("POLITO", "MyUser from viewMode = ${thisUser.fullName}")
                /*if (it.hasImage == true) {
                    val storage = Firebase.storage
                    val imageRef = storage.reference.child("users/$acc_email.jpg")
                    imageRef.downloadUrl.addOnSuccessListener { Uri ->
                        val image_uri = Uri.toString()
                        Glide.with((activity as AppCompatActivity)).load(image_uri).into(imageView)
                    }
                } else {
                    imageUri = Uri.parse(default_str_profile)
                    imageView.setImageURI(imageUri)
                }*/
            } else {
                writeTextView(view)
                Log.d("POLITO", "MyUser from viewMode is null")
            }
        })

    }

    private fun writeTextView(view: View){
        val default_str_profile = "android.resource://it.polito.mad.car_pooling/drawable/default_image"
        view.findViewById<TextInputLayout>(R.id.editViewFullName).editText?.setText("Full Name")
        view.findViewById<TextInputLayout>(R.id.editViewNickName).editText?.setText("Nick Name")
        view.findViewById<TextInputLayout>(R.id.editViewEmail).editText?.setText("Email@Address")
        view.findViewById<TextInputLayout>(R.id.editViewLocation).editText?.setText("Location")
        view.findViewById<TextView>(R.id.editViewBirthday).text = "Birthday"
        view.findViewById<TextInputLayout>(R.id.editViewPhoneNumber).editText?.setText("PhoneNumber")
        imageUri = Uri.parse(default_str_profile)
        view.findViewById<ImageView>(R.id.imageViewEditPhoto).setImageURI(imageUri)
    }

    private fun loadProfileInFields(profile: Profile, view: View) {
        view.findViewById<TextInputLayout>(R.id.editViewFullName).editText?.setText(profile.fullName)
        view.findViewById<TextInputLayout>(R.id.editViewNickName).editText?.setText(profile.nickName)
        view.findViewById<TextInputLayout>(R.id.editViewEmail).editText?.setText(profile.email)
        view.findViewById<TextInputLayout>(R.id.editViewLocation).editText?.setText(profile.location)
        view.findViewById<TextView>(R.id.editViewBirthday).text = profile.birthday
        view.findViewById<TextInputLayout>(R.id.editViewPhoneNumber).editText?.setText(profile.phoneNumber)
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
    // ----------------------------- Manage Shared Preferences ---------------------
    // First Load
    /*
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
    */



    private fun savedProfileData (): Profile {
        val inputFullName = requireView().findViewById<TextInputLayout>(R.id.editViewFullName).editText?.text.toString()
        val inputNickname =  requireView().findViewById<TextInputLayout>(R.id.editViewNickName).editText?.text.toString()
        val inputLocation =  requireView().findViewById<TextInputLayout>(R.id.editViewLocation).editText?.text.toString()
        val inputBirthday =  requireView().findViewById<TextView>(R.id.editViewBirthday).text.toString()
        val inputPhoneNumber =  requireView().findViewById<TextInputLayout>(R.id.editViewPhoneNumber).editText?.text.toString()
        // val inputPhotoView =  imageUri.toString()

        val profileToSave = Profile(inputFullName)
        profileToSave.nickName = inputNickname
        profileToSave.phoneNumber = inputPhoneNumber
        profileToSave.location = inputLocation
        profileToSave.birthday = inputBirthday
        profileToSave.email = acc_email
        profileToSave.hasImage = true

        return profileToSave
    }

    // ----------------------------- Option Menu ---------------------
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_profile_option_menu, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    private fun saveImagePhoto() {
        val imageView = requireView().findViewById<ImageView>(R.id.imageViewEditPhoto)
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val storage = Firebase.storage
        val storageRef = storage.reference
        storageRef.child("users/$acc_email.jpg").putBytes(data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.saveItem -> {
                saveImagePhoto()
                val profileToStore = savedProfileData()

                viewModel.saveUser(profileToStore)
                    .addOnSuccessListener {
                        Snackbar.make(requireView(), R.string.profileEditedSucces, Snackbar.LENGTH_SHORT)
                                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                                .show()

                        val sp = PreferenceManager.getDefaultSharedPreferences(context)
                        val hideAllRef = sp.getBoolean("hideAll",false)
                        val showLocationRef= sp.getBoolean("showLocation",false)
                        val showBirthdayRef = sp.getBoolean("showBirthday",false)
                        val showPhoneNumberRef = sp.getBoolean("showPhoneNumber",false)
                        val hidePlateRef = sp.getBoolean("hidePlate",false)
                        val db = FirebaseFirestore.getInstance()
                        val users = db.collection("Users")
                        val my_profile = users.document(acc_email)
                        my_profile.update("hideAll", hideAllRef.toString(),
                            "showLocation", showLocationRef.toString(),
                            "showBirthday", showBirthdayRef.toString(),
                            "showPhoneNumber", showPhoneNumberRef.toString(),
                            "hidePlate", hidePlateRef.toString())
                            .addOnSuccessListener { Log.d("settingFragment", "DocumentSnapshot successfully updated!") }
                            .addOnFailureListener { e -> Log.w("settingFragment", "Error updating document", e) }

                        val profileFragment = fragmentManager?.findFragmentByTag("profileFragment")
                        if (profileFragment != null) {
                            val ft = fragmentManager?.beginTransaction()
                            ft?.detach(profileFragment)
                            ft?.attach(profileFragment)
                            ft?.commit()
                        }

                        findNavController().popBackStack()
                    }
                    .addOnFailureListener {
                        Log.d("POLITO", "An error ocurrs updating profile")

                    }

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
                Snackbar.make(requireView(), getString(R.string.problemOpeningCamera) , Snackbar.LENGTH_SHORT)
                        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                        .show()
                // makeText(requireContext(), "There was a problem while taking the photo", Toast.LENGTH_SHORT).show()
            }
            //imageView.setImageURI(imageUri)
            //setPic(imageView, photoFile?.absolutePath)
            Log.d("imageUriLocal", imageUri.toString())
            findNavController().previousBackStackEntry?.savedStateHandle?.set("imageUriLocal", imageUri.toString())
            val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString( "imageChangedOrNot", "yes")
                commit()
            }
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
            Log.d("imageUriLocal", imageUri.toString())
            findNavController().previousBackStackEntry?.savedStateHandle?.set("imageUriLocal", imageUri.toString())
            val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString( "imageChangedOrNot", "yes")
                commit()
            }
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