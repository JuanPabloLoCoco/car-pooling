package it.polito.mad.car_pooling

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TripListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val  rv= requireView().findViewById<RecyclerView>(R.id.rv)

        return inflater.inflate(R.layout.fragment_trip_list, container, false)
    }


}
