package com.jakjak.passenger.utils

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserModel(
    @DocumentId
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val role: String = Constants.ROLE_PASSENGER,
    val fcmToken: String = "",
    val isActive: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null
)
