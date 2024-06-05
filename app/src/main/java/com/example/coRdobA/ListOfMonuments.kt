package com.example.coRdobA

import android.os.Bundle
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
import com.example.coRdobA.adapters.MonumentAdapter
import com.example.coRdobA.data.Monument
import com.example.coRdobA.databinding.FragmentListOfMonumentsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class ListOfMonuments : Fragment(), MenuProvider {

    private var homeBinding : FragmentListOfMonumentsBinding? = null
    private val binding get() = homeBinding!!

    //Needed to list the monuments
    private lateinit var recyclerView : RecyclerView
    private lateinit var myAdapter: MonumentAdapter
    private lateinit var monuList : ArrayList<Monument>
    private lateinit var uidList : ArrayList<String>
    private lateinit var searchView : SearchView

    //Firebase
    private lateinit var fStore : FirebaseFirestore
    private lateinit var fAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeBinding = FragmentListOfMonumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fAuth = Firebase.auth

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        searchView = binding.searchMonument
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


        recyclerView = view.findViewById(R.id.homeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.setHasFixedSize(true)

        monuList = arrayListOf()
        uidList = arrayListOf()

        myAdapter = MonumentAdapter(monuList, uidList, true)

        recyclerView.adapter = myAdapter

        EventChangeListener()

        binding.addFab.setOnClickListener{
            it.findNavController().navigate(R.id.action_listOfMonuments_to_createMonument)
        }

        binding.refresh.setOnClickListener{
            myAdapter.setFilteredList(monuList)
        }

    }

    private fun filterList(text: String) {
        val filteredList: ArrayList<Monument> = arrayListOf()
        for(monument : Monument in monuList){
            if (monument.name!!.lowercase().contains(text.lowercase()))
                filteredList.add(monument)
        }
        myAdapter.setFilteredList(filteredList)

    }

    private fun EventChangeListener() {
        fStore = FirebaseFirestore.getInstance()
        fStore.collection("Monuments").get().addOnSuccessListener { result ->
            for(document in result){
                val monument = document.toObject<Monument>()
                monuList.add(monument)
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
                view?.findNavController()?.navigate(R.id.action_listOfMonuments_to_adminMain)

            R.id.logout -> {
                fAuth.signOut()
                view?.findNavController()?.navigate(R.id.action_listOfMonuments_to_login)
            }
        }
        return false
    }
}
