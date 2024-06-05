package com.example.coRdobA.create

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.example.coRdobA.R
import com.example.coRdobA.data.User
import com.example.coRdobA.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class Register : AppCompatActivity(), View.OnFocusChangeListener, View.OnKeyListener {

    private lateinit var binding : ActivityRegisterBinding
    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore : FirebaseFirestore
    private lateinit var user : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)

        fAuth = Firebase.auth
        fStore = FirebaseFirestore.getInstance()

        binding.RegNET.onFocusChangeListener = this
        binding.RegMET.onFocusChangeListener = this
        binding.RegPET.onFocusChangeListener = this
        binding.RegCPET.onFocusChangeListener = this

        binding.RegButton.setOnClickListener{
            val validate = validate()

            if(validate) {
                val mail = binding.RegMET.text.toString()
                val pass = binding.RegPET.text.toString()
                val name = binding.RegNET.text.toString()

                fAuth.createUserWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(this) {task ->
                        if(task.isSuccessful){
                            val fUser : FirebaseUser? = fAuth.currentUser
                            if(fUser != null) {
                                Toast.makeText(this, "Accout created", Toast.LENGTH_SHORT).show()
                                val df: DocumentReference = fStore.collection("Users").document(fUser.uid)
                                user = User(name, pass, mail)

                                df.set(user)
                            }
                            finish()
                        }else{
                            Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show()
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
        val value = binding.RegNET.text.toString()
        if(value.isEmpty()){
            errorMsg = "Name is empty"
        }

        if( errorMsg != null){
            binding.RegNIL.apply {
                isErrorEnabled = true
                error = errorMsg
            }
        }
        return errorMsg == null
    }

    private fun checkMail () : Boolean{
        var errorMsg :String? = null
        val value = binding.RegMET.text.toString()
        if(value.isEmpty()){
            errorMsg = "Name is required"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()){
            errorMsg = "Email address is not valid"
        }

        if( errorMsg != null){
            binding.RegMIL.apply {
                isErrorEnabled = true
                error = errorMsg
            }
        }
        return errorMsg == null
    }

    private fun checkPassword () : Boolean{
        var errorMsg :String? = null
        val value = binding.RegPET.text.toString()
        if(value.isEmpty()){
            errorMsg = "Password is required"
        } else if (value.length < 6){
            errorMsg = "Password must be a least 6 characters long"
        }

        if( errorMsg != null){
            binding.RegPIL.apply {
                isErrorEnabled = true
                error = errorMsg
            }
        }


        return errorMsg == null
    }

    private fun checkConfirmPassword () : Boolean{
        var errorMsg :String? = null
        val pass = binding.RegPET.text.toString()
        val conf = binding.RegCPET.text.toString()
        if(pass != conf){
            errorMsg = "Passwords don't match"
        }

        if( errorMsg != null){
            binding.RegCPIL.apply {
                isErrorEnabled = true
                error = errorMsg
            }
        }
        return errorMsg == null
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v != null){
            when(v.id){
                R.id.RegNET -> {
                    if(hasFocus){
                        if(binding.RegNIL.isErrorEnabled){
                            binding.RegNIL.isErrorEnabled = false
                        }
                    } else{
                        if(checkName() && binding.RegNET.text!!.isNotEmpty()){
                            if(binding.RegNIL.isErrorEnabled){
                                binding.RegNIL.isErrorEnabled = false
                            }
                            binding.RegNIL.apply{
                                setStartIconDrawable(R.drawable.baseline_check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }
                R.id.RegMET -> {
                    if(hasFocus){
                        if(binding.RegMIL.isErrorEnabled){
                            binding.RegMIL.isErrorEnabled = false
                        }
                    } else{
                        if(checkMail() && binding.RegMET.text!!.isNotEmpty()){
                            if(binding.RegMIL.isErrorEnabled){
                                binding.RegMIL.isErrorEnabled = false
                            }
                            binding.RegMIL.apply{
                                setStartIconDrawable(R.drawable.baseline_check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }
                R.id.RegPET -> {
                    if(hasFocus){
                        if(binding.RegPIL.isErrorEnabled){
                            binding.RegPIL.isErrorEnabled = false
                        }
                    } else{
                        if(checkPassword() && binding.RegPET.text!!.isNotEmpty()){
                            if(binding.RegPIL.isErrorEnabled){
                                binding.RegPIL.isErrorEnabled = false
                            }
                            binding.RegPIL.apply{
                                setStartIconDrawable(R.drawable.baseline_check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }
                R.id.RegCPET -> {
                    if(hasFocus){
                        if(binding.RegCPIL.isErrorEnabled){
                            binding.RegCPIL.isErrorEnabled = false
                        }
                    } else{
                        if(checkConfirmPassword() && binding.RegCPET.text!!.isNotEmpty()){
                            if(binding.RegCPIL.isErrorEnabled){
                                binding.RegCPIL.isErrorEnabled = false
                            }
                            binding.RegCPIL.apply{
                                setStartIconDrawable(R.drawable.baseline_check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }
}