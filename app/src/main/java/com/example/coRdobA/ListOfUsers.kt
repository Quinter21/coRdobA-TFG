package com.example.coRdobA

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coRdobA.adapters.UserAdapter
import com.example.coRdobA.data.User
import com.example.coRdobA.databinding.FragmentListOfUsersBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class ListOfUsers : Fragment(), MenuProvider {

    private var homeBinding : FragmentListOfUsersBinding? = null
    private val binding get() = homeBinding!!

    //Needed to list the users
    private lateinit var recyclerView : RecyclerView
    private lateinit var myAdapter: UserAdapter
    private lateinit var userList : ArrayList<User>
    private lateinit var uidList : ArrayList<String>
    private lateinit var searchView : SearchView

    //Firebase
    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore : FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeBinding = FragmentListOfUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        searchView = binding.searchUser
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterList(newText)
                return true
            }
        })

        fAuth = Firebase.auth

        recyclerView = view.findViewById(R.id.homeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.setHasFixedSize(true)

        userList = arrayListOf()
        uidList = arrayListOf()

        myAdapter = UserAdapter(userList, uidList)

        recyclerView.adapter = myAdapter

        EventChangeListener()

        binding.addFab.setOnClickListener{
            it.findNavController().navigate(R.id.action_listOfUsers_to_createUser)
        }

        binding.refresh.setOnClickListener {
            myAdapter.setFilteredList(userList)
        }

    }

    private fun filterList(text: String) {
        val filteredList: ArrayList<User> = arrayListOf()
        for(user : User in userList){
            if (user.name!!.lowercase().contains(text.lowercase()))
                filteredList.add(user)
        }
        myAdapter.setFilteredList(filteredList)
    }

    private fun EventChangeListener() {
        fStore = FirebaseFirestore.getInstance()
        fStore.collection("Users").get().addOnSuccessListener { result ->
            for(document in result){
                Log.d("TAG", "${document.id} => ${document.data}")
                var name = document["name"].toString()
                val mail = document["mail"].toString()
                if (fAuth.currentUser!!.email.toString() ==  mail)
                    name = name + " (ME)"
                val pass = document["password"].toString()
                val admin = document["admin"].toString().toBoolean()

                userList.add(User(name,pass,mail,admin))
                uidList.add(document.id)
            }
            myAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.admin_menu, menu)
        val item = menu.findItem(R.id.ChangeView)
        item.setVisible(false)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.MainMenu ->
                view?.findNavController()?.navigate(R.id.action_listOfUsers_to_adminMain)

            R.id.logout -> {
                fAuth.signOut()
                view?.findNavController()?.navigate(R.id.action_listOfUsers_to_login)
            }
        }
        return false
    }
}
