package com.example.easyparking

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class SeeImageActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.see_image)

        val imageUrl = intent.getStringExtra("imageUrl")

        var image: ImageView = findViewById<ImageView>(R.id.saved_image)
        Picasso.get()
            .load(imageUrl) // load the image
            .into(image)
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