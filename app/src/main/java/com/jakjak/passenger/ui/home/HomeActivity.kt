package com.jakjak.passenger.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.jakjak.passenger.R
import com.jakjak.passenger.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNav()
    }

    private fun setupBottomNav() {
        // Muat fragment default saat pertama kali dibuka
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // Ganti fragment sesuai pilihan nav
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> { replaceFragment(HomeFragment()); true }
                R.id.nav_history -> { replaceFragment(HistoryFragment()); true }
                R.id.nav_profile -> { replaceFragment(ProfileFragment()); true }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
