package com.example.coRdobA.create

import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.coRdobA.R
import com.example.coRdobA.data.Monument
import com.example.coRdobA.databinding.FragmentEditMonumentBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.IOException
import java.io.OutputStream

class EditMonument : Fragment() {

    private var homeBinding : FragmentEditMonumentBinding? = null
    private val binding get() = homeBinding!!

    private val args : EditMonumentArgs by navArgs()
    private lateinit var fStore : FirebaseFirestore
    private lateinit var monument : Monument

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeBinding = FragmentEditMonumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fStore = FirebaseFirestore.getInstance()

        val df : DocumentReference = fStore.collection("Monuments").document(args.mid!!)
        df.get().addOnSuccessListener{doc ->
            Log.d("TAG", "onSuccess: " + doc.data)
            monument = doc.toObject<Monument>()!!

            binding.editMonumentName.setText(monument.name)
            binding.editMonumentInfo.setText(monument.information)
            //binding.editMonumentWeb.setText(monument.url)
            //binding.editMonumentTime.setText(monument.time)

        }.addOnFailureListener{
            Toast.makeText(context,"Failed to load user", Toast.LENGTH_SHORT).show()
            view.findNavController().navigate(R.id.action_editMonument_to_listOfMonuments)
        }

        binding.editSave.setOnClickListener{
            monument.name = binding.editMonumentName.text.toString()
            monument.information = binding.editMonumentInfo.text.toString()
            fStore.collection("Monuments").document(args.mid!!).set(monument)
            delayNav(view)
        }

        binding.editDeleteMonument.setOnClickListener {
            val builder = AlertDialog.Builder(this.context)
            builder.setMessage("Are you sure you want to delete?")
                .setCancelable(false)
                .setPositiveButton("YES") {dialog, id ->
                    fStore.collection("Monuments").document(args.mid!!).delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Monument deleted", Toast.LENGTH_SHORT).show()

                            delayNav(view)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("NO"){dialog, id ->
                    dialog.dismiss()
                }
            builder.create().show()
        }

        binding.QRButton.setOnClickListener {
            val data = args.mid!!
            val writer = QRCodeWriter()
            try {
                //Create QR
                val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
                val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
                for (x in 0 until 512)
                    for (y in 0 until 512){
                        bmp.setPixel(x, y, if(bitMatrix[x,y]) Color.BLACK else Color.WHITE)
                    }
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, monument.name+"_QR.jpg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Pruebas")
                }

                val resolver = requireContext().contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if(uri != null){
                    var outputStream: OutputStream? = null
                    try {
                        outputStream = resolver.openOutputStream(uri)
                        if (outputStream != null){
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        }
                    } catch (e : IOException){
                        e.printStackTrace()
                    } finally {
                        outputStream?.close()
                        Toast.makeText(this.context, "QR generated", Toast.LENGTH_SHORT).show()
                    }
                }

            }catch (e: WriterException){
                e.printStackTrace()
            }
        }
    }

    private fun delayNav(view: View) {
        val run : Runnable = Runnable {
            run {
                view.findNavController().navigate(R.id.action_editMonument_to_listOfMonuments)
            }
        }
        val handler : Handler = Handler(Looper.getMainLooper())
        handler.postDelayed(run, 1000)
    }
}