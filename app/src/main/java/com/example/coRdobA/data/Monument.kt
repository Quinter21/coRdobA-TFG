package com.example.coRdobA.data

import com.google.firebase.firestore.GeoPoint

data class Monument(
    var name: String? = null,
    var information: String? = null,
    var coordinates: GeoPoint? = null,
    var imageURL: String? = null
)