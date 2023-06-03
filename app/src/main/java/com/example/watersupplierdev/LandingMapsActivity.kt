package com.example.watersupplierdev

//import android.R
//import com.google.protobuf.Parser

//import org.chromium.base.Promise

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.beust.klaxon.*
import com.example.watersupplierdev.databinding.ActivityLandingMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.common.base.Utf8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.text.StringBuilder as StringBuilder1


//import com.beust.klaxon

//import org.jetbrains.anko.async
//import org.jetbrains.anko.uiThread
//import java.util.jar.Manifest


class LandingMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityLandingMapsBinding
    private val TAG = LandingMapsActivity::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLandingMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera
        // val sydney = LatLng(-34.0, 151.0)
        val home = LatLng(19.878351, 73.841438)
        val zoom = 19f
        val overlaySize = 100f

        val androidOverlay = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(R.drawable.icons8_location_start))
            .position(home, overlaySize)

        map.addGroundOverlay(androidOverlay)

        map.addMarker(MarkerOptions().position(home).title("Marker in Home"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, zoom))
        setMapLongClick(map, home)
        setPoiClick(map)
        //Set the map style from JSON map_styles.json
        setMapStyle(map)

        enableMyLocation()
    }

    //override the onCreateOptionsMenu() method and inflate the map_options file
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    //To change the map type, use the setMapType() method on the GoogleMap object,
    // passing in one of the map-type constants.
    //Override the onOptionsItemSelected() method.
    // The following code changes the map type when the user selects one of the menu options
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Change the map type based on the user's selection.
        return when (item.itemId) {
            R.id.normal_map -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.hybrid_map -> {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            R.id.satellite_map -> {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_map -> {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //    Create a method stub in LandingMaps Activity called setMapLongClick()
    //    that takes final GoogleMap as an argument and returns void:
    private fun setMapLongClick(map: GoogleMap, home: LatLng) {
        map.setOnMapLongClickListener { latLng ->
            // A snippet is additional text that's displayed after the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_location_end))
            )

            // Declare polyline object and set up color and width
            val options = PolylineOptions()
            options.color(Color.RED)
            options.width(5f)


            // declare bounds object to fit whole route in screen
            val LatLongB = LatLngBounds.Builder()


            // build URL to call API for directions
            val url = getURLforResponse(home, latLng)
            Log.i("INFO - Directions", url);

            // build URL to call API for distance
            val urlDistance = getURLforDistanceAPIResponse(home, latLng)
            Log.i("INFO - Distance", urlDistance);

            var result = ""

            lifecycleScope.executeAsyncTask(onPreExecute = {
                // ...
                }, doInBackground = {
                    // ...
                    // "Result" // send data to "onPostExecute"

                    // Connect to URL, download content and convert into string asynchronously
                    result = URL(url).readText()
                Log.i("INFO - URL Response", result);


                Log.i("INFO - URL Response", URL(urlDistance).readText());

                    return@executeAsyncTask result
                }, onPostExecute = {
                    // ... here "it" is a data returned from "doInBackground"
                    // When API call is done, create parser and convert into JsonObjec
                    val parser: Parser = Parser.default()
                    val stringBuilder: StringBuilder1 = StringBuilder1(result)
                    val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                    // get to the correct element in JsonObject
                    val routes = json.array<JsonObject>("routes")
                    val points = routes!!["legs"]["steps"][0] as JsonArray<JsonObject>
                    // For every element in the JsonArray, decode the polyline string and pass all points to a List
                    val polypts = points.flatMap { decodePoly(it.obj("polyline")?.string("points")!!)  }
                    // Add  points to polyline and bounds
                    options.add(home)
                    LatLongB.include(home)
                    for (point in polypts)  {
                        options.add(point)
                        LatLongB.include(point)
                    }
                    options.add(latLng)
                    LatLongB.include(latLng)
                    // build bounds
                    val bounds = LatLongB.build()
                    // add polyline to the map
                    map!!.addPolyline(options)
                    // show map with route centered
                    map!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                })
        }
    }

    private fun setPoiClick(map: GoogleMap){
        map.setOnPoiClickListener{ poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
        }
    }

    //Also in MapsActivity, create a setMapStyle() function that takes in a GoogleMap.
    //In setMapStyle(), add a try{} block.
    //In the try{} block, create a val success for the success of styling. (You add the following catch block.)
    //In the try{} block, set the JSON style to the map, call setMapStyle() on the GoogleMap object.
    // Pass in a MapStyleOptions object, which loads the JSON file.
    private fun setMapStyle(map: GoogleMap){
        try{
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_styles
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

//    To check if permissions are granted, create a method in the MapsActivity called isPermissionGranted(). In this method, check if the user has granted the permission.
    private fun isPermissionGranted() : Boolean{
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

//    To enable location tracking in your app, create a method in MapsActivity called enableMyLocation() that takes no arguments and doesn't return anything. Inside, check for the ACCESS_FINE_LOCATION permission. If the permission is granted, enable the location layer. Otherwise, request the permission.
    private fun enableMyLocation(){
        if(isPermissionGranted()){
            map.isMyLocationEnabled = true
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }
//Override the onRequestPermissionsResult() method. Check if the requestCode is equal to REQUEST_LOCATION_PERMISSION. If it is, that means that the permission is granted. If the permission is granted, also check if the grantResults array contains PackageManager.PERMISSION_GRANTED in its first slot. If that is true, call enableMyLocation().
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    private fun stylePolyline(polyline: Polyline?) {
        polyline?.let {
            it.color = R.color.teal_700
//                ContextCompat.getColor(binding.mapPlaceholder.context, R.color.lineColor)
            it.pattern = mutableListOf(Dot(), Gap(5F))
            it.startCap = RoundCap()
            it.jointType = JointType.ROUND
        }
    }

    /**
     * Method to decode polyline points
     * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }

//    Now, we need to build the URL we will use to make the API call.
    private fun getURLforResponse(from : LatLng, to : LatLng) : String {
        val origin = "origin=" + from.latitude + "," + from.longitude
        val dest = "destination=" + to.latitude + "," + to.longitude
        val sensor = "sensor=false"
        val applicationKey = "key=AIzaSyCltheQX7BlspjgonjPQyICRcV6jWQcnvE"
//    + R.string.landing_map_api_key
        val params = "$origin&$dest&$sensor&$applicationKey"
        return "https://maps.googleapis.com/maps/api/directions/json?$params"
    }

    //    Now, we need to build the URL we will use to make the API call.
    private fun getURLforDistanceAPIResponse(from : LatLng, to : LatLng) : String {
        val origin = "origins=" + from.latitude + "," + from.longitude
        val dest = "destinations=" + to.latitude + "," + to.longitude
        val sensor = "sensor=false"
        val applicationKey = "key=AIzaSyCltheQX7BlspjgonjPQyICRcV6jWQcnvE"
//        + R.string.landing_map_api_key
        val params = "$origin&$dest&$sensor&$applicationKey"
        return "https://maps.googleapis.com/maps/api/distancematrix/json?$params"
    }
}

private fun <R> CoroutineScope.executeAsyncTask(
    onPreExecute: () -> Unit,
    doInBackground: () -> R,
    onPostExecute: (R) -> Unit
) = launch {
    onPreExecute()
    val result = withContext(Dispatchers.IO) { // runs in background thread without blocking the Main Thread
        doInBackground()
    }
    onPostExecute(result)
}
