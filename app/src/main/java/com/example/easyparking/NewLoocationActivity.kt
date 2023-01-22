package com.example.easyparking

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.text.DecimalFormat
import android.location.Geocoder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.add_location.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class NewLoocationActivity : AppCompatActivity(), OnMapReadyCallback
{
    val MY_PERMISSIONS_REQUEST_CAMERA = 1

    val REQUEST_IMAGE_CAPTURE = 1

    var imageTaken = false

    var url = ""
    var d = ""
    var uuid = ""
    var imagePathSave = ""

    private var gotAddress = false;

    var dateTime = "gg"

    var database = FirebaseDatabase.getInstance().reference
    var storage = FirebaseStorage.getInstance().reference

    private var address = ""
    private var gotLoc  = false
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var myLocation: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_location)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        takePic.setOnClickListener {
            dispatchTakePictureIntent()
        }

        cancel.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }

        park.setOnClickListener() {
            if (!imageTaken) {
                Toast.makeText(this, "Take a picture to save location", Toast.LENGTH_SHORT).show()
            } else if (!gotAddress) {
                val builder = AlertDialog.Builder(this)
                val dialogLayout = layoutInflater.inflate(R.layout.dialog_box, null)
                val editText = dialogLayout.findViewById<EditText>(R.id.enterAddress)

                with(builder) {
                    setTitle("EasyParking couldn't not obtain address. Enter a description of the location or leave empty")
                    setPositiveButton("submit") { dialog, which ->
                        if (editText.text.toString() != null && !(editText.text.toString().isEmpty())) {
                            address = editText.text.toString()

                        }
                        saveNexit()
                    }
                    setView(dialogLayout)
                    show()
                }
            }
            else {
                saveToFirebase()
                val i = Intent(this, RecentLocationsActivity::class.java)
                startActivity(i)
            }
        }

        checkPermission()

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                MY_PERMISSIONS_REQUEST_CAMERA
            )
        } 
    }

    private fun saveNexit () {
        saveToFirebase()
        val i = Intent(this, RecentLocationsActivity::class.java)
        startActivity(i)
    }

    // get current location
    private fun setUpMap() {

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 123
            )
            mMap.isMyLocationEnabled = true
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            if (location != null) {
                myLocation = LatLng(location.latitude, location.longitude)
                if (myLocation == null) {
                    Toast.makeText(
                        this, "mylocation is null",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    mMap.addMarker(MarkerOptions().position(myLocation!!).title("Here"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18.0f))

                    address = getAddress(myLocation)
                }
                gotLoc = true
            }
        }       .addOnFailureListener {
            Toast.makeText(
                this, "Failed on getting current location",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpMap()
    }

    private fun getAddress(loc: LatLng?): String {
        val geocoder = Geocoder(this)
        if (loc != null) {

            val df = DecimalFormat()
            df.maximumFractionDigits = 3
            try {
                val list = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                gotAddress = true
                return list[0].getAddressLine(0)
            } catch(e: IOException) {
                Toast.makeText(
                    this, "couldn't get the address",
                    Toast.LENGTH_SHORT).show()
                return "Unknown Location"
            }
        } else {
            return ""
        }
    }

     private fun dispatchTakePictureIntent() {
         Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
             takePictureIntent.resolveActivity(packageManager)?.also {
                 startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
             }
         }
     }


     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
             val thumbnail = data!!.extras!!.get("data") as Bitmap
             val k = thumbnail
             image_preview!!.setImageBitmap(thumbnail)

             imageTaken = true

             image_preview.tag = savePic(k)
         }
     }

     private fun savePic(myBitmap: Bitmap):String {
         val bytes = ByteArrayOutputStream()
         myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
         val wallpaperDirectory = File(
             (Environment.getExternalStorageDirectory()).toString()
         )
         // have the object build the directory structure, if needed.
         if (!wallpaperDirectory.exists())
         { wallpaperDirectory.mkdirs()}

         try {
             val f = File(
                 wallpaperDirectory, ((Calendar.getInstance()
                     .timeInMillis).toString() + ".jpg")
             )
             f.createNewFile()
             val fo = FileOutputStream(f)
             fo.write(bytes.toByteArray())
             MediaScannerConnection.scanFile(
                 this,
                 arrayOf(f.path),
                 arrayOf("image/jpeg"), null
             )
             fo.close()
             Log.d("TAG", "File Saved::--->" + f.absolutePath)
             return f.absolutePath
         }
         catch (e1: IOException) {
             e1.printStackTrace()
         }
         return ""
     }

     private fun saveToFirebase() {
         // Step 1: Save image to cloud storage
         var file = Uri.fromFile(File(image_preview.tag.toString()))
         imagePathSave = "images/${file.lastPathSegment}"
         val imageRef = storage.child("images/${file.lastPathSegment}")
         val uploadTask = imageRef.putFile(file)

         // Step 2: Get URL of file and save profile to database
         uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
             if (!task.isSuccessful) {
                 task.exception?.let {
                     throw it
                 }
             }
             return@Continuation imageRef.downloadUrl
         }).addOnCompleteListener { task ->
             if (task.isSuccessful) {
                 val photoUrl = task.result

                 url = photoUrl.toString()

                 // create post

                 val profile = ParkData(
                     address,
                     photoUrl.toString(), myLocation.latitude,
                     myLocation.longitude,
                     dateTime, imagePathSave
                 )

                 // make a key for this profile
                 val key = database.child("profiles").push().key!!
                 profile.uuid = key
                 uuid = key
                 database.child("profiles").child(key).setValue(profile)

                 Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show()

             } else {
                 Toast.makeText(this, "Failed to save location!", Toast.LENGTH_SHORT).show()
             }
         }
     }

     override fun onRequestPermissionsResult(
         requestCode: Int,
         permissions: Array<String>, grantResults: IntArray
     ) {
         when (requestCode) {
             MY_PERMISSIONS_REQUEST_CAMERA -> {
                 // If request is cancelled, the result arrays are empty.
                 if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                     // permission was granted, yay! Do the
                     // contacts-related task you need to do.
                     Toast.makeText(this, "Nice!", Toast.LENGTH_SHORT).show()

                 } else {
                     Toast.makeText(this, "Sorry, no camera for you :(", Toast.LENGTH_SHORT).show()
                     // permission denied, boo! Disable the
                     // functionality that depends on this permission.
                 }
                 return
             }

             // Add other 'when' lines to check for other
             // permissions this app might request.
             else -> {
                 // Ignore all other requests.
             }
         }
     }
     private fun checkPermission() {
         if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

         }
         if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(
                 this,
                 arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                 MY_PERMISSIONS_REQUEST_CAMERA
             )

         }
         if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(
                 this,
                 arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                 MY_PERMISSIONS_REQUEST_CAMERA
             )

         }

         if (ActivityCompat.checkSelfPermission(
                         this,
                         android.Manifest.permission.ACCESS_FINE_LOCATION
                 ) != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(
                     this,
                     arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 123
             )
         }

     }
}