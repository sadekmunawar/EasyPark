package com.example.easyparking

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.view_location.*

val databasee = FirebaseDatabase.getInstance().reference

class ViewLocation  : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_location)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        seeImage.setOnClickListener{
            val i = Intent(this, SeeImageActivity::class.java)

            val imageUrl = intent.getStringExtra("imageUrl")

            i.putExtra("imageUrl", imageUrl)

            startActivity(i)
        }
        delete.setOnClickListener {

            // remove from database
            val uuid = intent.getStringExtra("uuid")
            val parkingInfo = database.child("profiles").child(uuid);
            parkingInfo.removeValue()

            // remove from storage
            val path = intent.getStringExtra("imagePath")
            val storageRef = FirebaseStorage.getInstance().reference.child(path)

            storageRef.delete().addOnSuccessListener {
                Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Problem occurred", Toast.LENGTH_SHORT).show()
            }

            val i = Intent(this, RecentLocationsActivity::class.java)
            startActivity(i)
        }
    }

    private fun setUpMap() {

        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 123)
            mMap.isMyLocationEnabled = true
            return
        }
        mMap.isMyLocationEnabled = true

        val lat = intent.getStringExtra("lat").toDouble()
        val lng = intent.getStringExtra("lng").toDouble()
        val myLocation = LatLng(lat, lng)

        mMap.addMarker(MarkerOptions().position(myLocation!!).title("You are here"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18.0f))

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpMap()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_3, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.park_now -> {
                val i = Intent(this, NewLoocationActivity::class.java)
                startActivity(i)
                return true
            }
            R.id.view_locations -> {
                val i = Intent(this, RecentLocationsActivity::class.java)
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