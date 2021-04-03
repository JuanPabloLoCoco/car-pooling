package it.polito.s287288.showprofileactivity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.*
import java.util.jar.Manifest

class EditProfileActivity : AppCompatActivity() {
    var photo : Uri? = null
    lateinit var profileImageView: ImageView;
    val FILE_NAME = "photo.jpg"

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_OPEN_GALLERY = 2

    lateinit var editImageUri: String;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile_layout)

        // profileImageView = findViewById(R.id.profileImageView)

        // Instantiate each variable
        var etFullName: TextView = findViewById(R.id.editViewFullName) as TextView;
        var etNickname: TextView = findViewById(R.id.editViewNickName) as TextView;
        var etEmail: TextView = findViewById(R.id.editViewEmail) as TextView;
        var etLocation: TextView = findViewById(R.id.editViewLocation) as TextView;
        var etBirthday: TextView = findViewById(R.id.editViewBirthday) as TextView;
        var etPhoneNumber: TextView = findViewById(R.id.editViewPhoneNumber) as TextView;

        var editPhotoView = findViewById<ImageView>(R.id.imageViewEditPhoto);

        // Get the data from the intent
        val fullName = this.intent.getStringExtra("group32.lab1.FULL_NAME")
        val nickName = this.intent.getStringExtra("group32.lab1.NICK_NAME")
        val email = this.intent.getStringExtra("group32.lab1.EMAIL")
        val location = this.intent.getStringExtra("group32.lab1.LOCATION")
        val birthday = this.intent.getStringExtra("group32.lab1.BIRTHDAY")
        val phoneNumber = this.intent.getStringExtra("group32.lab1.PHONE_NUMBER")
        val imageUri = this.intent.getStringExtra("group32.lab1.IMAGE_URI")

        // Set Text
        etFullName.text = fullName;
        etNickname.text = nickName;
        etEmail.text = email
        etLocation.text = location
        etBirthday.text = birthday
        etPhoneNumber.text = phoneNumber

        if (imageUri != null && imageUri.isNotEmpty()) {
            editPhotoView.setImageURI(Uri.parse(imageUri))
        }

        val imageButton = findViewById<ImageButton>(R.id.imageButton1)
        registerForContextMenu(imageButton)

        imageButton?.setOnClickListener{
            openContextMenu(imageButton)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.edit_profile_option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.saveItem -> {
                //sendResults()
                // Toast.makeText(this, "Save item", Toast.LENGTH_SHORT).show()
                saveItems()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveItems () {
        val intent = Intent()

        // Get the values from the Edit Texts
        val fullName = findViewById<TextView>(R.id.editViewFullName).text.toString()
        val nickName = findViewById<TextView>(R.id.editViewNickName).text.toString()
        val email = findViewById<TextView>(R.id.editViewEmail).text.toString()
        val location = findViewById<TextView>(R.id.editViewLocation).text.toString()
        val birthday = findViewById<TextView>(R.id.editViewBirthday).text.toString()
        val phoneNumber = findViewById<TextView>(R.id.editViewPhoneNumber).text.toString()

        // Populate the Intent
        intent.putExtra("group32.lab1.FULL_NAME", fullName);
        intent.putExtra("group32.lab1.NICK_NAME", nickName)
        intent.putExtra("group32.lab1.EMAIL", email)
        intent.putExtra("group32.lab1.LOCATION", location)
        intent.putExtra("group32.lab1.BIRTHDAY", birthday)
        intent.putExtra("group32.lab1.PHONE_NUMBER", phoneNumber)

        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        menuInflater.inflate(R.menu.edit_profile_menu, menu)
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.d("POLITO_LOG", "Item Seleccionado -> ${item.itemId}")

        return when (item.itemId) {
            R.id.select_image -> {
                //showGallery_CLick()
                return true
            }
            R.id.take_photo -> {
                // Toast.makeText(this, "TAKE PHOTO", Toast.LENGTH_SHORT).show()
                Log.d("POLITO_LOG", "Take phtoo")
                openCameraClick()
                // openCameraClick2()
                return true
                //dispatchTakePictureIntent()

            }
            else -> {
                //Log.d("POLITO_LOG", "CUALQUIERA;")
                //Toast.makeText(this, "CUALQUIER", Toast.LENGTH_SHORT)
                return true
            }
        }

    }

    private fun getPhotoFIle(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    /*
    private fun openCameraClick2() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFIle(FILE_NAME)
        val fileProvider = FileProvider.getUriForFile(this, "it.polito.s287288.FileProvider", photoFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (takePictureIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_LONG).show()
        }
    }

     */

    private fun dispatchTakePictureIntent() {
        val value = ContentValues()
        value.put(MediaStore.Images.Media.TITLE, "newImage")
        var myImage = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
        var takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, myImage)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

        /*

        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
        */
    }

    private fun openCameraClick () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissionsCamera = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permissionsCamera, REQUEST_IMAGE_CAPTURE)
            } else {
                openCamera();
            }
        } else {
            openCamera()
        }

        // Check for permissions

        openCamera()
    }

    private fun openCamera () {
        val value = ContentValues()
        value.put(MediaStore.Images.Media.TITLE, "data")
        photo = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value);
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photo)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)

        /*
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFIle(FILE_NAME)
        val fileProvider = FileProvider.getUriForFile(this, "it.polito.s287288.FileProvider", photoFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (takePictureIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_LONG).show()
        }
        */
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(applicationContext, "You cannot access to camera", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_OPEN_GALLERY -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showGallery()
                } else {
                    Toast.makeText(applicationContext, "You cannot access to gallery", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showGallery_CLick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Ask for permisision
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                // Ask for permission
                val permissionGallery = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissionGallery, REQUEST_OPEN_GALLERY)
            } else {
               showGallery()
            }
        } else {
            showGallery()
        }
    }

    private fun showGallery () {
        Toast.makeText(applicationContext, "SHow gallery", Toast.LENGTH_LONG).show();
        val intentGallery = Intent(Intent.ACTION_PICK);
        intentGallery.type = "image/*"
        startActivityForResult(intentGallery, REQUEST_OPEN_GALLERY);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_OPEN_GALLERY) {
            profileImageView.setImageURI(data?.data)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            //val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            //profileImageView.setImageBitmap(takenImage)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /*
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
     */

    private fun setPic(imageView: ImageView, photoPath: String) {
        // Get the dimensions of the View
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
            inPurgeable = true
        }
        BitmapFactory.decodeFile(photoPath, bmOptions)?.also { bitmap ->
            imageView.setImageBitmap(bitmap)
        }
    }

}