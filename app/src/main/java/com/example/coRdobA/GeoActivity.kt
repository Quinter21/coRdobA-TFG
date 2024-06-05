package com.example.coRdobA

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.navigation.navArgs
import com.example.coRdobA.adapters.GoogleMapsAPI
import com.example.coRdobA.common.helpers.FullScreenHelper
import com.example.coRdobA.common.render.SampleRender
import com.example.coRdobA.data.DirectionsResponse
import com.example.coRdobA.helpers.ARCoreSessionLifecycleHelper
import com.example.coRdobA.helpers.GeoView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.maps.android.PolyUtil
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GeoActivity : AppCompatActivity() {

    lateinit var view: GeoView
    lateinit var renderer: GeoRenderer
    lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var googleMapsAPI: GoogleMapsAPI
    var points : MutableList<LatLng> = mutableListOf()

    val args : GeoActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup ARCore session lifecycle helper and configuration.
        arCoreSessionHelper = ARCoreSessionLifecycleHelper(this)
        // If Session creation or Session.resume() fails, display a message and log detailed
        // information.
        arCoreSessionHelper.exceptionCallback =
            { exception ->
                val message =
                    when (exception) {
                        is UnavailableUserDeclinedInstallationException ->
                            "Please install Google Play Services for AR"
                        is UnavailableApkTooOldException -> "Please update ARCore"
                        is UnavailableSdkTooOldException -> "Please update this app"
                        is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
                        is CameraNotAvailableException -> "Camera not available. Try restarting the app."
                        else -> "Failed to create AR session: $exception"
                    }
                Log.e("GeoActivity", "ARCore threw an exception", exception)
                view.snackbarHelper.showError(this, message)
            }

        // Configure session features.
        arCoreSessionHelper.beforeSessionResume = ::configureSession
        lifecycle.addObserver(arCoreSessionHelper)

        // Set up the Hello AR renderer.
        renderer = GeoRenderer(this)
        lifecycle.addObserver(renderer)

        // Set up Hello AR UI.
        view = GeoView(this)
        lifecycle.addObserver(view)
        setContentView(view.root)

        // Sets up an example renderer using our HelloGeoRenderer.
        SampleRender(view.surfaceView, renderer, assets)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        googleMapsAPI = retrofit.create(GoogleMapsAPI::class.java)

        fetchDirections()
    }

    // Configure the session, setting the desired options according to your usecase.
    fun configureSession(session: Session) {
        // TODO: Configure ARCore to use GeospatialMode.ENABLED.
        session.configure(
            session.config.apply {
                geospatialMode = Config.GeospatialMode.ENABLED
            }
        )
    }

    fun fetchDirections(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    view.getDestinationsFromFirebase(userLatLng, googleMapsAPI, args.mid!!)
                }
            }
            .addOnFailureListener {
                view.snackbarHelper.showError(this, "Failed to get user location")
            }
    }

    fun calculateDistance(user : LatLng, anchor : LatLng ) : Float{
        val result = FloatArray(1)
        Location.distanceBetween(user.latitude, user.longitude, anchor.latitude, anchor.longitude, result)
        return result[0]
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
    }

    fun saveDirections(directions : DirectionsResponse) {
        val steps = directions.routes[0].legs[0].steps
        renderer.maxPoint = -1
        for (step in steps) {
            val pointsPoly = PolyUtil.decode(step.polyline.points)
            for (point in pointsPoly) {
                points.add(point)
                Log.d("LOCATION POINT", point.toString())
                renderer.maxPoint++
            }

        }
        Log.d("LOCATION POINT", renderer.maxPoint.toString())
    }
}