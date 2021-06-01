package it.polito.mad.car_pooling

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView

class StartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startTextImage1 = view.findViewById<ImageView>(R.id.imageViewText1)
        val startTextImage2 = view.findViewById<ImageView>(R.id.imageViewText2)
        val startAnimationView = view.findViewById<LottieAnimationView>(R.id.lottieAnimationViewStart)
        val skipButton = view.findViewById<Button>(R.id.skipButton)

        startTextImage1.animate().translationY(-1600f).setDuration(1000).startDelay = 4000
        startTextImage2.animate().translationY(-1600f).setDuration(1000).startDelay = 4000
        startAnimationView.animate().translationY(1600f).setDuration(1000).startDelay = 4000
        skipButton.animate().translationY(1600f).setDuration(1000).startDelay = 4000

        val handler = Handler()
        handler.postDelayed(Runnable {
            (activity as AppCompatActivity).supportActionBar?.show()
            findNavController().navigate(R.id.singInFragment)
        },6300)

        skipButton.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            (activity as AppCompatActivity).supportActionBar?.show()
            findNavController().navigate(R.id.singInFragment)
        }
    }
}