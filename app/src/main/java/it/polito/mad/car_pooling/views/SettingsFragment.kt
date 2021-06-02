package it.polito.mad.car_pooling.views

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.car_pooling.R

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var account_email: String

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        account_email = sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email").toString()
        val sp_edit = PreferenceManager.getDefaultSharedPreferences(context).edit()
        sp_edit.putString("current_account", "$account_email").apply()

    }

    override fun onResume() {
        super.onResume()
        val toolbar : Toolbar = (activity as AppCompatActivity).findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = (activity as AppCompatActivity).findViewById(R.id.drawer_layout)
        val navView: NavigationView = (activity as AppCompatActivity).findViewById(R.id.nav_view)
        val navController = (activity as AppCompatActivity).findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_other_list_trip, R.id.nav_list_trip, R.id.nav_profile, R.id.nav_list_interest_trip, R.id.nav_list_bought_trip, R.id.nav_setting), drawerLayout)
        (activity as AppCompatActivity).setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_dehaze_24)
    }

    override fun onPause() {
        super.onPause()

        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val hideAllRef = sp.getBoolean("hideAll",false)
        val showLocationRef= sp.getBoolean("showLocation",false)
        val showBirthdayRef = sp.getBoolean("showBirthday",false)
        val showPhoneNumberRef = sp.getBoolean("showPhoneNumber",false)
        val hidePlateRef = sp.getBoolean("hidePlate",false)

        Log.d("setting!!!!", "$hideAllRef")
        Log.d("setting!!!!", "$account_email")

        val db = FirebaseFirestore.getInstance()
        val users = db.collection("Users")
        val my_profile = users.document(account_email)
        my_profile.update("hideAll", hideAllRef.toString(),
            "showLocation", showLocationRef.toString(),
                                "showBirthday", showBirthdayRef.toString(),
                                "showPhoneNumber", showPhoneNumberRef.toString(),
                                "hidePlate", hidePlateRef.toString())
            .addOnSuccessListener { Log.d("settingFragment", "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w("settingFragment", "Error updating document", e) }
    }

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val signOutButton = activity?.findViewById<Button>(R.id.sign_out_button)
        signOutButton?.setOnClickListener{
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("785578307134-aa8d3165sln0kqhm489fdmvt0nfntuph.apps.googleusercontent.com")
                //.requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            googleSignInClient.signOut()
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.singInFragment)
            val drawerLayout: DrawerLayout = view.findViewById(R.id.drawer_layout)
            drawerLayout.closeDrawers()
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }*/
}