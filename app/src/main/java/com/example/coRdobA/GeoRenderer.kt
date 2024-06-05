package com.example.coRdobA

import android.content.Intent
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.coRdobA.common.helpers.DisplayRotationHelper
import com.example.coRdobA.common.helpers.TrackingStateHelper
import com.example.coRdobA.common.render.*
import com.example.coRdobA.data.Monument
import com.google.android.gms.maps.model.LatLng
import com.google.ar.core.Anchor
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import java.io.IOException

class GeoRenderer(val activity: GeoActivity) :
    SampleRender.Renderer, DefaultLifecycleObserver {
    //<editor-fold desc="ARCore initialization" defaultstate="collapsed">
    companion object {
        val TAG = "HelloGeoRenderer"

        private val Z_NEAR = 0.1f
        private val Z_FAR = 1000f
    }

    lateinit var backgroundRenderer: BackgroundRenderer
    lateinit var virtualSceneFramebuffer: Framebuffer
    var hasSetTextureNames = false

    // Virtual object (ARCore pawn)
    lateinit var virtualObjectMesh: Mesh
    lateinit var virtualObjectShader: Shader
    lateinit var virtualObjectTexture: Texture

    // Position and information of monuments
    lateinit var fStore: FirebaseFirestore
    lateinit var fAuth: FirebaseAuth
    val listOfUbications: ArrayList<LatLng> = arrayListOf()
    val monuments: ArrayList<Monument> = arrayListOf()

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    val modelMatrix = FloatArray(16)
    val viewMatrix = FloatArray(16)
    val projectionMatrix = FloatArray(16)
    val modelViewMatrix = FloatArray(16) // view x model

    val modelViewProjectionMatrix = FloatArray(16) // projection x view x model

    val session
        get() = activity.arCoreSessionHelper.session

    val displayRotationHelper = DisplayRotationHelper(activity)
    val trackingStateHelper = TrackingStateHelper(activity)

    // Control generation of AR anchors
    private val anchors = mutableListOf<Anchor>()
    var actualAnchor = 0
    var maxPoint = 1

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        fAuth = Firebase.auth
        fStore = FirebaseFirestore.getInstance()

        getUnlockedMonuments()
        delayFun()
    }

    override fun onResume(owner: LifecycleOwner) {
        displayRotationHelper.onResume()
        hasSetTextureNames = false
    }

    override fun onPause(owner: LifecycleOwner) {
        displayRotationHelper.onPause()
    }

    override fun onSurfaceCreated(render: SampleRender) {
        // Prepare the rendering objects.
        // This involves reading shaders and 3D model files, so may throw an IOException.
        try {
            backgroundRenderer = BackgroundRenderer(render)
            virtualSceneFramebuffer = Framebuffer(render, /*width=*/ 1, /*height=*/ 1)

            // Virtual object to render (Geospatial Marker)
            virtualObjectTexture =
                Texture.createFromAsset(
                    render,
                    "models/spatial_marker_baked.png",
                    Texture.WrapMode.CLAMP_TO_EDGE,
                    Texture.ColorFormat.SRGB
                )

            virtualObjectMesh = Mesh.createFromAsset(render, "models/geospatial_marker.obj");
            virtualObjectShader =
                Shader.createFromAssets(
                    render,
                    "shaders/ar_unlit_object.vert",
                    "shaders/ar_unlit_object.frag",
                    /*defines=*/ null
                )
                    .setTexture("u_Texture", virtualObjectTexture)

            backgroundRenderer.setUseDepthVisualization(render, false)
            backgroundRenderer.setUseOcclusion(render, false)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read a required asset file", e)
            showError("Failed to read a required asset file: $e")
        }
    }

    override fun onSurfaceChanged(render: SampleRender, width: Int, height: Int) {
        displayRotationHelper.onSurfaceChanged(width, height)
        virtualSceneFramebuffer.resize(width, height)
    }
    //</editor-fold>

    override fun onDrawFrame(render: SampleRender) {
        val session = session ?: return

        //<editor-fold desc="ARCore frame boilerplate" defaultstate="collapsed">
        // Texture names should only be set once on a GL thread unless they change. This is done during
        // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
        // initialized during the execution of onSurfaceCreated.
        if (!hasSetTextureNames) {
            session.setCameraTextureNames(intArrayOf(backgroundRenderer.cameraColorTexture.textureId))
            hasSetTextureNames = true
        }

        displayRotationHelper.updateSessionIfNeeded(session)

        val frame = try {
            session.update()
        } catch (e: CameraNotAvailableException) {
            Log.e(TAG, "Camera not available during onDrawFrame", e)
            showError("Camera not available. Try restarting the app.")
            return
        }

        val camera = frame.camera

        // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
        // used to draw the background camera image.
        backgroundRenderer.updateDisplayGeometry(frame)

        // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
        trackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)

        // -- Draw background
        if (frame.timestamp != 0L) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            backgroundRenderer.drawBackground(render)
        }

        // If not tracking, don't draw 3D objects.
        if (camera.trackingState == TrackingState.PAUSED) {
            return
        }

        // Get projection matrix.
        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR)

        // Get camera matrix and draw.
        camera.getViewMatrix(viewMatrix, 0)

        render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f)

        //Obtain Geospatial info and display it on the map.
        val earth = session.earth

        anchor1?.let {
            render.renderCompassAtAnchor(it)
        }
        anchor2?.let {
            render.renderCompassAtAnchor(it)
        }

        backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR)

        val auxPosition = LatLng(earth!!.cameraGeospatialPose.latitude, earth.cameraGeospatialPose.longitude)
        Log.d("LOCATION POINT", "Actual anchor" + activity.points[actualAnchor])
        Log.d("LOCATION POINT", "MAX" + activity.points.size)

        if (activity.calculateDistance(auxPosition, activity.points[actualAnchor]) < 5f) {
            Log.d("TEST DISTANCE", "CHANGING")
            actualAnchor++
            if(actualAnchor >= maxPoint-1){
                val intent  = Intent(activity, QRscan::class.java)
                activity.startActivity(intent)
                activity.finish()
            }
        }

        if (actualAnchor+1 < maxPoint)
            drawNextAnchors(activity.points[actualAnchor], activity.points[actualAnchor+1])
        else
            drawNextAnchors(activity.points[actualAnchor], activity.points[actualAnchor])
    }

    private fun SampleRender.renderCompassAtAnchor(anchor: Anchor) {
        Log.d("ANCHOR", anchor.toString())
        // Get the current pose of the Anchor in world space. The Anchor pose is updated
        // during calls to session.update() as ARCore refines its estimate of the world.
        anchor.pose.toMatrix(modelMatrix, 0)

        // Calculate model/view/projection matrices
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

        // Update shader properties and draw
        virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
        draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer)
    }

    var anchor1: Anchor? = null
    var anchor2: Anchor? = null

    private fun showError(errorMessage: String) =
        activity.view.snackbarHelper.showError(activity, errorMessage)

    private fun getUnlockedMonuments() {
        var unlockedList: List<String> = emptyList()

        fStore.collection("Users").document(fAuth.currentUser!!.uid).get()
            .addOnSuccessListener { doc ->
                val aux =
                    doc["monuments"].toString().replace("[", "").replace("]", "").replace(" ", "")
                unlockedList = aux.split(",")
            }

        fStore.collection("Monuments").get().addOnSuccessListener { result ->
            for (document in result) {
                if (unlockedList.contains(document.id)) {
                    monuments.add(document.toObject<Monument>())
                }
            }
            Log.d("Unlocked Monuments", "Monuments: " + monuments.toString())
        }

    }

    private fun getPositionOfMonuments() {
        Log.d("Position Monuments", "Getting positions")
        for (monument in monuments) {
            //Log.d("Position Monuments", monument.coordinates.toString())
            val aux = LatLng(monument.coordinates!!.latitude, monument.coordinates!!.longitude)
            listOfUbications.add(aux)

        }
        Log.d("Position Monuments", "Final list: " + listOfUbications.toString())
    }

    private fun delayFun() {
        val run: Runnable = Runnable {
            run {
                getPositionOfMonuments()
            }
        }
        val handler: Handler = Handler(Looper.getMainLooper())
        handler.postDelayed(run, 3000)
    }

    private fun clearAnchors(){
        anchors.forEach { it.detach() }
        anchors.clear()
    }

    fun drawNextAnchors(anc1 : LatLng, anc2: LatLng) {
        val earth = session?.earth ?: return
        //Log.d("TEST DISTANCE", "DRAWING")
        if(earth.trackingState != TrackingState.TRACKING){
            return
        }

        Log.d("TEST DISTANCE", "Current: " + actualAnchor.toString())
        Log.d("TEST DISTANCE", "Max: " + maxPoint.toString())
        Log.d("TEST DISTANCE", "ANC1: " + anc1.toString())
        Log.d("TEST DISTANCE", "ANC2: " + anc2.toString())


        anchor1?.detach()
        anchor1 = earth.createAnchor(
            anc1.latitude,
            anc1.longitude,
            earth.cameraGeospatialPose.altitude-1.3,
            0f,
            0f,
            0f,
            1f
        )

        anchor2?.detach()
        anchor2 = earth.createAnchor(
            anc2.latitude,
            anc2.longitude,
            earth.cameraGeospatialPose.altitude-1.3,
            0f,
            0f,
            0f,
            1f
        )
    }
}
