package com.jakjak.passenger.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jakjak.passenger.R
import com.jakjak.passenger.databinding.FragmentProfileBinding

/**
 * Halaman profil sederhana: nama & email penumpang yang sedang login.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvName.text = getString(R.string.guest_name)
        binding.tvEmail.text = getString(R.string.guest_email)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
