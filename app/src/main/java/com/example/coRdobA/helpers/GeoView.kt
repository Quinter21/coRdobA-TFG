package com.example.coRdobA.helpers

import android.opengl.GLSurfaceView
import android.util.Log
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.coRdobA.GeoActivity
import com.example.coRdobA.R
import com.example.coRdobA.adapters.GoogleMapsAPI
import com.example.coRdobA.common.helpers.SnackbarHelper
import com.example.coRdobA.data.DirectionsResponse
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GeoView(val activity: GeoActivity) : DefaultLifecycleObserver {

    val root = View.inflate(activity, R.layout.activity_geo, null)
    val surfaceView = root.findViewById<GLSurfaceView>(R.id.surfaceview)

    val session
        get() = activity.arCoreSessionHelper.session

    val snackbarHelper = SnackbarHelper()

    override fun onResume(owner: LifecycleOwner) {
        surfaceView.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        surfaceView.onPause()
    }

    fun getDestinationsFromFirebase(userLatLng: LatLng, googleMapsAPI: GoogleMapsAPI, monument : String){
        val fStore = FirebaseFirestore.getInstance()
        val destinations = mutableListOf<LatLng>()
        Log.d("DEBUG", monument)

        fStore.collection("Monuments").get().addOnSuccessListener { result ->
            for (document in result){
                if(document.id == monument) {
                    val coordinates = document.getGeoPoint("coordinates")
                    coordinates?.let {
                        destinations.add(LatLng(it.latitude, it.longitude))
                        Log.d("DEBUG", "ADDED:" + destinations.toString())
                    }
                }
            }
            requestDirections(userLatLng, destinations, googleMapsAPI)
        }
    }

    fun requestDirections(userLatLng: LatLng, destinations: MutableList<LatLng>, googleMapsAPI: GoogleMapsAPI){
        val apiKey = activity.getString(R.string.GoogleCloudApiKey)

        destinations.forEach { destinationLatLng ->
            val origin = "${userLatLng.latitude},${userLatLng.longitude}"
            val destination = "${destinationLatLng.latitude},${destinationLatLng.longitude}"
            Log.d("DEBUG", destination)

            googleMapsAPI.getDirections("walking", origin, destination, apiKey).enqueue(object : Callback<DirectionsResponse> {
                override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { directions ->
                            activity.saveDirections(directions)
                        }
                    } else {
                        snackbarHelper.showError(activity, "Failed to get directions: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    snackbarHelper.showError(activity, "Failed to get directions: ${t.message}")
                }
            })
        }
    }
}