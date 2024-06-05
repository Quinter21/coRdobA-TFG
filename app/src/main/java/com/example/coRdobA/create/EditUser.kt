package com.example.coRdobA.create

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.coRdobA.R
import com.example.coRdobA.data.User
import com.example.coRdobA.databinding.FragmentEditUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class EditUser : Fragment() {

    private var homeBinding : FragmentEditUserBinding? = null
    private val binding get() = homeBinding!!

    private lateinit var fStore : FirebaseFirestore
    private lateinit var fAuth : FirebaseAuth


    private val args : EditUserArgs by navArgs()
    private lateinit var user : User
    private var userAux : User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeBinding = FragmentEditUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fAuth = Firebase.auth
        fStore = FirebaseFirestore.getInstance()

        val docRef = fStore.collection("Users").document(fAuth.currentUser!!.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            userAux = documentSnapshot.toObject<User>()
        }

        val df : DocumentReference = fStore.collection("Users").document(args.uid!!)
        df.get().addOnSuccessListener{doc ->
            Log.d("TAG", "onSuccess: " + doc.data)

            binding.editUserName.setText(doc.getString("name"))
            user = User(doc.getString("name"), doc.getString("password"), doc.getString("mail"))

        }.addOnFailureListener{
            Toast.makeText(context,"Failed to load user", Toast.LENGTH_SHORT).show()
            view.findNavController().navigate(R.id.action_editUser_to_listOfUsers)
        }

        binding.editSave.setOnClickListener{
            user.name = binding.editUserName.text.toString()
            user.admin = binding.editAdmin.isChecked
            fStore.collection("Users").document(args.uid!!).set(user)
            delayNav(view)
        }

        binding.editDeleteUser.setOnClickListener {
            createUser(it)
        }

    }

    private fun delayNav(view: View) {
        val run : Runnable = Runnable {
            run {
                view.findNavController().navigate(R.id.action_editUser_to_listOfUsers)
            }
        }
        val handler : Handler = Handler(Looper.getMainLooper())
        handler.postDelayed(run, 1000)
    }

    private fun createUser(view : View) {
        val builder = AlertDialog.Builder(this.context)
        builder.setMessage("Are you sure you want to delete?")
            .setCancelable(false)
            .setPositiveButton("YES") { dialog, id ->
                fStore.collection("Users").document(args.uid!!).delete()
                fAuth.signOut()
                fAuth.signInWithEmailAndPassword(user.mail!!, user.password!!)
                    .addOnCompleteListener {
                        if (it.isCanceled) {
                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT)
                                .show()
                        } else if (it.isSuccessful) {
                            fAuth.currentUser!!.delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        fAuth.signInWithEmailAndPassword(
                                            userAux!!.mail.toString(),
                                            userAux!!.password.toString()
                                        )
                                        Toast.makeText(context,"Account deleted",Toast.LENGTH_SHORT).show()
                                        delayNav(view)

                                    } else
                                        Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            }
            .setNegativeButton("NO") { dialog, id ->
                dialog.dismiss()
            }
        builder.create().show()
    }
}