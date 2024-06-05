package com.example.coRdobA.create

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.coRdobA.data.Monument
import com.example.coRdobA.databinding.ActivityCreateMonumentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CreateMonument : AppCompatActivity() {

    private lateinit var binding : ActivityCreateMonumentBinding

    private lateinit var mFusedLoc : FusedLocationProviderClient
    private lateinit var geoPoint: GeoPoint

    private lateinit var fStore : FirebaseFirestore
    private lateinit var fStorage : StorageReference

    private var imgUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateMonumentBinding.inflate(layoutInflater)

        mFusedLoc = LocationServices.getFusedLocationProviderClient(this)

        fStore = FirebaseFirestore.getInstance()
        fStorage = FirebaseStorage.getInstance().reference.child("Images")

        getLastLocation(this)



        binding.uploadImg.setOnClickListener{
            resultLauncher.launch("image/*")
        }

        binding.createButton.setOnClickListener() {
            if (binding.createNET.text.toString().isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                if (imgUri != null)
                    uploadToFirebase()
                else Toast.makeText(this, "Upload image", Toast.LENGTH_SHORT).show()
            }
        }
        setContentView(binding.root)

    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()){
        imgUri = it
        binding.uploadImg.setImageURI(it)
    }

    private fun uploadToFirebase() {
        fStorage = fStorage.child(System.currentTimeMillis().toString())
        imgUri?.let {
            fStorage.putFile(it).addOnSuccessListener {
                fStorage.downloadUrl.addOnSuccessListener { uri ->
                    val name = binding.createNET.text.toString()
                    val info = binding.createDET.text.toString()

                    val monument = Monument(name, info, geoPoint, uri.toString())
                    fStore.collection("Monuments").add(monument).addOnSuccessListener {
                        Toast.makeText(this, "Monument created", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLastLocation(context: Context){
        Log.d("Get Location", "Get location : called")
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
        mFusedLoc.lastLocation.addOnSuccessListener {
            if (it != null) {
                val location: Location = it
                geoPoint = GeoPoint(location.latitude, location.longitude)
                Log.d("Get Location", "Latitude: " + geoPoint.latitude)
                Log.d("Get Location", "Longitude: " + geoPoint.longitude)
            } else{
                Toast.makeText(this, "Ubication is not activated", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}