package com.example.easyparking

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.location.view.*

class LocAdapter (  var activity: Activity, var parkList:List<ParkData>,
    var getPosition:(Int) -> Unit) : RecyclerView.Adapter<ParkViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkViewHolder =
                ParkViewHolder(
                        LayoutInflater.from(activity).inflate(R.layout.location, parent, false))

        override fun onBindViewHolder(holder: ParkViewHolder, position: Int) =
                holder.bind(parkList[position], position, getPosition)

        //gets the number of items in the list
        override fun getItemCount() = parkList.size

    }


    class ParkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var address: TextView = view.addr
        var date : TextView = view.date
        var view: View = view

        fun bind(item: ParkData, position: Int, getPosition: (Int) -> Unit) {
            address.text = item.address
            date.text = item.dateTime

            view.save.setOnClickListener{
                getPosition(position)
            }
        }
    }