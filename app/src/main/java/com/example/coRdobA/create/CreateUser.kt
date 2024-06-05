package com.example.coRdobA.create

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.coRdobA.R
import com.example.coRdobA.data.User
import com.example.coRdobA.databinding.ActivityCreateUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class CreateUser : AppCompatActivity(), View.OnFocusChangeListener {

    private lateinit var binding : ActivityCreateUserBinding
    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore : FirebaseFirestore
    private lateinit var user: User
    private var userAux : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateUserBinding.inflate(layoutInflater)

        fAuth = Firebase.auth
        fStore = FirebaseFirestore.getInstance()
        val docRef = fStore.collection("Users").document(fAuth.currentUser!!.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            userAux = documentSnapshot.toObject<User>()
        }

        binding.createNET.onFocusChangeListener = this
        binding.createMET.onFocusChangeListener = this
        binding.createPET.onFocusChangeListener = this
        binding.createCPET.onFocusChangeListener = this

        binding.createButton.setOnClickListener{
            val validate = validate()

            if(validate) {
                val name = binding.createNET.text.toString()
                val mail = binding.createMET.text.toString()
                val pass = binding.createPET.text.toString()
                val admin = binding.isadmin.isChecked

                fAuth.createUserWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            var fUser: FirebaseUser? = fAuth.currentUser
                            if (fUser != null) {
                                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                                val df: DocumentReference = fStore.collection("Users").document(fUser.uid)
                                user = User(name, pass, mail, admin)

                                df.set(user)
                            }
                            fAuth.signInWithEmailAndPassword(
                                userAux!!.mail.toString(),
                                userAux!!.password.toString()
                            )
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }
        }
        setContentView(binding.root)
    }

    private fun validate(): Boolean {
        if (!checkName()) return false
        if (!checkMail()) return false
        if (!checkPassword()) return false
        if (!checkConfirmPassword()) return false
        return true
    }

    private fun checkName () : Boolean{
        var errorMsg :String? = null
        val value = binding.createNET.text.toString()
        if(value.isEmpty()){
            errorMsg = "Name is empty"
        }

        if( errorMsg != null){
            binding.createNIL.apply {
                isErrorEnabled = true
                error = errorMsg
            }
        }
        return errorMsg == null
    }

    private fun checkMail () : Boolean{
        var errorMsg :String? = null
        val value = binding.createMET.text.toString()
        if(value.isEmpty()){
            errorMsg = "Name is required"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()){
            errorMsg = "Email address is not valid"
        }

        if( errorMsg != null){
            binding.createMIL.apply {
                isErrorEnabled = true
                error = errorMsg
            }
        }
        return errorMsg == null
    }

    private fun checkPassword () : Boolean{
        var errorMsg :String? = null
        val value = binding.createPET.text.toString()
        if(value.isEmpty()){
            errorMsg = "Password is required"
        } else if (value.length < 6){
            errorMsg = "Password must be a least 6 characters long"
        }

        if( errorMsg != null){
            binding.createPIL.apply {
                isErrorEnabled = true
                error = errorMsg
            }
        }


        return errorMsg == null
    }

    private fun checkConfirmPassword () : Boolean{
        var errorMsg :String? = null
        val pass = binding.createPET.text.toString()
        val conf = binding.createCPET.text.toString()
        if(pass != conf){
            errorMsg = "Passwords don't match"
        }

        if( errorMsg != null){
            binding.createCPIL.apply {
                isErrorEnabled = true
                error = errorMsg
            }
        }
        return errorMsg == null
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v != null){
            when(v.id){
                R.id.createNET -> {
                    if(hasFocus){
                        if(binding.createNIL.isErrorEnabled){
                            binding.createNIL.isErrorEnabled = false
                        }
                    } else{
                        if(checkName() && binding.createNET.text!!.isNotEmpty()){
                            if(binding.createNIL.isErrorEnabled){
                                binding.createNIL.isErrorEnabled = false
                            }
                            binding.createNIL.apply{
                                setStartIconDrawable(R.drawable.baseline_check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }
                R.id.createMET -> {
                    if(hasFocus){
                        if(binding.createMIL.isErrorEnabled){
                            binding.createMIL.isErrorEnabled = false
                        }
                    } else{
                        if(checkMail() && binding.createMET.text!!.isNotEmpty()){
                            if(binding.createMIL.isErrorEnabled){
                                binding.createMIL.isErrorEnabled = false
                            }
                            binding.createMIL.apply{
                                setStartIconDrawable(R.drawable.baseline_check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }
                R.id.createPET -> {
                    if(hasFocus){
                        if(binding.createPIL.isErrorEnabled){
                            binding.createPIL.isErrorEnabled = false
                        }
                    } else{
                        if(checkPassword() && binding.createPET.text!!.isNotEmpty()){
                            if(binding.createPIL.isErrorEnabled){
                                binding.createPIL.isErrorEnabled = false
                            }
                            binding.createPIL.apply{
                                setStartIconDrawable(R.drawable.baseline_check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }
                R.id.createCPET -> {
                    if(hasFocus){
                        if(binding.createCPIL.isErrorEnabled){
                            binding.createCPIL.isErrorEnabled = false
                        }
                    } else{
                        if(checkConfirmPassword() && binding.createCPET.text!!.isNotEmpty()){
                            if(binding.createCPIL.isErrorEnabled){
                                binding.createCPIL.isErrorEnabled = false
                            }
                            binding.createCPIL.apply{
                                setStartIconDrawable(R.drawable.baseline_check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }
            }
        }
    }
}