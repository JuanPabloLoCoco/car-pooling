package it.polito.mad.car_pooling

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

class OthersTripListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.fragment_others_trip_list, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.depLocationSearch -> {
                return true
            }
            R.id.ariLocationSearch -> {
                return true
            }
            R.id.depDateTimeSearch -> {
                return true
            }
            R.id.avaSeatsSearch -> {
                return true
            }
            R.id.priceSearch -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}