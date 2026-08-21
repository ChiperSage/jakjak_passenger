package com.jakjak.passenger.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jakjak.passenger.databinding.ActivitySplashBinding
import com.jakjak.passenger.ui.home.HomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(SPLASH_DELAY_MS)
            navigateNext()
        }
    }

    private fun navigateNext() {
        // Mode tamu: langsung ke halaman utama tanpa login/daftar
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    companion object {
        private const val SPLASH_DELAY_MS = 2000L
    }
}
