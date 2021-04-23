package it.polito.mad.car_pooling

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

/**
 * A simple [Fragment] subclass.
 * Use the [ShowProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShowProfileFragment : Fragment() {
    private lateinit var imageUri: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        imageUri = ""
        return inflater.inflate(R.layout.profile_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readSharedPreferences(view)
    }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.show_profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.edit_profile -> {
                findNavController().navigate(R.id.nav_edit_profile)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile(){
        //val intent = Intent(this, EditProfileActivity::class.java)
        /*
        val tvFullName = findViewById<TextView>(R.id.textViewFullName)
        val tvNickname = findViewById<TextView>(R.id.textViewNickName)
        val tvEmail = findViewById<TextView>(R.id.textViewEmail)
        val tvLocation = findViewById<TextView>(R.id.textViewLocation)
        val tvBirthday = findViewById<TextView>(R.id.textViewBirthday)
        val tvPhoneNumber = findViewById<TextView>(R.id.textViewPhoneNumber)

        // Get the data as string
        val nameText : String = tvFullName.text.toString()
        val nicknameText: String = tvNickname.text.toString()
        val emailText: String = tvEmail.text.toString()
        val locationText: String = tvLocation.text.toString()
        val birthdayText: String = tvBirthday.text.toString()
        val phonenumberText: String = tvPhoneNumber.text.toString()


        // Populate the Intent
        intent.putExtra("group32.lab1.FULL_NAME", nameText)
        intent.putExtra("group32.lab1.NICK_NAME", nicknameText)
        intent.putExtra("group32.lab1.EMAIL", emailText)
        intent.putExtra("group32.lab1.LOCATION", locationText)
        intent.putExtra("group32.lab1.BIRTHDAY", birthdayText)
        intent.putExtra("group32.lab1.PHONE_NUMBER", phonenumberText)
        intent.putExtra("group32.lab1.IMAGE_URI", imageUri)

        // Start intent
        startActivityForResult(intent,EDIT_PROFILE_CODE)
         */
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