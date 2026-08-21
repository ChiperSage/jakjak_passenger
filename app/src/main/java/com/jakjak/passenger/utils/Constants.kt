package com.jakjak.passenger.utils

object Constants {

    // Firestore collections
    const val COLLECTION_USERS      = "users"
    const val COLLECTION_RIDES      = "rides"
    const val COLLECTION_DRIVERS    = "drivers"

    // User roles
    const val ROLE_PASSENGER        = "passenger"

    // SharedPreferences
    const val PREF_NAME             = "jakjak_passenger_prefs"
    const val PREF_USER_ID          = "user_id"
    const val PREF_FCM_TOKEN        = "fcm_token"
    const val PREF_GUEST_ID         = "guest_id"

    // Notification channels
    const val CHANNEL_RIDE          = "channel_ride"
    const val CHANNEL_PROMO         = "channel_promo"

    // Ride status
    const val STATUS_SEARCHING      = "searching"
    const val STATUS_DRIVER_FOUND   = "driver_found"
    const val STATUS_PICKED_UP      = "picked_up"
    const val STATUS_ON_TRIP        = "on_trip"
    const val STATUS_COMPLETED      = "completed"
    const val STATUS_CANCELLED      = "cancelled"

    // Matching
    const val MATCH_TIMEOUT_MS      = 15_000L

    // Alasan pembatalan
    const val CANCEL_REASON_NO_DRIVER         = "no_driver"
    const val CANCEL_REASON_DRIVER_REJECT     = "driver_reject"
    const val CANCEL_REASON_PASSENGER_CANCEL  = "passenger_cancel"
    const val CANCEL_REASON_TIMEOUT           = "timeout"

    // Request codes
    const val RC_LOCATION           = 1001
    const val RC_NOTIFICATION       = 1002
    const val RC_CAMERA             = 1003
    const val RC_GALLERY            = 1004

    // FCM topics
    const val TOPIC_PASSENGERS      = "passengers"
    const val TOPIC_DRIVERS         = "drivers"
}
