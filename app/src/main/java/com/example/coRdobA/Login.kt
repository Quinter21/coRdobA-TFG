package com.example.coRdobA

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.coRdobA.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class Login : Fragment(R.layout.fragment_login) {

    private var homeBinding : FragmentLoginBinding? = null
    private val binding get() = homeBinding!!

    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fAuth = Firebase.auth
        fStore = FirebaseFirestore.getInstance()

        binding.LoginButton.setOnClickListener{
            val mail = binding.logMET.text.toString()
            val pass = binding.logPET.text.toString()
            if(mail.isNotEmpty() && pass.isNotEmpty()) {
                fAuth.signInWithEmailAndPassword(mail, pass).addOnSuccessListener {
                    checkUserLevel(it.user?.uid, view)
                }
                    .addOnFailureListener {
                        Toast.makeText(this.context, "User or password is invalid", Toast.LENGTH_SHORT).show()
                    }
            }else {
                Toast.makeText(this.context, "Please enter a user", Toast.LENGTH_SHORT).show()
                }
        }
        binding.GoRegister.setOnClickListener {
            it.findNavController().navigate(R.id.action_login_to_register)
        }
    }

    private fun checkUserLevel(uid: String?, view: View) {
        if (uid == null){
            Toast.makeText(context, "Algo va mal repetir", Toast.LENGTH_SHORT).show()
        } else {
            val df : DocumentReference = fStore.collection("Users").document(uid)
            df.get().addOnSuccessListener{
                Log.d("TAG", "onSuccess: " + it.data)
                if(it.getBoolean("admin") == true){
                    view.findNavController().navigate(R.id.action_login_to_adminMain)
                } else {
                    view.findNavController().navigate(R.id.action_login_to_userListOfMonumets)
                }

            }.addOnFailureListener{
                Toast.makeText(context,"Pues no se que eres", Toast.LENGTH_SHORT).show()
            }
        }
    }

}