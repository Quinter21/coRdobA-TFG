package com.example.coRdobA

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.example.coRdobA.databinding.FragmentAdminMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AdminMain : Fragment(), MenuProvider {

    private var homeBinding : FragmentAdminMainBinding? = null
    private val binding get() = homeBinding!!

    private lateinit var fAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeBinding = FragmentAdminMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        fAuth = Firebase.auth

        binding.usersButton.setOnClickListener{
            it.findNavController().navigate(R.id.action_adminMain_to_listOfUsers)
        }

        binding.monumentsButton.setOnClickListener{
            it.findNavController().navigate(R.id.action_adminMain_to_listOfMonuments)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.admin_menu, menu)
        val item = menu.findItem(R.id.MainMenu)
        item.setVisible(false)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.ChangeView ->
                view?.findNavController()?.navigate(R.id.action_adminMain_to_userListOfMonumets)

            R.id.logout -> {
                fAuth.signOut()
                view?.findNavController()?.navigate(R.id.action_adminMain_to_login)
            }
        }
        return false
    }


}