package it.polito.mad.car_pooling

import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.polito.mad.car_pooling.models.StopLocation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import java.util.*


class MapFragment : Fragment() {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private lateinit var map : MapView
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    val PERMISSION_ID = 1010
    val args: MapFragmentArgs by navArgs()
    lateinit var originListLocation: MutableList<Map<String, String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        RequestPermission()
        getLastLocation()
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val source = args.source
        /*val type = object: TypeToken<MutableList<StopLocation>>(){}.type
        originListLocation = Gson().fromJson<MutableList<StopLocation>>(args.sourceLocation, type)*/
        val type = object: TypeToken<MutableList<Map<String, String>>>(){}.type
        originListLocation = Gson().fromJson(args.sourceLocation, type)

        Configuration.getInstance().load(activity, PreferenceManager.getDefaultSharedPreferences(activity))
        map = view.findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        map.getController().setZoom(18.0)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { currentLocation : Location? ->
            val point = currentLocation?.let { GeoPoint(it.latitude, it.longitude) }
            map.getController().setCenter(point)
        }
        val compassOverlay =  CompassOverlay(requireContext(), map)
        compassOverlay.enableCompass()
        map.getOverlays().add(compassOverlay)
        val startMarker = Marker(map)

        if (source == "addInter") {
            map.overlays.add(object: Overlay() {
                override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                    val projection = mapView.projection
                    val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt())
                    val set_point = GeoPoint(geoPoint.latitude, geoPoint.longitude)
                    val geoCoder = Geocoder(requireContext(), Locale.getDefault())
                    val address = geoCoder.getFromLocation(set_point.latitude, set_point.longitude,1)
                    startMarker.setPosition(set_point)
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    map.getOverlays().add(startMarker)
                    val city = address.get(0).getLocality()
                    val country = address.get(0).getCountryName()
                    val buttonSaveLocation = view.findViewById<Button>(R.id.buttonSaveLocation)
                    buttonSaveLocation.setOnClickListener{
                        val tempLocation = StopLocation(address.get(0).getAddressLine(0))
                        tempLocation.address = address.get(0).getAddressLine(0)
                        tempLocation.latitude = geoPoint.latitude.toString()
                        tempLocation.longitude = geoPoint.longitude.toString()
                        tempLocation.city = city
                        tempLocation.country = country
                        val newList : MutableList<StopLocation> = emptyList<StopLocation>().toMutableList()
                        for (i in 0..originListLocation.size - 1) {
                            val temp = StopLocation(originListLocation[i]["address"]!!)
                            temp.address = (originListLocation[i]["address"]!!)//.split(",")[0]
                            temp.city = (originListLocation[i]["city"]!!)
                            temp.country = (originListLocation[i]["country"]!!)
                            temp.latitude = originListLocation[i]["latitude"]!!
                            temp.longitude = originListLocation[i]["latitude"]!!
                            newList.add(temp)
                        }
                        newList.add(tempLocation)
                        findNavController().previousBackStackEntry?.savedStateHandle?.set("location", Gson().toJson(newList))
                        findNavController().popBackStack()
                    }
                    return true
                }
            })
        } else {
            map.overlays.add(object: Overlay() {
                override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                    val projection = mapView.projection
                    val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt())
                    val set_point = GeoPoint(geoPoint.latitude, geoPoint.longitude)
                    val geoCoder = Geocoder(requireContext(), Locale.getDefault())
                    val address = geoCoder.getFromLocation(set_point.latitude, set_point.longitude,1)
                    startMarker.setPosition(set_point)
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    map.getOverlays().add(startMarker)
                    val city = address.get(0).getLocality()
                    val country = address.get(0).getCountryName()
                    val buttonSaveLocation = view.findViewById<Button>(R.id.buttonSaveLocation)
                    buttonSaveLocation.setOnClickListener{
                        val tempLocation = StopLocation(address.get(0).getAddressLine(0))
                        tempLocation.address = address.get(0).getAddressLine(0)
                        tempLocation.latitude = geoPoint.latitude.toString()
                        tempLocation.longitude = geoPoint.longitude.toString()
                        tempLocation.city = city
                        tempLocation.country = country
                        val key = if (source == "departure") "depLocation" else "arrLocation"
                        Log.d("map!!!!!!", "${tempLocation}")
                        Log.d("map!!!!!!", "${Gson().toJson(tempLocation)}")
                        findNavController().previousBackStackEntry?.savedStateHandle?.set(key, Gson().toJson(tempLocation))
                        findNavController().popBackStack()
                    }
                    return true
                }
            })
        }


        //37.4219983,-122.084
        /*val origin = GeoPoint(37.4219983,-122.084)
        val p1 = GeoPoint(40.4219983,-115.084)
        val p2 = GeoPoint(45.4219983,-110.084)
        var geoPoints = ArrayList<GeoPoint>()
        geoPoints.add(origin)
        geoPoints.add(p1)
        geoPoints.add(p2)
        Log.d("map!!!", "${geoPoints}")
        val line = Polyline()
        line.setPoints(geoPoints)
        map.overlays.add(line)*/
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(){
        if(CheckPermission()){
            if(isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {task->
                    var location:Location? = task.result
                    if(location == null){
                        NewLocationData()
                    }else{
                        Log.d("Debug:" ,"Your Location:"+ location.longitude)
                    }
                }
            }else{
                Toast.makeText(requireContext(),"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
            }
        }else{
            RequestPermission()
        }
    }

    private fun CheckPermission():Boolean{
        if(
            ActivityCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }
        return false
    }

    private fun isLocationEnabled():Boolean{
        var locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun RequestPermission(){
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID)
    }

    @SuppressLint("MissingPermission")
    fun NewLocationData(){
        var locationRequest =  LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }


    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            Log.d("Debug:","your last last location: "+ lastLocation.longitude.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == PERMISSION_ID){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Debug:","You have the Permission")
            }
        }
    }

}