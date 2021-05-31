package it.polito.mad.car_pooling

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.models.Rating
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.viewModels.ProfileViewModel
import it.polito.mad.car_pooling.viewModels.ProfileViewModelFactory
import java.io.File
import java.lang.Double

class ShowProfileFragment : Fragment() {
    private lateinit var imageUri: String
    private var profile: Profile? = null
    private lateinit var adapter: RatingListCardAdapter
    private lateinit var appBarConfiguration: AppBarConfiguration
    val args: ShowProfileFragmentArgs by navArgs()

    private lateinit var viewModel: ProfileViewModel
    private lateinit var viewModelFactory: ProfileViewModelFactory
    private lateinit var userId: String
    private lateinit var ratingList: List<Rating>

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

        return inflater.inflate(R.layout.profile_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isOwner = args.isOwner
        if (!isOwner) {
            view.findViewById<TextView>(R.id.textViewLocation).visibility = View.GONE
            view.findViewById<TextView>(R.id.textViewBirthday).visibility = View.GONE
            view.findViewById<TextView>(R.id.textViewPhoneNumber).visibility = View.GONE
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

        // Fake Rating List
        ratingList = listOf(
                Rating("Bad trip,Bad trip,Bad trip,Bad trip,Bad trip,Bad trip,Bad trip,Bad trip,Bad trip,",1.0),
                Rating("Good Driver", 4.5),
                Rating("The path was nice", 3.0)
        )

        val reciclerView = view.findViewById<RecyclerView>(R.id.rateRV)
        reciclerView.layoutManager = LinearLayoutManager(requireContext())

        val ratingTitleView = view.findViewById<TextView>(R.id.userRating)

        adapter = RatingListCardAdapter(ratingList, requireContext())
        reciclerView.adapter = adapter

        reciclerView.visibility = View.GONE
        ratingTitleView.visibility = View.GONE

        viewModel.profileRatingList.observe(viewLifecycleOwner, {
            ratingList = it
            adapter.updateCollection(ratingList)
            if (ratingList.isEmpty()) {
                reciclerView.visibility = View.GONE
                ratingTitleView.visibility = View.GONE
            } else {
                reciclerView.visibility = View.VISIBLE
                ratingTitleView.visibility = View.VISIBLE
                
                val ratingAvg = it.map { r -> r.ratingNumber }.average()
                val finalAvg = Math.floor(ratingAvg * 100) / 100
                ratingTitleView.text = "${getString(R.string.userRating)}: ${finalAvg}/5"
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

class RatingListCardAdapter(
        var ratingList: List<Rating>,
        val context: Context,
        ): RecyclerView.Adapter<RatingListCardAdapter.RatingCardViewHolder>() {

    class RatingCardViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val commentTextView =v.findViewById<TextView>(R.id.commentextView)
        val ratingbarView = v.findViewById<RatingBar>(R.id.ratingBar2)
        val reviewName = v.findViewById<TextView>(R.id.reviewuserView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingCardViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.ratingbar, parent, false)
        return RatingCardViewHolder(v)
    }

    fun getStringFromField(field: String?): String {
        return if (field == null) "" else field
    }

    override fun onBindViewHolder(holder: RatingCardViewHolder, position: Int) {
        val selecterRating: Rating = ratingList[position]
        holder.commentTextView.text= selecterRating.comment
        holder.ratingbarView.rating =selecterRating.ratingNumber.toFloat()
        holder.reviewName.text = "${selecterRating.writer} said: "
    }

    override fun getItemCount(): Int {
        return ratingList.size
    }

    fun updateCollection(newRatingList: List<Rating>) {
        ratingList = newRatingList
        notifyDataSetChanged()
    }


}
