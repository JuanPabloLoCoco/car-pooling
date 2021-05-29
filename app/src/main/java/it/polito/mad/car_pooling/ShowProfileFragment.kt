package it.polito.mad.car_pooling

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.viewModels.ProfileViewModel
import it.polito.mad.car_pooling.viewModels.ProfileViewModelFactory

class ShowProfileFragment : Fragment() {
    private lateinit var imageUri: String
    private var profile: Profile? = null

    private lateinit var appBarConfiguration: AppBarConfiguration
    val args: ShowProfileFragmentArgs by navArgs()

    private lateinit var viewModel: ProfileViewModel
    private lateinit var viewModelFactory: ProfileViewModelFactory
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val isOwner = args.isOwner
        setHasOptionsMenu(isOwner)
        imageUri = ""


        userId = args.userId
        if (userId == "no email") {
            userId = ModelPreferencesManager.get(getString(R.string.keyCurrentAccount))?: "no email"
        }
        viewModelFactory = ProfileViewModelFactory(userId)
        viewModel = viewModelFactory.create(ProfileViewModel::class.java)

        //if (args.userId == "no email") (ModelPreferencesManager.get(getString(R.string.keyCurrentAccount))?: "no email") else args.userId
        // Create the instances of the viewModel class

        return inflater.inflate(R.layout.profile_layout, container, false)
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
         */
        // val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        //var acc_email = args.userId //sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email")
        val isOwner = args.isOwner
        if (!isOwner) {
            view.findViewById<TextView>(R.id.textViewLocation).visibility = View.INVISIBLE
            view.findViewById<TextView>(R.id.textViewBirthday).visibility = View.INVISIBLE
            view.findViewById<TextView>(R.id.textViewPhoneNumber).visibility = View.INVISIBLE
        /*val toolbar: Toolbar = (activity as AppCompatActivity).findViewById(R.id.toolbar)
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
            toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            toolbar.setNavigationOnClickListener(View.OnClickListener(){
                val drawerLayout: DrawerLayout = (activity as AppCompatActivity).findViewById(R.id.drawer_layout)
                val navView: NavigationView = (activity as AppCompatActivity).findViewById(R.id.nav_view)
                val navController = (activity as AppCompatActivity).findNavController(R.id.nav_host_fragment)
                appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_other_list_trip, R.id.nav_list_trip, R.id.nav_profile), drawerLayout)
                (activity as AppCompatActivity).setupActionBarWithNavController(navController, appBarConfiguration)
                navView.setupWithNavController(navController)
                requireActivity().onBackPressed()
                //findNavController().popBackStack()
            })*/
        }

        val imageView = view.findViewById<ImageView>(R.id.imageViewPhoto)
        val default_str_profile = "android.resource://it.polito.mad.car_pooling/drawable/default_image"

        viewModel.profile.observe(viewLifecycleOwner, {
            profile = it
            if (it == null) {
                Log.d("POLITO", "user is null")
                writeTextView(view)
            } else {
                loadProfileInFields(it, view)
                // The profile is not null
                if (it.hasImage == true) {
                    val storage = Firebase.storage
                    val imageRef = storage.reference.child("users/${it.email}.jpg")
                    imageRef.downloadUrl.addOnSuccessListener { Uri ->
                        val image_uri = Uri.toString()
                        Glide.with(this)
                            .load(image_uri)
                            .into(imageView)
                    }
                } else {
                    imageUri = default_str_profile
                    imageView.setImageURI(Uri.parse(imageUri))
                }
            }
        })
    }

    private fun writeTextView(view: View){
        val default_str_profile = "android.resource://it.polito.mad.car_pooling/drawable/default_image"
        view.findViewById<TextView>(R.id.textViewFullName).text = "Full Name"
        view.findViewById<TextView>(R.id.textViewNickName).text = "Nick Name"
        view.findViewById<TextView>(R.id.textViewEmail).text = "Email@Address"
        view.findViewById<TextView>(R.id.textViewLocation).text = "Location"
        view.findViewById<TextView>(R.id.textViewBirthday).text = "Birthday"
        view.findViewById<TextView>(R.id.textViewPhoneNumber).text = "PhoneNumber"
        imageUri = default_str_profile
        view.findViewById<ImageView>(R.id.imageViewPhoto).setImageURI(Uri.parse(imageUri))
    }


    private fun loadProfileInFields(profile: Profile, view: View) {
        view.findViewById<TextView>(R.id.textViewFullName).text = profile.fullName
        view.findViewById<TextView>(R.id.textViewNickName).text = profile.nickName
        view.findViewById<TextView>(R.id.textViewEmail).text = profile.email
        view.findViewById<TextView>(R.id.textViewLocation).text = profile.location
        view.findViewById<TextView>(R.id.textViewPhoneNumber).text = profile.phoneNumber
        view.findViewById<TextView>(R.id.textViewBirthday).text = profile.birthday
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.show_profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.edit_profile -> {
                // Here comes the arguments
                findNavController().navigate(R.id.nav_edit_profile)
                true
            }
            else -> super.onOptionsItemSelected(item)
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
}