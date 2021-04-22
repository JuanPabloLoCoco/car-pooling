package it.polito.mad.car_pooling

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView



data class  Model(val name: String= "", val count: Int = 0)

class TripListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // val  rv= requireView().findViewById<RecyclerView>(R.id.rv)
        return inflater.inflate(R.layout.fragment_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val reciclerView = view.findViewById<RecyclerView>(R.id.rv)
        reciclerView.layoutManager = LinearLayoutManager(requireContext())
        val dataList = ArrayList<Model>()
        dataList.add(Model("Phone", 1))
        dataList.add(Model("Watch", 2))
        dataList.add(Model("Note", 3))
        dataList.add(Model("Pin", 4))
        dataList.add(Model("Pin1", 5))
        dataList.add(Model("Pin2", 6))
        dataList.add(Model("Pin3", 7))
        val rvAdapter = ModelAdapter(dataList.shuffled())
        reciclerView.adapter = rvAdapter
        // Log.d("POLITO_ERRORS", "Recicler view es null: " + (reciclerView == null).toString())
    }
}

class ModelAdapter (val userList: List<Model>): RecyclerView.Adapter<ModelAdapter.ModelViewHolder>() {

    class ModelViewHolder(v: View): RecyclerView.ViewHolder(v){
        //val nameView = v.findViewById<TextView>(R.id.text)
        val name = v.findViewById<TextView>(R.id.tvName)
        val count = v.findViewById<TextView>(R.id.tvCount)

        fun bind(m: Model) {

        }
        fun unbind(){

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ModelViewHolder(v)
    }

    override fun onViewRecycled(holder: ModelViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.name?.text = userList[position].name
        holder.count?.text = userList[position].count.toString()
    }

    override fun getItemCount(): Int {
        return userList.size
    }

}