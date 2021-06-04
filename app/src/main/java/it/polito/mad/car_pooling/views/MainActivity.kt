package it.polito.mad.car_pooling.views

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.R
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val NAV_IMAGE: String = "NAV_IMAGE"
    private val NAV_FULL_NAME: String = "NAV_FULL_NAME"

    private lateinit var imageUri: String
    private lateinit var fullName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ModelPreferencesManager.with(application, getString(R.string.preference_file_key))
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.hide()

        // val viewModel = ViewModelProviders.of(this).get(MyTripListViewModel::class.java)

        imageUri = ""
        fullName = ""
        readSharedPreferences()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_other_list_trip, R.id.nav_list_trip, R.id.nav_profile, R.id.nav_list_interest_trip, R.id.nav_list_bought_trip, R.id.nav_setting), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val signOutButton = findViewById<Button>(R.id.sign_out_button)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("785578307134-aa8d3165sln0kqhm489fdmvt0nfntuph.apps.googleusercontent.com")
                //.requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        signOutButton.setOnClickListener{
            googleSignInClient.signOut()
            FirebaseAuth.getInstance().signOut()
            navController.navigate(R.id.singInFragment)
            drawerLayout.closeDrawers()
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun readSharedPreferences() {
        val sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val acc_email = sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email")
        // Get Stored Data and display data
        val db = FirebaseFirestore.getInstance()
        val users = db.collection("Users")
        val default_str_profile = "android.resource://it.polito.mad.car_pooling/drawable/default_image"
        if (acc_email != "no email"){
            users.document(acc_email.toString()).addSnapshotListener { value, error ->
                if (error != null) throw error
                if (value != null) {
                    if (value.exists()){
                        imageUri = if (value["hasImage"] != true || value["hasImage"].toString().isEmpty()) default_str_profile
                                   else value["hasImage"].toString()
                        //writeDefaultValue(value["full_name"].toString(), imageUri)
                        val navView: NavigationView = findViewById(R.id.nav_view)
                        val hView =  navView.getHeaderView(0)
                        hView.findViewById<TextView>(R.id.nav_header_full_name).text = value["full_name"].toString()
                        val headerImage = hView.findViewById<ImageView>(R.id.nav_header_image)
                        if (imageUri == default_str_profile){
                            headerImage.setImageURI(Uri.parse(imageUri))
                        } else {
                            val storage = Firebase.storage
                            /*val localFile = File.createTempFile("my_profile", "jpg")
                            storage.reference.child("users/$acc_email.jpg").getFile(localFile).addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                headerImage.setImageBitmap(bitmap)
                                //ModelPreferencesManager.put()
                                val sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                                with(sharedPreferences.edit()) {
                                    putString( getString(R.string.keyMyProfile), localFile.absolutePath.toString())
                                    commit()
                                }
                            }*/
                            val imageRef = storage.reference.child("users/$acc_email.jpg")
                            imageRef.downloadUrl.addOnSuccessListener { Uri ->
                                val image_uri = Uri.toString()
                                Glide.with(this).load(image_uri).into(headerImage)
                            }
                        }
                    } else {
                        writeDefaultValue("Full Name", default_str_profile)
                        //Log.d("main_activity", "${acc_email} yessssssssssssss")
                    }
                } else {
                    writeDefaultValue("Full Name", default_str_profile)
                }
            }
        } else {
            writeDefaultValue("Full Name", default_str_profile)
        }
        /*var storedProfile = ModelPreferencesManager.get<Profile>(getString(R.string.KeyProfileData))
        if (storedProfile === null) {
            storedProfile = Profile("")
        }
        fullName = storedProfile.fullName //sharedPreferences.getString(getString(R.string.KeyFullName), getString(R.string.fullName)).toString()
        val storedImageUri =  if (storedProfile.imageUri.isEmpty()) getUriFromResource(R.drawable.default_image).toString() else storedProfile.imageUri
        imageUri = storedImageUri*/

        // Set stored data in view
        //findViewById<ImageView>(R.id.nav_header_image).setImageURI(Uri.parse(storedImageUri))
        //findViewById<TextView>(R.id.nav_header_full_name).text = fullName
    }

    private fun writeDefaultValue(inputFullName: String, inputUri: String){
        val navView: NavigationView = findViewById(R.id.nav_view)
        val hView =  navView.getHeaderView(0)
        hView.findViewById<TextView>(R.id.nav_header_full_name).text = inputFullName
        hView.findViewById<ImageView>(R.id.nav_header_image).setImageURI(Uri.parse(inputUri))
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putString(NAV_IMAGE, imageUri)
        outState.putString(NAV_FULL_NAME, fullName)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        supportActionBar?.show()

        val savedImageUri = savedInstanceState.getString(NAV_IMAGE)
        val savedFullName = savedInstanceState.getString(NAV_FULL_NAME)

        if (savedImageUri != null) {
            imageUri = savedImageUri
            //findViewById<ImageView>(R.id.nav_header_image).setImageURI(Uri.parse(imageUri))
        }

        if (savedFullName != null) {
            fullName = savedFullName
        }
        val navView: NavigationView = findViewById(R.id.nav_view)
        imageUri = if (imageUri.isEmpty()) getUriFromResource(R.drawable.default_image).toString() else imageUri
        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        val hView =  navView.getHeaderView(0);
        hView.findViewById<ImageView>(R.id.nav_header_image).setImageURI(Uri.parse(imageUri))
        hView.findViewById<TextView>(R.id.nav_header_full_name).text = fullName
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

}