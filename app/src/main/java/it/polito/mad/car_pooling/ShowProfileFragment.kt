package it.polito.mad.car_pooling

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.models.Profile

class ShowProfileFragment : Fragment() {
    private lateinit var imageUri: String
    private lateinit var profile: Profile
    val args: ShowProfileFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val isOwner = args.isOwner
        setHasOptionsMenu(isOwner)
        imageUri = ""
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
        //val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val acc_email = args.userId //sharedPreferences.getString(getString(R.string.keyCurrentAccount), "no email")
        val isOwner = args.isOwner
        if (!isOwner) {
            view.findViewById<TextView>(R.id.textViewLocation).visibility = View.INVISIBLE
            view.findViewById<TextView>(R.id.textViewBirthday).visibility = View.INVISIBLE
            view.findViewById<TextView>(R.id.textViewPhoneNumber).visibility = View.INVISIBLE

        }


        val db = FirebaseFirestore.getInstance()

        val users = db.collection("Users")
        if (acc_email != "no email"){
            val my_profile = users.document(acc_email.toString())
            my_profile.addSnapshotListener { value, error ->
                if (error != null) throw error
                if (value != null) {
                    if (value.exists()){
                        view.findViewById<TextView>(R.id.textViewFullName).text = value["full_name"].toString()
                        view.findViewById<TextView>(R.id.textViewNickName).text = value["nick_name"].toString()
                        view.findViewById<TextView>(R.id.textViewEmail).text = value["email"].toString()
                        view.findViewById<TextView>(R.id.textViewLocation).text = value["location"].toString()
                        view.findViewById<TextView>(R.id.textViewBirthday).text = value["birthday"].toString()
                        view.findViewById<TextView>(R.id.textViewPhoneNumber).text = value["phone_number"].toString()
                        val default_str_profile = "android.resource://it.polito.mad.car_pooling/drawable/default_image"
                        val imageView = view.findViewById<ImageView>(R.id.imageViewPhoto)
                        if (value["image_uri"].toString() == "" || value["image_uri"].toString().isEmpty()) {
                            imageUri = default_str_profile
                            imageView.setImageURI(Uri.parse(imageUri))
                        } else {
                            val storage = Firebase.storage
                            val imageRef = storage.reference.child("users/$acc_email.jpg")
                            imageRef.downloadUrl.addOnSuccessListener { Uri ->
                                val image_uri = Uri.toString()
                                Glide.with(this).load(image_uri).into(imageView)
                            }
                        }
                    } else {
                        writeTextView(view)
                        Log.d("showProfile", "${value.exists()} 11111111")
                    }
                }
            }
        }

        else {
            Log.d("showProfile", "22222222222")
            writeTextView(view)
        }
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

    /*
    private fun loadProfileInFields(profile: Profile, view: View) {
        val fullName = profile.fullName //sharedPreferences.getString(getString(R.string.KeyFullName), getString(R.string.fullName))
        val nickname = profile.nickName //sharedPreferences.getString(getString(R.string.KeyNickName), getString(R.string.nickName))
        val email = profile.email // getString(getString(R.string.KeyEmail), getString(R.string.email))
        val location = profile.location //sharedPreferences.getString(getString(R.string.KeyLocation), getString(R.string.location))
        val phoneNumber = profile.phoneNumber //sharedPreferences.getString(getString(R.string.KeyPhoneNumber), getString(R.string.phoneNumber))
        val birthday = profile.birthday //sharedPreferences.getString(getString(R.string.KeyBirthday), getString(R.string.birthday))
        val storedImageUri =  if (profile.imageUri.isEmpty()) getUriFromResource(R.drawable.default_image).toString() else profile.imageUri //sharedPreferences.getString(getString(R.string.KeyImage), getUriFromResource(R.drawable.default_image).toString())

        view.findViewById<TextView>(R.id.textViewFullName).text = if (fullName == null || fullName.isEmpty() || fullName.isBlank()) getString(R.string.fullName) else fullName
        view.findViewById<TextView>(R.id.textViewNickName).text = if (nickname == null || nickname.isEmpty() || nickname.isBlank()) getString(R.string.nickName) else nickname
        view.findViewById<TextView>(R.id.textViewEmail).text = if (email == null || email.isEmpty() || email.isBlank()) getString(R.string.email) else email
        view.findViewById<TextView>(R.id.textViewLocation).text = if (location == null || location.isEmpty() || location.isBlank()) getString(R.string.location) else location
        view.findViewById<TextView>(R.id.textViewPhoneNumber).text = if (phoneNumber == null || phoneNumber.isEmpty() || phoneNumber.isBlank()) getString(R.string.phoneNumber) else phoneNumber
        view.findViewById<TextView>(R.id.textViewBirthday).text = if (birthday == null || birthday.isEmpty() || birthday.isBlank()) getString(R.string.birthday) else birthday

        view.findViewById<ImageView>(R.id.imageViewPhoto).setImageURI(Uri.parse(storedImageUri))
        imageUri = storedImageUri.toString()
    } */

    /* This code will be deleted
    private fun readSharedPreferences (view: View) {
        val sharedPreferences = this.requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        // Get stored data
        val fullName = sharedPreferences.getString(getString(R.string.KeyFullName), getString(R.string.fullName))
        val nickname = sharedPreferences.getString(getString(R.string.KeyNickName), getString(R.string.nickName))
        val email = sharedPreferences.getString(getString(R.string.KeyEmail), getString(R.string.email))
        val location = sharedPreferences.getString(getString(R.string.KeyLocation), getString(R.string.location))
        val phoneNumber = sharedPreferences.getString(getString(R.string.KeyPhoneNumber), getString(R.string.phoneNumber))
        val birthday = sharedPreferences.getString(getString(R.string.KeyBirthday), getString(R.string.birthday))
        val storedImageUri =  sharedPreferences.getString(getString(R.string.KeyImage), getUriFromResource(R.drawable.default_image).toString())

        // Set stored data
        view.findViewById<TextView>(R.id.textViewFullName).text = if (fullName == null || fullName.isEmpty() || fullName.isBlank()) getString(R.string.fullName) else fullName
        view.findViewById<TextView>(R.id.textViewNickName).text = if (nickname == null || nickname.isEmpty() || nickname.isBlank()) getString(R.string.nickName) else nickname
        view.findViewById<TextView>(R.id.textViewEmail).text = if (email == null || email.isEmpty() || email.isBlank()) getString(R.string.email) else email
        view.findViewById<TextView>(R.id.textViewLocation).text = if (location == null || location.isEmpty() || location.isBlank()) getString(R.string.location) else location
        view.findViewById<TextView>(R.id.textViewPhoneNumber).text = if (phoneNumber == null || phoneNumber.isEmpty() || phoneNumber.isBlank()) getString(R.string.phoneNumber) else phoneNumber
        view.findViewById<TextView>(R.id.textViewBirthday).text = if (birthday == null || birthday.isEmpty() || birthday.isBlank()) getString(R.string.birthday) else birthday

        view.findViewById<ImageView>(R.id.imageViewPhoto).setImageURI(Uri.parse(storedImageUri))
        imageUri = storedImageUri.toString()
    }
     */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.show_profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.edit_profile -> {
                // Here comes the arguments
                //val editProfileArgs = ShowProfileFragmentDirections.actionShowProfileFragmentToEditProfileFragment(profile.id)
                //findNavController().navigate(editProfileArgs)
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