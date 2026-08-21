package com.jakjak.passenger.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.jakjak.passenger.R
import com.jakjak.passenger.databinding.FragmentHomeBinding
import com.jakjak.passenger.utils.gone
import com.jakjak.passenger.utils.snack
import com.jakjak.passenger.utils.visible
import com.jakjak.passenger.viewmodel.HomeViewModel

/**
 * Halaman landing pemesanan: nama, asal, tujuan, keterangan + tombol pesan.
 * Setelah tombol ditekan: ambil lokasi GPS, buat order, lalu cari driver
 * aktif terdekat (tiap driver punya 15 detik untuk accept / auto-reject).
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                requestLocationAndOrder()
            } else {
                binding.root.snack(getString(R.string.msg_location_denied))
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnOrder.setOnClickListener { attemptOrder() }
        binding.btnCancelSearch.setOnClickListener { viewModel.cancelSearch() }
        observeMatchingState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun attemptOrder() {
        val name        = binding.etName.text.toString().trim()
        val origin      = binding.etOrigin.text.toString().trim()
        val destination = binding.etDestination.text.toString().trim()

        var isValid = true

        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.msg_name_required); isValid = false
        } else binding.tilName.error = null

        if (origin.isEmpty()) {
            binding.tilOrigin.error = getString(R.string.msg_origin_required); isValid = false
        } else binding.tilOrigin.error = null

        if (destination.isEmpty()) {
            binding.tilDestination.error = getString(R.string.msg_destination_required); isValid = false
        } else binding.tilDestination.error = null

        if (isValid) requestLocationAndOrder()
    }

    private fun requestLocationAndOrder() {
        if (!hasLocationPermission()) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        getCurrentLocation { lat, lng -> createOrder(lat, lng) }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun getCurrentLocation(onResult: (Double, Double) -> Unit) {
        if (!hasLocationPermission()) return

        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    onResult(loc.latitude, loc.longitude)
                } else {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { current ->
                            if (current != null) {
                                onResult(current.latitude, current.longitude)
                            } else {
                                binding.root.snack(getString(R.string.msg_location_unavailable))
                            }
                        }
                        .addOnFailureListener {
                            binding.root.snack(getString(R.string.msg_location_unavailable))
                        }
                }
            }
            .addOnFailureListener {
                binding.root.snack(getString(R.string.msg_location_unavailable))
            }
    }

    private fun createOrder(lat: Double, lng: Double) {
        val name        = binding.etName.text.toString().trim()
        val origin      = binding.etOrigin.text.toString().trim()
            .ifEmpty { getString(R.string.current_location) }
        val destination = binding.etDestination.text.toString().trim()
        val note        = binding.etNote.text.toString().trim()
        viewModel.createRide(name, origin, destination, note, lat, lng)
    }

    private fun observeMatchingState() {
        viewModel.matchingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeViewModel.MatchingState.Idle -> Unit
                is HomeViewModel.MatchingState.Searching ->
                    showSearching(state.driverName, state.secondsLeft)

                is HomeViewModel.MatchingState.DriverFound -> {
                    binding.cardSearching.gone()
                    binding.cardForm.gone()
                    binding.cardInfo.gone()
                    binding.cardFound.visible()
                    binding.root.snack(getString(R.string.msg_driver_found))
                }

                is HomeViewModel.MatchingState.NoDrivers -> {
                    hideSearching()
                    binding.root.snack(getString(R.string.msg_no_driver))
                }

                is HomeViewModel.MatchingState.Failed -> {
                    hideSearching()
                    binding.root.snack(state.message)
                }
            }
        }
    }

    private fun showSearching(driverName: String, secondsLeft: Long) {
        binding.cardForm.gone()
        binding.cardInfo.gone()
        binding.cardFound.gone()
        binding.cardSearching.visible()
        binding.tvSearchStatus.text = getString(R.string.searching_driver, driverName)
        binding.tvCountdown.text = getString(R.string.searching_countdown, secondsLeft)
    }

    private fun hideSearching() {
        binding.cardSearching.gone()
        binding.cardFound.gone()
        binding.cardForm.visible()
        binding.cardInfo.visible()
    }
}
