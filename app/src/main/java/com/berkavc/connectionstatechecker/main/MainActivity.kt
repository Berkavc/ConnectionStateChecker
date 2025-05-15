package com.berkavc.connectionstatechecker.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.berkavc.connectionstatechecker.R
import com.berkavc.connectionstatechecker.SHARED_PREF_ENABLED
import com.berkavc.connectionstatechecker.SharedPreference
import com.berkavc.connectionstatechecker.base.BaseActivity
import com.berkavc.connectionstatechecker.databinding.ActivityMainBinding
import com.berkavc.connectionstatechecker.getPingMs
import com.berkavc.connectionstatechecker.startConnectivityService
import com.berkavc.connectionstatechecker.stopConnectivityService
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {
    override val layoutRes: Int = R.layout.activity_main
    override val viewModel: MainViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this

    private lateinit var sharedPreference: SharedPreference

    private val PERMISSIONS_REQUEST_CODE = 1001

    private val requiredPermissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.INTERNET
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreference = SharedPreference(this)
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        arrangeMainUI()
    }

    private fun arrangeMainUI() {
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        val isEnabled = sharedPreference.getValueBoolean(
            SHARED_PREF_ENABLED, false
        )
        binding.switchEnable.isChecked = isEnabled
        binding.switchEnable.setOnCheckedChangeListener { buttonView, isChecked ->
            sharedPreference.saveBooleanSynchronized(SHARED_PREF_ENABLED, isChecked)
            if (isChecked) {
                checkAndRequestPermissions()
            } else {
                stopConnectivityService()
                binding.buttonCheckPing.visibility = View.GONE
            }
        }
        binding.buttonCheckPing.visibility = if (isEnabled) View.VISIBLE else View.GONE

        binding.buttonCheckPing.setOnClickListener {
            lifecycleScope.launch {
                val ping = getPingMs()
                Toast.makeText(this@MainActivity, getString(R.string.ping, ping), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startService() {
        if (sharedPreference.getValueBoolean(SHARED_PREF_ENABLED, false)) {
            startConnectivityService()
            binding.buttonCheckPing.visibility = View.VISIBLE
        }
    }

    private fun checkAndRequestPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missing.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            startService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val denied =
                grantResults.indices.filter { grantResults[it] != PackageManager.PERMISSION_GRANTED }
            if (denied.isNotEmpty()) {
                lifecycleScope.launch {
                    binding.switchEnable.isChecked = false
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.permission_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                startService()
            }
        }
    }

    override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.adView.resume()
    }

    override fun onDestroy() {
        binding.adView.destroy()
        super.onDestroy()
    }

    override fun observeViewModel() {}
}