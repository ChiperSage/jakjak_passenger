package com.jakjak.passenger.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

/**
 * Singleton helper untuk akses Firebase secara terpusat.
 */
object FirebaseHelper {

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val messaging: FirebaseMessaging by lazy { FirebaseMessaging.getInstance() }

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = currentUser != null

    /** Referensi koleksi users */
    fun usersRef() = firestore.collection(Constants.COLLECTION_USERS)

    /** Referensi dokumen user yang sedang login */
    fun currentUserRef() = currentUser?.uid?.let { usersRef().document(it) }

    /** Referensi koleksi rides */
    fun ridesRef() = firestore.collection(Constants.COLLECTION_RIDES)

    /** Ambil FCM token secara suspend */
    suspend fun getFcmToken(): String? = runCatching {
        kotlinx.coroutines.tasks.await(messaging.token)
    }.getOrNull()
}
