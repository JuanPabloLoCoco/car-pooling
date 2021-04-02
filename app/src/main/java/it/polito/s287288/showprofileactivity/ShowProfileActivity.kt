package it.polito.s287288.showprofileactivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class ShowProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_layout)
        val image = findViewById<ImageView>(R.id.imageView1)
        //image.setImageResource(R.drawable.default_image)
        val name = findViewById<TextView>(R.id.textView1)
        val nickname = findViewById<TextView>(R.id.textView2)
        val email = findViewById<TextView>(R.id.textView3)
        val location = findViewById<TextView>(R.id.textView4)
        val birthday = findViewById<TextView>(R.id.textView5)
        val phoneNumber = findViewById<TextView>(R.id.textView6)
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
        startActivityForResult(intent,1)
    }
}