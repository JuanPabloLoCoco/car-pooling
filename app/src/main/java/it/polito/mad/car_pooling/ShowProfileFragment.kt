package it.polito.mad.car_pooling

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.models.Rating
import it.polito.mad.car_pooling.models.Trip
import it.polito.mad.car_pooling.viewModels.ProfileViewModel
import it.polito.mad.car_pooling.viewModels.ProfileViewModelFactory

class ShowProfileFragment : Fragment() {
    private lateinit var imageUri: String
    private var profile: Profile? = null
    private lateinit var adapter: RatingListCardAdapter
    private lateinit var appBarConfiguration: AppBarConfiguration
    val args: ShowProfileFragmentArgs by navArgs()

    private lateinit var viewModel: ProfileViewModel
    private lateinit var viewModelFactory: ProfileViewModelFactory
    private lateinit var userId: String
    private lateinit var ratingList: MutableList<Rating>

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
            val db = FirebaseFirestore.getInstance()
            val users = db.collection("Users")
            val profile = users.document(userId)
            profile.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("showProfileFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val hideAll = snapshot["hideAll"].toString().toBoolean()
                    val showLocation = snapshot["showLocation"].toString().toBoolean()
                    val showBirthday = snapshot["showBirthday"].toString().toBoolean()
                    val showPhoneNumber = snapshot["showPhoneNumber"].toString().toBoolean()
                    if (hideAll) {
                        if (!showLocation) {
                            val view1 = view.findViewById<TextView>(R.id.textViewLocation)
                            view1.visibility = View.INVISIBLE
                            val params1: ViewGroup.LayoutParams = view1.layoutParams
                            params1.height = 0
                            view1.layoutParams = params1
                        }

                        if (!showBirthday) {
                            val view2 = view.findViewById<TextView>(R.id.textViewBirthday)
                            view2.visibility = View.INVISIBLE
                            val param2: ViewGroup.LayoutParams = view2.layoutParams
                            param2.height = 0
                            view2.layoutParams = param2
                        }

                        if (!showPhoneNumber) {
                            val view3 = view.findViewById<TextView>(R.id.textViewPhoneNumber)
                            view3.visibility = View.INVISIBLE
                            val params3: ViewGroup.LayoutParams = view3.layoutParams
                            params3.height = 0
                            view3.layoutParams = params3
                        }
                    }

                    val idx = requireActivity().fragmentManager.backStackEntryCount
                    val backEntry = fragmentManager?.getBackStackEntryAt(idx)
                    val tag = backEntry?.name
                    if (tag == "2-2131296624") {
                        Log.d("showProfile!!!!!!","$tag")
                        val view3 = view.findViewById<TextView>(R.id.textViewPhoneNumber)
                        view3.visibility = View.VISIBLE
                        val params3: ViewGroup.LayoutParams = view3.layoutParams
                        params3.height = 140
                        view3.layoutParams = params3
                    }
                } else {
                    Log.d("showProfileFragment", "Current data: null")
                }
            }
        } else {
            val view = view.findViewById<Button>(R.id.sendEmailButton)
            view.visibility = View.INVISIBLE
            val params: ViewGroup.LayoutParams = view.layoutParams
            params.height = 0
            view.layoutParams = params
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
        ratingList = mutableListOf(
                Rating("Bad trip,Bad trip,Bad trip,Bad trip,Bad trip,Bad trip,Bad trip,Bad trip,Bad trip,",1.0),
                Rating("Good Driver", 4.5),
                Rating("The path was nice", 3.0)
        )
        val reciclerView = view.findViewById<RecyclerView>(R.id.rateRV)
        reciclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = RatingListCardAdapter(ratingList, requireContext())
        reciclerView.adapter = adapter

        val sendEmailButton = view?.findViewById<Button>(R.id.sendEmailButton)
        if (sendEmailButton != null) {
            sendEmailButton.setOnClickListener{
                val action = ShowProfileFragmentDirections.actionNavProfileToSendEmailFragment(view.findViewById<TextView>(R.id.textViewEmail).text.toString())
                findNavController().navigate(action)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val isOwner = args.isOwner
        if (!isOwner){
            val toolbar : Toolbar = (activity as AppCompatActivity).findViewById(R.id.toolbar)
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
            toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            toolbar.setNavigationOnClickListener(View.OnClickListener(){
                requireActivity().onBackPressed()
                //findNavController().popBackStack()
            })
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
        /*

        val departureLocationView = v.findViewById<TextView>(R.id.depatureview)
        val arriveLocationView = v.findViewById<TextView>(R.id.arriveview)
        val departureTimeView = v.findViewById<TextView>(R.id.timeview)
        val priceView = v.findViewById<TextView>(R.id.priceview)
        val availableSeatsView = v.findViewById<TextView>(R.id.tripAvailableSeatsField)
        val tripImageView = v.findViewById<ImageView>(R.id.imageview)
        val tripCardView = v.findViewById<CardView>(R.id.tripCard)
          */
        fun bind(t: Trip) {

        }

        fun unbind() {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingCardViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.ratingbar, parent, false)
        return RatingCardViewHolder(v)
    }

    override fun onViewRecycled(holder: RatingCardViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
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


}
