package com.jakjak.passenger.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.jakjak.passenger.utils.Constants
import com.jakjak.passenger.utils.FirebaseHelper
import com.jakjak.passenger.utils.RideModel
import com.jakjak.passenger.utils.haversineDistanceKm
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // ---- State UI untuk proses matching ----
    sealed class MatchingState {
        object Idle : MatchingState()
        data class Searching(val driverName: String, val secondsLeft: Long) : MatchingState()
        object DriverFound : MatchingState()
        object NoDrivers : MatchingState()
        data class Failed(val message: String) : MatchingState()
    }

    private val _matchingState = MutableLiveData<MatchingState>(MatchingState.Idle)
    val matchingState: LiveData<MatchingState> = _matchingState

    private var matchingJob: Job? = null
    private var rideListener: ListenerRegistration? = null
    private var currentRideId: String? = null
    private var rideStatus = Constants.STATUS_SEARCHING
    private var rideCancelReason = ""

    /** Kandidat driver terdekat */
    private data class DriverCandidate(val id: String, val name: String, val distanceKm: Double)

    /**
     * Buat pesanan baru (status searching, lokasi asal dari GPS penumpang),
     * lalu mulai matching: tawarkan ke driver aktif terdekat, 15 detik accept
     * atau otomatis pindah ke driver berikutnya.
     */
    fun createRide(
        name: String,
        origin: String,
        destination: String,
        note: String,
        originLat: Double,
        originLng: Double
    ) {
        viewModelScope.launch {
            runCatching {
                val ride = RideModel(
                    passengerId       = getOrCreateGuestId(),
                    passengerName     = name,
                    origin            = origin,
                    destination       = destination,
                    note              = note,
                    originLocation    = GeoPoint(originLat, originLng),
                    status            = Constants.STATUS_SEARCHING,
                    driverId          = "",
                    attemptedDriverIds = emptyList(),
                    cancelReason      = "",
                    expiresAt         = System.currentTimeMillis() + Constants.MATCH_TIMEOUT_MS
                )
                FirebaseHelper.ridesRef().add(ride).await().id
            }.onSuccess { rideId ->
                startMatching(rideId)
            }.onFailure { e ->
                _matchingState.value = MatchingState.Failed(e.message ?: "Gagal membuat pesanan")
            }
        }
    }

    /** Batalkan pencarian (oleh penumpang) */
    fun cancelSearch() {
        currentRideId?.let { rideId ->
            viewModelScope.launch {
                runCatching {
                    FirebaseHelper.ridesRef().document(rideId).update(
                        "status", Constants.STATUS_CANCELLED,
                        "cancelReason", Constants.CANCEL_REASON_PASSENGER_CANCEL
                    ).await()
                }
            }
        }
        stopMatching()
        _matchingState.value = MatchingState.Idle
    }

    // ---- Mesin matching ----
    private fun startMatching(rideId: String) {
        stopMatching()
        currentRideId = rideId
        rideStatus = Constants.STATUS_SEARCHING
        rideCancelReason = ""
        observeRide(rideId)

        matchingJob = viewModelScope.launch {
            val origin = FirebaseHelper.ridesRef().document(rideId).get().await()
                ?.getGeoPoint("originLocation")
                ?: run {
                    _matchingState.value = MatchingState.Failed("Lokasi jemput tidak tersedia")
                    return@launch
                }

            val candidates = fetchActiveDriversSortedByDistance(origin)
            if (candidates.isEmpty()) {
                updateRide(rideId, status = Constants.STATUS_CANCELLED, cancelReason = Constants.CANCEL_REASON_NO_DRIVER)
                _matchingState.value = MatchingState.NoDrivers
                return@launch
            }

            val attempted = mutableSetOf<String>()

            for (driver in candidates) {
                if (!isActive) return@launch
                if (driver.id in attempted) continue
                attempted.add(driver.id)

                // Tawarkan order ke driver ini
                updateRide(
                    rideId,
                    status = Constants.STATUS_SEARCHING,
                    driverId = driver.id,
                    attempted = attempted,
                    cancelReason = ""
                )
                rideStatus = Constants.STATUS_SEARCHING
                rideCancelReason = ""

                var driverRejected = false

                for (i in MATCH_TIMEOUT_SECONDS downTo 1) {
                    if (!isActive) return@launch
                    when (rideStatus) {
                        Constants.STATUS_DRIVER_FOUND -> {
                            stopMatching()
                            _matchingState.value = MatchingState.DriverFound
                            return@launch
                        }
                        Constants.STATUS_CANCELLED -> {
                            if (rideCancelReason == Constants.CANCEL_REASON_PASSENGER_CANCEL) return@launch
                            if (rideCancelReason == Constants.CANCEL_REASON_DRIVER_REJECT) {
                                driverRejected = true
                                break
                            }
                        }
                    }
                    _matchingState.value = MatchingState.Searching(driver.name, i.toLong())
                    delay(1000)
                }

                if (driverRejected) continue // driver menolak -> langsung coba berikutnya
                // Timeout 15 detik untuk driver ini -> lanjut ke driver berikutnya
            }

            // Semua kandidat sudah dicoba tanpa ada yang menerima
            if (rideStatus != Constants.STATUS_DRIVER_FOUND) {
                updateRide(rideId, status = Constants.STATUS_CANCELLED, cancelReason = Constants.CANCEL_REASON_NO_DRIVER)
                _matchingState.value = MatchingState.NoDrivers
            }
        }
    }

    /** Ambil driver yang online, urutkan berdasarkan jarak (terdekat duluan) */
    private suspend fun fetchActiveDriversSortedByDistance(origin: GeoPoint): List<DriverCandidate> {
        val snapshot = FirebaseHelper.firestore.collection(Constants.COLLECTION_DRIVERS)
            .whereEqualTo("status_online", true)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val name = doc.getString("nama") ?: return@mapNotNull null
            val loc  = doc.getGeoPoint("location") ?: return@mapNotNull null
            val distance = haversineDistanceKm(
                origin.latitude, origin.longitude,
                loc.latitude, loc.longitude
            )
            DriverCandidate(doc.id, name, distance)
        }.sortedBy { it.distanceKm }
    }

    /** Pantau dokumen ride untuk mendeteksi accept / reject dari driver */
    private fun observeRide(rideId: String) {
        rideListener?.remove()
        rideListener = FirebaseHelper.ridesRef().document(rideId)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null || !snap.exists()) return@addSnapshotListener
                rideStatus = snap.getString("status") ?: Constants.STATUS_SEARCHING
                rideCancelReason = snap.getString("cancelReason") ?: ""

                if (rideStatus == Constants.STATUS_DRIVER_FOUND) {
                    stopMatching()
                    _matchingState.value = MatchingState.DriverFound
                }
            }
    }

    private suspend fun updateRide(
        rideId: String,
        status: String,
        driverId: String? = null,
        attempted: Set<String>? = null,
        cancelReason: String? = null
    ) {
        val data = mutableMapOf<String, Any>("status" to status)
        driverId?.let { data["driverId"] = it }
        attempted?.let { data["attemptedDriverIds"] = it.toList() }
        cancelReason?.let { data["cancelReason"] = it }
        data["expiresAt"] = System.currentTimeMillis() + Constants.MATCH_TIMEOUT_MS
        FirebaseHelper.ridesRef().document(rideId).update(data).await()
    }

    private fun stopMatching() {
        matchingJob?.cancel()
        matchingJob = null
        rideListener?.remove()
        rideListener = null
    }

    /** ID tamu persisten per perangkat (dibuat sekali, disimpan di SharedPreferences) */
    private fun getOrCreateGuestId(): String {
        val prefs = getApplication<Application>()
            .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        prefs.getString(Constants.PREF_GUEST_ID, null)?.let { return it }

        val newId = "guest_${UUID.randomUUID()}"
        prefs.edit().putString(Constants.PREF_GUEST_ID, newId).apply()
        return newId
    }

    companion object {
        private const val MATCH_TIMEOUT_SECONDS = 15L
    }
}
