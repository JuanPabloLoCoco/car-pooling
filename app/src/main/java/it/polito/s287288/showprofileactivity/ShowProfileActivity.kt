package it.polito.s287288.showprofileactivity

import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class ShowProfileActivity : AppCompatActivity() {

    lateinit var imageUri: String;

    private val EDIT_PROFILE_CODE : Int = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_layout)


        //image.setImageResource(R.drawable.default_image)

        // Instantiate each variable

        imageUri = ""
        readSharedPreferences();
        //name.text = "Song Tailai"
        //nickname.text = "Song's nickname"
        //email.text = "s287288@polito.it"
        //location.text = "Torino Politecnico"
        //bithday.text = "yyyy/mm/dd"
        //phoneNumber.text = "123456789"
        /*val Song = ProfileUser(image,
       name.text.toString(),
       nickname.text.toString(),
       email.text.toString(),
       location.text.toString(),
       birthday.text.toString(),
       phoneNumber.text.toString()) */

    }

    //data class ProfileUser(val image:ImageView, val name:String, val nickname:String, val email:String, val location:String, val birthday:String, val phoneNumber:String){}

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.show_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.edit_profile -> {
                editProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile(){
        val intent = Intent(this, EditProfileActivity::class.java)

        val tvFullName = findViewById<TextView>(R.id.textViewFullName)
        val tvNickname = findViewById<TextView>(R.id.textViewNickName)
        val tvEmail = findViewById<TextView>(R.id.textViewEmail)
        val tvLocation = findViewById<TextView>(R.id.textViewLocation)
        val tvBirthday = findViewById<TextView>(R.id.textViewBirthday)
        val tvPhoneNumber = findViewById<TextView>(R.id.textViewPhoneNumber)

        // Get the data as string
        val nameText : String = tvFullName.text.toString();
        val nicknameText: String = tvNickname.text.toString();
        val emailText: String = tvEmail.text.toString();
        val locationText: String = tvLocation.text.toString();
        val birthdayText: String = tvBirthday.text.toString();
        val phonenumberText: String = tvPhoneNumber.text.toString();


        // Populate the Intent
        intent.putExtra("group32.lab1.FULL_NAME", nameText)
        intent.putExtra("group32.lab1.NICK_NAME", nicknameText)
        intent.putExtra("group32.lab1.EMAIL", emailText)
        intent.putExtra("group32.lab1.LOCATION", locationText)
        intent.putExtra("group32.lab1.BIRTHDAY", birthdayText)
        intent.putExtra("group32.lab1.PHONE_NUMBER", phonenumberText)
        intent.putExtra("group32.lab1.IMAGE_URI", imageUri)

        // Start intent
        startActivityForResult(intent,EDIT_PROFILE_CODE);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EDIT_PROFILE_CODE && resultCode == RESULT_OK) {
            val tvFullName = findViewById<TextView>(R.id.textViewFullName)
            val tvNickname = findViewById<TextView>(R.id.textViewNickName)
            val tvEmail = findViewById<TextView>(R.id.textViewEmail)
            val tvLocation = findViewById<TextView>(R.id.textViewLocation)
            val tvBirthday = findViewById<TextView>(R.id.textViewBirthday)
            val tvPhoneNumber = findViewById<TextView>(R.id.textViewPhoneNumber)
            val ivPhoto = findViewById<ImageView>(R.id.imageViewPhoto)

            val fullName = data?.getStringExtra("group32.lab1.FULL_NAME")
            val nickName = data?.getStringExtra("group32.lab1.NICK_NAME")
            val email = data?.getStringExtra("group32.lab1.EMAIL")
            val location = data?.getStringExtra("group32.lab1.LOCATION")
            val birthday = data?.getStringExtra("group32.lab1.BIRTHDAY")
            val phoneNumber = data?.getStringExtra("group32.lab1.PHONE_NUMBER")
            val dataImageUri = data?.getStringExtra("group32.lab1.IMAGE_URI")

            tvFullName.text = fullName
            if (fullName != null) {
                Log.d("POLITO_ERROR", fullName)
            }
            tvNickname.text = nickName
            tvEmail.text = email
            tvLocation.text = location
            tvBirthday.text = birthday
            tvPhoneNumber.text = phoneNumber

            if (dataImageUri != null && dataImageUri.isNotEmpty()) {
                ivPhoto.setImageURI(Uri.parse(dataImageUri))
                imageUri = dataImageUri.toString()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun readSharedPreferences () {
        val sharedPreferences = this?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        val fullName = sharedPreferences.getString(getString(R.string.KeyFullName), getString(R.string.fullName))
        val nickname = sharedPreferences.getString(getString(R.string.KeyNickName), getString(R.string.nickName))
        val email = sharedPreferences.getString(getString(R.string.KeyEmail), getString(R.string.email))
        val location = sharedPreferences.getString(getString(R.string.KeyLocation), getString(R.string.location))
        val phoneNumber = sharedPreferences.getString(getString(R.string.KeyPhoneNumber), getString(R.string.phoneNumber))
        val birthday = sharedPreferences.getString(getString(R.string.KeyBirthday), getString(R.string.birthday))

        findViewById<TextView>(R.id.textViewFullName).text = fullName
        findViewById<TextView>(R.id.textViewNickName).text = nickname
        findViewById<TextView>(R.id.textViewEmail).text = email
        findViewById<TextView>(R.id.textViewLocation).text = location
        findViewById<TextView>(R.id.textViewPhoneNumber).text = phoneNumber
        findViewById<TextView>(R.id.textViewBirthday).text = birthday
    }
}