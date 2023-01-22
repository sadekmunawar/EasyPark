package com.example.easyparking

import com.google.android.gms.maps.model.LatLng

data class ParkData(
    val address: String = "",
    val imageUrl: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val dateTime: String = "",
    var imagePath : String = "",
    var uuid : String = ""
)
