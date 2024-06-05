package com.example.coRdobA

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coRdobA.data.Monument
import com.example.coRdobA.databinding.ActivityAux2Binding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.squareup.picasso.Picasso

class QRscan : AppCompatActivity() {

    private lateinit var fStore : FirebaseFirestore

    private lateinit var binding : ActivityAux2Binding

    private val scanLauncher = registerForActivityResult(ScanContract()){result ->
        run {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                this.finish()
            } else {
                setResult(result.contents, this)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAux2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        showCamera()

        binding.endInfo.setOnClickListener {
            finish()
        }

    }

    private fun showCamera(){
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan QR code")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(false)
        options.setOrientationLocked(false)

        scanLauncher.launch(options)
    }

    private fun setResult(string : String, context : Context){
        fStore = FirebaseFirestore.getInstance()

        val df : DocumentReference = fStore.collection("Monuments").document(string)
        df.get().addOnSuccessListener{doc ->
            if (doc.data != null) {
                Log.d("TAG", "onSuccess: " + doc.data)
                val monument = doc.toObject<Monument>()

                binding.specMonumentName.text = monument!!.name
                binding.specMonumentInfo.text = monument.information
                if (monument.imageURL != null) {
                    Picasso.get().load(monument.imageURL).into(binding.specMonumentImg)
                }
            } else {
                Toast.makeText(context, "El código no es válido", Toast.LENGTH_SHORT).show()
                finish()
            }

        }.addOnFailureListener{
            Toast.makeText(context, "Algo ha  ido mal", Toast.LENGTH_SHORT).show()
            finish()
        }

    }
}