package com.example.easyparking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.location_history.*


val parkDataList: ArrayList<ParkData> = ArrayList<ParkData>()
val database = FirebaseDatabase.getInstance().reference

class RecentLocationsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_history)

        loc_recycler_view.layoutManager = LinearLayoutManager(this)
        loc_recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        loc_recycler_view.adapter = LocAdapter(this, parkDataList) { position ->

            val i = Intent(this, ViewLocation::class.java)
            i.putExtra("addr", parkDataList[position].address)
            i.putExtra("imageUrl", parkDataList[position].imageUrl)
            i.putExtra("date", parkDataList[position].dateTime)
            i.putExtra("lat", parkDataList[position].lat.toString())
            i.putExtra("lng", parkDataList[position].lng.toString())
            i.putExtra("uuid", parkDataList[position].uuid)
            i.putExtra("imagePath", parkDataList[position].imagePath)

            startActivity(i)
        }

        val postListener =  object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("ERROR", error.message)
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                parkDataList.clear()
                snapshot.children.forEach {

                    val profile = it.getValue<ParkData>(ParkData::class.java)

                    parkDataList.add(ParkData(profile!!.address, profile!!.imageUrl,profile!!.lat,
                            profile!!.lng,
                            profile!!.dateTime, profile!!.imagePath, profile!!.uuid))
                }
                parkDataList.reverse()
                loc_recycler_view.adapter?.notifyDataSetChanged()
            }
        }
        database.child("profiles").addValueEventListener(postListener)


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.park_now -> {
                val i = Intent(this, NewLoocationActivity::class.java)
                startActivity(i)
                return true
            }
            R.id.home -> {
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}