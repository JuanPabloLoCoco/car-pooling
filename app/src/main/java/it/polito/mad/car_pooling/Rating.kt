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
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.car_pooling.models.Profile
import it.polito.mad.car_pooling.viewModels.EditProfileViewModel
import it.polito.mad.car_pooling.viewModels.EditProfileViewModelFactory
import java.sql.Driver

@SuppressLint("ValidFragment")
class Rating : Fragment() {

    var mRatingBar: RatingBar? =null
    var mRatingScale: TextView?= null
    var mFeedback:EditText?= null
    var mSendFeedback: Button? =null

    val args:RatingArgs by navArgs()

//    var userSrc: ProfileInformation = PrifileInfromation()
//    var userDst: ProfileInformation = ProfileInformation()

//    private lateinit var userSrc: EditProfileViewModel
//    private lateinit var userDst: EditProfileViewModel
//    private lateinit var viewModelFactory: EditProfileViewModelFactory
//    private lateinit var profile: Profile
    var rateBefore:Int = -1
    val db = Firebase.firestore
    var userIdSrc:String = ""
    var userIdDst: String = ""
    fun UserRating(){

    }

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
        mRatingBar =view.findViewById<View>(R.id.rating_bar)  as  RatingBar
        mRatingScale = view.findViewById<View>(R.id.tvRatingScale) as TextView
        mFeedback = view.findViewById<View>(R.id.etFeedback) as EditText
        mSendFeedback = view.findViewById<View>(R.id.bt_submit) as Button
        val tripRequestId = args.tripRequestId
        Log.d("POLITO", "My triprequest id is $tripRequestId")

//        userModel = ViewModelProviders.of(requireActivity()).get(UserModelView::class.java)
       // userModel =ViewModelProviders.of(requireActivity()).get(EditProfileViewModel::class.java)

//        userModel?.getUserFeedback(userIdDst)?.observe(viewLifecycleOwner, Observer<EditProfileViewModel>{user->
//            userModel = user
//            fillFeedback()
//        })
      //  return inflater.inflate(R.layout.fragment_rating, container, false)

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
        return view
    }

    fun fillFeedback() {
//        var count:Int = 0
//        var obj = userDst?.feedBacks!![userIdSrc]
//        if(obj != null){
//            mFeedback?.setText( obj?.feedback!!)
//            mRatingBar?.rating = obj?.rate!!.toFloat()
//        }
    }
    private fun ratingsave() {

//        userModel = requireView().findViewById<EditText>(R.id.etFeedback).editText?.text.toString()
//        mRatingScale = requireView().findViewById<EditText>(R.id.etFeedback).editableText?.text.toString()
//        userModel?.addUserFeedback(userIdSrc,userIdDst,mRatingBar?.rating!!.toInt(),mFeedback?.text.toString())
//
//        var bundle: Bundle = Bundle()
//        bundle.putSerializable("PassData",userDst)
//        val navOptions = NavOptions.Builder()
//            .setEnterAnim(android.R.anim.slide_in_left)
//            .setExitAnim(android.R.anim.slide_out_right)
//            .setPopEnterAnim(android.R.anim.slide_in_left)
//            .setPopExitAnim(android.R.anim.slide_out_right)
//            .build()
//        findNavController().navigate(R.id.ShowProfileFragment,bundle,navOptions)
    }
}




