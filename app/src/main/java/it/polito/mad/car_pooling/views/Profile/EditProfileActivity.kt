package it.polito.mad.car_pooling.views.Profile

import android.app.Activity
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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import it.polito.mad.car_pooling.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_OPEN_GALLERY = 2

    private var imageUri: Uri? = null
    private var photoFile: File? = null
    // private lateinit var currentPhotoPath: String

    // ----------------------------- Life Cycle methods --------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile_layout)

        // Instantiate each variable
        val etFullName: TextView = findViewById(R.id.editViewFullName) as TextView
        val etNickname: TextView = findViewById(R.id.editViewNickName) as TextView
        val etEmail: TextView = findViewById(R.id.editViewEmail) as TextView
        val etLocation: TextView = findViewById(R.id.editViewLocation) as TextView
        val etBirthday: TextView = findViewById(R.id.editViewBirthday) as TextView
        val etPhoneNumber: TextView = findViewById(R.id.editViewPhoneNumber) as TextView
        val editPhotoView = findViewById<ImageView>(R.id.imageViewEditPhoto)

        // Get the data from the intent
        val fullName = this.intent.getStringExtra("group32.lab1.FULL_NAME")
        val nickName = this.intent.getStringExtra("group32.lab1.NICK_NAME")
        val email = this.intent.getStringExtra("group32.lab1.EMAIL")
        val location = this.intent.getStringExtra("group32.lab1.LOCATION")
        val birthday = this.intent.getStringExtra("group32.lab1.BIRTHDAY")
        val phoneNumber = this.intent.getStringExtra("group32.lab1.PHONE_NUMBER")
        val dataImageUri = this.intent.getStringExtra("group32.lab1.IMAGE_URI")

        // Set Text
        etFullName.text = if (fullName == getString(R.string.fullName)) "" else fullName
        etNickname.text = if (nickName == getString(R.string.nickName)) "" else nickName
        etEmail.text = if (email == getString(R.string.email)) "" else email
        etLocation.text = if (location == getString(R.string.location)) "" else location
        etBirthday.text = if (birthday == getString(R.string.birthday)) "" else birthday
        etPhoneNumber.text = if (phoneNumber == getString(R.string.phoneNumber)) "" else phoneNumber

        if (dataImageUri != null && dataImageUri.isNotEmpty()) {
            imageUri = Uri.parse(dataImageUri)
            editPhotoView.setImageURI(imageUri)
        }

        // Register photo menu
        val imageButton = findViewById<ImageButton>(R.id.imageButton1)
        registerForContextMenu(imageButton)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save state in variables
        Log.d("POLITO_ERRORS", "On save uri: " + imageUri.toString())
        outState.putString("IMAGE_URI", imageUri.toString())
        outState.putString("EDIT_TEXT_FULL_NAME", findViewById<TextView>(R.id.editViewFullName).text.toString())
        outState.putString("EDIT_TEXT_NICK_NAME", findViewById<TextView>(R.id.editViewNickName).text.toString())
        outState.putString("EDIT_TEXT_EMAIl", findViewById<TextView>(R.id.editViewEmail).text.toString())
        outState.putString("EDIT_TEXT_LOCATION", findViewById<TextView>(R.id.editViewLocation).text.toString())
        outState.putString("EDIT_TEXT_PHONE_NUMBER", findViewById<TextView>(R.id.editViewPhoneNumber).text.toString())
        outState.putString("EDIT_TEXT_BIRTHDAY", findViewById<TextView>(R.id.editViewBirthday).text.toString())

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restore state in views
        val savedImageUri = savedInstanceState.getString("IMAGE_URI")
        Log.d("POLITO_ERRORS","On restore uri: " + savedImageUri)
        if (savedImageUri != null) {
            imageUri = Uri.parse(savedImageUri)
            findViewById<ImageView>(R.id.imageViewEditPhoto).setImageURI(imageUri)
        }

        findViewById<TextView>(R.id.editViewFullName).text = savedInstanceState.getString("EDIT_TEXT_FULL_NAME")
        findViewById<TextView>(R.id.editViewNickName).text = savedInstanceState.getString("EDIT_TEXT_NICK_NAME")
        findViewById<TextView>(R.id.editViewEmail).text = savedInstanceState.getString("EDIT_TEXT_EMAIl")
        findViewById<TextView>(R.id.editViewLocation).text = savedInstanceState.getString("EDIT_TEXT_LOCATION")
        findViewById<TextView>(R.id.editViewPhoneNumber).text = savedInstanceState.getString("EDIT_TEXT_PHONE_NUMBER")
        findViewById<TextView>(R.id.editViewBirthday).text = savedInstanceState.getString("EDIT_TEXT_BIRTHDAY")
    }

    // ----------------------------- Permissions --------------------------------------
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                // Log.d("POLITO_ERRORS", grantResults[0].toString())
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(applicationContext, "You cannot access to camera", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_OPEN_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(applicationContext, "You cannot access to gallery", Toast.LENGTH_SHORT).show()
                }
            }

            else -> {
                // Nothing
            }
        }
    }

    // ----------------------------- Options Menu --------------------------------------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.edit_profile_option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.saveItem -> {
                saveItems()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveItems() {
        val intent = Intent()

        // Get the values from the Edit Texts
        val fullName = findViewById<TextView>(R.id.editViewFullName).text.toString()
        val nickName = findViewById<TextView>(R.id.editViewNickName).text.toString()
        val email = findViewById<TextView>(R.id.editViewEmail).text.toString()
        val location = findViewById<TextView>(R.id.editViewLocation).text.toString()
        val birthday = findViewById<TextView>(R.id.editViewBirthday).text.toString()
        val phoneNumber = findViewById<TextView>(R.id.editViewPhoneNumber).text.toString()

        // Populate the Intent
        intent.putExtra("group32.lab1.FULL_NAME", fullName)
        intent.putExtra("group32.lab1.NICK_NAME", nickName)
        intent.putExtra("group32.lab1.EMAIL", email)
        intent.putExtra("group32.lab1.LOCATION", location)
        intent.putExtra("group32.lab1.BIRTHDAY", birthday)
        intent.putExtra("group32.lab1.PHONE_NUMBER", phoneNumber)
        intent.putExtra("group32.lab1.IMAGE_URI", imageUri.toString())

        writeSharedPreferences()
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun writeSharedPreferences() {
        val sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        with(sharedPreferences.edit()) {
            putString( getString(R.string.KeyFullName), findViewById<TextView>(R.id.editViewFullName).text.toString())
            putString( getString(R.string.KeyNickName), findViewById<TextView>(R.id.editViewNickName).text.toString())
            putString( getString(R.string.KeyEmail), findViewById<TextView>(R.id.editViewEmail).text.toString())
            putString( getString(R.string.KeyLocation), findViewById<TextView>(R.id.editViewLocation).text.toString())
            putString( getString(R.string.KeyPhoneNumber), findViewById<TextView>(R.id.editViewPhoneNumber).text.toString())
            putString( getString(R.string.KeyBirthday), findViewById<TextView>(R.id.editViewBirthday).text.toString())
            putString( getString(R.string.KeyImage), imageUri.toString())
            commit()
        }
    }
    // ----------------------------- Photo Menu ----------------------------------------
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        menuInflater.inflate(R.menu.edit_profile_menu, menu)
        super.onCreateContextMenu(menu, v, menuInfo)
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
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
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
        val fileProvider = FileProvider.getUriForFile(this, "it.polito.s287288.showprofileactivity.fileprovider", photoFile!!)
        takenPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

        startActivityForResult(takenPictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    // ----------------------------- Open Gallery --------------------------------------
    private fun openGalleryClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
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
        val imageView = findViewById<ImageView>(R.id.imageViewEditPhoto)
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
                Toast.makeText(applicationContext, "There was a problem while taking the photo", Toast.LENGTH_SHORT).show()
            }
            //imageView.setImageURI(imageUri)
            //setPic(imageView, photoFile?.absolutePath)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_OPEN_GALLERY) {
            imageUri = data?.data
            // Log.d("POLITO_ERRORS", "Image uri: " + imageUri.toString())
            // imageView.setImageURI(data?.data)
            // setPic(imageView, imageUri.toString())

            val source = imageUri?.let { ImageDecoder.createSource(this.contentResolver, it) }
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

    // ----------------------------- Util Functions ------------------------------------


    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
    private fun bitmapToFile(bitmap:Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(applicationContext)

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
        }catch (e:IOException){
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
