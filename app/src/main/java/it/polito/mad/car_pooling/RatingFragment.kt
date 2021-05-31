package it.polito.mad.car_pooling

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.car_pooling.Utils.ModelPreferencesManager
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.models.Rating
import it.polito.mad.car_pooling.models.TripRequestResponse
import it.polito.mad.car_pooling.viewModels.RatingViewModel
import it.polito.mad.car_pooling.viewModels.RatingViewModelFactory
import java.sql.Driver

@SuppressLint("ValidFragment")
class RatingFragment : Fragment() {

    var mRatingBar: RatingBar? =null
    var mRatingScale: TextView?= null
    var mFeedback:EditText?= null
    var mSendFeedback: Button? =null

    val args:RatingFragmentArgs by navArgs()
    private val TAG = "RatingFragment"

    private lateinit var viewModel: RatingViewModel
    private lateinit var viewModelFactory: RatingViewModelFactory
    private lateinit var tripRequestRespose: TripRequestResponse
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val callback =requireActivity().onBackPressedDispatcher.addCallback(this){
            var bundle:Bundle =Bundle()
            findNavController().navigate(R.id.rv,bundle)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_rating,container,false)
        val tripRequestId = args.tripRequestId

        Log.d("POLITO", "My triprequest id is $tripRequestId")
        viewModelFactory = RatingViewModelFactory(tripRequestId)
        viewModel = viewModelFactory.create(RatingViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRatingBar =view.findViewById<View>(R.id.rating_bar)  as  RatingBar
        mRatingScale = view.findViewById<View>(R.id.tvRatingScale) as TextView
        mFeedback = view.findViewById<View>(R.id.etFeedback) as EditText
        mSendFeedback = view.findViewById<View>(R.id.bt_submit) as Button

        mRatingBar!!.onRatingBarChangeListener =
                RatingBar.OnRatingBarChangeListener { ratingBar, v, b ->
                    mRatingScale!!.text = v.toString()
                    when (ratingBar.rating.toInt()) {
                        1 -> mRatingScale!!.text = "Very bad"
                        2 -> mRatingScale!!.text = "Not bad"
                        3 -> mRatingScale!!.text = "Good"
                        4 -> mRatingScale!!.text = "Great"
                        5 -> mRatingScale!!.text = "Awesome."
                        else -> mRatingScale!!.text = ""
                    }
                }
        mSendFeedback!!.setOnClickListener {
            Toast.makeText(
                    this.context,
                    "Thank you for sharing your feedback",
                    Toast.LENGTH_SHORT

            ).show()
            ratingsave()

        }

        userId = ModelPreferencesManager.get(getString(R.string.keyCurrentAccount))?: "no email"
        viewModel.tripRequestResponse.observe(viewLifecycleOwner, {
            tripRequestRespose = it
            if (tripRequestRespose != null) {
                Log.d(TAG, "Driver email: ${tripRequestRespose.driver.email}, Pass: ${tripRequestRespose.passenger.email}")
            } else {
                Log.d(TAG, "Trip Request response is null: $")
            }

        })
    }

    private fun getNewRating(): Rating {
        val ratingComment: String = mFeedback?.text.toString() ?: ""
        val ratingNumber: Double = mRatingBar?.rating?.toDouble()?: 0.0

        val newRating = Rating(ratingComment, ratingNumber)

        newRating.writer = userId
        newRating.tripId = tripRequestRespose.tripRequest.tripId
        if (userId == tripRequestRespose.driver.email) {
            // I'm the driver!!
            newRating.rated = tripRequestRespose.passenger.email
        } else {
            // I'm the passenger
            newRating.rated = tripRequestRespose.driver.email
        }


        return newRating
    }


    private fun ratingsave() {
        val newRating = getNewRating()
        viewModel.saveRating(newRating)
            .addOnSuccessListener {
                val message =  getString(R.string.ratingCreated)
                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
                        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                        .show()
                Log.d(TAG, "Rating save!!. The new rating is ${newRating.toMap()}")
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                Log.d(TAG, "An error happen createing the new rating")
                findNavController().popBackStack()
            }
    }
}




