package com.example.coRdobA.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.coRdobA.ListOfUsersDirections
import com.example.coRdobA.data.User
import com.example.coRdobA.databinding.SearchUserRowBinding

class UserAdapter(private var userList : ArrayList<User>, private val uidList : ArrayList<String>)
    : RecyclerView.Adapter<UserAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            SearchUserRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = userList[position]
        val currentUid = uidList[position]
        holder.name.text = currentItem.name
        holder.mail.text = currentItem.mail

        if(currentItem.admin)
            holder.itemBinding.userCard.setCardBackgroundColor(Color.parseColor("#EABF6F"))
        else
            holder.itemBinding.userCard.setCardBackgroundColor(Color.WHITE)

        holder.itemView.setOnClickListener{
            val dir = ListOfUsersDirections.actionListOfUsersToEditUser(currentUid)
            it.findNavController().navigate(dir)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun setFilteredList(itemList : ArrayList<User>){
        this.userList = itemList
        notifyDataSetChanged()
    }

    class MyViewHolder(val itemBinding : SearchUserRowBinding) : RecyclerView.ViewHolder(itemBinding.root){
        val name : TextView = itemBinding.listName
        val mail : TextView = itemBinding.listMail
    }
}