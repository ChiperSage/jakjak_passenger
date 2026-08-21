package com.jakjak.passenger.utils

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Model pesanan (ride) yang disimpan ke koleksi Firestore `rides`.
 *
 * Alur matching: penumpang membuat order -> status `searching` dengan driverId
 * ditunjuk ke kandidat terdekat -> driver punya 15 detik (expiresAt) untuk accept
 * -> jika tidak, di-assign ke driver berikutnya -> habis semua, status `cancelled`.
 */
data class RideModel(
    @DocumentId
    val rideId: String = "",
    val passengerId: String = "",
    val passengerName: String = "",
    val origin: String = "",
    val destination: String = "",
    val note: String = "",
    val originLocation: GeoPoint? = null,
    val status: String = Constants.STATUS_SEARCHING,
    val driverId: String = "",
    val attemptedDriverIds: List<String> = emptyList(),
    val cancelReason: String = "",
    val expiresAt: Long = 0L,
    @ServerTimestamp
    val requestedAt: Date? = null,
    @ServerTimestamp
    val acceptedAt: Date? = null
)
