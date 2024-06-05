package com.example.coRdobA.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.coRdobA.ListOfMonumentsDirections
import com.example.coRdobA.UserListOfMonumetsDirections
import com.example.coRdobA.data.Monument
import com.example.coRdobA.databinding.SearchMonumentRowBinding
import com.squareup.picasso.Picasso

class MonumentAdapter(private var monuList : ArrayList<Monument>, private val uidList : ArrayList<String>, private val admin : Boolean)
    : RecyclerView.Adapter<MonumentAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            SearchMonumentRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = monuList[position]
        val currentUid = uidList[position]
        if(currentItem.imageURL != null) {
            Picasso.get().load(currentItem.imageURL).into(holder.image)
        }
        holder.name.text = currentItem.name


        holder.itemView.setOnClickListener {
            if (admin) {
                val dir = ListOfMonumentsDirections.actionListOfMonumentsToEditMonument(currentUid)
                it.findNavController().navigate(dir)
            }
            else {
                val dir = UserListOfMonumetsDirections.actionUserListOfMonumetsToGeoActivity(currentUid)
                it.findNavController().navigate(dir)
            }
        }
    }

    override fun getItemCount(): Int {
        return monuList.size
    }

    fun setFilteredList(itemList : ArrayList<Monument>){
        this.monuList = itemList
         notifyDataSetChanged()
    }

    class MyViewHolder(val itemBinding : SearchMonumentRowBinding) : RecyclerView.ViewHolder(itemBinding.root){
        val name : TextView = itemBinding.listName
        val image : ImageView = itemBinding.listImage
    }
}