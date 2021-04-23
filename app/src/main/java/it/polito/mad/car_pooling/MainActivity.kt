package it.polito.mad.car_pooling

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import com.google.android.material.badge.BadgeDrawable

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

        imageUri = ""
        fullName = ""
        readSharedPreferences()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        val hView =  navView.getHeaderView(0);
        hView.findViewById<ImageView>(R.id.nav_header_image).setImageURI(Uri.parse(imageUri))
        hView.findViewById<TextView>(R.id.nav_header_full_name).text = fullName

        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home, R.id.nav_profile, R.id.nav_list_trip), drawerLayout)
        // appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home, R.id.nav_profile, R.id.nav_trip), drawerLayout)
        //var badge = navController.
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //navView.findViewById<ImageView>(R.id.nav_header_image).setImageURI(Uri.parse(imageUri))
    }


    private fun readSharedPreferences() {
        val sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        // Get Stored Data
        fullName = sharedPreferences.getString(getString(R.string.KeyFullName), getString(R.string.fullName)).toString()
        val storedImageUri =  sharedPreferences.getString(getString(R.string.KeyImage), getUriFromResource(R.drawable.default_image).toString())
        //findViewById<ImageView>(R.id.nav_header_image).setImageURI(Uri.parse(storedImageUri))
        // findViewById<TextView>(R.id.nav_header_full_name).text = fullName

        imageUri = storedImageUri.toString()

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